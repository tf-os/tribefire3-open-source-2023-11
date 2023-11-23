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
package com.braintribe.model.generic.reflection;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.value.EntityReference;

public interface DeprecatedReflectionApi {

	/** @deprecated use {@link SimpleTypes#TYPES_SIMPLE} */
	@Deprecated
	List<SimpleType> stSimpleTypes = SimpleTypes.TYPES_SIMPLE;

	/** @deprecated call {@link GenericEntity#entityType()} directly */
	@Deprecated
	<T extends GenericEntity, T1 extends GenericEntity> EntityType<T1> getEntityType(T entity) throws GenericModelException;

	/**
	 * @deprecated use {@link GenericModelTypeReflection#getType(Object)} followed by
	 *             {@link GenericModelType#getTypeSignature()}
	 */
	@Deprecated
	String getTypeSignature(Object value) throws GenericModelException;

	/** @deprecated call {@link GenericEntity#reference()} directly */
	@Deprecated
	<T extends GenericEntity> EntityReference getEntityReference(T entity) throws GenericModelException;

	/** @deprecated use {@link GenericModelTypeReflection#getType(String)} */
	@Deprecated
	GenericModelType getRegisteredType(String typeSignature) throws GenericModelException;

	/** @deprecated use {@link SimpleTypes#TYPES_SIMPLE} */
	@Deprecated
	List<SimpleType> getSimpleTypes();

	/** @deprecated use {@link SimpleTypes#TYPES_SIMPLE} of {@link GenericModelTypeReflection#getType(Class)} */
	@Deprecated
	Map<Class<?>, SimpleType> getSimpleTypeMap();

	/** @deprecated use {@link GenericModelTypeReflection#getType(String)} */
	@Deprecated
	Map<String, SimpleType> getSimpleTypeNameMap();

	/** @deprecated use {@link GenericModelTypeReflection#getType(Object)} */
	@Deprecated
	Set<Class<?>> getSimpleTypeClasses();

	/** @deprecated not sure why anyone would need this... */
	@Deprecated
	Set<String> getSimpleTypeNames();

	/** @deprecated use {@link GenericModelTypeReflection#getType(Type)} */
	@Deprecated
	GenericModelType getGenericModelType(Type type) throws GenericModelException;

	/** @deprecated use {@link GenericModelTypeReflection#getType(Class)} */
	@Deprecated
	GenericModelType getGenericModelType(Class<?> declarationIface) throws GenericModelException;

	/** @deprecated use {@link GenericModelTypeReflection#getType(Object)} */
	@Deprecated
	GenericModelType getObjectType(Object value);

	/**
	 * @deprecated use either {@link GenericModelTypeReflection#getType(String)} or
	 *             {@link GenericModelTypeReflection#findType(String)}.
	 */
	@Deprecated
	EnumType getEnumType(String typeName, boolean require);

	/**
	 * @deprecated use {@link GenericModelTypeReflection#getType(String)} or
	 *             {@link GenericModelTypeReflection#findType(String)}
	 */
	@Deprecated
	<T extends GenericEntity> EntityType<T> getEntityType(String typeSignature, boolean require) throws GenericModelException;
}
