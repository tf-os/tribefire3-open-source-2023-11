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
package com.braintribe.model.processing.elasticsearch.indexing;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.Property;

public class IndexingPackage {

	private Map<GenericEntity, List<Property>> entitiesToIndex = new HashMap<>();
	private String typeSignature;
	private String packageId;
	private Runnable callback;
	private String state;
	private String accessId;

	public IndexingPackage(String accessId, String typeSignature) {
		this.accessId = accessId;
		this.typeSignature = typeSignature;

		this.packageId = UUID.randomUUID().toString();
	}

	public void addIndexableEntity(GenericEntity ge, List<Property> properties) {
		this.entitiesToIndex.put(ge, properties);
	}

	public Integer getPackageSize() {
		return this.entitiesToIndex.size();
	}

	public Map<GenericEntity, List<Property>> getEntitiesToIndex() {
		return entitiesToIndex;
	}

	public void setCallback(Runnable callback) {
		this.callback = callback;
	}

	public Runnable getCallback() {
		return callback;
	}

	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder();
		String separator = ", ";

		sb.append("Package (Id: ");
		sb.append(this.packageId);
		sb.append(separator);
		sb.append("Access Id: ");
		sb.append(this.accessId);
		sb.append(separator);
		sb.append("Type Signature: ");
		sb.append(this.typeSignature);
		sb.append(separator);
		sb.append("Size of package: ");
		sb.append(this.entitiesToIndex.size());

		if (this.state != null) {
			sb.append(separator);
			sb.append("State: ");
			sb.append(this.state);
		}
		sb.append(")");

		return sb.toString();
	}

	public void close(String state) {

		if (callback != null) {
			callback.run();
		}

		this.state = state;

	}

	public String getAccessId() {
		return accessId;
	}

}
