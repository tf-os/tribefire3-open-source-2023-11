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
package com.braintribe.web.multipart.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;

import com.braintribe.exception.Exceptions;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.stream.BasicDelegateOutputStream;
import com.braintribe.utils.stream.api.PipeStatus;
import com.braintribe.utils.stream.api.StreamPipe;
import com.braintribe.utils.stream.api.StreamPipeFactory;
import com.braintribe.utils.stream.api.StreamPipes;
import com.braintribe.utils.stream.file.FileBackedPipe;
import com.braintribe.web.multipart.api.FormDataWriter;
import com.braintribe.web.multipart.api.PartHeader;
import com.braintribe.web.multipart.api.PartWriter;

/**
 * as long as only one part is written at a time we can use physical part writing an avoid buffering if more than one
 * part is written at a time the additional parts can be backed in FileBackedPipe and being sequentially physically
 * written after the single physical part has been closed
 * 
 * @author Dirk Scheffler
 * @author Neidhart.Orlich
 */
public class SequentialParallelFormDataWriter implements FormDataWriter {
	private final FormDataWriter formDataWriter;
	private BufferedPartWriter currentBufferedPart;
	private SelfReleasingOutputStream currentPartOut;
	private final Queue<BufferedPartWriter> bufferedParts = new ConcurrentLinkedQueue<>();
	private boolean closed = false;
	private final Collection<Future<?>> bufferedPartWriterWaitingFutures = new ArrayList<>();
	private StreamPipeFactory streamPipeFactory;

	/**
	 * @deprecated Please use {@link #SequentialParallelFormDataWriter(FormDataWriter, StreamPipeFactory)} to explicitly specify a {@link StreamPipeFactory}
	 */
	@Deprecated
	public SequentialParallelFormDataWriter(FormDataWriter formDataWriter) {
		this(formDataWriter, StreamPipes.simpleFactory());
	}
	
	public SequentialParallelFormDataWriter(FormDataWriter formDataWriter, StreamPipeFactory streamPipeFactory) {
		super();
		this.formDataWriter = formDataWriter;
		this.streamPipeFactory = streamPipeFactory;
	}

	@Override
	public void close() throws Exception {
		synchronized (this) {
			if (closed)
				return;

			closed = true;
		}

		// Throw an exception if the currently opened part is not buffered
		if (currentPartOut != null && currentBufferedPart == null) {
			throw new IllegalStateException("Can't close FormDataWriter as a part is still open.");
		}
		
		// Throw an exception if there is still some input expected in any of the not written parts
		// (-> the user did not call the close() method on all of them)
		boolean pipesNotCompleted = bufferedParts.stream() //
			.anyMatch(p -> p.streamPipe.getStatus() != PipeStatus.completed);

		if (pipesNotCompleted) {
			throw new IllegalStateException("Can't close FormDataWriter because at least one part is not closed yet.");
		}
		
		// Wait until all buffered parts finished
		for (Future<?> future : bufferedPartWriterWaitingFutures) {
			future.get();
		}

		formDataWriter.close();
	}

	@Override
	public PartWriter openPart(PartHeader header) throws IOException {
		synchronized (this) {
			if (closed) {
				throw new IllegalStateException("Can't open new part because FormDataWriter is already closed.");
			}
			
			if (currentPartOut == null) {
				SelfReleasingPartWriter currentPartWriter = new SelfReleasingPartWriter(formDataWriter.openPart(header));
				currentPartOut = currentPartWriter.outputStream();
				return currentPartWriter;
			} else {
				StreamPipe fileBackedPipe = streamPipeFactory.newPipe(header.getName());
				BufferedPartWriter bufferedPartWriter = new BufferedPartWriter(header, fileBackedPipe);

				bufferedParts.add(bufferedPartWriter);
				return bufferedPartWriter;
			}
		}
	}

	public void setStreamPipeFactory(StreamPipeFactory streamPipeFactory) {
		this.streamPipeFactory = streamPipeFactory;
	}

	private class BufferedPartWriter extends DelegatingPartHeader implements PartWriter {
		StreamPipe streamPipe;
		CompletableFuture<Void> future = new CompletableFuture<>();

		public BufferedPartWriter(PartHeader delegate, StreamPipe streamPipe) {
			super(delegate);
			this.streamPipe = streamPipe;
			bufferedPartWriterWaitingFutures.add(future);
		}

		@Override
		public OutputStream outputStream() {
			return streamPipe.acquireOutputStream();
		}

		public void writeOut() {
			try (SelfReleasingOutputStream out = new SelfReleasingPartWriter(formDataWriter.openPart(this)).outputStream()) {
				currentPartOut = out;
				IOTools.pump(streamPipe.openInputStream(), out);

			} catch (IOException e) {
				throw Exceptions.unchecked(e, "Could not write buffered part out to actual stream");
			}
			future.complete(null);
		}
	}

	private class SelfReleasingOutputStream extends BasicDelegateOutputStream {
		boolean streamClosed;
		
		public SelfReleasingOutputStream(OutputStream delegate) {
			super(delegate);
		}

		@Override
		public void close() throws IOException {
			synchronized (SequentialParallelFormDataWriter.this) {
				if (streamClosed)
					return;
				
				streamClosed = true; 
				
				super.close();

				if (bufferedParts.isEmpty()) {
					currentPartOut = null;
					currentBufferedPart = null;
				} else {
					BufferedPartWriter bufferedPart = bufferedParts.poll();
					currentBufferedPart = bufferedPart;
					// TODO: Why does the FileBackedPipe have this method?
					FileBackedPipe.getExecutor().execute(bufferedPart::writeOut);

				}
			}
		}
	}

	private class SelfReleasingPartWriter extends DelegatingPartHeader implements PartWriter {
		private final PartWriter delegate;

		public SelfReleasingPartWriter(PartWriter delegate) {
			super(delegate);
			this.delegate = delegate;
		}

		@Override
		public SelfReleasingOutputStream outputStream() {
			currentPartOut = new SelfReleasingOutputStream(delegate.outputStream());
			return currentPartOut;
		}

	}
}