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
package com.braintribe.model.openapi.v3_0.reference;

import com.braintribe.model.openapi.v3_0.reference.JsonReferenceOptimizer.OptimizationResult;

/**
 * This exception is just to signal that a Reference can't be created because it is not valid in the current
 * {@link ReferenceRecyclingContext}. This means that eventual referencing parents can't be created as well and the
 * current evaluation of the reference tree should be aborted.
 * <p>
 * This is not a classical Exception and should never be reported to the user. Note also that no stack trace is created.
 *
 * @see ReferenceRecycler#isValidInContext(ReferenceRecyclingContext)
 * @author Neidhart.Orlich
 *
 */
public class CantBuildReferenceException extends RuntimeException {
	private static final long serialVersionUID = 1997812406963071558L;
	@SuppressWarnings("unused") // useful for debugging
	private final ReferenceRecyclingContext<?> context;
	@SuppressWarnings("unused") // useful for debugging
	private final ReferenceRecycler<?, ?> referenceRecycler;
	@SuppressWarnings("unused") // useful for debugging
	private final OptimizationResult optimizationResult;

	public CantBuildReferenceException(ReferenceRecyclingContext<?> context, ReferenceRecycler<?, ?> referenceRecycler,
			OptimizationResult optimizationResult) {
		super("Can't provide component '" + referenceRecycler.getContextUnawareRefString() + "' for context " + context.contextDescription()
				+ " nor any child and parent contexts.", null, false, false);
		this.context = context;
		this.referenceRecycler = referenceRecycler;
		this.optimizationResult = optimizationResult;
	}

}
