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
package com.braintribe.utils.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import com.braintribe.common.lcd.function.CheckedConsumer;
import com.braintribe.common.lcd.function.CheckedFunction;

/**
 * Fluent API for reading from a file with various options and ability to eat {@link IOException}s, meaning no method throws such a method, everywhere
 * possible it is converted to an {@link UncheckedIOException}.
 * <p>
 * When reading a text file, the default encoding is UTF-8. If the file uses different encoding, specify it with
 * {@link #withCharset(java.nio.charset.Charset)} or {@link #withCharset(String)}.
 * <p>
 * <i>Examples:</i>
 * 
 * <pre>
 * List&lt;String&gt; lines = readerBuilder.withCharset("UTF-8").asLines();
 * 
 * String text = readerBuilder.buffered().withCharset(StandardCharsets.UTF_8).asString();
 * 
 * byte[] bytes = readerBuilder.asBytes();
 * 
 * SomeEntity entity = (SomeEntity) readerBuilder.fromInputStream(jsonMarshaller::unmarshall)
 * </pre>
 * 
 * @author peter.gazdik
 */
public interface ReaderBuilder extends CharsetReaderBuilder {

	byte[] asBytes();

	/**
	 * Allows the client an access to an {@link InputStream} of the underlying "resource" and propagates the result value (i.e. returns the value
	 * which is returned by the given function).
	 */
	<T> T fromInputStream(CheckedFunction<? super InputStream, T, IOException> inputStreamUser);

	/** Allows the client an access to an {@link InputStream} of the underlying "resource". */
	void consumeInputStream(CheckedConsumer<? super InputStream, IOException> inputStreamConsumer);

}
