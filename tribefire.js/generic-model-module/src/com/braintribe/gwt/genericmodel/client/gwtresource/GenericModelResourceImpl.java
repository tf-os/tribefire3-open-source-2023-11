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
package com.braintribe.gwt.genericmodel.client.gwtresource;

import com.braintribe.codec.CodecException;
import com.braintribe.gwt.genericmodel.client.codec.dom4.GmXmlCodec;

public abstract class GenericModelResourceImpl implements GenericModelResource {
	protected abstract String getText();

	private final GenericModelResourceFormat format;

	private GmXmlCodec<Object> codec; // Never assigned? WTF?

	public GenericModelResourceImpl(GenericModelResourceFormat format) {
		this.format = format;
	}

	@Override
	public <T> T getAssembly() {
		switch (format) {
			case xml:
			default:
				return (T) getXmlAssembly();
		}
	}

	protected <T> T getXmlAssembly() {
		try {
			return (T) codec.decode(getText());

		} catch (CodecException e) {
			throw new RuntimeException("error while decoding GenericModel XML for resource " + getName(), e);
		}
	}

	protected <T> T getJsonAssembly() {
		throw new UnsupportedOperationException();
	}
}
