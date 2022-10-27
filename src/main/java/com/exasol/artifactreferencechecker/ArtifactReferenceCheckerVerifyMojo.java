package com.exasol.artifactreferencechecker;

import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * This class checks the artifacts references in a maven project.
 */
@Mojo(name = "verify", defaultPhase = LifecyclePhase.PACKAGE)
public class ArtifactReferenceCheckerVerifyMojo extends AbstractArtifactReferenceCheckerMojo {

    @Override
    protected FileAndLineVisitor getFileAndLineVisitor() {
        return new ValidationFileAndLineVisitor(getLog());
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
                        "Found outdated artifact reference: '" + matcher.group() + "' in " + this.currentFileName);
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
