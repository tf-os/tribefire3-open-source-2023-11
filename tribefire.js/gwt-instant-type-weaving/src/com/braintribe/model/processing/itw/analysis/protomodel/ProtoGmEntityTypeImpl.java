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
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmTypeKind;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.weaving.ProtoGmEntityType;
import com.braintribe.model.weaving.ProtoGmProperty;
import com.braintribe.model.weaving.ProtoGmType;
import com.braintribe.model.weaving.override.ProtoGmPropertyOverride;

/**
 * Pseudo-implementation of {@link GmEntityType}
 * 
 * @see GenericEntity_pseudo
 * 
 * @author peter.gazdik
 */
public class ProtoGmEntityTypeImpl extends ProtoGmCustomTypeImpl implements ProtoGmEntityType {

	private List<ProtoGmEntityType> superTypes;
	private List<ProtoGmProperty> properties;
	private List<ProtoGmPropertyOverride> propertyOverrides;
	private Set<MetaData> metaData;
	private boolean isAbstract;
	private ProtoGmType evaluatesTo;

	@Override
	public List<ProtoGmEntityType> getSuperTypes() {
		return superTypes;
	}

	public void setSuperTypes(List<ProtoGmEntityType> superTypes) {
		this.superTypes = superTypes;
	}

	@Override
	public List<ProtoGmProperty> getProperties() {
		return properties;
	}

	public void setProperties(List<ProtoGmProperty> properties) {
		this.properties = properties;
	}

	@Override
	public List<ProtoGmPropertyOverride> getPropertyOverrides() {
		return propertyOverrides;
	}

	public void setPropertyOverrides(List<ProtoGmPropertyOverride> propertyOverrides) {
		this.propertyOverrides = propertyOverrides;
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
	public boolean getIsAbstract() {
		return isAbstract;
	}

	@Override
	public void setIsAbstract(boolean isAbstract) {
		this.isAbstract = isAbstract;
	}

	@Override
	public ProtoGmType getEvaluatesTo() {
		return evaluatesTo;
	}

	public void setEvaluatesTo(ProtoGmType evaluatesTo) {
		this.evaluatesTo = evaluatesTo;
	}

	@Override
	public GmTypeKind typeKind() {
		return GmTypeKind.ENTITY;
	}

}
