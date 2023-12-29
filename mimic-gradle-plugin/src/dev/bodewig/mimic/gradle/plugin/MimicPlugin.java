package dev.bodewig.mimic.gradle.plugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public abstract class MimicPlugin implements Plugin<Project> {

	public void apply(Project project) {
		project.getTasks().register("mimic", task -> {
			// task.getExtensions().add(null, null);
			task.doLast(s -> {
				System.out.println("make output dir");
				System.out.println("load classes");
				System.out.println("create mimics");
				System.out.println("add source classes?");
			});
		});
	}
}
