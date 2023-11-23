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
package com.braintribe.product.rat.imp.impl.model;

import java.util.Collection;

import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.product.rat.imp.GenericMultiImp;

/**
 * A {@link GenericMultiImp} specialized in {@link GmMetaModel}
 */
public class MultiModelImp extends GenericMultiImp<GmMetaModel, ModelImp> {
	private final Collection<ModelImp> modelImps;

	public MultiModelImp(PersistenceGmSession session, Collection<ModelImp> modelImps) {
		super(session, modelImps);
		this.modelImps = modelImps;
	}

	/**
	 * {@link ModelImp#deleteRecursively()} every model managed by this imp
	 */
	public void deleteRecursively() {
		modelImps.forEach(imp -> imp.deleteRecursively());
	}

}
