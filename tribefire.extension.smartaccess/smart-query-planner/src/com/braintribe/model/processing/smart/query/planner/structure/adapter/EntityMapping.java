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
package com.braintribe.model.processing.smart.query.planner.structure.adapter;

import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.meta.GmEntityType;

/**
 * 
 */
public class EntityMapping {

	private final GmEntityType smartEntityType;
	private final GmEntityType delegateEntityType;
	private final IncrementalAccess access;
	private final EmUseCase useCase;
	private final DiscriminatedHierarchy discriminatedHierarchy;

	public EntityMapping(GmEntityType smartEntityType, GmEntityType delegateEntityType, DiscriminatedHierarchy discriminatedHierarchy,
			IncrementalAccess access, EmUseCase useCase) {

		this.smartEntityType = smartEntityType;
		this.delegateEntityType = delegateEntityType;
		this.discriminatedHierarchy = discriminatedHierarchy;
		this.access = access;
		this.useCase = useCase;
	}

	public GmEntityType getSmartEntityType() {
		return smartEntityType;
	}

	public GmEntityType getDelegateEntityType() {
		return delegateEntityType;
	}

	public IncrementalAccess getAccess() {
		return access;
	}

	public EmUseCase getUseCase() {
		return useCase;
	}

	public boolean isPolymorphicAssignment() {
		return discriminatedHierarchy != null;
	}
	
	public DiscriminatedHierarchy getDiscriminatedHierarchy() {
		return discriminatedHierarchy;
	}

	// ########################################
	// ## . . . . . . . ToString . . . . . . ##
	// ########################################

	/**
	 * Prints the mapping in the form: "${smartSignature} -> ${mappedDelegateSignature} (${delegateAccess.externalId})"
	 */
	@Override
	public String toString() {
		return smartEntityType.getTypeSignature() + " -> " + delegateEntityType.getTypeSignature() + " (" + access.getExternalId() + ")" +
				useCaseStr();
	}

	private String useCaseStr() {
		return useCase == null ? "" : " [" + useCase + "]";
	}

}
