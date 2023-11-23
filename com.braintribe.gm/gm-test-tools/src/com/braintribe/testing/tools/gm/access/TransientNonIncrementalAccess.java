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
package com.braintribe.testing.tools.gm.access;

import java.util.Collections;

import com.braintribe.model.access.ModelAccessException;
import com.braintribe.model.access.NonIncrementalAccess;
import com.braintribe.model.meta.GmMetaModel;

/**
 * A {@link NonIncrementalAccess} that doesn't actually persist any data.
 *
 * @author peter.gazdik
 */
public class TransientNonIncrementalAccess implements NonIncrementalAccess {

	private GmMetaModel metaModel;

	public TransientNonIncrementalAccess() {
		// nothing to do
	}

	public TransientNonIncrementalAccess(GmMetaModel metaModel) {
		this.metaModel = metaModel;
	}

	public void setMetaModel(GmMetaModel metaModel) {
		this.metaModel = metaModel;
	}

	@Override
	public GmMetaModel getMetaModel() {
		return metaModel;
	}

	@Override
	public Object loadModel() throws ModelAccessException {
		return Collections.emptySet();
	}

	@Override
	public void storeModel(Object model) throws ModelAccessException {
		// nothing to do
	}

}
