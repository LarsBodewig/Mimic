package dev.bodewig.mimic.gradle.plugin;

import java.io.File;

import org.gradle.api.provider.Property;

public interface MimicPluginExtension {

	Property<String> getPackageName();

	Property<String[]> getClasses();

	Property<File> getOutputDirectory();
}
