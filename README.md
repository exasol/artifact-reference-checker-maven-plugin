# Artifact Reference Checker Maven Plugin

This [Maven-Plugin](https://maven.apache.org/plugins/index.html) helps you not to forget to change reference to your artifacts.
For example if you reference your binary in the README.md file, this plugin will break the build, if you forgot to update the version number there.

In addition this plugin can unify the versions for you.

## Installation

Add the following lines to your `pom.xml` file.

```xml
<plugin>
    <groupId>com.exasol</groupId>
    <artifactId>artifact-reference-checker-maven-plugin</artifactId>
    <version>0.0.1</version>
    <executions>
        <execution>
            <goals>
                <goal>verify</goal>
            </goals>
        </execution>
    </executions>
</plugin> 
```

## Usage

* Run checks manually: `mvn artifact-reference-checker:verify`
## Dependencies

### Run Time Dependencies

| Dependency                                                                             | Purpose                                                | License                          |
|-------------------------------------------------------------------------------------|--------------------------------------------------------|----------------------------------|
| [Maven Plugin API](http://maven.apache.org/ref/3.6.3/maven-plugin-api/)             | Building a Maven plugin                                | Apache License 2.0               |
| [Maven Core](http://maven.apache.org/ref/3.6.3/maven-core/)                         | Building a Maven plugin                                | Apache License 2.0               |
| [Maven Artifact](https://maven.apache.org/ref/3.3.1/maven-artifact/)                | Building a Maven plugin                                | Apache License 2.0               |
| [Maven Compat](https://maven.apache.org/ref/3.6.3/maven-compat/)                    | Building a Maven plugin                                | Apache License 2.0               |
| [Maven Project][maven-project]                                                      | Access to pom file contents                            | Apache License 2.0               |

### Test Dependencies

| Dependency                                                                             | Purpose                                                | License                          |
|-------------------------------------------------------------------------------------|--------------------------------------------------------|----------------------------------|
| [JUnit](https://junit.org/junit5)                                                   | Unit testing framework                                 | Eclipse Public License 1.0       |
| [Maven Plugin Testing Harness][maven-plugin-testing-harness]                        | Unit testing for Maven plugin                          | Apache License 2.0               |

### Maven Plug-ins

| Plug-in                                                                             | Purpose                                                | License                          |
|-------------------------------------------------------------------------------------|--------------------------------------------------------|----------------------------------|
| [Maven Plugin Plugin](https://maven.apache.org/plugin-tools/maven-plugin-plugin/)   | Building a Maven plugin                                | Apache License 2.0               |

[maven-plugin-testing-harness]: https://maven.apache.org/plugin-testing/maven-plugin-testing-harness/
[maven-project]: https://maven.apache.org/ref/3.5.0/apidocs/org/apache/maven/project/MavenProject.html