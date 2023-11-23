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
package com.braintribe.model.processing.itw.analysis.protomodel;

import com.braintribe.model.generic.pseudo.GenericEntity_pseudo;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.weaving.ProtoGmMetaModel;
import com.braintribe.model.weaving.ProtoGmType;

/**
 * Pseudo-implementation of {@link GmType}
 * 
 * @see GenericEntity_pseudo
 * 
 * @author peter.gazdik
 */
public abstract class ProtoGmTypeImpl extends ProtoGmModelElementImpl implements ProtoGmType {

	private String typeSignature;
	private ProtoGmMetaModel declaringModel;

	@Override
	public String getTypeSignature() {
		return typeSignature;
	}

	@Override
	public void setTypeSignature(String typeSignature) {
		this.typeSignature = typeSignature;
	}

	@Override
	public ProtoGmMetaModel getDeclaringModel() {
		return declaringModel;
	}

	public void setDeclaringModel(ProtoGmMetaModel declaringModel) {
		this.declaringModel = declaringModel;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " [" + typeSignature + "]";
	}

}
