package dev.bodewig.mimic.gradle.plugin;

import java.io.Serializable;
import java.util.List;

class DefaultModel implements ResultModel, Serializable {

	private final List<String> pluginClassNames;

	private final String test;

	public DefaultModel(List<String> pluginClassNames, String test) {
		this.pluginClassNames = pluginClassNames;
		this.test = test;
	}

	public boolean hasPlugin(Class type) {
		return pluginClassNames.contains(type.getName());
	}

	@Override
	public String test() {
		return test;
	}
}