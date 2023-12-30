# mimic-gradle-plugin

## Maven

This module only invokes the Gradle build and ensures the build order.

The following phases are mapped to Gradle tasks:

| Maven phase | Gradle task         |
| ----------- | ------------------- |
| clean       | clean               |
| compile     | build               |
| install     | publishToMavenLocal | 
| deploy      | publish             |

The `maven-install-plugin` and `maven-deploy-plugin` are skipped since Gradle installs custom artifacts with the same coordinates.


## Gradle

To invoke Gradle without Maven, the following project properties have to be supplied on the command line:

* groupId=dev.bodewig.mimic
* artifactId=mimic-gradle-plugin
* version=

The `mimic-gradle-plugin` also applies the Gradle Java plugin.

See the project `README` for the plugin usage.
