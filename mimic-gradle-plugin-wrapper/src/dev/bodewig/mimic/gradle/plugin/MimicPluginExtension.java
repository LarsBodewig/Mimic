package dev.bodewig.mimic.gradle.plugin;

/**
 * DSL extension to configure the Mimic plugin
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
	 * The target package of the generated Mimics
	 */
	public String packageName;

	/**
	 * List of fully qualified class names to create Mimics for. The classes must be
	 * loadable from the compile classpath of the main
	 * {@link org.gradle.api.tasks.SourceSet}.
	 * <p>
	 * Due to limitations in Gradle this excludes classes that are part of the
	 * module the Mimic plugin is applied to. If you need Mimics for your own
	 * classes, create a separate project module that depends on your other module
	 * and applies the plugin.
	 */
	public String[] classes;

	/**
	 * Relative project path where the generated Mimics are written to. Will be
	 * added as a SourceSetDirectory to the main SourceSet.
	 */
	public String outputDirectory;
}
