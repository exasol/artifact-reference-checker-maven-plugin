package com.exasol.artifactreferencechecker;

import org.apache.commons.io.FileUtils;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mojo(name = "verify", defaultPhase = LifecyclePhase.COMPILE)
public class ArtifactReferenceCheckerMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    public void execute() throws MojoExecutionException, MojoFailureException {
        final List<Plugin> buildPlugins = project.getBuildPlugins();
        final Plugin assemblyPlugin = buildPlugins.stream().filter(plugin -> plugin.getArtifactId().equals("maven-assembly-plugin")).findAny().orElseThrow(() -> new IllegalStateException("Could not find assembly plugin."));
        Xpp3Dom config = (Xpp3Dom) assemblyPlugin.getConfiguration();
        String resolvedFinalName = config.getChild("finalName").getValue() + ".jar";
        getLog().info("Detected artifact name:" + resolvedFinalName);
        final String searchPattern = buildSearchPattern();
        getLog().debug("Generated pattern: " + searchPattern);
        if(!matchPatternInProjectFiles(searchPattern, resolvedFinalName)){
            throw new MojoFailureException("Found invalid artifact references (see previous errors). You can use mvn artifact-reference-checker:unify to fix them.");
        }
    }

    private String buildSearchPattern() {
        String finalName = getUnresolvedFinalName().trim() + ".jar";
        Pattern variablePattern = Pattern.compile("\\$\\{([^\\}]*)\\}");
        final Matcher matcher = variablePattern.matcher(finalName);
        return "\\Q" + matcher.replaceAll("\\\\E.*?\\\\Q") + "\\E";
    }

    public String getUnresolvedFinalName() {
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
        String[] extensions = new String[]{"java", "md"};
        final File projectDirectory = project.getModel().getProjectDirectory();
        final Collection<File> files = FileUtils.listFiles(projectDirectory, extensions, true);
        boolean success = true;
        for (File file : files) {
            if(!matchPatternInFile(pattern, file, expected)){
                success = false;
            }
        }
        return success;
    }

    private boolean matchPatternInFile(Pattern pattern, File file, String expected) throws MojoExecutionException {
        boolean success = true;
        try (Scanner fileScanner = new Scanner(file)) {
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine();
                final Matcher matcher = pattern.matcher(line);
                if (matcher.find() && !matcher.group().equals(expected)) {
                    getLog().error("found outdated artifact reference: " + matcher.group() + " in  " + file.getAbsolutePath());
                    success = false;
                }
            }
            return success;
        } catch (FileNotFoundException exception) {
            throw new MojoExecutionException("Could not open project file " + file.getAbsolutePath() + ". Cause: " + exception.getMessage(), exception);
        }
    }
}