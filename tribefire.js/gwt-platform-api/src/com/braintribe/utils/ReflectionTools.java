// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
// 
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package com.braintribe.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.braintribe.common.lcd.GenericRuntimeException;
import com.braintribe.common.uncheckedcounterpartexceptions.UncheckedClassNotFoundException;
import com.braintribe.common.uncheckedcounterpartexceptions.UncheckedIllegalAccessException;
import com.braintribe.utils.lcd.Arguments;
import com.braintribe.utils.lcd.Not;

/**
 * This class may contain Java-only (i.e. GWT incompatible) code. For further information please see
 * {@link com.braintribe.utils.lcd.ReflectionTools}.
 *
 * @author michael.lafite
 */
public final class ReflectionTools extends com.braintribe.utils.lcd.ReflectionTools {

	private ReflectionTools() {
		// no instantiation required
	}

	/**
	 * {@link #getField(String, Class) Recursively searches} the specified field and then
	 * {@link #getFieldValue(Field, Object) gets its value}.
	 *
	 * @throws IllegalArgumentException
	 *             if the specified field doesn't exist.
	 * @throws GenericRuntimeException
	 *             if anything else goes wrong.
	 */

	public static Object getFieldValue(final String fieldName, final Object object) throws IllegalArgumentException, GenericRuntimeException {
		final Field field = getField(fieldName, Not.Null(object.getClass()));
		if (field == null) {
			throw new IllegalArgumentException("Class " + object.getClass().getName() + " has no field '" + fieldName + "'!");
		}
		return getFieldValue(field, object);
	}

	/**
	 * Gets the value of the passed <code>field</code> in the passed <code>object</code>.
	 *
	 * @throws GenericRuntimeException
	 *             if anything goes wrong.
	 */

	public static Object getFieldValue(final Field field, final Object object) throws GenericRuntimeException {
		try {
			field.setAccessible(true);
			return field.get(object);
		} catch (final Exception e) {
			throw new GenericRuntimeException("Error while getting value of field "
					+ com.braintribe.utils.lcd.CommonTools.getStringRepresentation(field.getName()) + " from object " + object + "!", e);
		}
	}

	public static <T> T getStaticFieldValue(Field field) {
		try {
			return (T) field.get(null);

		} catch (IllegalAccessException e) {
			throw new UncheckedIllegalAccessException(
					"Error while accessing field " + field.getDeclaringClass().getName() + "." + field.getName() + " on", e);
		}
	}

	/**
	 * Recursively searches the specified field in the passed class and its superclasses. If the field is not found,
	 * <code>null</code> is returned.
	 */
	public static Field getField(final String fieldName, final Class<?> clazz) {
		final Field[] fields = clazz.getDeclaredFields();
		for (final Field field : fields) {
			if (field.getName().equals(fieldName)) {
				return field;
			}
		}
		final Class<?> superClass = clazz.getSuperclass();
		if (superClass == null) {
			return null;
		}
		return getField(fieldName, superClass);
	}

