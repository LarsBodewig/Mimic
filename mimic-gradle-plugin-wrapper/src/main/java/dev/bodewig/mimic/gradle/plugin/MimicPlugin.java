package dev.bodewig.mimic.gradle.plugin;

import java.util.Arrays;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.compile.JavaCompile;

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
		project.getPlugins().apply(JavaPlugin.class);
		MimicPluginExtension extension = project.getExtensions().create("mimic", MimicPluginExtension.class);

		TaskProvider<JavaCompile> mimic = project.getTasks().register("generateMimics",JavaCompile.class);
        mimic.configure(task -> {
			task.setGroup("Mimic");
			task.setDescription("Create Mimics for the configured classes");
            task.setSource(variant.getSourceFolders(SourceKind.JAVA).get(0));
            task.getOptions().setAnnotationProcessorPath(variant.getAnnotationProcessorConfiguration());
            task.getOptions().getCompilerArgs().addAll(
                    Arrays.asList(
                            "-proc:only",
                            "-implicit:none",
                            "-processor", processor
                            // ... extension.forEach(c -> "-Aclass=" + c)
                    )
            );
            task.getOptions().setFork(true);
        });

		project.getTasksByName("compileJava", false).forEach(t -> t.dependsOn(mimic));
	}
}
