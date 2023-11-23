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
package com.braintribe.model.processing.query.test.check;

import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.function.Predicate;

import org.assertj.core.api.AbstractBooleanAssert;
import org.assertj.core.api.AbstractIntegerAssert;
import org.assertj.core.api.AbstractLongAssert;
import org.assertj.core.api.AbstractObjectAssert;
import org.assertj.core.api.IterableAssert;
import org.assertj.core.api.ListAssert;
import org.assertj.core.api.MapAssert;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.value.PersistentEntityReference;

/**
 * NOTE regarding naming - methods ending with an underscore automatically close the current level and return to a parent. For example, this is valid:
 * {@code
 * 		.hasType(Person.T)
 * 		.whereProperty("name").is_("Jack")              // is_ returns from Person.name to Person
 *  	.whereProperty("gender").is_(Gender.MALE)       // is_ returns from Person.gender to Person
 * }
 */
public abstract class AbstractEntityAssemblyChecker<T extends AbstractEntityAssemblyChecker<T>> {

	protected static final GenericModelTypeReflection typeReflection = GMF.getTypeReflection();
	protected static final String PROPERTY_DELIMITER = "#";

	protected final T self;
	protected final GenericEntity root;

	protected final Stack<Object> stack = new Stack<Object>();

	protected Object currentObject;
	protected GenericEntity currentEntity;
	protected EntityType<GenericEntity> entityType;

	protected String path = "";

	protected AbstractEntityAssemblyChecker(GenericEntity root) {
		this.self = (T) this;
		this.root = root;

		clear();
	}

	protected T clear() {
		stack.clear();
		loadObject(root);

		if (root != null)
			path = entityType.getShortName();

		return self;
	}

	public T isNull() {
		assertCurrentObject().isNull();
		return self;
	}

	public T isNotNull() {
		assertCurrentObject().isNotNull();
		return self;
	}

	public T isTrue_() {
		return is_(Boolean.TRUE);
	}

	public T isFalse_() {
		return is_(Boolean.FALSE);
	}

	public T hasType_(Class<?> clazz) {
		return hasType(clazz).close();
	}

	public T hasType(Class<?> clazz) {
		assertCurrentObject().isInstanceOf(clazz);
		return self;
	}

	public T matches(Predicate<Object> predicate) {
		assertCurrentObject().matches(predicate);
		return self;
	}

	public T matches(Predicate<Object> predicate, String predicateDescription) {
		assertCurrentObject().matches(predicate, predicateDescription);
		return self;
	}

	public <E> T matches(Class<E> clazz, Predicate<? super E> predicate) {
		hasType(clazz);
		assertCurrentObject().matches((Predicate<Object>) predicate);
		return self;
	}

	public <E> T matches(Class<E> clazz, Predicate<? super E> predicate, String predicateDescription) {
		hasType(clazz);
		assertCurrentObject().matches((Predicate<Object>) predicate, predicateDescription);
		return self;
	}

	public T hasType(EntityType<?> et) {
		return hasType(et.getJavaType());
	}

	public T hasType_(EntityType<?> et) {
		return hasType_(et.getJavaType());
	}
	
	public T isSetWithSize(int size) {
		hasType(Set.class);
		assertCurrentCollection().hasSize(size);
		return self;
	}

	public T isListWithSize(int size) {
		hasType(List.class);
		assertCurrentList().hasSize(size);
		return self;
	}

	public T isMapWithSize(int size) {
		hasType(Map.class);
		assertCurrentMap().hasSize(size);
		return self;
	}

	public T is_(String s) {
		assertCurrentObject().isEqualTo(s);
		return close();
	}

	public T is_(Integer i) {
		assertCurrentInteger().isEqualTo(i);
		return close();
	}

	public T is_(Long l) {
		assertCurrentLong().isEqualTo(l);
		return close();
	}

	public T is_(Boolean b) {
		assertCurrentBoolean().isEqualTo(b);
		return close();
	}

	public T is_(Object o) {
		assertCurrentObject().isEqualTo(o);
		return close();
	}

	public T contains(Object... os) {
		assertCurrentCollection().contains(os);
		return self;
	}

	public T containsOnly(Object... os) {
		assertCurrentCollection().containsOnly(os);
		return self;
	}

	public T isNullOrEmptyCollection() {
		return currentObject != null ? isEmptyCollection() : self; 
	}

	private T isEmptyCollection() {
		assertCurrentCollection().isEmpty();
		return self;
	}

	public T isNullOrEmptyList() {
		return currentObject != null ? isEmptyList() : self; 
	}

	public T isEmptyList() {
		return isList();
	}

	public T isList(Object... os) {
		hasType(List.class);
		assertCurrentList().isEqualTo(Arrays.asList(os));
		return self;
	}

