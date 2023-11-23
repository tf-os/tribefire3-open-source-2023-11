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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import com.braintribe.common.lcd.function.CheckedConsumer;
import com.braintribe.common.lcd.function.CheckedSupplier;
import com.braintribe.utils.IOTools;

/**
 * @author peter.gazdik
 */
public class FlushingOutputStreamWriterBuilder implements WriterBuilder<OutputStream> {

	private final OutputStream out;
	private final String outDescriptor;
	private String charsetName = StandardCharsets.UTF_8.name();

	/**
	 * @param out
	 *            {@link OutputStream} we write to
	 * @param outDescriptor
	 *            text descriptor of given {@link OutputStream} used for exception messages
	 */
	public FlushingOutputStreamWriterBuilder(OutputStream out, String outDescriptor) {
		this.out = out;
		this.outDescriptor = outDescriptor;
	}

	@Override
	public CharsetWriterBuilder<OutputStream> withCharset(String charsetName) {
		this.charsetName = charsetName;
		return this;
	}

	@Override
	public CharsetWriterBuilder<OutputStream> withCharset(Charset charset) {
		this.charsetName = charset.name();
		return this;
	}

	@Override
	public OutputStream string(String string) {
		return usingWriter(w -> w.write(string));
	}

	@Override
	public OutputStream lines(Iterable<? extends CharSequence> lines) {
		return usingWriter(writer -> writeLines(writer, lines));
	}

	private void writeLines(Writer writer, Iterable<? extends CharSequence> lines) throws IOException {
		Iterator<? extends CharSequence> it = lines.iterator();
		while (it.hasNext()) {
			writer.append(it.next());
			if (it.hasNext()) {
				writer.append("\n");
			}
		}
	}

	@Override
	public OutputStream usingWriter(CheckedConsumer<Writer, IOException> actualWriter) {
		try {
			Writer writer = writer(out);
			actualWriter.accept(writer);
			writer.flush();
			return out;

		} catch (IOException e) {
			throw new RuntimeException("Error while writing " + outDescriptor, e);
		}
	}

	private Writer writer(OutputStream os) {
		return new BufferedWriter(new OutputStreamWriter(os, Charset.forName(charsetName)));
	}

	@Override
	public OutputStream bytes(byte[] bytes) {
		try {
			out.write(bytes);
			return flush();

		} catch (IOException e) {
			throw new RuntimeException("Error while writing " + outDescriptor, e);
		}
	}

	@Override
	public OutputStream fromInputStreamFactory(CheckedSupplier<InputStream, ? extends Exception> isSupplier) {
		return fromInputStreamFactory(isSupplier, e -> {
			throw new RuntimeException("Error while writing " + outDescriptor, e);
		});
	}

	@Override
	public OutputStream fromInputStream(InputStream is) {
		return usingOutputStream(os -> IOTools.pump(is, os));
	}

	@Override
	public OutputStream usingOutputStream(CheckedConsumer<OutputStream, IOException> outputStreamConsumer) {
		try {
			outputStreamConsumer.accept(out);
			return flush();

		} catch (IOException e) {
			throw new RuntimeException("Error while writing " + outDescriptor, e);
		}
	}

	private OutputStream flush() throws IOException {
		out.flush();
		return out;
	}

}
