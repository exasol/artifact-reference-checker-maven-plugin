package com.exasol.artifactreferencechecker;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import com.exasol.mavenpluginintegrationtesting.MavenIntegrationTestEnvironment;
import com.exasol.mavenprojectversiongetter.MavenProjectVersionGetter;

class ArtifactReferenceCheckerMojoIT {
    private static final String CURRENT_VERSION = MavenProjectVersionGetter.getCurrentProjectVersion();
    private static final File PLUGIN_JAR = Path
            .of("target", "artifact-reference-checker-maven-plugin-" + CURRENT_VERSION + ".jar").toFile();
    private static final File PLUGIN_POM = Path.of(".flattened-pom.xml").toFile();
    private static final Path TEST_PROJECT = Path.of("src", "test", "resources", "unit", "test_project");
    private static MavenIntegrationTestEnvironment testEnvironment;
    @TempDir
    Path tempDir;
    private Verifier verifier;

    @BeforeAll
    static void beforeAll() {
        testEnvironment = new MavenIntegrationTestEnvironment();
        testEnvironment.installPlugin(PLUGIN_JAR, PLUGIN_POM);
    }

    @BeforeEach
    void beforeEach() throws IOException {
        FileUtils.copyDirectory(TEST_PROJECT.toFile(), this.tempDir.toFile());
        writeCurrentVersionToPom();
        this.verifier = testEnvironment.getVerifier(this.tempDir);
    }

    private void writeCurrentVersionToPom() throws IOException {
        replaceInPom("CURRENT_VERSION", CURRENT_VERSION);
    }

    private void replaceInPom(final String search, final String replacement) throws IOException {
        final Path pom = this.tempDir.resolve("pom.xml");
        final String pomTemplate = Files.readString(pom);
        final String pomContent = pomTemplate.replace(search, replacement);
        Files.writeString(pom, pomContent);
    }

    @Test
    void testVerify() {
        final String message = assertThrows(VerificationException.class, () -> this.verifier.executeGoal("package"))
                .getMessage();
        assertAll(//
                () -> assertThat(message, containsString("Detected artifact name: "
                        + "'prefix-propval-middle-project-under-test-1.3.3.7.jar'")),
                () -> assertThat(message, containsString("Found outdated artifact reference: "
                        + "'prefix-propval-middle-project-under-test-0.0.1.jar' in ")),
                () -> assertThat(message,
                        containsString("Found outdated artifact reference: "
                                + "'prefix-different_value-middle-project-under-test-1.3.3.7.jar' in ")),
                () -> assertThat(message, not(containsString("/valid.md"))),
                // The following files are excluded, so the contained references must not appear.
                () -> assertThat(message, not(containsString("prefix-propval-middle-project-under-test-4.5.6.jar"))),
                () -> assertThat(message, not(containsString("prefix-propval-middle-project-under-test--7.8.9.jar")))
        );
    }

    @Test
    void testVerifyWithShadePlugin() throws IOException {
        replaceInPom("maven-assembly-plugin", "maven-shade-plugin");
        final String message = assertThrows(VerificationException.class, () -> this.verifier.executeGoal("package"))
                .getMessage();
        assertThat(message, containsString("Detected artifact name: "
                + "'prefix-propval-middle-project-under-test-1.3.3.7.jar'"));
    }

    @Test
    void testUnify() throws IOException, VerificationException {
        runUnify();
        final String fileContent = Files.readString(this.tempDir.resolve("nested/nested_invalid.md"));
        assertAll(//
                () -> assertThat(fileContent, not(containsString("test-prefix-0.0.0-dynamodb-3.2.1.jar"))),
                () -> assertThat(fileContent, containsString("prefix-propval-middle-project-under-test-1.3.3.7.jar")));
    }

    @Test
    void testUnifyLeavesSurroundingTextIntact() throws  IOException, VerificationException {
        runUnify();
        final String fileContent = Files.readString(this.tempDir.resolve("invalid.md"));
        assertAll(//
                () -> assertThat(fileContent, containsString("For instance, `s3://prefix/some/other/path/elements/"
                        + "prefix-propval-middle-project-under-test-1.3.3.7.jar`.")));
    }

    @Test
    void testUnifyDoesNotChangeExcluded() throws IOException, VerificationException {
        runUnify();
        final String fileContent = Files.readString(this.tempDir.resolve("nested/excluded_invalid.md"));
        assertThat(fileContent, containsString("prefix-propval-middle-project-under-test--7.8.9.jar"));
    }

    private void runUnify() throws VerificationException {
        this.verifier.executeGoal("artifact-reference-checker:unify");
    }
}
