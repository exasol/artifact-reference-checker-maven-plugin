package com.exasol.artifactreferencechecker;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;

import java.io.File;

public class ArtifactReferenceCheckerMojoTest extends AbstractMojoTestCase {

    public ArtifactReferenceCheckerMojoTest(){
        super();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }


    public void testSomething() throws Exception {
        File pom = getTestFile("src/test/resources/unit/project-to-test/pom.xml");
        assertNotNull(pom);
        assertTrue(pom.exists());
        ArtifactReferenceCheckerMojo mojo = (ArtifactReferenceCheckerMojo) lookupMojo("verify", pom);
        assertNotNull(mojo);
        mojo.execute();
    }
}
