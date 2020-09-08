# Artifact Reference Checker Maven Plugin


[![Build Status](https://travis-ci.com/exasol/artifact-reference-checker-maven-plugin.svg?branch=master)](https://travis-ci.com/exasol/artifact-reference-checker-maven-plugin)
[![Maven Central](https://img.shields.io/maven-central/v/com.exasol/artifact-reference-checker-maven-plugin)](https://search.maven.org/artifact/com.exasol/artifact-reference-checker-maven-plugin)

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=com.exasol%3Aartifact-reference-checker-maven-plugin&metric=alert_status)](https://sonarcloud.io/dashboard?id=com.exasol%3Aartifact-reference-checker-maven-plugin)

[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=com.exasol%3Aartifact-reference-checker-maven-plugin&metric=security_rating)](https://sonarcloud.io/dashboard?id=com.exasol%3Aartifact-reference-checker-maven-plugin)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=com.exasol%3Aartifact-reference-checker-maven-plugin&metric=reliability_rating)](https://sonarcloud.io/dashboard?id=com.exasol%3Aartifact-reference-checker-maven-plugin)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=com.exasol%3Aartifact-reference-checker-maven-plugin&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=com.exasol%3Aartifact-reference-checker-maven-plugin)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=com.exasol%3Aartifact-reference-checker-maven-plugin&metric=sqale_index)](https://sonarcloud.io/dashboard?id=com.exasol%3Aartifact-reference-checker-maven-plugin)

[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=com.exasol%3Aartifact-reference-checker-maven-plugin&metric=code_smells)](https://sonarcloud.io/dashboard?id=com.exasol%3Aartifact-reference-checker-maven-plugin)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=com.exasol%3Aartifact-reference-checker-maven-plugin&metric=coverage)](https://sonarcloud.io/dashboard?id=com.exasol%3Aartifact-reference-checker-maven-plugin)
[![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=com.exasol%3Aartifact-reference-checker-maven-plugin&metric=duplicated_lines_density)](https://sonarcloud.io/dashboard?id=com.exasol%3Aartifact-reference-checker-maven-plugin)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=com.exasol%3Aartifact-reference-checker-maven-plugin&metric=ncloc)](https://sonarcloud.io/dashboard?id=com.exasol%3Aartifact-reference-checker-maven-plugin)


This [Maven-Plugin](https://maven.apache.org/plugins/index.html) helps you not to forget to change references to your artifacts.
For example if you reference your binary in the README.md file, this plugin will break the build, if you forgot to update the version number there.

The plugin automatically extracts the naming schema of your artifact from the assembly plugin in the `pom.xml` file.

In addition it can automatically adapt the references so that they refer to the current version.

## Installation

Add the following lines to your `pom.xml` file.

```xml
<plugin>
    <groupId>com.exasol</groupId>
    <artifactId>artifact-reference-checker-maven-plugin</artifactId>
    <version>0.3.0</version>
    <executions>
        <execution>
            <goals>
                <goal>verify</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <!-- Add configuration here -->
    </configuration>
</plugin> 
```

## Usage

* When you run `mvn verify` this plugin will run the checks 
* Run checks manually: `mvn artifact-reference-checker:verify`
* Unify the versions using: `mvn artifact-reference-checker:unify`

## Configuration

* Exclude file:
    You can exclude files from verification and unification.
     
     ```xml
    <excludes>
        <exclude>doc/changes/*</exclude>
    </excludes>
    ``` 
  
  If the path starts with a `/` it is relative to the project's root directory. Otherwise, it acts as a pattern that can also exclude files in nested directories without specifying the whole path. For example `*.md`.

## Information for Users

* [Changelog](doc/changes/changelog.md)

## Dependencies

### Run Time Dependencies

| Dependency                                                                             | Purpose                                                | License                          |
|-------------------------------------------------------------------------------------|--------------------------------------------------------|----------------------------------|
| [Maven Plugin API](http://maven.apache.org/ref/3.6.3/maven-plugin-api/)             | Building a Maven plugin                                | Apache License 2.0               |
| [Maven Project][maven-project]                                                      | Access to pom file contents                            | Apache License 2.0               |
| [Maven Plugin Annotations][maven-plugin-annotations]                                | Building a Maven plugin                                | Apache License 2.0               |

### Test Dependencies

| Dependency                                                                             | Purpose                                                | License                          |
|-------------------------------------------------------------------------------------|--------------------------------------------------------|----------------------------------|
| [Apache Maven](https://maven.apache.org/)                                           | Build tool                                             | Apache License 2.0               |
| [Java Hamcrest](http://hamcrest.org/JavaHamcrest/)                                  | Checking for conditions in code via matchers           | BSD License                      |
| [JUnit](https://junit.org/junit5)                                                   | Unit testing framework                                 | Eclipse Public License 1.0       |
| [Testcontainers](https://www.testcontainers.org/)                                   | Container-based integration tests                      | MIT License                      |
| [SLF4J](http://www.slf4j.org/)                                                      | Logging facade                                         | MIT License                      |

### Maven Plug-ins

| Plug-in                                                                             | Purpose                                                | License                          |
|-------------------------------------------------------------------------------------|--------------------------------------------------------|----------------------------------|
| [Maven Plugin Plugin](https://maven.apache.org/plugin-tools/maven-plugin-plugin/)   | Building a Maven plugin                                | Apache License 2.0               |
| [Sonatype OSS Index Maven Plugin][sonatype-oss-index-maven-plugin]                  | Checking Dependencies Vulnerability                    | ASL2                             |
| [Versions Maven Plugin][versions-maven-plugin]                                      | Checking if dependencies updates are available         | Apache License 2.0               |
| [Maven Enforcer Plugin][maven-enforcer-plugin]                                      | Controlling environment constants                      | Apache License 2.0               |
| [Maven Failsafe Plugin](https:/maven.apache.org/surefire/maven-surefire-plugin/)    | Integration testing                                    | Apache License 2.0               |
| [Maven Compiler Plugin](https://maven.apache.org/plugins/maven-compiler-plugin/)    | Setting required Java version                          | Apache License 2.0               |
| [Maven Source Plugin](https://maven.apache.org/plugins/maven-source-plugin/)        | Creating a source code JAR                             | Apache License 2.0               |
| [Maven Javadoc Plugin](https://maven.apache.org/plugins/maven-javadoc-plugin/)      | Creating a Javadoc JAR                                 | Apache License 2.0               |
| [Maven GPG Plugin](https://maven.apache.org/plugins/maven-gpg-plugin/)              | Signs JARs                                             | Apache License 2.0               |

[maven-project]: https://maven.apache.org/ref/3.5.0/apidocs/org/apache/maven/project/MavenProject.html
[sonatype-oss-index-maven-plugin]: https://sonatype.github.io/ossindex-maven/maven-plugin/
[versions-maven-plugin]: https://www.mojohaus.org/versions-maven-plugin/
[maven-enforcer-plugin]: http://maven.apache.org/enforcer/maven-enforcer-plugin/
[maven-plugin-annotations]:https://maven.apache.org/plugin-tools/maven-plugin-annotations/index.html
