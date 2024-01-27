package dev.bodewig.mimic.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to create Mimics with the mimic-annotation-processor
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface Mimic {

	public static final String PACKAGE_FROM_COMPILER_ARG = "PACKAGE_FROM_COMPILER_ARG";

	/**
	 * Define the packageName for the generated Mimic, overrides the value passed as
	 * compilerArg
	 * 
	 * @return the packageName
	 */
	String packageName() default PACKAGE_FROM_COMPILER_ARG;
}
