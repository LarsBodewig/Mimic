package dev.bodewig.mimic.generator;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;

import javax.annotation.processing.Generated;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

/**
 * Use {@link #createMimicFromClass(Class, String, File)} to create a Mimic for
 * a given class in a configured package in the supplied output directory.
 */
public class MimicGenerator {

	/**
	 * Default constructor
	 */
	private MimicGenerator() {
	}

	/**
	 * Creates a Mimic for the given class in a the given package in the given
	 * output directory.
	 * <p>
	 * A Mimic is a generated wrapper with type-safe accessors using Java reflection
	 * to get and set non-public fields.
	 *
	 * @param clazz           The class to create a Mimic for
	 * @param packageName     The target package for the generated Mimic
	 * @param outputDirectory The output directory for the java class
	 * @throws IOException If writing the java class file to the output directory
	 *                     fails
	 */
	public static void createMimicFromClass(Class<?> clazz, String packageName, File outputDirectory)
			throws IOException {
		ModelAdapter<Class<?>> model = ModelAdapter.fromClass(clazz);
		TypeSpec spec = createMimicType(model);
		JavaFile javaFile = JavaFile.builder(packageName, spec).build();
		javaFile.writeTo(outputDirectory);
	}

	/**
	 * 
	 * @param type
	 * @param packageName
	 * @param outputFile
	 * @throws IOException
	 */
	public static void createMimicFromType(TypeElement type, String packageName, Writer outputFile) throws IOException {
		ModelAdapter<Element> model = ModelAdapter.fromType(type);
		TypeSpec spec = createMimicType(model);
		JavaFile javaFile = JavaFile.builder(packageName, spec).build();
		javaFile.writeTo(outputFile);
	}

	/**
	 * 
	 * @param pkg
	 * @param simpleName
	 * @return
	 */
	public static String buildQualifiedMimicName(String pkg, String simpleName) {
		return pkg + "." + buildSimpleMimicName(simpleName);
	}

	/**
	 * 
	 * @param simpleName
	 * @return
	 */
	public static String buildSimpleMimicName(String simpleName) {
		return simpleName + "Mimic";
	}

	/**
	 * Creates a pascal case string by converting the first character to upper case.
	 *
	 * @param s The string to convert
	 * @return The pascal case string
	 */
	private static String pascalCase(String s) {
		return s.substring(0, 1).toUpperCase() + s.substring(1);
	}

	/**
	 * Creates a Mimic for the given class.
	 * <p>
	 * The type contains an instance field, a constructor with a parameter to set
	 * the instance and getters and setters for each field from the class.
	 *
	 * @param clazz The class to create a Mimic for
	 * @return The {@code TypeSpec} for the Mimic
	 */
	private static TypeSpec createMimicType(ModelAdapter<?> model) {
		String typeName = buildSimpleMimicName(model.getSimpleName());
		TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(typeName).addModifiers(Modifier.PUBLIC)
				.addAnnotation(AnnotationSpec.builder(Generated.class)
						.addMember("value", "$S", MimicGenerator.class.getName()).build());

		typeBuilder.addField(model.getTypeName(), "instance", Modifier.PRIVATE, Modifier.FINAL);
		MethodSpec constructor = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC)
				.addParameter(model.getTypeName(), "instance").addStatement("this.instance = instance").build();
		typeBuilder.addMethod(constructor);

		for (FieldAdapter<?> f : model.getFields()) {
			MethodSpec getter = createGetter(f);
			MethodSpec setter = createSetter(f);
			typeBuilder.addMethod(getter);
			typeBuilder.addMethod(setter);
		}

		return typeBuilder.build();
	}

	/**
	 * Creates a getter for the given field. Uses reflection if the field is
	 * non-public.
	 *
	 * @param f The field to create a getter for
	 * @return The {@code MethodSpec} for the getter
	 */
	private static MethodSpec createGetter(FieldAdapter<?> f) {
		String getterName = "get" + pascalCase(f.getName());
		MethodSpec.Builder getterBuilder = MethodSpec.methodBuilder(getterName).addModifiers(Modifier.PUBLIC)
				.returns(f.getType());
		if (f.isPublic()) {
			getterBuilder.addStatement("return instance.$L", f.getName());
		} else {
			getterBuilder.beginControlFlow("try")
					.addStatement("$T f = $T.class.getDeclaredField($S)", Field.class, f.getDeclaringClass(),
							f.getName())
					.addStatement("f.setAccessible(true)").addStatement("return ($T) f.get(instance)", f.getType())
					.nextControlFlow("catch ($T | $T e)", NoSuchFieldException.class, IllegalAccessException.class)
					.addStatement("throw new $T(e)", RuntimeException.class).endControlFlow();
		}
		return getterBuilder.build();
	}

	/**
	 * Creates a setter for the given field. Uses reflection if the field is
	 * non-public.
	 *
	 * @param f The field to create a setter for
	 * @return The {@code MethodSpec} for the setter
	 */
	private static MethodSpec createSetter(FieldAdapter<?> f) {
		String setterName = "set" + pascalCase(f.getName());
		MethodSpec.Builder setterBuilder = MethodSpec.methodBuilder(setterName).addModifiers(Modifier.PUBLIC)
				.addParameter(f.getType(), "value");
		if (f.isPublic()) {
			setterBuilder.addStatement("instance.$L = value", f.getName());
		} else {
			setterBuilder.beginControlFlow("try")
					.addStatement("$T f = $T.class.getDeclaredField($S)", Field.class, f.getDeclaringClass(),
							f.getName())
					.addStatement("f.setAccessible(true)").addStatement("f.set(instance, value)")
					.nextControlFlow("catch ($T | $T e)", NoSuchFieldException.class, IllegalAccessException.class)
					.addStatement("throw new $T(e)", RuntimeException.class).endControlFlow();
		}
		return setterBuilder.build();
	}
}
