package dev.bodewig.mimic.gradle.plugin;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.internal.consumer.BlockingResultHandler;

import dev.bodewig.mimic.core.MimicCreator;

/**
 * A Mimic is a generated wrapper with type-safe accessors using Java reflection
 * to get and set non-public fields.
 * <p>
 * This plugin creates Mimics for a configured list of classes on the compile
 * classpath, in a configured package, in a configured output directory (see
 * {@link MimicPluginExtension}).
 * <p>
 * The generated classes are written in Java. The
 * {@link MimicPluginExtension#outputDirectory outputDirectory} is automatically
 * added to the main {@link SourceSet}.
 */
public abstract class MimicPlugin implements Plugin<Project> {

	/**
	 * Default constructor
	 */
	public MimicPlugin() {
	}

	@Override
	public void apply(Project project) {
		MimicPluginExtension extension = project.getExtensions().create("mimic", MimicPluginExtension.class);
		project.getPlugins().apply(JavaPlugin.class);

		boolean skipInParameters = Boolean.parseBoolean((String) project.getProperties().get("mimic.skip"));
		boolean skipInExtension = extension.skip != null && extension.skip.booleanValue();
		boolean skip = skipInParameters || skipInExtension;
		if (!skip) {
			TaskProvider<Task> mimic = project.getTasks().register("mimic", task -> {
				task.doLast(s -> {
					s.setGroup("Mimic");
					s.setDescription("Create Mimics for the configured classes");

					// make outputDirectory
					File outputDir = new File(project.getProjectDir(), extension.outputDirectory);
					outputDir.mkdirs();

					Map<String, String> properties = project.getGradle().getStartParameter().getProjectProperties();

					ProjectConnection connection = GradleConnector.newConnector()
							// .useGradleVersion(project.getGradle().getGradleVersion())
							.forProjectDirectory(project.getLayout().getProjectDirectory().getAsFile()).connect();

					BlockingResultHandler<Object> handler = new BlockingResultHandler<>(Object.class);

					try {
						BuildLauncher build = connection.newBuild();
						for (String key : properties.keySet()) {
							String param = key;
							if (!properties.get(key).isEmpty()) {
								param += "=" + properties.get(key);
							}
							System.out.println(param);
							build = build.addArguments("-P" + param);
						}
						build = build.addArguments("-Pmimic.skip=true").forTasks("compileJava");
						build.run(handler);

						/* Object result = */handler.getResult();
						// System.out.println(result.toString());
					} finally {
						connection.close();
					}
					/*
					 * JavaCompile compiler =
					 * project.getTasks().withType(JavaCompile.class).iterator().next();
					 * InputChanges changes = new InputChanges() {
					 * 
					 * } compiler.compile(changes);
					 */

					SourceSetContainer sourceSets = project.getExtensions().getByType(SourceSetContainer.class);
					SourceSet main = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME);

					// create Mimics
					try (URLClassLoader cl = createClassLoader(main)) {
						for (String className : extension.classes) {
							Class<?> clazz = cl.loadClass(className);
							MimicCreator.createMimic(clazz, extension.packageName, outputDir);
						}
					} catch (ClassNotFoundException | IOException e) {
						throw new RuntimeException(e);
					}

					// add outputDirectory as compile source
					main.getJava().srcDir(extension.outputDirectory);
				});
			});

			project.getTasksByName("compileJava", false).forEach(t -> t.dependsOn(mimic));
		}
	}

	/**
	 * Returns a new class loader that can load all classes from the compile
	 * classpath of the passed {@code SourceSet}
	 *
	 * @param main The main {@code SourceSet}
	 * @return A class loader that can load all classes from the compile classpath
	 *         of {@code main}
	 * @throws MalformedURLException See {@link java.net.URI#toURL()}
	 */
	protected URLClassLoader createClassLoader(SourceSet main) throws MalformedURLException {
		List<File> classFiles = new ArrayList<>(main.getCompileClasspath().getFiles());
		URL[] urls = new URL[classFiles.size()];
		for (int i = 0; i < classFiles.size(); i++) {
			urls[i] = classFiles.get(i).toURI().toURL();
		}
		return new URLClassLoader(urls, this.getClass().getClassLoader());
	}
}
