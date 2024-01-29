package dev.bodewig.mimic.gradle.plugin;

import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;

/**
 * DSL extension to configure the Mimic plugin
 *
 * @see #getClasses
 * @see #getPackageName
 */
public abstract class MimicPluginExtension {

	/**
	 * Default constructor
	 */
	public MimicPluginExtension() {
	}
	
	/**
	 * The default target package of the generated Mimics. Can be overwritten if the
	 * Mimic annotation is used.
	 * 
	 * @return the default target package name
	 */
	public abstract Property<String> getPackageName();

	/**
	 * List of fully qualified class names to create Mimics for. If you need Mimics
	 * for your own classes, the Mimic annotation can be used instead.
	 * 
	 * @return the classes to create Mimics for
	 */
	public abstract ListProperty<String> getClasses();
}
