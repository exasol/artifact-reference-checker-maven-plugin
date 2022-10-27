# Artifact reference checker and unifier 0.4.2, released 2022-10-27

Code name: Upgrade dependencies

## Summary

Release 0.4.2 fixes situations where the artifact has a prefix. Before the fix it happened that the `unify` command overwrote the prefix and the artifact instead of only the artifact.

Instead of looking for any matching characters in variable replacements, only letters, numbers, dash, underscore and dot are accepted now. This resolves situations where words with any kind of separators (spaces, slashes, commas, etc.) were interpreted as part of the artifact name.

Note that due to the nature of the replacement mechanism, you can still construct situations in which prefixes are mistakenly changed, but they should not appear but that only happens if you have repetitions in the actual artifact name, which is very unlikely.

## Bugfixes

* #22: Updated dependencies to fix vulnerabilities
* #24: Fixed handling of prefixes

## Dependency Updates

### Test Dependency Updates

* Updated `com.exasol:maven-plugin-integration-testing:1.1.1` to `1.1.2`
* Removed `junit:junit:4.13.2`
* Updated `org.jacoco:org.jacoco.agent:0.8.7` to `0.8.8`

### Plugin Dependency Updates

* Updated `com.exasol:error-code-crawler-maven-plugin:1.0.0` to `1.1.2`
* Updated `com.exasol:project-keeper-maven-plugin:1.3.4` to `2.4.6`
* Updated `org.apache.maven.plugins:maven-compiler-plugin:3.10.0` to `3.10.1`
* Updated `org.apache.maven.plugins:maven-dependency-plugin:2.8` to `3.3.0`
* Updated `org.apache.maven.plugins:maven-deploy-plugin:3.0.0-M2` to `3.0.0-M1`
* Updated `org.apache.maven.plugins:maven-failsafe-plugin:3.0.0-M3` to `3.0.0-M5`
* Updated `org.apache.maven.plugins:maven-javadoc-plugin:3.3.2` to `3.4.0`
* Updated `org.apache.maven.plugins:maven-surefire-plugin:3.0.0-M3` to `3.0.0-M5`
* Added `org.codehaus.mojo:flatten-maven-plugin:1.2.7`
* Updated `org.codehaus.mojo:versions-maven-plugin:2.9.0` to `2.10.0`
* Updated `org.jacoco:jacoco-maven-plugin:0.8.7` to `0.8.8`
* Added `org.sonarsource.scanner.maven:sonar-maven-plugin:3.9.1.2184`
* Updated `org.sonatype.plugins:nexus-staging-maven-plugin:1.6.8` to `1.6.13`
