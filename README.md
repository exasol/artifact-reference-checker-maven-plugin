# Artifact Reference Checker Maven Plugin

[![Build Status](https://github.com/exasol/artifact-reference-checker-maven-plugin/actions/workflows/ci-build.yml/badge.svg)](https://github.com/exasol/artifact-reference-checker-maven-plugin/actions/workflows/ci-build.yml)
[![Maven Central &ndash; Artifact reference checker and unifier](https://img.shields.io/maven-central/v/com.exasol/artifact-reference-checker-maven-plugin)](https://search.maven.org/artifact/com.exasol/artifact-reference-checker-maven-plugin)

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=com.exasol%3Aartifact-reference-checker-maven-plugin&metric=alert_status)](https://sonarcloud.io/dashboard?id=com.exasol%3Aartifact-reference-checker-maven-plugin)

[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=com.exasol%3Aartifact-reference-checker-maven-plugin&metric=security_rating)](https://sonarcloud.io/dashboard?id=com.exasol%3Aartifact-reference-checker-maven-plugin)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=com.exasol%3Aartifact-reference-checker-maven-plugin&metric=reliability_rating)](https://sonarcloud.io/dashboard?id=com.exasol%3Aartifact-reference-checker-maven-plugin)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=com.exasol%3Aartifact-reference-checker-maven-plugin&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=com.exasol%3Aartifact-reference-checker-maven-plugin)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=com.exasol%3Aartifact-reference-checker-maven-plugin&metric=sqale_index)](https://sonarcloud.io/dashboard?id=com.exasol%3Aartifact-reference-checker-maven-plugin)

[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=com.exasol%3Aartifact-reference-checker-maven-plugin&metric=code_smells)](https://sonarcloud.io/dashboard?id=com.exasol%3Aartifact-reference-checker-maven-plugin)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=com.exasol%3Aartifact-reference-checker-maven-plugin&metric=coverage)](https://sonarcloud.io/dashboard?id=com.exasol%3Aartifact-reference-checker-maven-plugin)
[![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=com.exasol%3Aartifact-reference-checker-maven-plugin&metric=duplicated_lines_density)](https://sonarcloud.io/dashboard?id=com.exasol%3Aartifact-reference-checker-maven-plugin)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=com.exasol%3Aartifact-reference-checker-maven-plugin&metric=ncloc)](https://sonarcloud.io/dashboard?id=com.exasol%3Aartifact-reference-checker-maven-plugin)

This [Maven-Plugin](https://maven.apache.org/plugins/index.html) helps you not to forget to change references to your artifacts. For example if you reference your binary in the README.md file, this plugin will break the build, if you forgot to update the version number there.

The plugin automatically extracts the naming schema of your artifact from the assembly plugin in the `pom.xml` file.

In addition it can automatically adapt the references so that they refer to the current version.

## Installation

Add the following lines to your `pom.xml` file.

```xml

<plugin>
    <groupId>com.exasol</groupId>
    <artifactId>artifact-reference-checker-maven-plugin</artifactId>
    <version>0.3.1</version>
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
* [Dependencies](dependencies.md)
