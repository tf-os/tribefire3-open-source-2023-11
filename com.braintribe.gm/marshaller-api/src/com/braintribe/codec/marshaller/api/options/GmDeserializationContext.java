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
package com.braintribe.codec.marshaller.api.options;

import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.utils.collection.impl.MapAttributeContext;

public class GmDeserializationContext extends MapAttributeContext implements GmDeserializationOptions {
	
	public GmDeserializationContext() {
		super();
	}
	
	public GmDeserializationContext(GmDeserializationContext parent) {
		super(parent);
	}
	
	@Override
	public GmDeserializationContextBuilderImpl derive() {
		return new GmDeserializationContextBuilderImpl(new GmDeserializationContext(this));
	}
}
