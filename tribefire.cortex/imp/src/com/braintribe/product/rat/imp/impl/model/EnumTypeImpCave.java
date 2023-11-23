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

import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

/**
 * A {@link AbstractCustomTypeImpCave} specialized in {@link GmEntityType}
 */
public class EnumTypeImpCave extends AbstractCustomTypeImpCave<EnumTypeImp, GmEnumType> {

	EnumTypeImpCave(PersistenceGmSession session) {
		super(session, GmEnumType.T);
	}

	@Override
	protected EnumTypeImp buildImp(GmEnumType instance) {
		return new EnumTypeImp(session(), instance);
	}
}
