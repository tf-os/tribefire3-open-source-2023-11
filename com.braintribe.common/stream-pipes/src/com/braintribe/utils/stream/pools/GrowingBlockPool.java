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

import java.io.File;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.braintribe.utils.FileTools;
import com.braintribe.utils.stream.blocks.Block;
import com.braintribe.utils.stream.pools.CompoundBlockPoolBuilder.FileBlockSupplier;
import com.braintribe.utils.stream.pools.CompoundBlockPoolBuilder.InMemoryBlockSupplier;
import com.braintribe.utils.stream.stats.BlockKind;
import com.braintribe.utils.stream.stats.PoolKind;


/**
 * A {@link BlockPool} that is initially empty and grows until a certain maximum number of blocks which will never be destroyed until {@link #shutDown()} 
 * 
 * @author Neidhart.Orlich
 *
 */
public class GrowingBlockPool extends BlockPool {
	protected final int maxBlocks;

	private final Queue<Block> blocks = new ConcurrentLinkedQueue<>();
	private final Supplier<Block> blockSupplier;

	protected final SafeCounterWithoutLock totalBlockCounter = new SafeCounterWithoutLock();
	
	/**
	 * @param blockSupplier used to construct a new block
	 * @param maxNumBlocks maximum total number of blocks that can be <i>created</i> by this pool. This is not related to the number of blocks currently <i>contained</i>
	 */
	public GrowingBlockPool(Supplier<Block> blockSupplier, int maxNumBlocks) {
		
		this.blockSupplier = blockSupplier;
		this.maxBlocks = maxNumBlocks;
		
	}

	@Override
	public Block get() {
		Block block = blocks.poll();
		
		if (block == null && !shutDown) {
			if (maxBlocks > 0 && totalBlockCounter.getValue() >= maxBlocks) {
				return null;
			}
			
			block = blockSupplier.get();
			block.setReturnConsumer(this::giveBack);
			totalBlockCounter.increment();
		}
		
		return block;
	}
	
	@Override
	protected void giveBackImpl(Block block) {
		blocks.add(block);
	}
	
	@Override
	protected void shutDownImpl() {
		blocks.forEach(block -> {
			block.destroy(); 
			blocks.remove(block);
		});
	}

	@Override
	public int getNumUnused() {
		return blocks.size();
	}

	@Override
	public int getNumTotal() {
		return totalBlockCounter.getValue();
	}

	@Override
	public long getBytesUnused() {
		return blocks.stream().collect(Collectors.summingLong(Block::getBytesAllocated));
	}

	@Override
	public long getBytesTotal() {
		if (blockSupplier instanceof FileBlockSupplier) {
			File blockFileRootDir = ((FileBlockSupplier) blockSupplier).getBlockFileRootDir();
			return FileTools.getFolderSizeInBytes(blockFileRootDir.toPath());
		} 
		else if (blockSupplier instanceof InMemoryBlockSupplier) {
			return getNumTotal() * getBlockSize();
		}
		
		return 0;
	}
	
	@Override
	public int getMaxBlocksAllocatable() {
		return maxBlocks;
	}
	
	@Override
	public int getBlockSize() {
		switch (getBlockKind()) {
			case file:
				return -1;
			case inMemory:
				return ((InMemoryBlockSupplier) blockSupplier).getBlockSize();
			default:
				throw new IllegalStateException("Unexpected block type: " + getBlockKind());
		}
	}
	
	@Override
	public BlockKind getBlockKind() {
		if (blockSupplier instanceof FileBlockSupplier) {
			return BlockKind.file;
		} 
		else if (blockSupplier instanceof InMemoryBlockSupplier) {
			return BlockKind.inMemory;
		}
		
		throw new IllegalStateException("Unexpected block supplier: " + blockSupplier);
	}

	@Override
	public String getLocation() {
		if (blockSupplier instanceof FileBlockSupplier) {
			return ((FileBlockSupplier) blockSupplier).getBlockFileRootDir().getAbsolutePath();
		} 
		
		return null;
	}

	@Override
	public PoolKind getPoolKind() {
		return PoolKind.growing;
	}
	
}
