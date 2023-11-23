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
package com.braintribe.codec.marshaller;

import java.net.URL;

import com.braintribe.codec.marshaller.api.AbstractGmResourceProvider;
import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.codec.marshaller.stax.StaxMarshaller;


public class GmXmlResourceProvider<E> extends AbstractGmResourceProvider<E> {

	protected URL resource;

	@Override
	public E get() throws RuntimeException {
		try {
			@SuppressWarnings("unchecked")
			E result = (E) getMarshaller().unmarshall(getResource().openStream());
			return result;
		} catch (Exception e) {
			throw new RuntimeException("Error while unmarshalling URL!", e);
		}
	}

	public URL getResource() {
		URL result = this.resource;
		if (result != null) {
			return result;

		}
		throw new IllegalStateException("No resource set! " + GmXmlResourceProvider.class.getSimpleName() + " not initialized properly (yet)!");
	}

	public void setResource(URL resource) {
		this.resource = resource;
	}

	@Override
	protected Marshaller getMarshaller() {
		return StaxMarshaller.defaultInstance;
	}
}
