package com.exasol.artifactreferencechecker;

import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * This class checks the artifacts references in a maven project.
 */
@Mojo(name = "verify", defaultPhase = LifecyclePhase.VERIFY)
public class ArtifactReferenceCheckerVerifyMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;
    
    private final BaseReferenceCheckerMojo baseReferenceCheckerMojo;

    public ArtifactReferenceCheckerVerifyMojo() {
        super();
        this.baseReferenceCheckerMojo = new BaseReferenceCheckerMojo(new ValidationFileAndLineVisitor(getLog()),
                getLog());
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        this.baseReferenceCheckerMojo.execute(this.project);
    }

    private static class ValidationFileAndLineVisitor implements FileAndLineVisitor {
        private final Log log;
        private String currentFileName;
        private boolean success = true;

        public ValidationFileAndLineVisitor(final Log log) {
            this.log = log;
        }

        @Override
        public void visit(final Path file) {
            this.currentFileName = file.toString();
        }

        @Override
        public void leave(final Path file) {
            this.currentFileName = null;
        }

        @Override
        public void visitLine(final String line, final Pattern pattern, final String expected) {
            final Matcher matcher = pattern.matcher(line);
            if (matcher.find() && !matcher.group().equals(expected)) {
                this.log.error(
                        "Found outdated artifact reference: " + matcher.group() + " in  " + this.currentFileName);
                this.success = false;
            }
        }

        @Override
        public void report() throws MojoFailureException {
            if (!this.success) {
                throw new MojoFailureException(
                        "Found invalid artifact references, please check the reported errors. You can use mvn artifact-reference-checker:unify to fix them.");
            }
        }
    }
}
