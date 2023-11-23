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
package com.braintribe.model.access.impl;

import java.net.URL;
import java.util.function.Supplier;

import org.w3c.dom.Document;

import com.braintribe.cfg.Required;
import com.braintribe.codec.dom.genericmodel.GenericModelRootDomCodec;
import com.braintribe.model.generic.GMF;

import com.braintribe.utils.xml.XmlTools;

public class XmlBasedModelDataProvider<T> implements Supplier<T> {
	private URL url;
	private GenericModelRootDomCodec<Object> codec;

	protected GenericModelRootDomCodec<Object> getCodec() {
		if (this.codec == null) {
			this.codec = new GenericModelRootDomCodec<Object>();
			this.codec.setType(GMF.getTypeReflection().getBaseType());
		}

		return this.codec;
	}

	@Required
	public void setUrl(final URL url) {
		this.url = url;
	}

	@Override
	public T get() throws RuntimeException {
		try {
			final Document document = XmlTools.loadXML(this.url);
			final Object data = getCodec().decode(document);
			@SuppressWarnings("unchecked")
			final T castedData = (T) data;
			return castedData;
		} catch (final Exception e) {
			throw new RuntimeException("error while loading or decoding document from file", e);
		}
	}
}
