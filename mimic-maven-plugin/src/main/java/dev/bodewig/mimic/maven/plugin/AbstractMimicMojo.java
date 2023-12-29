package dev.bodewig.mimic.maven.plugin;

import java.io.File;
import java.util.List;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

public abstract class AbstractMimicMojo extends AbstractMojo {

	@Parameter(defaultValue = "${project}", readonly = true)
	protected MavenProject mavenProject;

	@Parameter(defaultValue = "${session}", readonly = true)
	protected MavenSession mavenSession;

	@Component
	protected BuildPluginManager pluginManager;

	@Parameter(required = true)
	protected List<String> classes;

	@Parameter(defaultValue = "${project.build.directory}/generated-sources/mimic")
	protected File outputDirectory;

	@Parameter(required = true)
	protected String packageName;
}
