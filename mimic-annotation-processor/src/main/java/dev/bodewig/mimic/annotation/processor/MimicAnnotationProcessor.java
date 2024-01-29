package dev.bodewig.mimic.annotation.processor;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.JavaFileObject;

import dev.bodewig.mimic.annotation.Mimic;
import dev.bodewig.mimic.generator.MimicGenerator;

/**
 * Generator to create Mimics for annotated and configured classes via
 * compilerArgs
 */
@SupportedOptions({ MimicAnnotationProcessor.OPTION_PACKAGE_NAME, MimicAnnotationProcessor.OPTION_MIMIC_CLASSES })
@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_19)
public class MimicAnnotationProcessor extends AbstractProcessor {

	/**
	 * The option name to pass a default package name for the created Mimics
	 */
	public static final String OPTION_PACKAGE_NAME = "mimic.packageName";

	/**
	 * The option name to pass a list of class names to create Mimics for
	 */
	public static final String OPTION_MIMIC_CLASSES = "mimic.classes";

	/**
	 * Already processed classes.
	 * <p>
	 * This is necessary since Gradle calls the
	 * {@link #process(Set, RoundEnvironment)} method twice.
	 */
	protected Set<TypeElement> processed;

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		processed = new HashSet<>();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		String classList = processingEnv.getOptions().get(OPTION_MIMIC_CLASSES);
		Set<String> classes = null;
		if (classList == null || classList.isBlank()) {
			classes = Collections.emptySet();
		} else {
			String[] classArray = classList.split(",");
			classes = Set.of(classArray);
		}
		String packageName = processingEnv.getOptions().get(OPTION_PACKAGE_NAME);

		Elements util = processingEnv.getElementUtils();

		Set<TypeElement> annotated = roundEnv.getElementsAnnotatedWith(Mimic.class).stream().map(c -> (TypeElement) c)
				.collect(Collectors.toSet());
		Set<TypeElement> configured = classes.stream().map(className -> {
			Set<? extends Element> elements = util.getAllTypeElements(className);
			if (elements.isEmpty()) {
				throw new RuntimeException("Configured class " + className + " not found!");
			} else if (elements.size() > 1) {
				throw new RuntimeException("Configured class " + className + " found in multiple modules!");
			} else {
				return elements.iterator().next();
			}
		}).map(c -> (TypeElement) c).collect(Collectors.toSet());

		Set<TypeElement> combined = new HashSet<>(annotated);
		combined.addAll(configured);
		combined.removeAll(processed);
		processed.addAll(combined);

		String name = null;
		try {
			Filer filer = processingEnv.getFiler();
			for (TypeElement type : combined) {
				String pkg = packageName;
				Mimic annotation = type.getAnnotation(Mimic.class);
				if (annotation != null) {
					if (!annotation.packageName().equals(Mimic.PACKAGE_FROM_COMPILER_ARG)) {
						pkg = annotation.packageName();
					}
				}
				if (pkg == null) {
					String simpleName = MimicGenerator.buildSimpleMimicName(type.getSimpleName().toString());
					throw new RuntimeException("Target package for Mimic " + simpleName + " is missing! "
							+ "Define a package via compilerArgs or as annotation parameter.");
				}
				name = MimicGenerator.buildQualifiedMimicName(pkg, type.getSimpleName().toString());
				JavaFileObject file = filer.createSourceFile(name);
				try (Writer writer = file.openWriter()) {
					MimicGenerator.createMimicFromType(type, pkg, writer);
				}
			}
		} catch (IOException e) {
			throw new RuntimeException("Error creating Mimic " + name, e);
		}

		return false;
	}
}
