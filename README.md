[![Maven plugin available on Maven Central](https://img.shields.io/maven-central/v/dev.bodewig.mimic/mimic-maven-plugin?label=Maven%20plugin%20available%20on%20Maven%20Central)](https://central.sonatype.com/artifact/dev.bodewig.mimic/mimic-maven-plugin)
[![Gradle plugin available on Maven Central](https://img.shields.io/maven-central/v/dev.bodewig.mimic/dev.bodewig.mimic.gradle.plugin?label=Gradle%20plugin%20available%20on%20Maven%20Central)](https://central.sonatype.com/artifact/dev.bodewig.mimic/dev.bodewig.mimic.gradle.plugin)

# Mimic

A Mimic is a generated wrapper for an object with non-public fields.
It offers type-safe accessors using Java reflection to get and set all object fields.
This is useful to create custom serializers/deserializers for third-party classes or unit tests for example.

This repository contains a generic Annotation processor to create Mimics, and Maven and Gradle plugins for more comfort.

## Maven plugin usage

```xml
<plugin>
	<groupId>dev.bodewig.mimic</groupId>
	<artifactId>mimic-maven-plugin</artifactId>
	<version>1.1.4</version>
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
| classes | | yes | List of fully qualified class names to create Mimics for. The classes must be loadable from the compile classpath. |
| outputDirectory | `${project.build.directory}/generated-sources/mimic` |  | Relative project path where the generated Mimics are written to. Will be added as additional compile source directory. |
| packageName | | yes | Target package for the generated java classes |


## Gradle plugin usage (groovy)

Add the Mimic Annotation to your own class or configure third party classes via the plugin extension DSL.
Each Mimic Annotation can be parameterized with a package name, the configured package name in the plugin extension is used as a fallback.

```groovy
plugins {
	id 'dev.bodewig.mimic' version '1.1.4'
}
mimic {
	packageName = 'dev.bodewig.mimic.gradle.test.generated'
	classes = ['dev.bodewig.mimic.maven.test.MyTestClass']
}
```

If you use the `dev.bodewig.mimic` plugin, the Gradle Java plugin is applied automatically.

### Gradle plugin configuration

| Property | Description |
| -------- | ----------- |
| classes | List of fully qualified class names to create Mimics for. The classes must be loadable from the compile classpath. |
| packageName | The default target package for the generated java classes, fallback if no annotation with parameter is present |

All properties are required, there are no defaults.


## Annotation processor usage

Add the Mimic Annotation to your own class or configure third party classes via compileArgs.
Each Mimic Annotation can be parameterized with a package name, the configured package name in the compileArgs is used as a fallback.

The annotation processor can be used with multiple build tools, e.g. with Maven:

```xml
<dependency>
	<groupId>dev.bodewig.mimic</groupId>
	<artifactId>mimic-annotation</artifactId>
	<version>1.1.4</version>
</dependency>
...
<plugin>
	<groupId>org.apache.maven.plugins</groupId>
	<artifactId>maven-compiler-plugin</artifactId>
	<configuration>
		<annotationProcessorPaths>
			<path>
				<groupId>dev.bodewig.mimic</groupId>
				<artifactId>mimic-annotation-processor</artifactId>
				<version>1.1.4</version>
			</path>
		</annotationProcessorPaths>
		<compilerArgs>
			<arg>-Amimic.packageName=dev.bodewig.mimic.annotation.test.generated.lang</arg>
			<arg>-Amimic.classes=my.test.class.One,my.test.class.Two</arg>
		</compilerArgs>
	</configuration>
</plugin>
```

### Annotation processor configuration

| CompilerArg | Description |
| ----------- | ----------- |
| mimic.classes | Comma-separated list of fully qualified class names to create Mimics for |
| mimic.packageName | The default target package for the generated java classes, fallback if no annotation with parameter is present |


## Example

```java
public class MyTestClass {
  public int count = 1;
  private String name = "test";
}
```

`MyTestClass` has two fields, one public and one private.<br>
This is the generated Mimic with getter and setter methods for both fields:

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

Since `count` is a public field, reflection is not necessary and the Mimic offers type-safe reflective access to `name`.

```java
MyTestClass orig = ...
System.out.println(orig.name); // not possible outside of MyTestClass!

MyTestClassMimic mimic = new MyTestClassMimic(orig);
System.out.println(mimic.getName()); // prints orig.name
```


---

Run `git config --add include.path ../.gitconfig` to include the template config in your project config.
