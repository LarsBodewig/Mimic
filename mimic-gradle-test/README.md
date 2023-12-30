# mimic-gradle-test

## Maven

This module only invokes the Gradle build and ensures the build order.

The following phases are mapped to Gradle tasks:

| Maven phase | Gradle task         |
| ----------- | ------------------- |
| clean       | clean               |
| compile     | build               |

The `maven-install-plugin` is skipped even though Gradle does not install any artifacts.


## Gradle

To invoke Gradle without Maven, the following project properties have to be supplied on the command line:

* groupId=dev.bodewig.mimic
* artifactId=mimic-gradle-plugin
* version=

The module has a `compileOnly` dependency on `mimic-maven-test` since the Gradle plugin cannot create mimics for classes located under `src`. The class packaged and installed in `mimic-maven-test` is only used for demonstration purpose. The `src` directory is empty to keep the `sourceSet` configuration and prevent IDE errors.
