package com.exasol.artifactreferencechecker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * This class contains the abstract implementation for verify and unify.
 */
public abstract class AbstractArtifactReferenceCheckerMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    private final FileAndLineVisitor fileAndLineVisitor;

    /**
     * Create a new instance of {@link AbstractArtifactReferenceCheckerMojo}.
     *
     */
    public AbstractArtifactReferenceCheckerMojo() {
        this.fileAndLineVisitor = getFileAndLineVisitor();
    }

    protected abstract FileAndLineVisitor getFileAndLineVisitor();

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final List<Plugin> buildPlugins = this.project.getBuildPlugins();
        final Plugin assemblyPlugin = buildPlugins.stream()
                .filter(plugin -> plugin.getArtifactId().equals("maven-assembly-plugin")).findAny()
                .orElseThrow(() -> new IllegalStateException("Could not find assembly plugin."));
        final Xpp3Dom config = (Xpp3Dom) assemblyPlugin.getConfiguration();
        final String resolvedFinalName = config.getChild("finalName").getValue() + ".jar";
        getLog().info("Detected artifact name:" + resolvedFinalName);
        final String searchPattern = buildSearchPattern();
        getLog().debug("Generated pattern: " + searchPattern);
        matchPatternInProjectFiles(searchPattern, resolvedFinalName);
    }

    private String buildSearchPattern() {
        final String finalName = getUnresolvedFinalName().trim() + ".jar";
        final Pattern variablePattern = Pattern.compile("\\$\\{([^\\}]*)\\}");
        final Matcher matcher = variablePattern.matcher(finalName);
        return "\\Q" + matcher.replaceAll("\\\\E.*?\\\\Q") + "\\E";
    }

    private String getUnresolvedFinalName() {
        try (final FileInputStream fileIS = new FileInputStream(
                this.project.getModel().getPomFile().getAbsolutePath())) {
            final DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, ""); // Compliant  
            builderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, ""); // compliant
            final DocumentBuilder builder = builderFactory.newDocumentBuilder();
            final Document xmlDocument = builder.parse(fileIS);
            final XPath xPath = XPathFactory.newInstance().newXPath();
            final String expression = "string(/project/build/plugins/plugin[artifactId=\"maven-assembly-plugin\"]/configuration/finalName)";
            return (String) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.STRING);
        } catch (final XPathExpressionException | IOException | SAXException | ParserConfigurationException exception) {
            throw new IllegalStateException("Could not find finalName in maven-assembly-plugin");
        }
    }

    private void matchPatternInProjectFiles(final String regex, final String expected)
            throws MojoExecutionException, MojoFailureException {
        final Pattern pattern = Pattern.compile(regex);
        final File projectDirectory = this.project.getModel().getProjectDirectory();
        final FileVisitor fileVisitor = new FileVisitor(this.fileAndLineVisitor, pattern, expected);
        try {
            Files.walkFileTree(projectDirectory.toPath(), fileVisitor);
        } catch (final IOException exception) {
            throw new MojoExecutionException("Could not check files.", exception);
        } catch (final ExceptionWrapper exceptionWrapper) {
            throw exceptionWrapper.getExecutionException();
        }
        fileVisitor.report();
    }

    /**
     * File Visitor that checks if the files have the correct extension.
     */
    private static class FileVisitor extends SimpleFileVisitor<Path> {
        private final FileAndLineVisitor fileAndLineVisitor;
        private final Pattern pattern;
        private final String expected;
        List<String> extensions = List.of("java", "md");

        private FileVisitor(final FileAndLineVisitor fileAndLineVisitor, final Pattern pattern, final String expected) {
            this.fileAndLineVisitor = fileAndLineVisitor;
            this.pattern = pattern;
            this.expected = expected;
        }

        private boolean hasCorrectEnding(final Path path) {
            return this.extensions.stream().anyMatch(extension -> path.toString().endsWith("." + extension));
        }

        @Override
        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
            if (hasCorrectEnding(file)) {
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
                throw new IllegalStateException("Could not open project file " + file.toString() + ".", exception);
            }
        }

        private void report() throws MojoFailureException {
            this.fileAndLineVisitor.report();
        }
    }

    private static class ExceptionWrapper extends RuntimeException {
        private final MojoExecutionException executionException;

        private ExceptionWrapper(final MojoExecutionException executionException) {
            this.executionException = executionException;
        }

        private MojoExecutionException getExecutionException() {
            return this.executionException;
        }
    }
}
