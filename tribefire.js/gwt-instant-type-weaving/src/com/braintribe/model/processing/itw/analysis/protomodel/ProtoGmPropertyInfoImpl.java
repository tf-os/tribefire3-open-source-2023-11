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

import java.util.Set;

import com.braintribe.model.generic.pseudo.GenericEntity_pseudo;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.weaving.info.ProtoGmPropertyInfo;

/**
 * Pseudo-implementation of {@link GmProperty}
 * 
 * @see GenericEntity_pseudo
 * 
 * @author peter.gazdik
 */
public abstract class ProtoGmPropertyInfoImpl extends GenericEntity_pseudo implements ProtoGmPropertyInfo {

	private Set<MetaData> metaData;
	private Object initializer;

	@Override
	public Set<MetaData> getMetaData() {
		return metaData;
	}

	@Override
	public void setMetaData(Set<MetaData> metaData) {
		this.metaData = metaData;
	}

	@Override
	public Object getInitializer() {
		return initializer;
	}

	@Override
	public void setInitializer(Object initializer) {
		this.initializer = initializer;
	}

}
