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

public class Mimic {

	private Mimic() {
	}

	public static void createMimic(Class<?> clazz, String packageName, File outputDirectory) throws IOException {
		TypeSpec type = createProxy(clazz);
		JavaFile javaFile = JavaFile.builder(packageName, type).build();
		javaFile.writeTo(outputDirectory);
	}

	public static String pascalCase(String s) {
		return s.substring(0, 1).toUpperCase() + s.substring(1);
	}

	public static TypeSpec createProxy(Class<?> clazz) {
		String typeName = clazz.getSimpleName() + "Mimic";
		TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(typeName).addModifiers(Modifier.PUBLIC);

		typeBuilder.addField(clazz, "instance", Modifier.PRIVATE, Modifier.FINAL);
		MethodSpec constructor = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC)
				.addParameter(clazz, "instance").addStatement("this.instance = instance").build();
		typeBuilder.addMethod(constructor);

		for (Field f : getFields(clazz)) {
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
			typeBuilder.addMethod(getterBuilder.build());
		}

		return typeBuilder.build();
	}

	public static List<Field> getFields(Class<?> clazz) {
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
