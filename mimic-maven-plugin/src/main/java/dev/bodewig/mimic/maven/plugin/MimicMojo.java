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

import dev.bodewig.mimic.core.Mimic;

@Mojo(name = "mimic", defaultPhase = LifecyclePhase.GENERATE_SOURCES, requiresDependencyResolution = ResolutionScope.RUNTIME)
@Execute(phase = LifecyclePhase.COMPILE)
public class MimicMojo extends AbstractMimicMojo {

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		outputDirectory.mkdirs();
		try (URLClassLoader cl = createClassLoader()) {
			for (String className : classes) {
				Class<?> clazz = loadClass(cl, className);
				try {
					Mimic.createMimic(clazz, packageName, outputDirectory);
				} catch (IOException e) {
					throw new MojoExecutionException("Could not write file to " + outputDirectory, e);
				}
			}
		} catch (IOException e1) {
			throw new MojoExecutionException("Could not close classloader", e1);
		}
		mavenProject.addCompileSourceRoot(outputDirectory.getPath());
	}

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

	protected Class<?> loadClass(ClassLoader cl, String className) throws MojoExecutionException {
		try {
			return cl.loadClass(className);
		} catch (ClassNotFoundException e) {
			throw new MojoExecutionException("Could not load class " + className, e);
		}
	}
}
