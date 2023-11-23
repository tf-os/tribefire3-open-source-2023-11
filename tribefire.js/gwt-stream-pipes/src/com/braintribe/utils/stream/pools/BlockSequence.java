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
package com.braintribe.utils.stream.pools;

import java.io.InputStream;
import java.util.function.Supplier;

import com.braintribe.collections.GrowingIterable;
import com.braintribe.utils.stream.SupplierBackedSequenceInputStream;
import com.braintribe.utils.stream.blocks.Block;

/**
 * Growing sequence of blocks
 * 
 * @author Neidhart.Orlich
 *
 */
class BlockSequence {
	private final Supplier<Block> blockPool;
	private final GrowingIterable<Block> memberBlocks = new GrowingIterable<>();

	public BlockSequence(Supplier<Block> blockPool) {
		this.blockPool = blockPool;
	}

	public Block addNewBlock() {
		Block newBlock = blockPool.get();
		memberBlocks.add(newBlock);

		return newBlock;
	}

	/**
	 * New input stream for the data stored in contained blocks that starts at the beginning. Can be called multiple
	 * times. It is possible that {@link InputStream#read()} returns <code>-1</code> but later on again has more data
	 * because a new block was added or further data was written to the last block.
	 */
	public InputStream inputStream() {
		return new SupplierBackedSequenceInputStream(memberBlocks);
	}

	public Iterable<Block> asIterable() {
		return memberBlocks;
	}
}
