package dev.bodewig.mimic.maven.plugin;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

import dev.bodewig.mimic.core.MimicCreator;

/**
 * A mimic is a generated wrapper with type-safe accessors using Java reflection
 * to get and set non-public fields.
 * <p>
 * This plugin creates mimics for a configured list of classes, in a configured
 * package, in a configured output directory.
 */
@Mojo(name = "mimic", defaultPhase = LifecyclePhase.GENERATE_SOURCES, requiresDependencyResolution = ResolutionScope.RUNTIME)
@Execute(phase = LifecyclePhase.COMPILE)
public class MimicMojo extends MimicMojoModel {

	/**
	 * Default constructor
	 */
	public MimicMojo() {
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		outputDirectory.mkdirs();
		try (URLClassLoader cl = createClassLoader()) {
			for (String className : classes) {
				Class<?> clazz = loadClass(cl, className);
				try {
					MimicCreator.createMimic(clazz, packageName, outputDirectory);
				} catch (IOException e) {
					throw new MojoExecutionException("Could not write file to " + outputDirectory, e);
				}
			}
		} catch (IOException e1) {
			throw new MojoExecutionException("Could not close classloader", e1);
		}
		mavenProject.addCompileSourceRoot(outputDirectory.getPath());
	}

	/**
	 * Creates a class loader configured with all classes from the compile classpath
	 * and build output directory
	 * 
	 * @return The configured class loader
	 * @throws MojoExecutionException See {@link java.net.URI#toURL() URI.toURL()}
	 *                                and
	 *                                {@link org.apache.maven.project.MavenProject#getCompileClasspathElements()
	 *                                MavenProject.getCompileClasspathElements()}
	 */
	@SuppressWarnings("unchecked")
	protected URLClassLoader createClassLoader() throws MojoExecutionException {
		URL[] urls = null;
		try {
			List<String> classpathElements = mavenProject.getCompileClasspathElements();
			classpathElements.add(mavenProject.getBuild().getOutputDirectory());
			urls = new URL[classpathElements.size()];
			for (int i = 0; i < classpathElements.size(); ++i) {
				urls[i] = new File(classpathElements.get(i)).toURI().toURL();
			}
		} catch (MalformedURLException | DependencyResolutionRequiredException e) {
			throw new MojoExecutionException("Could not create classloader", e);
		}
		return new URLClassLoader(urls, this.getClass().getClassLoader());
	}

	/**
	 * Load the class with the given name using the given class loader
	 * 
	 * @param cl        The class loader to use
	 * @param className The name of the class
	 * @return The loaded class
	 * @throws MojoExecutionException See {@link ClassLoader#loadClass(String)}
	 */
	protected Class<?> loadClass(ClassLoader cl, String className) throws MojoExecutionException {
		try {
			return cl.loadClass(className);
		} catch (ClassNotFoundException e) {
			throw new MojoExecutionException("Could not load class " + className, e);
		}
	}
}
