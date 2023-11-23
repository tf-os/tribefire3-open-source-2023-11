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

import java.util.Objects;

import com.braintribe.utils.stream.blocks.Block;

/**
 * This {@link BlockPool} uses a {@link GrowingBlockPool} as a delegate but might decide to {@link Block#destroy()} a block upon
 * push instead of returning it to its delegate if it decides that there are too many unused blocks.
 * That way unused resources can be freed and acquired again later if needed.
 * 
 * @author Neidhart.Orlich
 *
 */
public class DynamicBlockPool extends BlockPool {

	private final SafeCounterWithoutLock usedBlockCounter = new SafeCounterWithoutLock();
	private final GrowingBlockPool delegate;

	public DynamicBlockPool(GrowingBlockPool delegate) {
		Objects.requireNonNull(delegate, "DynamicBlockPool can't be created without a delegate.");
		this.delegate = delegate;
	}

	@Override
	public Block get() {
		Block block = delegate.get();

		if (block != null)
			usedBlockCounter.increment();

		return block;
	}

	@Override
	protected void giveBackImpl(Block block) {
		if (tooManyUnusedBlocks()) {
			block.destroy();
			delegate.totalBlockCounter.increment(-1);
		} else {
			delegate.giveBackImpl(block);
		}

		usedBlockCounter.increment(-1);
	}

	private boolean tooManyUnusedBlocks() {
		int total = delegate.totalBlockCounter.getValue();
		int used = usedBlockCounter.getValue();

		return used < total / 2;
	}

	@Override
	protected void shutDownImpl() {
		delegate.shutDown();
	}

}
