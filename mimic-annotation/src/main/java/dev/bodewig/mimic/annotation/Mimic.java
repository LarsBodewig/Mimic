package dev.bodewig.mimic.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to create Mimics with the Mimic Annotation Processor
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface Mimic {

	/**
	 * The default {@code packageName}. Can be used to explicitly set the default
	 * target package name.
	 */
	public static final String PACKAGE_FROM_COMPILER_ARG = "PACKAGE_FROM_COMPILER_ARG";

	/**
	 * Define the packageName for the generated Mimic, overrides the value passed as
	 * {@code compilerArg}
	 *
	 * @return The packageName
	 */
	String packageName() default PACKAGE_FROM_COMPILER_ARG;
}
