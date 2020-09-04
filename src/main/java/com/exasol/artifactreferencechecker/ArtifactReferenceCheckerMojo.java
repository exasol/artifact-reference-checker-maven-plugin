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
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * This class checks the artifacts references in a maven project.
 */
@Mojo(name = "verify", defaultPhase = LifecyclePhase.VERIFY)
public class ArtifactReferenceCheckerMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final List<Plugin> buildPlugins = project.getBuildPlugins();
        final Plugin assemblyPlugin = buildPlugins.stream()
                .filter(plugin -> plugin.getArtifactId().equals("maven-assembly-plugin")).findAny()
                .orElseThrow(() -> new IllegalStateException("Could not find assembly plugin."));
        Xpp3Dom config = (Xpp3Dom) assemblyPlugin.getConfiguration();
        String resolvedFinalName = config.getChild("finalName").getValue() + ".jar";
        getLog().info("Detected artifact name:" + resolvedFinalName);
        final String searchPattern = buildSearchPattern();
        getLog().debug("Generated pattern: " + searchPattern);
        if (!matchPatternInProjectFiles(searchPattern, resolvedFinalName)) {
            throw new MojoFailureException(
                    "Found invalid artifact references (see previous errors). You can use mvn artifact-reference-checker:unify to fix them.");
        }
    }

    private String buildSearchPattern() {
        String finalName = getUnresolvedFinalName().trim() + ".jar";
        Pattern variablePattern = Pattern.compile("\\$\\{([^\\}]*)\\}");
        final Matcher matcher = variablePattern.matcher(finalName);
        return "\\Q" + matcher.replaceAll("\\\\E.*?\\\\Q") + "\\E";
    }

    private String getUnresolvedFinalName() {
        try (FileInputStream fileIS = new FileInputStream(project.getModel().getPomFile().getAbsolutePath())) {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, ""); // Compliant  
            builderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, ""); // compliant
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document xmlDocument = builder.parse(fileIS);
            XPath xPath = XPathFactory.newInstance().newXPath();
            String expression = "string(/project/build/plugins/plugin[artifactId=\"maven-assembly-plugin\"]/configuration/finalName)";
            return (String) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.STRING);
        } catch (XPathExpressionException | IOException | SAXException | ParserConfigurationException exception) {
            throw new IllegalStateException("Could not find finalName in maven-assembly-plugin");
        }
    }

    private boolean matchPatternInProjectFiles(String regex, String expected) throws MojoExecutionException {
        final Pattern pattern = Pattern.compile(regex);
        final File projectDirectory = project.getModel().getProjectDirectory();
        final FileValidationVisitor fileValidationVisitor = new FileValidationVisitor(getLog(), pattern, expected);
        try {
            Files.walkFileTree(projectDirectory.toPath(), fileValidationVisitor);
        } catch (IOException exception) {
            throw new MojoExecutionException("Could not check files.", exception);
        }
        return fileValidationVisitor.isSuccess();
    }

    /**
     * File Visitor that checks of the files contain outdated artifact references
     */
    private static class FileValidationVisitor extends SimpleFileVisitor<Path> {
        private final Log log;
        private final Pattern pattern;
        private final String expected;
        List<String> extensions = List.of("java", "md");
        private boolean success = true;

        private FileValidationVisitor(Log log, Pattern pattern, String expected) {
            this.log = log;
            this.pattern = pattern;
            this.expected = expected;
        }

        private boolean hasCorrectEnding(Path path) {
            return extensions.stream().anyMatch(extension -> path.toString().endsWith("." + extension));
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (hasCorrectEnding(file)) {
                validateFile(file);
            }
            return FileVisitResult.CONTINUE;
        }

        private void validateFile(Path file) throws IOException {
            try (Scanner fileScanner = new Scanner(file)) {
                while (fileScanner.hasNextLine()) {
                    String line = fileScanner.nextLine();
                    validateLine(file, line);
                }
            } catch (FileNotFoundException exception) {
                throw new IllegalStateException("Could not open project file " + file.toString() + ".", exception);
            }
        }

        private void validateLine(Path file, String line) {
            final Matcher matcher = pattern.matcher(line);
            if (matcher.find() && !matcher.group().equals(expected)) {
                log.error("Found outdated artifact reference: " + matcher.group() + " in  " + file.toString());
                success = false;
            }
        }

        /**
         * Get if all files were valid.
         * 
         * @return true if all files were valid
         */
        public boolean isSuccess() {
            return success;
        }
    }
}