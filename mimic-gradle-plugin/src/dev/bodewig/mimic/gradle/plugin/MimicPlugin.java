package dev.bodewig.mimic.gradle.plugin;

import java.util.Arrays;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public abstract class MimicPlugin implements Plugin<Project> {

	public void apply(Project project) {
		MimicPluginExtension extension = project.getExtensions().create("mimic", MimicPluginExtension.class);

		project.getTasks().register("mimic", task -> {
			task.doLast(s -> {
				System.out.println("make output dir: " + project.absoluteProjectPath(extension.outputDirectory));
				System.out.println("load classes: " + Arrays.toString(extension.classes));
				System.out.println("create mimics: " + extension.packageName);
				System.out.println("add source classes?");
			});
		});
	}
}