	/**
	 * Checks if the passed <code>object</code> is an instance of all of the specified classes.
	 */
	public static boolean isInstanceOfAll(final Object object, final Class<?>... classes) {
		if (object == null || CommonTools.isEmpty(classes)) {
			return false;
		}
		for (final Class<?> clazz : classes) {
			if (!clazz.isInstance(object)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks if the passed <code>object</code> is an instance of any of the specified classes.
	 */
	public static boolean isInstanceOfAny(final Object object, final Class<?>... classes) {
		if (object == null || CommonTools.isEmpty(classes)) {
			return false;
		}
		for (final Class<?> clazz : classes) {
			if (clazz.isInstance(object)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks if all passed <code>objects</code> are instances of the specified class.
	 */
	public static boolean isAllInstanceOf(final Class<?> clazz, final Object... objects) {
		Arguments.notNullWithNames("class", clazz);
		if (CommonTools.isEmpty(objects)) {
			return false;
		}
		for (final Object object : objects) {
			// object may be null
			if (!clazz.isInstance(object)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks if at least one of the passed <code>objects</code> is an instance of the specified class.
	 */
	public static boolean isAnyInstanceOf(final Class<?> clazz, final Object... objects) {
		Arguments.notNullWithNames("class", clazz);
		if (CommonTools.isEmpty(objects)) {
			return false;
		}
		for (final Object object : objects) {
			// object may be null
			if (clazz.isInstance(object)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Gets the <code>Class</code> instance for the specified class and casts it to specified super class.
	 *
	 * @param className
	 *            the class name of the class to return.
	 * @param superClass
	 *            the super class.
	 * @return the <code>Class</code> instance
	 * @throws ClassNotFoundException
	 *             if the class is not found.
	 * @throws ClassCastException
	 *             if the class is not a sub type of the specified super class
	 */
	public static <T> Class<? extends T> getClassForName(final String className, final Class<T> superClass) throws ClassNotFoundException {
		try {
			return Not.Null(Class.forName(className).asSubclass(superClass));
		} catch (final ClassNotFoundException e) {
			throw new ClassNotFoundException(
					"Error while getting class instance for name class name " + CommonTools.getStringRepresentation(className) + ": class not found!",
					e);
		} catch (final ClassCastException e) {
			throw ThrowableTools.getWithCause(new ClassCastException("Error while getting class instance for name class name "
					+ com.braintribe.utils.lcd.CommonTools.getStringRepresentation(className)
					+ ": class is not a subclass of the specified superclass "
					+ com.braintribe.utils.lcd.CommonTools.getStringRepresentation(superClass.getName()) + "!"), e);
		}
	}

	/**
	 * Returns <code>true</code>, if {@link Class#forName(String)} returns a result for the specified
	 * <code>className</code>, otherwise <code>false</code>.
	 */
	public static boolean classExists(final String className) {
		try {
			Class.forName(className);
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

	/**
	 * Returns the {@link Class} for the specified <code>className</code> or <code>null</code>, if it cannot be found.
	 */
	public static Class<?> getClass(final String className) {
		try {
			return Class.forName(className);
		} catch (ClassNotFoundException e) {
			return null;
		}
	}

	/**
	 * Returns the {@link Class} for the specified <code>className</code>.
	 *
	 * @throws UncheckedClassNotFoundException
	 *             if the <code>Class</code> cannot be found.
	 */
	public static Class<?> getExistingClass(final String className) throws UncheckedClassNotFoundException {
		Class<?> clazz = getClass(className);
		if (clazz == null) {
			throw new UncheckedClassNotFoundException("Class '" + className + "' not found!");
		}
		return clazz;
	}

	/**
	 * Iterates through all {@link Class#getDeclaredMethods()} of the passed class and returns the first one with the
	 * specified <code>methodName</code>. If no method is found, the method is searched recursively in the super class.
	 *
	 * @return the first method found with the specified name or <code>null</code> if none is found.
	 */

	public static Method getMethod(final String methodName, final Class<?> clazz) {
		for (final Method method : clazz.getDeclaredMethods()) {
			if (method.getName().equals(methodName)) {
				return method;
			}
		}
		final Class<?> superClass = clazz.getSuperclass();
		if (superClass != null) {
			return getMethod(methodName, superClass);
		}
		return null;
	}

	/**
	 * Returns the default value for the specified <code>clazz</code>. Only {@link Class#isPrimitive() primitive} types
	 * have a default value. For all other types, the method returns <code>null</code>.
	 */
	public static Object getDefaultValue(Class<?> clazz) {
		if (clazz.isPrimitive()) {
			return PrimitivesTools.getDefaultValue(clazz);
		} else {
			return null;
		}
	}

	/**
	 * Returns the annotations of the passed <code>annotatable</code> object (e.g. class, method, etc.).
	 */
	public static List<Annotation> getAnnotations(Object annotatable) {
		Arguments.notNull(annotatable);
		Annotation[] annotations = null;
		if (annotatable instanceof AccessibleObject) {
			annotations = ((AccessibleObject) annotatable).getAnnotations();
		} else if (annotatable instanceof Class) {
			annotations = ((Class<?>) annotatable).getAnnotations();
		} else if (annotatable instanceof Package) {
			annotations = ((Package) annotatable).getAnnotations();
		} else {
			throw new IllegalArgumentException("Cannot get annotations from object " + annotatable + ", because it's type "
					+ annotatable.getClass().getName() + " is not supported!");
		}

		List<Annotation> result = Arrays.asList(annotations);
		return result;
	}

	/**
	 * Returns the annotations whose {@link Annotation#annotationType() type} matches the searched
	 * <code>annotationType</code>.
	 */
	public static List<Annotation> getAnnotationsByType(Iterable<Annotation> annotations, Class<? extends Annotation> annotationType) {
		List<Annotation> result = new ArrayList<Annotation>();

		for (Annotation annotation : annotations) {
			if (annotation.annotationType().equals(annotationType)) {
				result.add(annotation);
			}
		}

		return result;
	}

	/**
	 * Returns the annotations whose {@link Annotation#annotationType() type} {@link Class#getName() name} matches the
	 * specified <code>typeNameFilter</code>.
	 */
	public static List<Annotation> getAnnotationsByTypeName(Iterable<Annotation> annotations, Predicate<String> typeNameFilter) {
		List<Annotation> result = new ArrayList<Annotation>();

		for (Annotation annotation : annotations) {
			if (typeNameFilter.test(annotation.annotationType().getName())) {
				result.add(annotation);
			}
		}

		return result;
	}

	/**
	 * Returns the objects whose {@link Object#getClass() type} {@link Class#getName() name} matches the specified
	 * <code>typeNameFilter</code>.
	 */
	public static <T> List<T> getObjectsByTypeName(Iterable<T> objects, Predicate<String> typeNameFilter) {
		List<T> result = new ArrayList<T>();

		for (T object : objects) {
			if (object == null) {
				continue;
			}

			String typeName = object.getClass().getName();
			if (typeNameFilter.test(typeName)) {
				result.add(object);
			}
		}

		return result;
	}

	/** Checks whether the passed class is abstract or not. */
	public static boolean isAbstract(Class<?> clazz) {
		return Modifier.isAbstract(clazz.getModifiers());
	}

	/**
	 * Returns the no-argument constructor of the specified <code>clazz</code>, or <code>null</code>, if there is no
	 * such constructor.
	 */
	public static <T> Constructor<T> getNoArgumentConstructor(Class<T> clazz) {
		Constructor<T> constructor = null;
		try {
			constructor = clazz.getConstructor();
		} catch (NoSuchMethodException e) {
			// ignore
		}
		return constructor;
	}

	/**
	 * Creates a new instance of the specified <code>clazz</code> using the {@link #getNoArgumentConstructor(Class)
	 * no-argument constructor}.
	 *
	 * @throws IllegalArgumentException
	 *             if (for various reasons) the instance can't be created, e.g. because there is no no-argument
	 *             constructor or because the class is abstract, etc.
	 */
	public static <T> T newInstanceViaNoArgumentConstructor(Class<T> clazz) {
		Constructor<T> noArgumentConstructor = getNoArgumentConstructor(clazz);
		if (noArgumentConstructor == null)
			throw new IllegalArgumentException(
					"Cannot instantiate class " + clazz.getName() + ", because it doesn't have a no-argument constructor!");

		return newInstance(noArgumentConstructor);
	}

	public static <T> T newInstance(Constructor<? extends T> constructor, Object...initars) {
		try {
			return constructor.newInstance(initars);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new IllegalArgumentException("Couldn't instantiate class using constructor: " + constructor, e);
		}
	}

	public static boolean hasModifiers(Class<?> clazz, int modifiers) {
		return modifiersMatch(clazz.getModifiers(), modifiers);
	}
	
	public static boolean hasModifiers(Member member, int modifiers) {
		return modifiersMatch(member.getModifiers(), modifiers);
	}
	
	private static boolean modifiersMatch(int elementModifiers, int checkedModifiers) {
		return (elementModifiers & checkedModifiers) == checkedModifiers;
	}

	/** Returns the same as {@link Class#getField(String)} or null, if that method throws an exception. */
	public static Field findPublicField(Class<?> clazz, String fieldName) {
		try {
			return clazz.getField(fieldName);
		} catch (Exception e) {
			return null;
		}
	}

	public static Field findDeclaredField(Class<?> clazz, String fieldName) {
		try {
			return clazz.getDeclaredField(fieldName);
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * finds the first path from a class to a super type if existing 
	 * @param stack The collection that will hold the path if it was found
	 * @param subType The type that derives from superType
	 * @param superType The type that subType directly or indirectly inherits from
	 * @return true if a path was found
	 */
	public static boolean findInheritancePath(Deque<Type> stack, Type subType, Class<?> superType) {
		
		final Class<?> clazz;
		
		if (subType instanceof ParameterizedType) {
			clazz = (Class<?>)((ParameterizedType)subType).getRawType();
		}
		else if (subType instanceof Class<?>) {
			clazz = (Class<?>)subType;
		}
		else {
			throw new IllegalStateException("unexpected type in hierarchy: " + subType);
		}
		
		stack.push(subType);
		
		if (clazz == superType)
			return true;
		
		Iterable<Type> superTypes = Stream.concat( //
				Stream.of(clazz.getGenericSuperclass()).filter(t -> t != null), //  
				Stream.of(clazz.getGenericInterfaces()) // 
		)::iterator;
		
		for (Type curSuperType: superTypes) {
			if (findInheritancePath(stack, curSuperType, superType))
				return true;
		}

		stack.pop();
		
		return false;
	}
	
	/**
	 * Resolves the value for a generic parameter from a declaring class in the context of a derived class given that the generics parameter is fully concretized.  
	 * @param type the concrete class that 
	 * @param declaringType the class that declares the generics parameter
	 * @param parameterName the name of the generics parameter
	 * @return the concrete type that is assigned to the generics parameter.
	 */
	public static Type getGenericsParameter(Class<?> type, Class<?> declaringType, String parameterName) {
		Deque<Type> stack = new ArrayDeque<Type>();
		
		if (!findInheritancePath(stack, type, declaringType))
			throw new IllegalStateException("The given type [" + type.getName() + "] does not inherit from the type [" + declaringType.getName() + "]");
		
		String curParameterName = parameterName;
		
		for (Type curType: stack) {
			if (!(curType instanceof ParameterizedType))
				break;
					
			ParameterizedType parameterizedType = (ParameterizedType)curType;
			
			Class<?> rawType = (Class<?>)parameterizedType.getRawType();

			int parameterIndex = indexOfGenericParameter(rawType, curParameterName);
			
			if (parameterIndex == -1)
				throw new IllegalStateException("Class [" + curType.getTypeName() + "] is missing the claimed generics parameter named '" + curParameterName + "'");
			
			Type parameterValue = parameterizedType.getActualTypeArguments()[parameterIndex];
			
			if (parameterValue instanceof Class<?>) {
				return parameterValue;
			}
			else if (parameterValue instanceof ParameterizedType) {
				return parameterValue;
			}
			else if (parameterValue instanceof TypeVariable<?>) {
				curParameterName = ((TypeVariable<?>)parameterValue).getName();
			}
		}
	
		throw new IllegalStateException("The given type [" + type.getName() + "] does concretize the parameter '" + parameterName + "' of type [" + declaringType.getName() + "]");
	}
	
	private static int indexOfGenericParameter(Class<?> rawType, String parameterName) {
		int index = 0;
		for (TypeVariable<?> typeVar: rawType.getTypeParameters()) {
			if (typeVar.getName().equals(parameterName))
				return index;
			
			index++;
		}
		
		return -1;
	}

}
