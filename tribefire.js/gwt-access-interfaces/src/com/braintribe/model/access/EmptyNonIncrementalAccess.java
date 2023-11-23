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
package com.braintribe.model.access;

import java.util.Collections;
import java.util.function.Supplier;

import com.braintribe.model.meta.GmMetaModel;

/**
 * A {@link NonIncrementalAccess} that doesn't actually persist any data.
 * 
 * It can be used either as a base for an actual implementation, or as an actual value, serving the role of a transient
 * {@link NonIncrementalAccess} (one that does not load/store anything).
 *
 * @author peter.gazdik
 */
public class EmptyNonIncrementalAccess implements NonIncrementalAccess {

	private Supplier<GmMetaModel> metaModelSupplier;

	public EmptyNonIncrementalAccess() {
		// nothing to do
	}

	public EmptyNonIncrementalAccess(GmMetaModel metaModel) {
		this(() -> metaModel);
	}

	public EmptyNonIncrementalAccess(Supplier<GmMetaModel> metaModelSupplier) {
		this.metaModelSupplier = metaModelSupplier;
	}

	public void setMetaModel(GmMetaModel metaModel) {
		setMetaModelSupplier(() -> metaModel);
	}

	public void setMetaModelSupplier(Supplier<GmMetaModel> metaModelSupplier) {
		this.metaModelSupplier = metaModelSupplier;
	}

	@Override
	public GmMetaModel getMetaModel() {
		return metaModelSupplier.get();
	}

	@Override
	public Object loadModel() throws ModelAccessException {
		return Collections.emptySet(); // just in case the caller expects a non-null value
	}

	@Override
	public void storeModel(Object model) throws ModelAccessException {
		// nothing to do
	}

}
