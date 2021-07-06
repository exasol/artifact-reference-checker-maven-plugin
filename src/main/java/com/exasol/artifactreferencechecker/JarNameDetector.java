package com.exasol.artifactreferencechecker;

import java.io.*;
import java.util.*;

import org.apache.maven.model.*;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import com.exasol.errorreporting.ExaError;

/**
 * This class gets the name of the jar file of a maven project.
 */
class JarNameDetector {

    public static final Set<String> JAR_BUILDING_PLUGINS = Set.of("maven-assembly-plugin", "maven-shade-plugin");

    JarName getJarName(final MavenProject resolvedMavenProject) {
        final Model unresolvedModel = readUnresolvedModel(resolvedMavenProject.getModel().getPomFile());
        final List<Plugin> plugins = resolvedMavenProject.getBuild().getPlugins();
        final Plugin plugin = findJarBuildingPlugin(plugins);
        final Optional<Plugin> unresolvedPlugin = unresolvedModel.getBuild().getPlugins().stream()
                .filter(each -> each.getArtifactId().equals(plugin.getArtifactId())).findAny();
        if (unresolvedPlugin.isPresent()) {
            return new JarName(getFinalName(plugin), getFinalName(unresolvedPlugin.get()));
        } else {
            throw new IllegalStateException(ExaError.messageBuilder("F-ARCM-6")
                    .message("Could not find {{plugin}} in unresolved build.", plugin.getArtifactId())
                    .ticketMitigation().toString());
        }
    }

    private Plugin findJarBuildingPlugin(final List<Plugin> plugins) {
        for (final Plugin plugin : plugins) {
            if (JAR_BUILDING_PLUGINS.contains(plugin.getArtifactId())) {
                return plugin;
            }
        }
        throw new IllegalStateException(ExaError.messageBuilder("E-ARCM-5").message(
                "The project configured non of the following jar-building plugins: {{supported plugins}}. Other methods of building a jar file are currently not supported.",
                JAR_BUILDING_PLUGINS).mitigation("Please check your project's configuration")
                .mitigation(
                        "Open a ticket at the artifact-reference-checker-maven-plugin to add support for different jar-building plugins.")
                .toString());
    }

    private String getFinalName(final Plugin plugin) {
        final Xpp3Dom config = (Xpp3Dom) plugin.getConfiguration();
        final Optional<String> finalName = getFinalName(config);
        if (finalName.isPresent()) {
            return finalName.get();
        } else {
            final List<PluginExecution> executions = plugin.getExecutions();
            return getFinalName(plugin, executions);
        }
    }

    private String getFinalName(final Plugin plugin, final List<PluginExecution> executions) {
        for (final PluginExecution execution : executions) {
            final Optional<String> executionsFinalName = getFinalName((Xpp3Dom) execution.getConfiguration());
            if (executionsFinalName.isPresent()) {
                return executionsFinalName.get();
            }
        }
        throw new IllegalStateException(ExaError.messageBuilder("E-ARCM-7")
                .message("The {{plugin name}} does not specify a final name.", plugin.getArtifactId())
                .mitigation("Please set a final name.").toString());
    }

    private Optional<String> getFinalName(final Xpp3Dom config) {
        if (config == null) {
            return Optional.empty();
        } else {
            final Xpp3Dom finalName = config.getChild("finalName");
            if (finalName == null) {
                return Optional.empty();
            } else {
                return Optional.of(finalName.getValue().trim() + ".jar");
            }
        }
    }

    private Model readUnresolvedModel(final File pomFile) {
        try (final FileReader reader = new FileReader(pomFile)) {
            return new MavenXpp3Reader().read(reader);
        } catch (final IOException | XmlPullParserException exception) {
            throw new IllegalStateException(
                    ExaError.messageBuilder("E-ARCM-4").message("Failed to read project's pom file.").toString(),
                    exception);
        }
    }

    public static class JarName {
        private final String resolved;
        private final String unresolved;

        public JarName(final String resolved, final String unresolved) {
            this.resolved = resolved;
            this.unresolved = unresolved;
        }

        /**
         * Get the resolved jar-name where all pom-variables are already replaced by their values.
         *
         * @return unresolved jar-name
         */
        public String getResolved() {
            return this.resolved;
        }

        /**
         * Get the unresolved jar-name with pom variable expressions.
         * 
         * @return unresolved jar-name
         */
        public String getUnresolved() {
            return this.unresolved;
        }
    }
}
