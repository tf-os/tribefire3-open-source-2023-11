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
package com.braintribe.common;

import java.io.UncheckedIOException;
import java.net.URL;

import com.braintribe.common.lcd.transformer.Transformer;
import com.braintribe.common.lcd.transformer.TransformerException;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.lcd.Arguments;
import com.braintribe.utils.lcd.Not;

/**
 * Transforms a <code>URL</code> to a <code>String</code> containing the content read from the <code>URL</code>.
 *
 * @author michael.lafite
 */
public class UrlToUrlContentStringTransformer implements Transformer<URL, String, Object> {

	private String encoding;

	public UrlToUrlContentStringTransformer() {
		// nothing to do
	}

	public String getEncoding() {
		return this.encoding;
	}

	public void setEncoding(final String encoding) {
		this.encoding = encoding;
	}

	@Override
	public String transform(final URL url, final Object transformationContext) throws TransformerException {
		Arguments.notNull(url, "The passed URL must not be null!");
		try {
			return IOTools.urlToString(Not.Null(url), getEncoding());
		} catch (final UncheckedIOException e) {
			throw new TransformerException("Error while reading from URL " + url + "!", e);
		}
	}
}
