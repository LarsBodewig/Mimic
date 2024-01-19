package dev.bodewig.mimic.maven.plugin;

import java.io.File;
import java.util.List;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * The model for the {@link MimicMojo}
 */
public abstract class MimicMojoModel extends AbstractMojo {

	/**
	 * Default constructor
	 */
	public MimicMojoModel() {
	}

	/**
	 * The maven project
	 */
	@Parameter(defaultValue = "${project}", readonly = true)
	protected MavenProject mavenProject;

	/**
	 * The maven session
	 */
	@Parameter(defaultValue = "${session}", readonly = true)
	protected MavenSession mavenSession;

	/**
	 * The plugin manager
	 */
	@Component
	protected BuildPluginManager pluginManager;

	/**
	 * List of fully qualified class names to create Mimics for. The classes must be
	 * loadable from the compile classpath.
	 */
	@Parameter(required = true)
	protected List<String> classes;

	/**
	 * Relative project path where the generated Mimics are written to. Will be
	 * added as a SourceSetDirectory to the main SourceSet.
	 */
	@Parameter(defaultValue = "${project.build.directory}/generated-sources/mimic")
	protected File outputDirectory;

	/**
	 * The target package of the generated Mimics
	 */
	@Parameter(required = true)
	protected String packageName;
}
