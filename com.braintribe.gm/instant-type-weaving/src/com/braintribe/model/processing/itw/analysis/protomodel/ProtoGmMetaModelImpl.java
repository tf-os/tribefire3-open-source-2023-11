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
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.weaving.ProtoGmMetaModel;
import com.braintribe.model.weaving.ProtoGmType;
import com.braintribe.model.weaving.override.ProtoGmCustomTypeOverride;
import com.braintribe.processing.async.api.AsyncCallback;

/**
 * Pseudo-implementation of {@link GmMetaModel}
 * 
 * @see GenericEntity_pseudo
 * 
 * @author peter.gazdik
 */
public class ProtoGmMetaModelImpl extends GenericEntity_pseudo implements ProtoGmMetaModel {

	private Set<ProtoGmType> types;
	private Set<ProtoGmCustomTypeOverride> typeOverrides;
	private Set<MetaData> metaData;
	private String name;
	private String version;
	private List<ProtoGmMetaModel> dependencies;

	@Override
	public String getVersion() {
		return version;
	}

	@Override
	public void setVersion(String version) {
		this.version = version;
	}

	@Override
	public Set<ProtoGmType> getTypes() {
		return types;
	}

	public void setTypes(Set<ProtoGmType> types) {
		this.types = types;
	}

	@Override
	public Set<ProtoGmCustomTypeOverride> getTypeOverrides() {
		return typeOverrides;
	}

	public void setTypeOverrides(Set<ProtoGmCustomTypeOverride> typeOverrides) {
		this.typeOverrides = typeOverrides;
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
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public List<ProtoGmMetaModel> getDependencies() {
		return dependencies;
	}

	public void setDependencie(List<ProtoGmMetaModel> dependencies) {
		this.dependencies = dependencies;
	}

	@Override
	public void deploy() {
		throw new UnsupportedOperationException("Method 'ProtoGmMetaModelImpl.deploy' is not supported!");
	}

	@Override
	public void deploy(AsyncCallback<Void> asyncCallack) {
		throw new UnsupportedOperationException("Method 'ProtoGmMetaModelImpl.deploy' is not supported!");
	}

}
