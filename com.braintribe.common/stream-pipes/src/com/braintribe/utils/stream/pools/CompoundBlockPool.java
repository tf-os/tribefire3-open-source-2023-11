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

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.braintribe.utils.lcd.Arguments;
import com.braintribe.utils.stream.api.StreamPipeFactory;
import com.braintribe.utils.stream.blocks.Block;
import com.braintribe.utils.stream.stats.StreamPipeBlockStats;

/**
 * 
 * Use {@link CompoundBlockPoolBuilder} to construct an instance.
 * 
 * @author Neidhart.Orlich
 *
 */
public class CompoundBlockPool implements StreamPipeFactory {

	private final ReferenceQueue<? super BlockBackedPipe> pipeReferenceQueue = new ReferenceQueue<>();
	private final Set<PipePhantomReference> pipePhantomReferences = new HashSet<>();

	private final List<BlockPool> blockPools;

	CompoundBlockPool(List<BlockPool> blockPools) {
		Arguments.notNullWithNames("blockPools", blockPools);

		this.blockPools = blockPools;

	}

	protected Supplier<Block> blockSupplier() {
		Iterator<BlockPool> iterator = blockPools.iterator();

		return () -> {
			reclaimUnusedBlocks();
			
			while (iterator.hasNext()) {
				Block block = iterator.next().get();
				if (block != null)
					return block;
			}
			
			throw new IllegalStateException("Could not retrieve a new block");
		};
	}

	/**
	 * Creates a {@link BlockBackedPipe} that uses the blocks from the contained {@link BlockPool}s 
	 */
	@Override
	public BlockBackedPipe newPipe(String name, int autoBufferSize) {
		BlockSequence blockSequence = new BlockSequence(blockSupplier());
		BlockBackedPipe pipe = new BlockBackedPipe(blockSequence, name, autoBufferSize);
		pipePhantomReferences.add(new PipePhantomReference(pipe, blockSequence));

		return pipe;
	}

	private void reclaimUnusedBlocks() {
		PipePhantomReference phantomRef;
		while ((phantomRef = (PipePhantomReference) pipeReferenceQueue.poll()) != null) {
			phantomRef.release();
			pipePhantomReferences.remove(phantomRef);
		}
	}
	
	/**
	 * Shuts down all contained {@link BlockPool}s and frees their {@link Block}s' resources if applicable
	 * 
	 * @see BlockPool#shutDown()
	 */
	public void shutdown() {
		blockPools.forEach(BlockPool::shutDown);
	}

	private class PipePhantomReference extends PhantomReference<BlockBackedPipe> {
		private final BlockSequence blockSequence;

		public PipePhantomReference(BlockBackedPipe referent, BlockSequence blockSequence) {
			super(referent, pipeReferenceQueue);
			this.blockSequence = blockSequence;
		}

		public void release() {
			blockSequence.asIterable().forEach(Block::free);
		}
	}

	public List<StreamPipeBlockStats> calculateStats() {
		return blockPools.stream().map(BlockPool::getStats).collect(Collectors.toList());
	}
	
}
