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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.HashSet;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;

import com.braintribe.utils.stream.blocks.Block;
import com.braintribe.utils.stream.pools.CompoundBlockPoolBuilder.InMemoryBlockSupplier;

public class SoftReferencedBlockPoolTest extends AbstractBlockPoolTest {
	ReferenceQueue<Block> blockRefs;

	@Override
	BlockPool blockPool() {
		blockRefs = new ReferenceQueue<>();
		return new SoftReferencingBlockPool(new InMemoryBlockSupplier(1), poolSize(), blockRefs);
	}

	/**
	 * Assert that after high memory load all contained blocks are in the reference pool
	 */
	@Test
	public void testGarbageCollecting() throws IOException, Exception {
		BlockPool pool = blockPool();
		Set<Block> blocks = new HashSet<>();

		for (int i = 0; i < poolSize(); i++) {
			Block block = pool.get();
			blocks.add(block);

			// We need to write to make sure the resources are acquired (i.e. file is created)
			try (OutputStream out = block.openOutputStream()) {
				out.write(0);
			}
		}

		blocks.forEach(Block::free);
		blocks.clear();
		byte[][] b;
		try {
			b = new byte[1_000][];

			for (int i = 0; i < b.length; i++) {
				b[i] = new byte[1_000_000_000];
			}

			fail("Failed to simulate OutOfMemoryError");
		} catch (OutOfMemoryError e) {
			// simulate high memory load
			System.out.println("Simulated OutOfMemoryError");
			Thread.sleep(1000);
			System.out.println("Blocks should have been freed by now. Checking...");
		}

		// Assert that the blocks have been garbage collected by asserting the size of the reference queue
		for (int i = 0; i < poolSize(); i++) {
			Reference<? extends Block> ref = blockRefs.poll();

			assertThat(ref).isNotNull();
			Block block = ref.get();

			assertThat(block).isNull();
		}

		// Assert that blocks get generated again after being removed by garbage collection
		for (int i = 0; i < poolSize(); i++) {
			Block block = pool.get();
			assertThat(block).isNotNull();
			blocks.add(block);
		}

		assertThat(pool.get()).isNull();
	}

	@Override
	@Ignore
	public void testShutdown() throws IOException {
		// This test does not make sense so ignore it
		// As this pool only holds soft references to blocks it can't guarantee to destroy blocks anyway
	}

	@Override
	int poolSize() {
		return 10;
	}
}
