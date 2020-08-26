package com.exasol.artifactreferencechecker;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.plugin.testing.stubs.MavenProjectStub;
import org.apache.maven.shared.utils.ReaderFactory;

import java.io.File;

public class ProjectStub  extends MavenProjectStub {
    public ProjectStub() {
        MavenXpp3Reader pomReader = new MavenXpp3Reader();
        Model model;
        try
        {
            model = pomReader.read( ReaderFactory.newXmlReader( new File( getBasedir(), "pom.xml" ) ) );
            setModel( model );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e );
        }
    }
}
