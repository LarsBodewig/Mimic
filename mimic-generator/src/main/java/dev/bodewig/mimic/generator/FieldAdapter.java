package dev.bodewig.mimic.generator;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;

import com.squareup.javapoet.TypeName;

public interface FieldAdapter<T> {

	String getName();

	TypeName getType();

	boolean isPublic();

	T getDeclaringClass();

	static FieldAdapter<Class<?>> from(Field field) {
		return new FieldFieldAdapter(field);
	}

	static FieldAdapter<Element> from(VariableElement variable) {
		return new VariableFieldAdapter(variable);
	}

	class FieldFieldAdapter implements FieldAdapter<Class<?>> {

		protected final Field field;

		public FieldFieldAdapter(Field field) {
			this.field = field;
		}

		@Override
		public String getName() {
			return field.getName();
		}

		@Override
		public TypeName getType() {
			return TypeName.get(field.getType());
		}

		@Override
		public boolean isPublic() {
			return Modifier.isPublic(field.getModifiers());
		}

		@Override
		public Class<?> getDeclaringClass() {
			return field.getDeclaringClass();
		}
		
		@Override
		public String toString() {
			return "FieldFieldAdapter(" + field.getName() + ")";
		}
	}

	class VariableFieldAdapter implements FieldAdapter<Element> {

		protected final VariableElement variable;

		public VariableFieldAdapter(VariableElement variable) {
			this.variable = variable;
		}

		@Override
		public String getName() {
			return variable.getSimpleName().toString();
		}

		@Override
		public TypeName getType() {
			return TypeName.get(variable.asType());
		}

		@Override
		public boolean isPublic() {
			return variable.getModifiers().contains(javax.lang.model.element.Modifier.PUBLIC);
		}

		@Override
		public Element getDeclaringClass() {
			return variable.getEnclosingElement();
		}
		
		@Override
		public String toString() {
			return "VariableFieldAdapter(" + variable.getSimpleName() + ")";
		}
	}
}
