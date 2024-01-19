# mimic-gradle-test

## Maven

This module only invokes the Gradle build and ensures the build order.

The following lifecycle phases are mapped to Gradle tasks:

| Maven phase | Gradle task         |
| ----------- | ------------------- |
| clean       | clean               |
| compile     | assemble            |
| test        | test                |


## Gradle

To invoke Gradle without Maven, the following project properties have to be supplied on the command line:

* groupId=dev.bodewig.mimic
* artifactId=mimic-gradle-test
* version=1.0.0

The module has a `compileOnly` dependency on `mimic-maven-test` since the Gradle plugin cannot create Mimics for classes located under `src`. The class packaged and installed in `mimic-maven-test` is only used for demonstration purpose. The `src` directory is empty to keep the `sourceSet` configuration and prevent IDE errors.
