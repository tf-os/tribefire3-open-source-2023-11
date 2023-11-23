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
package com.braintribe.model.resource.utils;

import static com.braintribe.utils.lcd.CollectionTools2.asSet;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Function;

import com.braintribe.common.lcd.function.CheckedConsumer;
import com.braintribe.common.lcd.function.CheckedSupplier;
import com.braintribe.exception.Exceptions;
import com.braintribe.model.generic.session.InputStreamProvider;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.api.ResourceWriterBuilder;
import com.braintribe.model.resource.specification.ResourceSpecification;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.io.CharsetWriterBuilder;
import com.braintribe.utils.io.WriterBuilder;
import com.braintribe.utils.stream.api.StreamPipe;
import com.braintribe.utils.stream.api.StreamPipeFactory;

/**
 * {@link WriterBuilder} implementation for writing to a {@link Resource}.
 * 
 * @author peter.gazdik
 */
public class BasicResourceWriterBuilder implements ResourceWriterBuilder {

	private final StreamPipeFactory streamPipeFactory;

	private String name = "StreamPipe[ReseourceWriterBuilder]" + System.identityHashCode(this);
	private String charsetName = StandardCharsets.UTF_8.name();
	private Function<InputStreamProvider, Resource> resourceFactory = Resource::createTransient;

	private String mimeType;
	private Set<String> tags;
	private String md5;
	private Long fileSize;
	private Date created;
	private String creator;
	private ResourceSpecification specification;

	public BasicResourceWriterBuilder(StreamPipeFactory streamPipeFactory) {
		this.streamPipeFactory = streamPipeFactory;
	}

	public BasicResourceWriterBuilder setResourceFactory(Function<InputStreamProvider, Resource> resourceFactory) {
		this.resourceFactory = resourceFactory;
		return this;
	}

	@Override
	public CharsetWriterBuilder<Resource> withCharset(String charsetName) {
		this.charsetName = charsetName;
		return this;
	}

	@Override
	public CharsetWriterBuilder<Resource> withCharset(Charset charset) {
		this.charsetName = charset.name();
		return this;
	}

	/**
	 * The name is used as the name of the underlying pipe, i.e. it's passed to {@link StreamPipeFactory#newPipe(String)} and also will be the name of
	 * the resulting {@link Resource}.
	 */
	@Override
	public ResourceWriterBuilder withName(String name) {
		this.name = name;
		return this;
	}

	@Override
	public ResourceWriterBuilder withMimeType(String mimeType) {
		this.mimeType = mimeType;
		return this;
	}

	@Override
	public ResourceWriterBuilder withTags(String... tags) {
		this.tags = asSet(tags);
		return this;
	}

	@Override
	public ResourceWriterBuilder withMd5(String md5) {
		this.md5 = md5;
		return this;
	}

	@Override
	public ResourceWriterBuilder withFileSize(Long fileSize) {
		this.fileSize = fileSize;
		return this;
	}

	@Override
	public ResourceWriterBuilder withCreated(Date created) {
		this.created = created;
		return this;
	}

	@Override
	public ResourceWriterBuilder withCreator(String creator) {
		this.creator = creator;
		return this;
	}

	@Override
	public ResourceWriterBuilder withSpecification(ResourceSpecification specification) {
		this.specification = specification;
		return this;
	}

	@Override
	public Resource bytes(byte[] bytes) {
		return usingOutputStream(os -> os.write(bytes));
	}

	@Override
	public Resource lines(Iterable<? extends CharSequence> lines) {
		return usingWriter(writer -> writeLines(writer, lines));
	}

	private void writeLines(Writer writer, Iterable<? extends CharSequence> lines) throws IOException {
		Iterator<? extends CharSequence> it = lines.iterator();
		while (it.hasNext()) {
			writer.append(it.next());
			if (it.hasNext())
				writer.append("\n");
		}
	}

	@Override
	public Resource string(String string) {
		return usingWriter(w -> w.write(string));
	}

	@Override
	public Resource fromInputStreamFactory(CheckedSupplier<InputStream, ? extends Exception> isSupplier) {
		return fromInputStreamFactory(isSupplier, e -> {
			throw Exceptions.unchecked(e, "Error while writing resource with name: " + name);
		});
	}

	@Override
	public Resource fromInputStream(InputStream is) {
		return usingOutputStream(os -> IOTools.pump(is, os));
	}

	@Override
	public Resource usingOutputStream(CheckedConsumer<OutputStream, IOException> outputStreamConsumer) throws UncheckedIOException {
		StreamPipe pipe = streamPipeFactory.newPipe(name);

		try (OutputStream os = pipe.openOutputStream()) {
			outputStreamConsumer.accept(os);

		} catch (IOException e) {
			throw new RuntimeException("Error while writing resource: " + name, e);
		}

		return enrichResource(resourceFactory.apply(pipe::openInputStream));
	}

	@Override
	public Resource usingWriter(CheckedConsumer<Writer, IOException> writerConsumer) {
		StreamPipe pipe = streamPipeFactory.newPipe(name);

		try (OutputStream os = pipe.openOutputStream(); //
				Writer writer = writer(os)) {

			writerConsumer.accept(writer);

		} catch (IOException e) {
			throw new RuntimeException("Error while writing resource: " + name, e);
		}

		return enrichResource(resourceFactory.apply(pipe::openInputStream));
	}

	private Writer writer(OutputStream os) {
		return new BufferedWriter(new OutputStreamWriter(os, Charset.forName(charsetName)));
	}

	private Resource enrichResource(Resource resource) {
		resource.setName(name);
		resource.setCreated(created != null ? created : new Date());

		if (mimeType != null)
			resource.setMimeType(mimeType);
		if (tags != null)
			resource.setTags(tags);
		if (md5 != null)
			resource.setMd5(md5);
		if (fileSize != null)
			resource.setFileSize(fileSize);
		if (creator != null)
			resource.setCreator(creator);
		if (specification != null)
			resource.setSpecification(specification);

		return resource;
	}

}
