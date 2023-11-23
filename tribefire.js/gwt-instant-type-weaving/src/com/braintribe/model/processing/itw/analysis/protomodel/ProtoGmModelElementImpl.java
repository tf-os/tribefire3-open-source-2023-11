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

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.weaving.ProtoGmModelElement;

/**
 * Very special class used as a base when we need classes for our entities. One special example is InstantTypeWeaving
 * which needs instances of {@link GmMetaModel} (and co.) as input for actual weaving, so we simply need instances of
 * some entities before we weave the first entity.
 * 
 * NOTE that this base class only supports the two special properties from {@link GenericEntity} and not any other of
 * the inherited methods. Those are also not intended to be implemented in the future.
 * 
 * @author peter.gazdik
 */
public abstract class ProtoGmModelElementImpl implements ProtoGmModelElement {

	private Object id;
	private String partition;
	private String globalId;

	public <T> T getId() {
		return (T) id;
	}

	public void setId(Object id) {
		this.id = id;
	}

	public String getPartition() {
		return partition;
	}

	public void setPartition(String partition) {
		this.partition = partition;
	}

	@Override
	public String getGlobalId() {
		return globalId;
	}

	@Override
	public void setGlobalId(String globalId) {
		this.globalId = globalId;
	}

}
