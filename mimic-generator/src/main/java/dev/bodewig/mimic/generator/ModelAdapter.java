package dev.bodewig.mimic.generator;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;

import com.squareup.javapoet.TypeName;

public interface ModelAdapter<T> {

	static ModelAdapter<Class<?>> fromClass(Class<?> clazz) {
		return new ClassModelAdapter(clazz);
	}

	static ModelAdapter<Element> fromType(TypeElement type) {
		return new TypeModelAdapter(type);
	}

	String getSimpleName();

	TypeName getTypeName();

	Set<FieldAdapter<T>> getFields();

	class ClassModelAdapter implements ModelAdapter<Class<?>> {

		protected final Class<?> clazz;

		public ClassModelAdapter(Class<?> clazz) {
			this.clazz = clazz;
		}

		@Override
		public String getSimpleName() {
			return clazz.getSimpleName();
		}

		@Override
		public TypeName getTypeName() {
			return TypeName.get(clazz);
		}

		@Override
		public Set<FieldAdapter<Class<?>>> getFields() {
			return getFields(clazz).stream().map(FieldAdapter::from).collect(Collectors.toSet());
		}

		private static Set<Field> getFields(Class<?> clazz) {
			Set<Field> fields = new HashSet<>();
			Field[] ownFields = clazz.getDeclaredFields();
			AccessibleObject.setAccessible(ownFields, true);
			fields.addAll(List.of(ownFields));
			if (clazz.getSuperclass() != null) {
				fields.addAll(getFields(clazz.getSuperclass()));
			}
			return fields;
		}

		@Override
		public String toString() {
			return "ClassModelAdapter(" + clazz.getName() + ", " + getFields() + ")";
		}
	}

	class TypeModelAdapter implements ModelAdapter<Element> {

		protected final TypeElement type;

		public TypeModelAdapter(TypeElement type) {
			this.type = type;
		}

		@Override
		public String getSimpleName() {
			return type.getSimpleName().toString();
		}

		@Override
		public TypeName getTypeName() {
			return TypeName.get(type.asType());
		}

		@Override
		public Set<FieldAdapter<Element>> getFields() {
			return getFields(type).stream().map(FieldAdapter::from).collect(Collectors.toSet());
		}

		private Set<VariableElement> getFields(TypeElement t) {
			Set<VariableElement> fields = new HashSet<>();
			Set<VariableElement> ownFields = type.getEnclosedElements().stream()
					.filter(e -> e.getKind().equals(ElementKind.FIELD)).map(e -> (VariableElement) e)
					.collect(Collectors.toSet());
			fields.addAll(ownFields);
			if (!type.getSuperclass().getKind().equals(TypeKind.NONE)) {
				DeclaredType parentType = (DeclaredType) type.getSuperclass();
				TypeElement parentElement = (TypeElement) parentType.asElement();
				if (!parentElement.getQualifiedName().contentEquals(Object.class.getName())) {
					fields.addAll(getFields(parentElement));
				}
			}
			return fields;
		}

		@Override
		public String toString() {
			return "TypeModelAdapter(" + type.getQualifiedName() + ", " + getFields() + ")";
		}
	}
}
