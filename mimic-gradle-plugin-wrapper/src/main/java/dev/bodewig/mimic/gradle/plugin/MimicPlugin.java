package dev.bodewig.mimic.gradle.plugin;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.compile.JavaCompile;

import dev.bodewig.mimic.annotation.processor.MimicAnnotationProcessor;

/**
 * A Mimic is a generated wrapper with type-safe accessors using Java reflection
 * to get and set non-public fields.
 * <p>
 * This plugin automatically configures the Mimic annotation processor to create
 * Mimics for all annotated and configured classes (see
 * {@link MimicPluginExtension}). It also adds the Mimic annotation as a compile
 * dependency.
 * <p>
 * Use the DSL extension to configure the annotation processor:
 *
 * <pre>
 * mimic {
 *   packageName = 'my.default.target.package'
 *   classes = [
 *         'my.third.party.class.Name',
 *         'my.own.custom.class.Name'
 *   ]
 * }
 * </pre>
 *
 * The generated classes are written in Java.
 */
public abstract class MimicPlugin implements Plugin<Project> {

	private static final String GROUP_ID = "dev.bodewig.mimic";

	private static final String ARTIFACT_ID_ANNOTATION = "mimic-annotation";

	private static final String ARTIFACT_ID_ANNOTATION_PROCESSOR = "mimic-annotation-processor";

	private static final String MIMIC_VERSION_RESOURCE = "/mimic.version";

	/**
	 * The name of the plugin DSL extension
	 */
	public static final String EXTENSION_NAME = "mimic";

	/**
	 * The name of the registered task
	 */
	public static final String TASK_NAME = "spawnMimics";

	/**
	 * Default constructor
	 */
	public MimicPlugin() {
	}

	@Override
	public void apply(Project project) {
		project.getPlugins().apply(JavaPlugin.class);
		MimicPluginExtension extension = project.getExtensions().create(EXTENSION_NAME, MimicPluginExtension.class);

		SourceSetContainer sourceSets = project.getExtensions().getByType(SourceSetContainer.class);
		SourceSet main = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME);
		URL versionResource = MimicPlugin.class.getResource(MIMIC_VERSION_RESOURCE);
		String mimicVersion = project.getResources().getText().fromUri(versionResource).asString();

		// add mimic-annotation as compile dependency
		Dependency annotationDependency = project.getDependencies()
				.create(GROUP_ID + ":" + ARTIFACT_ID_ANNOTATION + ":" + mimicVersion);
		Configuration annotationConfig = project.getConfigurations().create(ARTIFACT_ID_ANNOTATION);
		annotationConfig.defaultDependencies(dependencySet -> dependencySet.add(annotationDependency));
		Configuration mainCompileOnlyConfig = project.getConfigurations()
				.getByName(main.getCompileOnlyConfigurationName());
		mainCompileOnlyConfig.extendsFrom(annotationConfig);

		// add mimic-annotation-processor as annotation processor dependency
		Configuration processorConfig = project.getConfigurations().create(ARTIFACT_ID_ANNOTATION_PROCESSOR);
		Dependency processorDependency = project.getDependencies()
				.create(GROUP_ID + ":" + ARTIFACT_ID_ANNOTATION_PROCESSOR + ":" + mimicVersion);
		processorConfig.defaultDependencies(dependencySet -> dependencySet.add(processorDependency));
		Configuration mainProcessorConfig = project.getConfigurations()
				.getByName(main.getAnnotationProcessorConfigurationName());
		mainProcessorConfig.extendsFrom(processorConfig);

		TaskProvider<Task> compileJava = project.getTasks().named(JavaPlugin.COMPILE_JAVA_TASK_NAME);
		compileJava.configure(t -> {
			JavaCompile task = (JavaCompile) t;
			// add the extension values as annotation processor arguments
			List<String> args = new ArrayList<>();
			if (extension.getPackageName().isPresent()) {
				args.add("-A" + MimicAnnotationProcessor.OPTION_PACKAGE_NAME + "=" + extension.getPackageName().get());
			}
			if (extension.getClasses().isPresent()) {
				String classList = String.join(",", extension.getClasses().get());
				args.add("-A" + MimicAnnotationProcessor.OPTION_MIMIC_CLASSES + "=" + classList);
			}
			task.getOptions().getCompilerArgs().addAll(args);
		});
	}
}
