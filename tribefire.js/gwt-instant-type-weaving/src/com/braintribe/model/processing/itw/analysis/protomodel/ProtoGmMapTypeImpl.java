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
import com.braintribe.model.meta.GmMapType;
import com.braintribe.model.meta.GmTypeKind;
import com.braintribe.model.weaving.ProtoGmMapType;
import com.braintribe.model.weaving.ProtoGmType;

/**
 * Pseudo-implementation of {@link GmMapType}
 * 
 * @see GenericEntity_pseudo
 * 
 * @author peter.gazdik
 */
public class ProtoGmMapTypeImpl extends ProtoGmTypeImpl implements ProtoGmMapType {

	private ProtoGmType keyType;
	private ProtoGmType valueType;

	@Override
	public ProtoGmType getKeyType() {
		return keyType;
	}

	public void setKeyType(ProtoGmType keyType) {
		this.keyType = keyType;
	}

	@Override
	public ProtoGmType getValueType() {
		return valueType;
	}

	public void setValueType(ProtoGmType valueType) {
		this.valueType = valueType;
	}

	@Override
	public GmTypeKind typeKind() {
		return GmTypeKind.MAP;
	}

}
