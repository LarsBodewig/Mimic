package dev.bodewig.mimic.gradle.plugin;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskProvider;

import dev.bodewig.mimic.core.Mimic;

public abstract class MimicPlugin implements Plugin<Project> {

	@Override
	public void apply(Project project) {
		MimicPluginExtension extension = project.getExtensions().create("mimic", MimicPluginExtension.class);

		TaskProvider<Task> mimic = project.getTasks().register("mimic", task -> {
			task.doLast(s -> {
				s.setGroup("Mimic");
				s.setDescription("Create Mimics for the configured classes");

				// make outputDirectory
				File outputDir = new File(project.getProjectDir(), extension.outputDirectory);
				outputDir.mkdirs();

				SourceSetContainer sourceSets = project.getExtensions().getByType(SourceSetContainer.class);
				SourceSet main = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME);

				// create mimics
				try (URLClassLoader cl = createClassLoader(main)) {
					for (String className : extension.classes) {
						Class<?> clazz = cl.loadClass(className);
						Mimic.createMimic(clazz, extension.packageName, outputDir);
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

	protected URLClassLoader createClassLoader(SourceSet main) throws MalformedURLException {
		List<File> classFiles = new ArrayList<>(main.getCompileClasspath().getFiles());
		URL[] urls = new URL[classFiles.size()];
		for (int i = 0; i < classFiles.size(); i++) {
			urls[i] = classFiles.get(i).toURI().toURL();
		}
		return new URLClassLoader(urls, this.getClass().getClassLoader());
	}
}
