package dev.bodewig.mimic.core;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

/**
 * Use {@link #createMimic(Class, String, File)} to create a mimic for a given
 * class in a configured package in the supplied output directory.
 */
public class MimicCreator {

	/**
	 * Default constructor
	 */
	private MimicCreator() {
	}

	/**
	 * Creates a mimic for the given class in a the given package in the given
	 * output directory.
	 * <p>
	 * A mimic is a generated wrapper with type-safe accessors using Java reflection
	 * to get and set non-public fields.
	 * 
	 * @param clazz           The class to create a mimic for
	 * @param packageName     The target package for the generated mimic
	 * @param outputDirectory The output directory for the java class
	 * @throws IOException If writing the java class file to the output directory
	 *                     fails
	 */
	public static void createMimic(Class<?> clazz, String packageName, File outputDirectory) throws IOException {
		TypeSpec type = createMimicType(clazz);
		JavaFile javaFile = JavaFile.builder(packageName, type).build();
		javaFile.writeTo(outputDirectory);
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
	 * Creates a mimic for the given class.
	 * <p>
	 * The type contains an instance field, a constructor with a parameter to set
	 * the instance and getters and setters for each field from the class.
	 * 
	 * @param clazz The class to create a mimic for
	 * @return The {@code TypeSpec} for the mimic
	 */
	private static TypeSpec createMimicType(Class<?> clazz) {
		String typeName = clazz.getSimpleName() + "Mimic";
		TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(typeName).addModifiers(Modifier.PUBLIC);

		typeBuilder.addField(clazz, "instance", Modifier.PRIVATE, Modifier.FINAL);
		MethodSpec constructor = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC)
				.addParameter(clazz, "instance").addStatement("this.instance = instance").build();
		typeBuilder.addMethod(constructor);

		for (Field f : getFields(clazz)) {
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
	private static MethodSpec createGetter(Field f) {
		String getterName = "get" + pascalCase(f.getName());
		MethodSpec.Builder getterBuilder = MethodSpec.methodBuilder(getterName).addModifiers(Modifier.PUBLIC)
				.returns(f.getType());
		if (java.lang.reflect.Modifier.isPublic(f.getModifiers())) {
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
	private static MethodSpec createSetter(Field f) {
		String setterName = "set" + pascalCase(f.getName());
		MethodSpec.Builder setterBuilder = MethodSpec.methodBuilder(setterName).addModifiers(Modifier.PUBLIC)
				.addParameter(f.getType(), "value");
		if (java.lang.reflect.Modifier.isPublic(f.getModifiers())) {
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

	/**
	 * Gets a list of all declared and inherited fields with any visibility
	 * modifier.
	 * 
	 * @param clazz The class to get fields from
	 * @return A list of all declared and inherited fields
	 */
	private static List<Field> getFields(Class<?> clazz) {
		Field[] ownFields = clazz.getDeclaredFields();
		AccessibleObject.setAccessible(ownFields, true);
		List<Field> fields = new ArrayList<>();
		fields.addAll(List.of(ownFields));
		if (clazz.getSuperclass() != null) {
			fields.addAll(getFields(clazz.getSuperclass()));
		}
		return fields;
	}
}
