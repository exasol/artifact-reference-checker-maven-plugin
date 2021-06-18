# Artifact reference checker and unifier 0.3.2, released 2021-??-??

Code name: Maintenance

## Summary

In this release we updated dependencies and project structure. By that we also fixed the transitive CVE-2020-15250.

## Bug Fixes

* #15: Fixed transitive CVE-2020-15250

## Dependency Updates

### Compile Dependency Updates

* Updated `org.apache.maven:maven-plugin-api:3.6.3` to `3.8.1`

### Runtime Dependency Updates

* Added `org.jacoco:org.jacoco.agent:0.8.5`

### Test Dependency Updates

* Added `com.exasol:maven-plugin-integration-testing:0.1.0`
* Added `com.exasol:maven-project-version-getter:0.1.0`
* Updated `org.junit.jupiter:junit-jupiter-engine:5.6.2` to `5.7.2`
* Updated `org.junit.jupiter:junit-jupiter-params:5.6.2` to `5.7.2`
* Removed `org.junit.platform:junit-platform-runner:1.6.2`
* Updated `org.slf4j:slf4j-jdk14:1.7.30` to `1.7.31`
* Removed `org.testcontainers:junit-jupiter:1.14.3`
* Removed `org.testcontainers:testcontainers:1.14.3`

### Plugin Dependency Updates

* Added `com.exasol:error-code-crawler-maven-plugin:0.1.1`
* Added `com.exasol:project-keeper-maven-plugin:0.7.3`
* Added `io.github.zlika:reproducible-build-maven-plugin:0.13`
* Added `org.apache.maven.plugins:maven-dependency-plugin:2.8`
* Updated `org.apache.maven.plugins:maven-surefire-plugin:2.12.4` to `3.0.0-M3`
* Added `org.jacoco:jacoco-maven-plugin:0.8.5`
* Added `org.sonatype.plugins:nexus-staging-maven-plugin:1.6.8`
