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
package com.braintribe.codec.marshaller.jse;

import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.codec.marshaller.jse.tree.JsePoolAddress;
import com.braintribe.model.generic.reflection.Property;

public class JsePropertyPreparation {
	public Property property;
	private JsePoolAddress poolAddress;
	private JsePoolAddress ownerPoolAddress;
	private JseEncodingContext encodingContext;
	
	public JsePropertyPreparation(JseEncodingContext encodingContext, JsePoolAddress ownerPoolAddress, Property property) {
		super();
		this.encodingContext = encodingContext;
		this.ownerPoolAddress = ownerPoolAddress;
		this.property = property;
	}

	public JsePoolAddress getPoolAddress() throws MarshallException {
		if (poolAddress == null) {
			poolAddress = encodingContext.aquirePropertyAddress(ownerPoolAddress, property);	
		}
		return poolAddress;
	}
}
