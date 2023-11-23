// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.model.processing.test.itw.build;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.braintribe.model.generic.builder.meta.MetaModelBuilder;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmType;

public class GmEntityBuilder {

	private final GmEntityType gmEntityType;
	private final boolean preserveOrder;

	public GmEntityBuilder(String typeSignature) {
		this(typeSignature, false);
	}

	public GmEntityBuilder(String typeSignature, boolean preserveOrder) {
		super();

		this.preserveOrder = preserveOrder;

		gmEntityType = MetaModelBuilder.entityType(typeSignature);
	}

	public GmEntityBuilder addSuper(GmEntityBuilder builder) {
		return addSuper(builder.gmEntityType);
	}

	public GmEntityBuilder addSuper(GmEntityType get) {
		List<GmEntityType> superTypes = gmEntityType.getSuperTypes();
		superTypes.add(get);

		return this;
	}

	public GmEntityBuilder setIsAbstract(boolean value) {
		gmEntityType.setIsAbstract(value);

		return this;
	}

	public GmEntityBuilder addProperty(String name, GmEntityBuilder builder) {
		return addProperty(name, builder.gmEntityType);
	}

	public GmEntityBuilder addProperty(String name, GmType type) {
		return addProperty(name, type, null);
	}

	public GmEntityBuilder addProperty(String name, GmType type, Object initializer) {
		GmProperty property = MetaModelBuilder.property(null, name, type);
		property.setInitializer(initializer);

		return addProperty(property);
	}

	public GmEntityBuilder addProperty(GmProperty gmProperty) {
		List<GmProperty> properties = gmEntityType.getProperties();
		if (properties == null) {
			properties = new ArrayList<GmProperty>();
			gmEntityType.setProperties(properties);
		}

		gmProperty.setDeclaringType(gmEntityType);
		properties.add(gmProperty);
		return this;
	}

	public GmEntityType addToMetaModel(GmMetaModel metaModel) {
		Set<GmType> types = metaModel.getTypes();
		if (types == null) {
			types = newSet();
			metaModel.setTypes(types);
		}
		types.add(gmEntityType);

		return gmEntityType;
	}

	public GmEntityType gmEntityType() {
		return gmEntityType;
	}

	private <E> Set<E> newSet() {
		return preserveOrder ? new LinkedHashSet<E>() : new HashSet<E>();
	}
}
