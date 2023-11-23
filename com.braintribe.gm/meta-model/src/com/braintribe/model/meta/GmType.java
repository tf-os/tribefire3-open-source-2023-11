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
package com.braintribe.model.meta;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.annotation.meta.Unique;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.weaving.ProtoGmType;

@Abstract
public interface GmType extends ProtoGmType, GmModelElement {

	EntityType<GmType> T = EntityTypes.T(GmType.class);

	@Override
	@Unique
	@Mandatory
	String getTypeSignature();
	@Override
	void setTypeSignature(String typeSignature);

	/** TODO This makes no sense for a collection. */
	@Override
	GmMetaModel getDeclaringModel();
	void setDeclaringModel(GmMetaModel declaringModel);

	@Override
	GmTypeKind typeKind();

	/** @return true iff this is an instance of {@link GmBaseType}. */
	@Override
	default boolean isGmBase() {
		return false;
	}

	/** @return true iff this is an instance of {@link GmSimpleType}. */
	@Override
	default boolean isGmSimple() {
		return false;
	}

	/** @return true iff this is an instance of {@link GmEntityType}. */
	@Override
	default boolean isGmEntity() {
		return false;
	}

	/** @return true iff this is an instance of {@link GmEnumType}. */
	@Override
	default boolean isGmEnum() {
		return false;
	}

	/** @return true iff this is an instance of {@link GmCollectionType}. */
	@Override
	default boolean isGmCollection() {
		return false;
	}

	/** Tells if the type is either {@link GmEnumType} or {@link GmEntityType} */
	@Override
	default boolean isGmCustom() {
		return false;
	}

	/** Tells if the type is either {@link GmSimpleType} or {@link GmEnumType} */
	@Override
	default boolean isGmScalar() {
		return false;
	}

	/** Tells if the type is either {@link GmSimpleType} or {@link GmEnumType} */
	@Override
	default boolean isGmNumber() {
		return false;
	}

	default <T extends GenericModelType> T reflectionType() {
		return GMF.getTypeReflection().getType(getTypeSignature());
	}
}
