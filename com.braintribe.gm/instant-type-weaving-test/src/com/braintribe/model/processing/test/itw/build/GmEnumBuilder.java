// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.model.processing.test.itw.build;

import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.util.List;
import java.util.Set;

import com.braintribe.model.generic.builder.meta.MetaModelBuilder;
import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmType;

public class GmEnumBuilder {

	private final GmEnumType gmEnumType;

	public GmEnumBuilder(String typeSignature) {
		gmEnumType = MetaModelBuilder.enumType(typeSignature);
	}

	public GmEnumBuilder addConstant(String name) {
		GmEnumConstant enumConstant = MetaModelBuilder.enumConstant(gmEnumType, name);

		List<GmEnumConstant> constants = gmEnumType.getConstants();
		if (constants == null) {
			constants = newList();
			gmEnumType.setConstants(constants);
		}

		constants.add(enumConstant);

		return this;
	}

	public GmEnumType addToMetaModel(GmMetaModel metaModel) {
		Set<GmType> types = metaModel.getTypes();
		if (types == null) {
			types = newSet();
			metaModel.setTypes(types);
		}
		types.add(gmEnumType);

		return gmEnumType;
	}

	public GmEnumType gmEnumType() {
		return gmEnumType;
	}

}
