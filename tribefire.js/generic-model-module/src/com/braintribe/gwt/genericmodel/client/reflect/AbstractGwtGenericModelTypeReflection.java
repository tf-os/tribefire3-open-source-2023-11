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
package com.braintribe.gwt.genericmodel.client.reflect;

import java.lang.reflect.Type;
import java.util.stream.Stream;

import com.braintribe.gwt.genericmodel.client.GwtGmPlatform;
import com.braintribe.gwt.genericmodel.client.itw.GwtScriptTypeSynthesis;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.AbstractGenericModelTypeReflection;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.type.custom.EnumTypeImpl;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.Weavable;
import com.braintribe.model.weaving.ProtoGmEntityType;
import com.braintribe.processing.async.api.AsyncCallback;
import com.google.gwt.core.client.JavaScriptObject;

import jsinterop.context.JsKeywords;

/**
 *
 */
public abstract class AbstractGwtGenericModelTypeReflection extends AbstractGenericModelTypeReflection {

	@Override
	public Object getItwClassLoader() {
		return null;
	}

	@Override
	public EnumType deployEnumType(Class<? extends Enum<?>> enumClass) {
		EnumType type = new EnumTypeImpl(enumClass);

		registerGenericModelType(enumClass, type);

		TypePackage.register(type, constants(type));
		return type;
	}

	private JavaScriptObject constants(EnumType type) {
		JavaScriptObject result = JavaScriptObject.createObject();

		for (Enum<?> value : type.getEnumValues())
			defineConstant(result, JsKeywords.javaIdentifierToJs(value.name()), value);

		return result;
	}

	private static native void defineConstant(JavaScriptObject constants, String name, Enum<?> value)/*-{
		constants[name] = value;
	}-*/;

	@Override
	public void deployEntityType(EntityType<?> entityType) {
		super.deployEntityType(entityType);
		TypePackage.register(entityType, entityType);
	}

	/** Called after instantiated, from {@link GwtGmPlatform}. */
	public void initialize() {
		initialTypes().forEach(this::deployEntityType);
	}

	public abstract Stream<EntityType<?>> initialTypes();

	private static GwtScriptTypeSynthesis gwtScriptTypeSynthesis;

	@Override
	public void deploy(Weavable weavable) {
		getGwtScriptTypeSynthesis().ensureModelTypes((GmMetaModel) weavable);
	}

	@SuppressWarnings("unusable-by-js")
	@Override
	public void deploy(Weavable weavable, final AsyncCallback<Void> asyncCallback) {
		getGwtScriptTypeSynthesis().ensureModelTypesAsync((GmMetaModel) weavable) //
				.andThen(asyncCallback::onSuccess) //
				.onError(asyncCallback::onFailure);
	}

	protected GwtScriptTypeSynthesis createGwtScriptTypeSynthesis() {
		return new GwtScriptTypeSynthesis();
	}

	private GwtScriptTypeSynthesis getGwtScriptTypeSynthesis() {
		if (gwtScriptTypeSynthesis == null)
			gwtScriptTypeSynthesis = createGwtScriptTypeSynthesis();

		return gwtScriptTypeSynthesis;
	}

	@Override
	protected <T extends GenericEntity> EntityType<T> createEntityType(Class<?> entityClass) throws GenericModelException {
		throw new GenericModelException("Unexpected 'createEntityType' invocation."
				+ " EntityTypes for all GenericEntities should have been created at compile-time and registered on bootstrap." + " Class name: "
				+ entityClass.getName());
	}

	@Override
	protected Class<?> getClassForName(String qualifiedEntityTypeName, boolean require) throws GenericModelException {
		if (require)
			throw new UnsupportedOperationException("No class lookup supported in GWT!");

		return null;
	}

	@Override
	public <T extends GenericModelType> T getType(Type type) throws GenericModelException {
		throw new UnsupportedOperationException("Method 'AbstractGwtGenericModelTypeReflection.getGenericModelType' is not supported in GWT.");
	}

	@Override
	protected <T extends GenericModelType> T createCustomType(Class<?> classType) {
		throw new GenericModelException("Unexpected 'createCustomType' invocation."
				+ " EntityTypes for all GenericEntities should have been created at compile-time and registered on bootstrap." + " Class name: "
				+ classType.getName());
	}

	@Override
	public ProtoGmEntityType findProtoGmEntityType(String typeSignature) {
		throw new UnsupportedOperationException("Method 'findProtoGmEntityType' is not supported in GWT!");
	}

}
