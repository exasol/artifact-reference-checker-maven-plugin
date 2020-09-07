package com.exasol.artifactreferencechecker;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * This class unifies the artifacts references in a maven project.
 */
@Mojo(name = "unify")
public class ArtifactReferenceCheckerUnifyMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    private final BaseReferenceCheckerMojo baseReferenceCheckerMojo;

    public ArtifactReferenceCheckerUnifyMojo() {
        super();
        this.baseReferenceCheckerMojo = new BaseReferenceCheckerMojo(new UnifyFileAndLineVisitor(), getLog());
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        this.baseReferenceCheckerMojo.execute(this.project);
    }

    private static class UnifyFileAndLineVisitor implements FileAndLineVisitor {
        private boolean hadFileReplacements;
        private StringBuilder result;

        @Override
        public void visit(final Path file) {
            this.hadFileReplacements = false;
            this.result = new StringBuilder();
        }

        @Override
        public void leave(final Path file) throws MojoExecutionException {
            if (this.hadFileReplacements) {
                try (final FileWriter writer = new FileWriter(file.toFile())) {
                    writer.write(this.result.toString());
                    writer.flush();
                } catch (final IOException exception) {
                    throw new MojoExecutionException("Could not modify file " + file.toString(), exception);
                }
            }
        }

        @Override
        public void visitLine(final String line, final Pattern pattern, final String expected) {
            final Matcher matcher = pattern.matcher(line);
            final String replaced = matcher.replaceAll(expected);
            this.result.append(replaced + System.lineSeparator());
            if (!line.equals(replaced)) {
                this.hadFileReplacements = true;
            }
        }

        @Override
        public void report() throws MojoFailureException {
            // unify has no report
        }
    }
}