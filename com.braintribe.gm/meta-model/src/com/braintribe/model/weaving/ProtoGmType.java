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
package com.braintribe.model.weaving;

import com.braintribe.model.meta.GmTypeKind;

public interface ProtoGmType extends ProtoGmModelElement {

	String getTypeSignature();
	void setTypeSignature(String typeSignature);

	/** TODO This makes no sense for a collection. */
	ProtoGmMetaModel getDeclaringModel();

	GmTypeKind typeKind();

	/** @return true iff this is an instance of {@link ProtoGmBaseType}. */
	default boolean isGmBase() {
		return false;
	}

	/** @return true iff this is an instance of {@link ProtoGmSimpleType}. */
	default boolean isGmSimple() {
		return false;
	}

	/** @return true iff this is an instance of {@link ProtoGmEntityType}. */
	default boolean isGmEntity() {
		return false;
	}

	/** @return true iff this is an instance of {@link ProtoGmEnumType}. */
	default boolean isGmEnum() {
		return false;
	}

	/** @return true iff this is an instance of {@link ProtoGmCollectionType}. */
	default boolean isGmCollection() {
		return false;
	}

	/** Tells if the type is either {@link ProtoGmEnumType} or {@link ProtoGmEntityType} */
	default boolean isGmCustom() {
		return false;
	}

	/** Tells if the type is either {@link ProtoGmSimpleType} or {@link ProtoGmEnumType} */
	default boolean isGmScalar() {
		return false;
	}

	/** Tells if the type is either {@link ProtoGmSimpleType} or {@link ProtoGmEnumType} */
	default boolean isGmNumber() {
		return false;
	}

}
