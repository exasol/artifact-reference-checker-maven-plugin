package com.exasol.artifactreferencechecker;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.Container.ExecResult;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class ArtifactReferenceCheckerMojoIT {
    private static final File PLUGIN = Path.of("target", "artifact-reference-checker-maven-plugin-0.2.0.jar").toFile();
    private static final File PLUGIN_POM = Path.of("pom.xml").toFile();
    private static final File TEST_PROJECT = Path.of("src", "test", "resources", "unit", "test_project").toFile();

    @Container
    public static GenericContainer mvnContainer = new GenericContainer("maven:3.6.3-openjdk-11")
            .withFileSystemBind(PLUGIN.getAbsolutePath(), "/tmp.jar", BindMode.READ_ONLY)
            .withFileSystemBind(PLUGIN_POM.getAbsolutePath(), "/plugin_pom.xml", BindMode.READ_ONLY)
            .withFileSystemBind(TEST_PROJECT.getAbsolutePath(), "/test_project", BindMode.READ_ONLY)
            .withCommand("tail", "-f", "/dev/null");

    @BeforeAll
    static void beforeAll() throws IOException, InterruptedException {
        runWithCheck("mvn", "--batch-mode", "install:install-file", "-Dfile=/tmp.jar", "-DpomFile=/plugin_pom.xml",
                "--log-file", "/dev/stdout");
    }

    private static void runWithCheck(final String... command) throws IOException, InterruptedException {
        final ExecResult result = mvnContainer.execInContainer(command);
        System.out.println(result.getStdout());
        System.out.println(result.getStderr());
        if (result.getExitCode() != 0) {
            throw new IllegalStateException("Command " + String.join(" ", command) + " failed");
        }
    }

    @AfterEach
    void after() throws IOException, InterruptedException {
        runWithCheck("rm", "-rf", "/tmp/test_project");
    }

    @Test
    void testVerify() throws IOException, InterruptedException {
        runWithCheck("cp", "-r", "/test_project", "/tmp/test_project");// copy to make it writeable
        final ExecResult result = mvnContainer.execInContainer("mvn", "--batch-mode", "-e", "-f",
                "/tmp/test_project/pom.xml",
                "verify", "--log-file", "/dev/stdout", "--no-transfer-progress");
        assertAll(//
                () -> assertThat(result.getExitCode(), not(is(0))),
                () -> assertThat(result.getStdout(),
                        containsString("Detected artifact name:test-prefix-1.2.3-dynamodb-1.0.0.jar")),
                () -> assertThat(result.getStdout(), containsString(
                        "Found outdated artifact reference: test-prefix-0.0.0-dynamodb-3.2.1.jar in  /tmp/test_project/invalid.md")),
                () -> assertThat(result.getStdout(), containsString(
                        "Found outdated artifact reference: test-prefix-0.0.0-dynamodb-3.2.1.jar in  /tmp/test_project/nested/nested_invalid.md")),
                () -> assertThat(result.getStdout(), not(containsString("/valid.md")))//
        );
    }

    @Test
    void testUnify() throws IOException, InterruptedException {
        runWithCheck("cp", "-r", "/test_project/", "/tmp/test_project");// copy to make it writeable
        runWithCheck("mvn", "--batch-mode", "-f", "/tmp/test_project/pom.xml", "artifact-reference-checker:unify",
                "--log-file", "/dev/stdout", "--no-transfer-progress");
        final ExecResult result = mvnContainer.execInContainer("cat", "/tmp/test_project/nested/nested_invalid.md");
        assertAll(//
                () -> assertThat(result.getExitCode(), is(0)),
                () -> assertThat(result.getStdout(), not(containsString("test-prefix-0.0.0-dynamodb-3.2.1.jar"))),
                () -> assertThat(result.getStdout(), containsString("test-prefix-1.2.3-dynamodb-1.0.0.jar")));
    }
}
