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
package com.braintribe.model.resource.source;

import java.io.ByteArrayInputStream;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;

import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.generic.session.InputStreamProvider;

/**
 * A {@link ResourceSource} that stores its data inside an internal String. It can be accessed via InputStream/OutputStream.
 * 
 */

public interface StringSource extends ResourceSource, StreamableSource {

	final EntityType<StringSource> T = EntityTypes.T(StringSource.class);

	String content = "content";

	String getContent();
	void setContent(String content);

	String encoding = "encoding";

	@Initializer("'UTF-8'")
	String getEncoding();
	void setEncoding(String encoding);

	@Override
	default InputStreamProvider inputStreamProvider() {
		return () -> new ByteArrayInputStream(toBytes());
	}

	default byte[] toBytes() {
		String contentData = getContent();
		if (contentData == null)
			return new byte[] {};
		String enc = getEncoding();
		if (enc == null)
			enc = "UTF-8";
		try {
			return contentData.getBytes(enc);
		} catch (UnsupportedEncodingException e) {
			throw new UncheckedIOException(e);
		}
	}
}
