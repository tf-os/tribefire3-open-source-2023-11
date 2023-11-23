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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.stream.Stream;

import com.braintribe.common.lcd.function.CheckedConsumer;

/**
 * @see WriterBuilder
 * 
 * @author peter.gazdik
 */
public interface CharsetWriterBuilder<T> {

	/**
	 * This is a hint that the {@link Writer} passed to {@link #usingWriter(CheckedConsumer)} should not be wrapped in an extra buffer.
	 * <p>
	 * This method should be avoided, but in special situations when a concrete implementation is known, one might prefer the lowest-level writer (say
	 * {@link FileWriter}), rather than getting a {@link BufferedWriter}.
	 */
	default CharsetWriterBuilder<T> notBuffered() {
		return this;
	}

	/** Specify the character encoding. Default is UTF-8. */
	CharsetWriterBuilder<T> withCharset(String charsetName);

	/** Specify the character encoding. Default is UTF-8. */
	CharsetWriterBuilder<T> withCharset(Charset charset);

	/**
	 * Specifies the name for the underlying data.
	 * <p>
	 * The actual usage for the name is implementation specific, it can be used to derive the name of the written file, or the URL, it can be used
	 * purely for logging, or it can be completely ignored, which is the default behavior defined on this interface directly.
	 * 
	 * @param name
	 *            name to be given to the underlying data, if this method is supported
	 */
	default CharsetWriterBuilder<T> withName(String name) {
		// Optional
		return this;
	}

	T string(String string);

	default T lines(Stream<? extends CharSequence> lines) {
		return lines(((Stream<CharSequence>) (Stream<?>) lines)::iterator);
	}

	T lines(Iterable<? extends CharSequence> lines);

	/** Allows the client an access to a {@link Writer} of the underlying "resource". */
	T usingWriter(CheckedConsumer<Writer, IOException> actualWriter);

}