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
import java.util.Collection;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.GmCoreApiInteropNamespaces;
import com.braintribe.model.generic.GmfException;
import com.braintribe.model.meta.Weavable;
import com.braintribe.processing.async.api.AsyncCallback;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;

@JsType(namespace = GmCoreApiInteropNamespaces.reflection)
@SuppressWarnings("unusable-by-js")
public interface GenericModelTypeReflection extends DeprecatedReflectionApi, EssentialTypes {

	String rootModelName = "com.braintribe.gm:root-model";

	/** Ensures types described by given {@link Weavable} are "woven" using iTW. */
	@JsIgnore
	void deploy(Weavable weavable) throws GmfException;

	void deploy(Weavable weavable, AsyncCallback<Void> asyncCallback);

	/**
	 * Returns the custom {@link ClassLoader} used in JVM to load the woven classes.
	 * <p>
	 * Return type is Object to not cause problems on GWT compilation, as ClassLoader is not emulated in GWT.
	 */
	Object getItwClassLoader();

	BaseType getBaseType();
	SimpleType getSimpleType(Class<?> javaType);

	@JsMethod(name = "getEnumTypeOf")
	EnumType getEnumType(Enum<?> enumConstant);
	EnumType getEnumType(Class<? extends Enum<?>> enumClass);

	<T extends GenericEntity> EntityType<T> getEntityType(Class<? extends GenericEntity> entityClass) throws GenericModelException;

	// #################################################
	// ## . . . . . . . . Collections . . . . . . . . ##
	// #################################################

	ListType getListType(GenericModelType elementType);
	SetType getSetType(GenericModelType elementType);
	MapType getMapType(GenericModelType keyType, GenericModelType valueType);
	<T extends CollectionType> T getCollectionType(String typeName, GenericModelType... parameterization);
	@JsIgnore
	<T extends CollectionType> T getCollectionType(Class<?> collectionClass, GenericModelType... parameterization);

	// #################################################
	// ## . . . . . . . GenericModelType . . . . . . .##
	// #################################################

	@JsIgnore
	<T extends GenericModelType> T getType(Type type) throws GenericModelException;

	<T extends GenericModelType> T getType(Class<?> clazz) throws GenericModelException;

	@JsMethod(name = "getTypeBySignature")
	<T extends GenericModelType> T getType(String typeSignature) throws GenericModelException;

	/**
	 * This is actually the same as {@link #getType(String)}, just more friendly in cases where type-inference cannot doesn't work.
	 */
	@JsMethod(name = "getEntityTypeBySignature")
	<T extends GenericEntity> EntityType<T> getEntityType(String typeSignature) throws GenericModelException;
	@JsMethod(name = "getEnumTypeBySignature")
	EnumType getEnumType(String typeName);

	<T extends GenericEntity> EntityType<T> findEntityType(String typeSignature);
	EnumType findEnumType(String typeSignature);

	/** Returns the {@link GenericModelType} of given value. If the value is null, {@link BaseType} is returned. */
	@JsMethod(name = "getTypeOf")
	<T extends GenericModelType> T getType(Object value);

	/**
	 * Returns the type for the type-signature if possible (i.e. all required types are accessible by class-loader), or <tt>null</tt> otherwise.
	 */
	<T extends GenericModelType> T findType(String typeSignature);

	// #################################################
	// ## . . . . . . . . . Models . . . . . . . . . .##
	// #################################################

	/** Similar to {@link #findModel(String)} but throws a {@link GenericModelException} if no model is found. */
	Model getModel(String modelName);

	/**
	 * @param modelName
	 *            name of the model, e.g.: com.braintribe.gm:root-model
	 * @return Model for given name if found on the classpath or <tt>null</tt> otherwise.
	 */
	default Model findModel(String modelName) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @return {@link Model} for given custom type signature.
	 * 
	 * @see CustomType#getModel()
	 */
	Model getModelForType(String customTypeSignature);

	/**
	 * @return a collection of all models that where packaged with the distribution (found on original classpath)
	 */
	Collection<? extends Model> getPackagedModels();

}
