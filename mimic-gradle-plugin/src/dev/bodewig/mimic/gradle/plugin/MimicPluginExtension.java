package dev.bodewig.mimic.gradle.plugin;

/**
 * DSL extension to configure the mimic plugin
 *
 * @see #classes
 * @see #outputDirectory
 * @see #packageName
 */
public class MimicPluginExtension {

	/**
	 * Default constructor
	 */
	public MimicPluginExtension() {
	}

	/**
	 * The target package of the generated mimics
	 */
	public String packageName;

	/**
	 * List of fully qualified class names to create mimics for. The classes must be
	 * loadable from the compile classpath of the main
	 * {@link org.gradle.api.tasks.SourceSet}.
	 * <p>
	 * Due to limitations in gradle this excludes classes that are part of the
	 * module the mimic plugin is applied to. If you need mimics for your own
	 * classes, create a separate project module that depends on your other module
	 * and applies the plugin.
	 */
	public String[] classes;

	/**
	 * Relative project path where the generated mimics are written to. Will be
	 * added as a SourceSetDirectory to the main SourceSet.
	 */
	public String outputDirectory;
}
