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

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import com.braintribe.utils.stream.blocks.Block;
import com.braintribe.utils.stream.blocks.InMemoryBlock;
import com.braintribe.utils.stream.stats.StaticBlockStats;
import com.braintribe.utils.stream.stats.StreamPipeBlockStats;

/**
 * A supplier of {@link Block}s which can be used by a {@link BlockBackedPipe} to temporarily and internally persist
 * streamed data.
 * 
 * @author Neidhart.Orlich
 *
 */
public abstract class BlockPool implements Supplier<Block>, StreamPipeBlockStats {

	protected boolean shutDown;

	/**
	 * Calling this method removes all {@link Block}s from the pool and {@link Block#destroy() destroy}s them (freeing
	 * their resources if necessary). Blocks returned to the pool with {@link #giveBack(Block)} after shutdown are
	 * destroyed instead. <br>
	 * <b>Note</b> that {@link SoftReferencingBlockPool} currently does not destroy blocks and thus should only be used
	 * together with {@link InMemoryBlock}s which don't have any resources to be freed.
	 */
	public void shutDown() {
		shutDown = true;
		shutDownImpl();
	}

	/**
	 * Use this method to return a block to the pool that was previously retrieved from this pool with {@link #get()}.
	 */
	protected void giveBack(Block block) {
		if (shutDown) {
			block.destroy();
		} else {
			giveBackImpl(block);
		}
	}

	/**
	 * Returns a block from the pool.
	 * 
	 * @return A Block which could be newly created or already previously contained. <code>null</code> is returned when
	 *         the pool is empty and can't create any new blocks.
	 */
	@Override
	public abstract Block get();

	protected abstract void giveBackImpl(Block block);
	protected abstract void shutDownImpl();

	// from https://www.baeldung.com/java-atomic-variables
	// TODO: find or create a braintribe utility class
	public class SafeCounterWithoutLock {
		private final AtomicInteger counter = new AtomicInteger(0);

		public int getValue() {
			return counter.get();
		}

		public void increment() {
			increment(1);
		}

		public void increment(int steps) {
			while (true) {
				int existingValue = getValue();
				int newValue = existingValue + steps;
				if (counter.compareAndSet(existingValue, newValue)) {
					return;
				}
			}
		}
	}

	/**
	 * The total number of bytes that are allocated by all blocks of this pool, currently used or unused.
	 * <p>
	 * Note that it's not possible to track used blocks but a good estimation can be made from inspecting the block
	 * supplier. Currently this is OK because there are no block pools in production that share a supplier but in future
	 * it might be necessary to find a better way to calculate this.
	 */
	@Override
	public abstract long getBytesTotal();
	
	public StreamPipeBlockStats getStats() {
		return new StaticBlockStats(this);
	}
}