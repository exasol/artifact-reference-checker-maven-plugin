package com.exasol.artifactreferencechecker;

import java.nio.file.Path;
import java.util.regex.Pattern;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Interface for verify or unify specific behaviour.
 */
public interface FileAndLineVisitor {

    /**
     * Visit the given file. This is called before the file content is processed with
     * {@link #visitLine(String, Pattern, String)}.
     * 
     * @param file the visited file
     */
    public void visit(Path file);

    /**
     * This is called after the complete file content is processed.
     * 
     * @param file the visited file
     * @throws MojoExecutionException in case processing fails
     */
    public void leave(Path file) throws MojoExecutionException;

    /**
     * This is called for each line of a visited file.
     * 
     * @param line     the processed line
     * @param pattern  the regular expression
     * @param expected the expected text
     */
    public void visitLine(String line, Pattern pattern, String expected);

    /**
     * This method is called after all files have been visited.
     * 
     * @throws MojoFailureException in case processing fails
     */
    public void report() throws MojoFailureException;
}
