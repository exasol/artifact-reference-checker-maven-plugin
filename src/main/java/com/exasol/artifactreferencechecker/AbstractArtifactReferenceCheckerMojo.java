package com.exasol.artifactreferencechecker;

import java.io.*;
import java.nio.file.*;
import java.nio.file.FileSystem;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.maven.plugin.*;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import com.exasol.errorreporting.ExaError;

/**
 * This class contains the abstract implementation for verify and unify.
 */
public abstract class AbstractArtifactReferenceCheckerMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    @Parameter(property = "exclude")
    private List<String> excludes;

    private final FileAndLineVisitor fileAndLineVisitor;

    /**
     * Create a new instance of {@link AbstractArtifactReferenceCheckerMojo}.
     */
    protected AbstractArtifactReferenceCheckerMojo() {
        this.fileAndLineVisitor = getFileAndLineVisitor();
    }

    /**
     * Get the visitor for processing the files.
     * 
     * @return the visitor
     */
    protected abstract FileAndLineVisitor getFileAndLineVisitor();

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final JarNameDetector.JarName jarName = new JarNameDetector().getJarName(this.project);
        getLog().info("Detected artifact name:" + jarName.getResolved());
        final String searchPattern = buildSearchPattern(jarName.getUnresolved());
        getLog().debug("Generated pattern: " + searchPattern);
        getLog().info("Excluded files:");
        this.excludes.forEach(getLog()::info);
        matchPatternInProjectFiles(searchPattern, jarName.getResolved());
    }

    private String buildSearchPattern(final String unresolvedJarName) {
        final Pattern variablePattern = Pattern.compile("\\$\\{([^\\}]*)\\}");
        final Matcher matcher = variablePattern.matcher(unresolvedJarName);
        return "\\Q" + matcher.replaceAll("\\\\E.*?\\\\Q") + "\\E";
    }

    private void matchPatternInProjectFiles(final String regex, final String expected)
            throws MojoExecutionException, MojoFailureException {
        final Pattern pattern = Pattern.compile(regex);
        final File projectDirectory = this.project.getModel().getProjectDirectory();
        final FileSystem fileSystem = FileSystems.getDefault();
        final List<PathMatcher> excludeMatchers = this.excludes.stream()
                .map(excludPattern -> buildGlob(excludPattern, projectDirectory)).map(fileSystem::getPathMatcher)
                .collect(Collectors.toList());
        final FileVisitor fileVisitor = new FileVisitor(this.fileAndLineVisitor, pattern, expected, excludeMatchers);
        try {
            Files.walkFileTree(projectDirectory.toPath(), fileVisitor);
        } catch (final IOException exception) {
            throw new MojoExecutionException(
                    ExaError.messageBuilder("E-ARCM-1").message("Could not check files.").toString(), exception);
        } catch (final ExceptionWrapper exceptionWrapper) {
            throw exceptionWrapper.getExecutionException();
        }
        fileVisitor.report();
    }

    private String buildGlob(final String excludePattern, final File projectDir) {
        if (excludePattern.startsWith("/")) {
            return "glob:" + projectDir + excludePattern;
        } else {
            return "glob:**" + excludePattern;
        }
    }

    /**
     * File Visitor that checks if the files have the correct extension.
     */
    private static class FileVisitor extends SimpleFileVisitor<Path> {
        private final FileAndLineVisitor fileAndLineVisitor;
        private final Pattern pattern;
        private final String expected;
        List<String> extensions = List.of("java", "md");
        final List<PathMatcher> excludeMatchers;

        private FileVisitor(final FileAndLineVisitor fileAndLineVisitor, final Pattern pattern, final String expected,
                final List<PathMatcher> excludeMatchers) {
            this.fileAndLineVisitor = fileAndLineVisitor;
            this.pattern = pattern;
            this.expected = expected;
            this.excludeMatchers = excludeMatchers;
        }

        private boolean isFileIncluded(final Path path) {
            return hasCorrectEnding(path) && !isFileExcluded(path);
        }

        private boolean isFileExcluded(final Path path) {
            return this.excludeMatchers.stream().anyMatch(matcher -> matcher.matches(path));
        }

        private boolean hasCorrectEnding(final Path path) {
            return this.extensions.stream().anyMatch(extension -> path.toString().endsWith("." + extension));
        }

        @Override
        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
            if (isFileIncluded(file)) {
                this.fileAndLineVisitor.visit(file);
                readLines(file);
                try {
                    this.fileAndLineVisitor.leave(file);
                } catch (final MojoExecutionException exception) {
                    throw new ExceptionWrapper(exception);
                }
            }
            return FileVisitResult.CONTINUE;
        }

        private void readLines(final Path file) throws IOException {
            try (final Scanner fileScanner = new Scanner(file)) {
                while (fileScanner.hasNextLine()) {
                    final String line = fileScanner.nextLine();
                    this.fileAndLineVisitor.visitLine(line, this.pattern, this.expected);
                }
            } catch (final FileNotFoundException exception) {
                throw new IllegalStateException(ExaError.messageBuilder("E-ARCM-3")
                        .message("Could not open project file {{file}}.", file).toString(), exception);
            }
        }

        private void report() throws MojoFailureException {
            this.fileAndLineVisitor.report();
        }
    }

    private static class ExceptionWrapper extends RuntimeException {
        private static final long serialVersionUID = 2123268790118308800L;
        private final MojoExecutionException executionException;

        private ExceptionWrapper(final MojoExecutionException executionException) {
            this.executionException = executionException;
        }

        private MojoExecutionException getExecutionException() {
            return this.executionException;
        }
    }
}
