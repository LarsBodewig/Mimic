package dev.bodewig.mimic.gradle.plugin;

/**
 * DSL extension to configure the Mimic plugin
 *
 * @see #classes
 * @see #packageName
 */
public class MimicPluginExtension {

	/**
	 * Default constructor
	 */
	public MimicPluginExtension() {
	}

	/**
	 * The default target package of the generated Mimics. Can be overwritten if the
	 * Mimic annotation is used.
	 */
	public String packageName;

	/**
	 * List of fully qualified class names to create Mimics for. If you need Mimics
	 * for your own classes, the Mimic annotation can be used instead.
	 */
	public String[] classes;
}
