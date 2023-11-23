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

import java.io.File;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.function.Supplier;

import com.braintribe.common.lcd.UnreachableCodeException;
import com.braintribe.exception.Exceptions;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.lcd.CommonTools;
import com.braintribe.utils.lcd.Not;

/**
 * Simple provider class that provides a <code>String</code> by reading it from the configured {@link #getUrl() url}.
 *
 * @author michael.lafite
 */
public class SimpleUrlReader implements Supplier<String> {

	private URL url;

	private String encoding;

	public SimpleUrlReader(final URL url) {
		init(url, null);
	}

	public SimpleUrlReader(final URL url, final String encoding) {
		init(url, encoding);
	}

	public SimpleUrlReader(final String urlString) {
		init(urlString, null);
	}

	public SimpleUrlReader(final String urlString, final String encoding) {
		init(urlString, encoding);
	}

	public SimpleUrlReader(final File file) {
		init(file, null);
	}

	public SimpleUrlReader(final File file, final String encoding) {
		init(file, encoding);
	}

	private void init(final Object objectUsedToCreateURL, final String encoding) {
		this.encoding = encoding;

		if (objectUsedToCreateURL instanceof URL) {
			this.url = (URL) objectUsedToCreateURL;
		} else if (objectUsedToCreateURL instanceof String) {
			this.url = IOTools.newUrl((String) objectUsedToCreateURL);
		} else if (objectUsedToCreateURL instanceof File) {
			this.url = FileTools.toURL((File) objectUsedToCreateURL);
		} else {
			throw new UnreachableCodeException(CommonTools.getParametersString("objectUsedToCreateURL", objectUsedToCreateURL));
		}
	}

	public String getEncoding() {
		return this.encoding;
	}

	public void setEncoding(final String encoding) {
		this.encoding = encoding;
	}

	public URL getUrl() {
		return Not.Null(this.url);
	}

	public void setUrl(final URL url) {
		this.url = url;
	}

	@Override
	public String get() throws RuntimeException {
		try {
			return IOTools.urlToString(getUrl(), getEncoding());
		} catch (final UncheckedIOException e) {
			throw Exceptions.unchecked(e, "Error while reading from URL " + getUrl() + "!");
		}
	}

}
