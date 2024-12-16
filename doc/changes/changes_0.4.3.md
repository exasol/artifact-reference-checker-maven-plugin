# Artifact reference checker and unifier 0.4.3, released 2024-12-16

Code name: Fix CVE-2024-47554 in commons-io:commons-io:jar:2.11.0:test

## Summary

This release updates dependencies to fix CVE-2024-47554 in `commons-io:commons-io:jar:2.11.0:test`.

## Security

* #28: Fixed CVE-2024-47554 in `commons-io:commons-io:jar:2.11.0:test`

## Dependency Updates

### Compile Dependency Updates

* Updated `com.exasol:error-reporting-java:1.0.0` to `1.0.1`

### Test Dependency Updates

* Updated `com.exasol:maven-plugin-integration-testing:1.1.2` to `1.1.3`
* Updated `com.exasol:maven-project-version-getter:1.2.0` to `1.2.1`
* Removed `org.jacoco:org.jacoco.agent:0.8.8`
* Added `org.junit.jupiter:junit-jupiter-api:5.11.3`
* Removed `org.junit.jupiter:junit-jupiter-engine:5.8.2`
* Updated `org.junit.jupiter:junit-jupiter-params:5.8.2` to `5.11.3`
* Updated `org.slf4j:slf4j-jdk14:2.0.3` to `2.0.16`

### Plugin Dependency Updates

* Updated `com.exasol:project-keeper-maven-plugin:2.8.0` to `4.5.0`
* Added `com.exasol:quality-summarizer-maven-plugin:0.2.0`
* Updated `io.github.zlika:reproducible-build-maven-plugin:0.15` to `0.17`
* Updated `org.apache.maven.plugins:maven-clean-plugin:2.5` to `3.4.0`
* Updated `org.apache.maven.plugins:maven-compiler-plugin:3.10.1` to `3.13.0`
* Removed `org.apache.maven.plugins:maven-dependency-plugin:3.3.0`
* Updated `org.apache.maven.plugins:maven-deploy-plugin:3.0.0-M1` to `3.1.3`
* Updated `org.apache.maven.plugins:maven-enforcer-plugin:3.1.0` to `3.5.0`
* Updated `org.apache.maven.plugins:maven-failsafe-plugin:3.0.0-M5` to `3.5.2`
* Updated `org.apache.maven.plugins:maven-gpg-plugin:3.0.1` to `3.2.7`
* Updated `org.apache.maven.plugins:maven-install-plugin:2.4` to `3.1.3`
* Updated `org.apache.maven.plugins:maven-javadoc-plugin:3.4.0` to `3.11.1`
* Updated `org.apache.maven.plugins:maven-plugin-plugin:3.6.4` to `3.15.1`
* Updated `org.apache.maven.plugins:maven-resources-plugin:2.6` to `3.3.1`
* Updated `org.apache.maven.plugins:maven-site-plugin:3.3` to `3.21.0`
* Updated `org.apache.maven.plugins:maven-surefire-plugin:3.0.0-M5` to `3.5.2`
* Added `org.apache.maven.plugins:maven-toolchains-plugin:3.2.0`
* Added `org.basepom.maven:duplicate-finder-maven-plugin:2.0.1`
* Updated `org.codehaus.mojo:flatten-maven-plugin:1.2.7` to `1.6.0`
* Updated `org.codehaus.mojo:versions-maven-plugin:2.10.0` to `2.18.0`
* Updated `org.jacoco:jacoco-maven-plugin:0.8.8` to `0.8.12`
* Updated `org.sonarsource.scanner.maven:sonar-maven-plugin:3.9.1.2184` to `5.0.0.4389`
* Updated `org.sonatype.plugins:nexus-staging-maven-plugin:1.6.13` to `1.7.0`