	public T isNullOrEmptySet() {
		return currentObject != null ? isEmptySet() : self; 
	}

	public T isEmptySet() {
		return isSet();
	}

	public T isSet(Object... os) {
		hasType(Set.class);
		assertCurrentCollection().containsOnly(os);
		return self;
	}

	public T isReference_(EntityType<? extends GenericEntity> et, Object id, String partition) {
		return isReference_(et.getTypeSignature(), id, partition);
	}

	public T isReference_(String signature, Object id, String partition) {
		return hasType(PersistentEntityReference.T) //
				.whereProperty("typeSignature").is_(signature) //
				.whereProperty("refId").is_(id) //
				.whereProperty("refPartition").is_(partition) //
				.close();
	}

	public T setToList() {
		Object list = newList((Set<?>) currentObject);
		return doTraverseStep(list, "[convertedToList]");
	}

	private AbstractIntegerAssert<?> assertCurrentInteger() {
		assertCurrentObject().isInstanceOf(Integer.class);
		return assertThat((Integer) currentObject).as(path);
	}

	private AbstractLongAssert<?> assertCurrentLong() {
		assertCurrentObject().isInstanceOf(Long.class);
		return assertThat((Long) currentObject).as(path);
	}

	private AbstractBooleanAssert<?> assertCurrentBoolean() {
		assertCurrentObject().isInstanceOf(Boolean.class);
		return assertThat((Boolean) currentObject).as(path);
	}

	private IterableAssert<Object> assertCurrentCollection() {
		return assertThat((Iterable<Object>) currentObject).as(path);
	}

	private ListAssert<Object> assertCurrentList() {
		return assertThat((List<Object>) currentObject).as(path);
	}

	private MapAssert<Object, Object> assertCurrentMap() {
		return assertThat((Map<Object, Object>) currentObject).as(path);
	}

	private AbstractObjectAssert<?, ?> assertCurrentObject() {
		return assertThat(currentObject).as(path);
	}

	public T whereProperty(String propertyName) {
		Object propertyValue = propertyValue(propertyName);
		return doTraverseStep(propertyValue, propertyName);
	}

	protected <E> E propertyValue(String propertyName) {
		if (entityType == null)
			throwNoEntityLevelException();

		return entityType.getProperty(propertyName).get(currentEntity);
	}

	private void throwNoEntityLevelException() {
		String msg = "Cannot load property as we are not at entity level. Current object: " + currentObject;
		if (currentObject != null)
			msg = msg + " (" + currentObject.getClass().getName() + ")";

		throw new RuntimeException(msg);
	}

	public T whereElementAt(int index) {
		Object elementValue = ((List<?>) currentObject).get(index);
		return doTraverseStep(elementValue, "[" + index + "]");
	}

	public T whereFirstElement() {
		Object elementValue = ((Collection<?>) currentObject).iterator().next();
		return doTraverseStep(elementValue, "[@first]");
	}

	public T whereValueFor(Object key) {
		Object elementValue = ((Map<?, ?>) currentObject).get(key);
		return doTraverseStep(elementValue, "[@:" + key + "]");
	}

	public T whereFirstKey() {
		Object elementValue = ((Map<?, ?>) currentObject).keySet().iterator().next();
		return doTraverseStep(elementValue, "[@firstKey]");
	}

	public T whereFirstValue() {
		Object elementValue = ((Map<?, ?>) currentObject).values().iterator().next();
		return doTraverseStep(elementValue, "[@firstValue]");
	}

	public T whenOrderedBy(String propertyPath) {
		Comparator<Object> cmp = new PropertyBasedComparator(propertyPath);
		currentObject = newList((Collection<?>) currentObject);
		Collections.sort((List<?>) currentObject, cmp);

		return self;
	}

	private T doTraverseStep(Object object, String pathElement) {
		stack.push(currentObject);
		path = path + PROPERTY_DELIMITER + pathElement;
		loadObject(object);

		return self;
	}

	public T close(int count) {
		for (int i = 1; i < count; i++)
			close();

		return close();
	}

	public T close() {
		loadObject(stack.pop());
		path = path.substring(0, path.lastIndexOf(PROPERTY_DELIMITER));
		return self;
	}

	private void loadObject(Object o) {
		currentObject = o;

		if (currentObject instanceof GenericEntity) {
			currentEntity = (GenericEntity) currentObject;
			entityType = currentEntity.entityType();

		} else {
			currentEntity = null;
			entityType = null;
		}
	}

	@Override
	public boolean equals(Object obj) {
		throw new RuntimeException("The 'equals' method should not be invoked, use one of 'isEqualTo' methods!");
	}

	@Override
	public int hashCode() {
		throw new RuntimeException("Why would you need this?");
	}

}
