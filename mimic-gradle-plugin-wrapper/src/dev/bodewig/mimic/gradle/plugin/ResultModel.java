package dev.bodewig.mimic.gradle.plugin;

import java.util.ArrayList;
import java.util.List;
import org.gradle.tooling.provider.model.ToolingModelBuilder;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;

interface ResultModel {

	String test();

	class Builder implements ToolingModelBuilder {
		@Override
		public boolean canBuild(String modelName) {
			return modelName.equals(ResultModel.class.getName());
		}

		@Override
		public Object buildAll(String modelName, Project project) {
			List<String> pluginClassNames = new ArrayList<String>();

			for (Plugin plugin : project.getPlugins()) {
				pluginClassNames.add(plugin.getClass().getName());
			}

			for (Task javaTask : project.getTasksByName("compileJava", false)) {
				System.out.println(javaTask.getOutputs());
			}
			
			String test = "test";
			
			return new DefaultModel(pluginClassNames, test);
		}
	}
}
