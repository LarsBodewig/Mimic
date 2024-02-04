package dev.bodewig.mimic.generator;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;

import com.squareup.javapoet.TypeName;

/**
 * Adapter to delegate operations to a {@link FieldFieldAdapter} or
 * {@link VariableFieldAdapter}
 *
 * @param <T> The return type of {@link #getDeclaringClass()}
 */
public interface FieldAdapter<T> {

	/**
	 * Returns the field's name
	 *
	 * @return The field's name
	 */
	String getName();

	/**
	 * Return the field's {@link TypeName}
	 *
	 * @return The field's {@link TypeName}
	 */
	TypeName getType();

	/**
	 * Returns if the field is public
	 *
	 * @return If the field is public
	 */
	boolean isPublic();

	/**
	 * Returns if the field is final
	 *
	 * @return If the field is final
	 */
	boolean isFinal();

	/**
	 * Returns if the field is a constant
	 *
	 * @return If the field is a constant
	 */
	boolean isConstant();

	/**
	 * Returns the class declaring the field
	 *
	 * @return The declaring class
	 */
	T getDeclaringClass();

	/**
	 * Static initializer for a {@link Field} instance
	 *
	 * @param field The {@code Field} instance
	 * @return A {@code FieldAdapter} with the {@code field} instance
	 */
	static FieldAdapter<Class<?>> from(Field field) {
		return new FieldFieldAdapter(field);
	}

	/**
	 * Static initializer for a {@link VariableElement} instance
	 *
	 * @param variable The {@code VariableElement} instance
	 * @return A {@code FieldAdapter} with the {@code variable} instance
	 */
	static FieldAdapter<Element> from(VariableElement variable) {
		return new VariableFieldAdapter(variable);
	}

	/**
	 * {@link FieldAdapter} for a {@link Field} instance
	 */
	class FieldFieldAdapter implements FieldAdapter<Class<?>> {

		/**
		 * The {@code Field} instance
		 */
		protected final Field field;

		/**
		 * Constructor with a {@code Field} instance
		 *
		 * @param field The {@code Field} instance
		 */
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
		public boolean isFinal() {
			return Modifier.isFinal(field.getModifiers());
		}

		@Override
		public boolean isConstant() {
			return Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers());
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

	/**
	 * {@link FieldAdapter} for a {@link VariableElement} instance
	 */
	class VariableFieldAdapter implements FieldAdapter<Element> {

		/**
		 * The {@code VariableElement} instance
		 */
		protected final VariableElement variable;

		/**
		 * Constructor with a {@code VariableElement} instance
		 *
		 * @param variable The {@code VariableElement} instance
		 */
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
		public boolean isFinal() {
			return variable.getModifiers().contains(javax.lang.model.element.Modifier.FINAL);
		}

		@Override
		public boolean isConstant() {
			return variable.getModifiers().contains(javax.lang.model.element.Modifier.STATIC)
					&& variable.getModifiers().contains(javax.lang.model.element.Modifier.FINAL);
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
