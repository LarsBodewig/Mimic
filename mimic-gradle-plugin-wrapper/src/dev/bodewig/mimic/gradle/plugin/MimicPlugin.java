package dev.bodewig.mimic.gradle.plugin;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
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
import org.gradle.tooling.ModelBuilder;
import org.gradle.tooling.internal.consumer.BlockingResultHandler;
import org.gradle.tooling.provider.model.ToolingModelBuilderRegistry;
import org.gradle.tooling.GradleConnectionException;
import org.gradle.tooling.ResultHandler;

import dev.bodewig.mimic.core.MimicCreator;
import dev.bodewig.mimic.gradle.plugin.ResultModel.Builder;

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

	private final ToolingModelBuilderRegistry registry;

	/**
	 * Default constructor
	 * 
	 * @param registry injected ToolingModelBuilderRegistry
	 */
	@Inject
	public MimicPlugin(ToolingModelBuilderRegistry registry) {
		this.registry = registry;
	}

	@Override
	public void apply(Project project) {
		project.getPlugins().apply(JavaPlugin.class);
		MimicPluginExtension extension = project.getExtensions().create("mimic", MimicPluginExtension.class);

		boolean runCollect = Boolean.parseBoolean((String) project.getProperties().get("mimic.collect"));
		if (runCollect) {
			registry.register(new ResultModel.Builder());
		} else {
			registerMimicTask(project, extension);
		}
	}

	private void registerMimicTask(Project project, MimicPluginExtension extension) {
		TaskProvider<Task> mimic = project.getTasks().register("mimic", task -> {
			task.setGroup("Mimic");
			task.setDescription("Create Mimics for the configured classes");
			task.doLast(s -> {
				// make outputDirectory
				File outputDir = new File(project.getProjectDir(), extension.outputDirectory);
				outputDir.mkdirs();

				Map<String, String> properties = project.getGradle().getStartParameter().getProjectProperties();

				ProjectConnection connection = GradleConnector.newConnector()
						.forProjectDirectory(project.getLayout().getProjectDirectory().getAsFile()).connect();
				try {
					ModelBuilder<ResultModel> build = connection.model(ResultModel.class);
					for (String key : properties.keySet()) {
						String param = key;
						if (!properties.get(key).isEmpty()) {
							param += "=" + properties.get(key);
						}
						// System.out.println(param);
						build = build.addArguments("-P" + param);
					}
					build = build.addArguments("-Pmimic.collect=true").addArguments("--stacktrace")
							.forTasks("compileJava");
					ResultModel result = build.get();

					System.out.println(result.test());
				} finally {
					connection.close();
				}

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
