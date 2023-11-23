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
package com.braintribe.utils.stream.blocks;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.braintribe.utils.stream.pools.BlockBackedPipe;
import com.braintribe.utils.stream.pools.BlockPool;

/**
 * A sequence of Blocks is used by a {@link BlockBackedPipe} to temporarily and internally buffer/cache streamed data. It
 * abstracts a way of writing to and reading from (a block of) data ({@link #openOutputStream()}, {@link #get()}) as
 * well as freeing eventual resources ({@link #destroy()}).
 * <p>
 * A Block is created and supplied by a {@link BlockPool}. The Block knows how to return itself to that pool during
 * {@link #free()}.
 * 
 * @author Neidhart.Orlich
 *
 */
abstract public class Block implements Supplier<InputStream> {
	private Consumer<Block> giveBack;

	private long bytesWritten = 0;

	public abstract OutputStream openOutputStream();
	protected abstract InputStream openRawInputStream();
	public abstract int getTreshold();
	public abstract void destroy();
	public abstract void autoBufferInputStreams(int bufferSize);
	public abstract boolean isAutoBuffered();
	public abstract long getBytesAllocated();

	/**
	 * Returns an {@link InputStream} to the data that is currently stored in this block. Repeatedly calling this method
	 * will return new InputStreams that all start from the beginning and can read concurrently in parallel.
	 */
	@Override
	public InputStream get() {
		return new BlockInputStream(this);
	}

	/**
	 * Returns the block to its {@link BlockPool} and makes previously persisted data unaccessible and ready to be overwritten. 
	 */
	public void free() {
		bytesWritten = 0;
		autoBufferInputStreams(0);
		giveBack.accept(this);
	}

	/**
	 * The return consumer is called by the Block to return itself to its original {@link BlockPool}.
	 */
	public void setReturnConsumer(Consumer<Block> giveBack) {
		this.giveBack = giveBack;
	}

	public void notifyBytesWritten(int numBytes) {
		bytesWritten += numBytes;
	}

	public long getBytesWritten() {
		return bytesWritten;
	}
}