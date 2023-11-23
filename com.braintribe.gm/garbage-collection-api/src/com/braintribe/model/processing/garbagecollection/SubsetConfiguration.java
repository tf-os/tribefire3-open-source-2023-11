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
package com.braintribe.model.processing.garbagecollection;

import com.braintribe.gwt.utils.genericmodel.EntitiesFinder;

/**
 * Holds all info/tools required to perform a {@link GarbageCollection GarbageCollection} on a particular subset of the
 * set of persisted entities.
 *
 * @author michael.lafite
 */
public class SubsetConfiguration {

	private String subsetId;

	private EntitiesFinder rootEntitiesFinder;

	private EntitiesFinder subsetEntitiesFinder;

	public SubsetConfiguration(final String subsetId, final EntitiesFinder rootEntitiesFinder,
			final EntitiesFinder subsetEntitiesFinder) {
		setSubsetId(subsetId);
		setRootEntitiesFinder(rootEntitiesFinder);
		setSubsetEntitiesFinder(subsetEntitiesFinder);
	}

	public String getSubsetId() {
		return this.subsetId;
	}

	public void setSubsetId(final String subsetId) {
		this.subsetId = subsetId;
	}

	public EntitiesFinder getRootEntitiesFinder() {
		return this.rootEntitiesFinder;
	}

	public void setRootEntitiesFinder(final EntitiesFinder rootEntitiesFinder) {
		this.rootEntitiesFinder = rootEntitiesFinder;
	}

	public EntitiesFinder getSubsetEntitiesFinder() {
		return this.subsetEntitiesFinder;
	}

	public void setSubsetEntitiesFinder(final EntitiesFinder subsetEntitiesFinder) {
		this.subsetEntitiesFinder = subsetEntitiesFinder;
	}

}
