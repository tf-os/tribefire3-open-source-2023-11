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

import java.util.List;

import com.braintribe.codec.marshaller.jse.tree.JsePoolAddress;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;

public class JseEntityTypePreparation {
	public EntityType<?> type;
	public JsePoolAddress address;
	public JsePropertyPreparation[] preparations;
	private JseEncodingContext context;
	
	public JseEntityTypePreparation(JseEncodingContext context, EntityType<?> type, JsePoolAddress address) {
		this.context = context;
		this.address = address;
		List<Property> properties = type.getProperties();
		preparations = new JsePropertyPreparation[properties.size()];
		int i = 0;
		for (Property property: properties) {
			JsePropertyPreparation propertyPreparation = new JsePropertyPreparation(context, address, property);
			propertyPreparation.property = property;
			preparations[i++] = propertyPreparation;
		}
	}
}
