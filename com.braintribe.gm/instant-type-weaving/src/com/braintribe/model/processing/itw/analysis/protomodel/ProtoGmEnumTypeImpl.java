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

import java.util.List;
import java.util.Set;

import com.braintribe.model.generic.pseudo.GenericEntity_pseudo;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmTypeKind;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.weaving.ProtoGmEnumConstant;
import com.braintribe.model.weaving.ProtoGmEnumType;

/**
 * Pseudo-implementation of {@link GmEnumType}
 * 
 * @see GenericEntity_pseudo
 * 
 * @author peter.gazdik
 */
public class ProtoGmEnumTypeImpl extends ProtoGmCustomTypeImpl implements ProtoGmEnumType {

	private List<ProtoGmEnumConstant> constants;
	private Set<MetaData> metaData;
	private Set<MetaData> enumConstantMetaData;

	@Override
	public List<ProtoGmEnumConstant> getConstants() {
		return constants;
	}

	public void setConstants(List<ProtoGmEnumConstant> constants) {
		this.constants = constants;
	}

	@Override
	public Set<MetaData> getMetaData() {
		return metaData;
	}

	@Override
	public void setMetaData(Set<MetaData> metaData) {
		this.metaData = metaData;
	}

	@Override
	public Set<MetaData> getEnumConstantMetaData() {
		return enumConstantMetaData;
	}

	@Override
	public void setEnumConstantMetaData(Set<MetaData> enumConstantMetaData) {
		this.enumConstantMetaData = enumConstantMetaData;
	}

	@Override
	public GmTypeKind typeKind() {
		return GmTypeKind.ENUM;
	}
	
	@Override
	public boolean isGmEnum() {
		return true;
	}


}
