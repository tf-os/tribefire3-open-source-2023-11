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
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Stream;

import com.braintribe.common.lcd.function.CheckedConsumer;
import com.braintribe.common.lcd.function.CheckedFunction;

/**
 * Fluent API for reading from a file with various options and ability to eat {@link IOException}s, meaning no method throws such a method, everywhere
 * possible it is converted to an {@link UncheckedIOException}.
 * 
 * @author peter.gazdik
 */
public interface CharsetReaderBuilder {

	/** Specify the character encoding. Default is UTF-8. */
	CharsetReaderBuilder withCharset(String charsetName);

	/** Specify the character encoding. Default is UTF-8. */
	CharsetReaderBuilder withCharset(Charset charset);

	String asString();

	List<String> asLines();

	Stream<String> asLineStream();

	/**
	 * Allows the client an access to an {@link Reader} of the underlying "resource" and propagates the result value (i.e. returns the value which is
	 * returned by the given function).
	 */
	<T> T fromReader(CheckedFunction<? super Reader, T, IOException> readerUser);

	/** Allows the client an access to a {@link Reader} of the underlying "resource", using UTF-8 as char encoding. */
	void consumeReader(CheckedConsumer<? super Reader, IOException> readerConsumer);

	default void toWriter(Writer writer) {
		toWriter(writer, true);
	}

	default void toWriter(Writer writer, boolean flush) {
		consumeReader(reader -> {
			final int SIZE_64K = 1 << 16;
			char[] buffer = new char[SIZE_64K];

			int c = -1;
			while ((c = reader.read(buffer)) != -1)
				writer.write(buffer, 0, c);

			if (flush)
				writer.flush();
		});

	}
}
