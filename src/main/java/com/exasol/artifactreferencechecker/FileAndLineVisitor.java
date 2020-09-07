package com.exasol.artifactreferencechecker;

import java.nio.file.Path;
import java.util.regex.Pattern;

import org.apache.maven.plugin.MojoFailureException;

/**
 * Interface for verify or unify specific behaviour.
 */
public interface FileAndLineVisitor {
    public void visit(Path file);

    public void leave(Path file);

    public void visitLine(String line, Pattern pattern, String expected);

    public void report() throws MojoFailureException;
}
