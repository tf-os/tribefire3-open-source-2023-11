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

import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

/**
 * A {@link SimpleTypeImp} specialized in {@link GmEnumType}
 */
public class EnumTypeImp extends SimpleTypeImp<GmEnumType> {

	EnumTypeImp(PersistenceGmSession session, GmEnumType gmType) {
		super(session, gmType);
	}

	/**
	 * creates new EnumConstants with provided names and adds them to the EnumType managed by this imp
	 */
	public EnumTypeImp addConstants(String... constantNames) {

		for (String constantName : constantNames) {
			GmEnumConstant constant = session().create(GmEnumConstant.T);
			constant.setDeclaringType(this.instance);
			constant.setName(constantName);

			this.instance.getConstants().add(constant);
		}

		return this;
	}

	/**
	 * removes all constants with names equal to the passed ones from the EnumType managed by this imp
	 */
	public EnumTypeImp removeConstants(String... constantNames) {
		for (String constantName : constantNames) {
			instance.getConstants().removeIf(c -> constantName.equals(c.getName()));
		}

		return this;
	}

}
