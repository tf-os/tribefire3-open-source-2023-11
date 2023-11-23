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
package com.braintribe.codec.marshaller.dom.coder.collection;

import java.util.ArrayList;
import java.util.List;

import com.braintribe.codec.marshaller.dom.coder.DomCoder;

public class ListDomCoder<T> extends CollectionDomCoder<T, List<T>> {
	
	public ListDomCoder(DomCoder<T> elementCoder) {
		super(elementCoder, "L");
	}
	public ListDomCoder(DomCoder<T> elementCoder, boolean returnNullOnEmptyCollection) {
		super(elementCoder, "L", returnNullOnEmptyCollection);
	}
	
	@Override
	protected List<T> createCollection() {
		return new ArrayList<T>();
	}

}
