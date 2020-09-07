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
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * This class contains the generic implementation for verify and unify.
 */
public class BaseReferenceCheckerMojo {
    private final FileAndLineVisitor fileAndLineVisitor;
    private final Log logger;

    /**
     * Create a new instance of {@link BaseReferenceCheckerMojo}.
     * 
     * @param fileAndLineVisitor visit or unify specific behaviour
     * @param logger             logger
     */
    public BaseReferenceCheckerMojo(final FileAndLineVisitor fileAndLineVisitor, final Log logger) {
        this.fileAndLineVisitor = fileAndLineVisitor;
        this.logger = logger;
    }

    /**
     * Iterate all files with correct file ending and their lines.
     *
     * @param project maven project (use to access the pom file)
     * @throws MojoExecutionException if something goes wrong
     * @throws MojoFailureException   if {@link FileAndLineVisitor#report()} throws a {@link MojoFailureException}
     */
    public void execute(final MavenProject project) throws MojoExecutionException, MojoFailureException {
        final List<Plugin> buildPlugins = project.getBuildPlugins();
        final Plugin assemblyPlugin = buildPlugins.stream()
                .filter(plugin -> plugin.getArtifactId().equals("maven-assembly-plugin")).findAny()
                .orElseThrow(() -> new IllegalStateException("Could not find assembly plugin."));
        final Xpp3Dom config = (Xpp3Dom) assemblyPlugin.getConfiguration();
        final String resolvedFinalName = config.getChild("finalName").getValue() + ".jar";
        this.logger.info("Detected artifact name:" + resolvedFinalName);
        final String searchPattern = buildSearchPattern(project);
        this.logger.debug("Generated pattern: " + searchPattern);
        matchPatternInProjectFiles(project, searchPattern, resolvedFinalName);
    }

    private String buildSearchPattern(final MavenProject project) {
        final String finalName = getUnresolvedFinalName(project).trim() + ".jar";
        final Pattern variablePattern = Pattern.compile("\\$\\{([^\\}]*)\\}");
        final Matcher matcher = variablePattern.matcher(finalName);
        return "\\Q" + matcher.replaceAll("\\\\E.*?\\\\Q") + "\\E";
    }

    private String getUnresolvedFinalName(final MavenProject project) {
        try (final FileInputStream fileIS = new FileInputStream(project.getModel().getPomFile().getAbsolutePath())) {
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

    private void matchPatternInProjectFiles(final MavenProject project, final String regex, final String expected)
            throws MojoExecutionException, MojoFailureException {
        final Pattern pattern = Pattern.compile(regex);
        final File projectDirectory = project.getModel().getProjectDirectory();
        final FileVisitor fileVisitor = new FileVisitor(this.fileAndLineVisitor, pattern, expected);
        try {
            Files.walkFileTree(projectDirectory.toPath(), fileVisitor);
        } catch (final IOException exception) {
            throw new MojoExecutionException("Could not check files.", exception);
        }
        fileVisitor.report();
    }

    /**
     * File Visitor that checks if the files have the correct ending.
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
                this.fileAndLineVisitor.leave(file);
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
}
