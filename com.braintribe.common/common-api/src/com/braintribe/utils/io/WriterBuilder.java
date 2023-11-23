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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.function.Function;
import java.util.function.Supplier;

import com.braintribe.common.lcd.function.CheckedConsumer;
import com.braintribe.common.lcd.function.CheckedSupplier;

/**
 * Fluent API for writing data with various options and ability to eat {@link IOException}s, meaning no method throws such an exception, everywhere
 * possible it is converted to an {@link UncheckedIOException}.
 * <p>
 * The terminal methods that handle the actual data return a descriptor of the data written (represented by the generic type parameter {@code T}).
 * This could for example be a File, Resource (in GM world) or URL.
 * <p>
 * The API is divided into two levels to improve user experience. The moment user chooses a text-file specific option (currently just charset), he
 * will only see methods relevant for a text-file (i.e those defined on {@link CharsetWriterBuilder}). This might be an overkill, but it looked cool
 * initially.
 * <p>
 * The default character encoding when writing text is UTF-8.
 * <p>
 * For implementations see FileTools.write(File) in <tt>platform-api</tt> artifact, and {@code BasicResourceWriterBuilder} in GM codebase.
 * <p>
 * <i>Examples:</i>
 * 
 * <pre>
 * File textFile = fileWriter1.lines(lines);
 * 
 * File jsonFile = fileWriter2.usingOutputStream(os -> jsonMarshaller.marshall(os, entity); // if marshall() throws IOException, it's handled inside
 * </pre>
 * 
 * @param <T>
 *            type of the descriptor for written data, could be File, Resource, URL...
 * 
 * @author peter.gazdik
 */
public interface WriterBuilder<T> extends CharsetWriterBuilder<T> {

	/**
	 * {@inheritDoc}
	 * 
	 * On this level, this semantics of {@link CharsetWriterBuilder#notBuffered()} is extended to {@link #usingOutputStream(CheckedConsumer)} as well.
	 */
	@Override
	default WriterBuilder<T> notBuffered() {
		return this;
	}

	/**
	 * Allows specification of the name for the data written. What exactly the name is used for is implementation specific, the first use-case was
	 * purely for logging.
	 */
	@Override
	default WriterBuilder<T> withName(String name) {
		// Optional
		return this;
	}

	T bytes(byte[] bytes);

	/**
	 * Uses given {@link InputStream} factory to open a new input stream to feed the underlying writer. <b>This method also closes the provided input
	 * stream</b>. {@link IOException}s thrown while closing the stream are re-thrown wrapped in {@link UncheckedIOException}s. For different
	 * exception handling use {@link #fromInputStreamFactory(CheckedSupplier, Function)}
	 * 
	 * @return the denotation for the data written, if all went correctly
	 * 
	 * @throws UncheckedIOException
	 *             if some error occurred while writing the data
	 */
	T fromInputStreamFactory(CheckedSupplier<InputStream, ? extends Exception> isSupplier);

	/**
	 * @see #fromInputStreamFactory(CheckedSupplier)
	 * 
	 * @return the denotation for the underlying resource, if all went correctly, or the result from given {@code errorHandler}
	 */
	default T fromInputStreamFactory(CheckedSupplier<InputStream, ? extends Exception> isSupplier, Function<? super Exception, T> errorHandler) {
		try (InputStream is = isSupplier.get()) {
			return fromInputStream(is);
		} catch (Exception e) {
			return errorHandler.apply(e);
		}
	}

	T fromInputStream(InputStream is);

	/**
	 * Gives the client an access to an {@link OutputStream} of the underlying "resource", which the client is expected to provide the
	 * to-be-written-data using
	 * 
	 * @see #notBuffered()
	 */
	T usingOutputStream(CheckedConsumer<OutputStream, IOException> outputStreamConsumer);

	// #########################################################
	// ## . . . . . . . . Convenience methods . . . . . . . . ##
	// #########################################################

	/** Write data from given {@link File} */
	default T fromFile(File file) {
		try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
			return fromInputStream(is);

		} catch (FileNotFoundException e) {
			throw new UncheckedIOException("File not found: " + file.getAbsolutePath(), e);

		} catch (IOException e) {
			throw new UncheckedIOException("Error while writing resource from: " + file.getAbsolutePath(), e);
		}
	}

	/** Uses supplied {@link InputStream} to feed the underlying writer. <b>The supplied input stream is not closed by this method.</b> */
	default T fromInputStreamSupplier(Supplier<InputStream> isSupplier) {
		return fromInputStream(isSupplier.get());
	}

}