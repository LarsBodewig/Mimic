[![Maven plugin available on Maven Central](https://img.shields.io/maven-central/v/dev.bodewig.mimic/mimic-maven-plugin?label=Maven%20plugin%20available%20on%20Maven%20Central)](https://central.sonatype.com/artifact/dev.bodewig.mimic/mimic-maven-plugin)
[![Gradle plugin available on Maven Central](https://img.shields.io/maven-central/v/dev.bodewig.mimic/mimic-gradle-plugin?label=Gradle%20plugin%20available%20on%20Maven%20Central)](https://central.sonatype.com/artifact/dev.bodewig.mimic/mimic-gradle-plugin)

# Mimic

A mimic is a generated wrapper for an object with non-public fields.
It offers type-safe accessors using Java reflection to get and set all object fields.
This is useful for example to create custom serializers/deserializers for third-party classes.

This repository contains a Maven plugin and a Gradle plugin to generate mimics.

## Maven plugin usage

```xml
<plugin>
	<groupId>dev.bodewig.mimic</groupId>
	<artifactId>mimic-maven-plugin</artifactId>
	<version>1.0.0</version>
	<executions>
		<execution>
			<goals>
				<goal>mimic</goal>
			</goals>
			<phase>generate-sources</phase><!-- default -->
			<configuration>
				<classes>
					<class>dev.bodewig.mimic.maven.test.MyTestClass</class>
				</classes>
				<outputDirectory>${project.build.directory}/generated-sources/mimic</outputDirectory><!-- default -->
				<packageName>dev.bodewig.mimic.maven.test.generated</packageName>
			</configuration>
		</execution>
	</executions>
</plugin>
```

### Maven plugin configuration

| Property | Default | Required | Description |
| -------- | ------- | -------- | ----------- |
| classes | | yes | List of fully qualified class names to create mimics for. The classes must be loadable from the compile classpath. |
| outputDirectory | `${project.build.directory}/generated-sources/mimic` |  | Relative project path where the generated mimics are written to. Will be added as a SourceSetDirectory to the main SourceSet. |
| packageName | | yes | Target package for the generated java classes |


## Gradle plugin usage (groovy)

```groovy
plugins {
	id 'dev.bodewig.mimic.gradle.plugin' version '1.0.0'
}
mimic {
	packageName = 'dev.bodewig.mimic.gradle.test.generated'
	classes = ['dev.bodewig.mimic.maven.test.MyTestClass']
	outputDirectory = 'src/generated/mimic/'
}
```

Due to limitations in Gradle, you cannot configure classes that are part of the module the mimic plugin is applied to.
If you need mimics for your own classes, create a separate project module that depends on your other module and applies the plugin.

### Gradle plugin configuration

| Property | Description |
| -------- | ----------- |
| classes | List of fully qualified class names to create mimics for. The classes must be loadable from the compile classpath. |
| outputDirectory | Relative project path where the generated mimics are written to. Will be added as a SourceSetDirectory to the main SourceSet. |
| packageName | Target package for the generated java classes |


## Example

```java
public class MyTestClass {
	public int count = 1;
	private String name = "test";
}
```

`MyTestClass` has two fields, one public and one private. This is the generated mimic with getter and setter methods for both fields:

```java
public class MyTestClassMimic {
  private final MyTestClass instance;

  public MyTestClassMimic(MyTestClass instance) {
    this.instance = instance;
  }

  public int getCount() {
    return instance.count;
  }

  public void setCount(int value) {
    instance.count = value;
  }

  public String getName() {
    try {
      Field f = MyTestClass.class.getDeclaredField("name");
      f.setAccessible(true);
      return (String) f.get(instance);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  public void setName(String value) {
    try {
      Field f = MyTestClass.class.getDeclaredField("name");
      f.setAccessible(true);
      f.set(instance, value);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }
}
```

Since `count` is a public field, no reflection is needed and the mimic offers type-safe  reflective access to `name`.

```java
MyTestClass orig = ...
System.out.println(orig.name); // not possible outside of MyTestClass!

MyTestClassMimic mimic = new MyTestClassMimic(orig);
System.out.println(mimic.getName()); // prints orig.name
```


---

Run `git config --add include.path ../.gitconfig` to include the template config in your project config.
