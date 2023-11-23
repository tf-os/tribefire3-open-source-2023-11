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

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.braintribe.utils.stream.blocks.Block;

public class DynamicBlockPoolTest extends AbstractBlockPoolTest {

	/**
	 * Assert that a part of the returned blocks were destroyed and created again
	 */
	@Test
	public void testReturning2() throws IOException {
		BlockPool pool = blockPool();

		Set<Block> recievedBlocks = new HashSet<>();

		for (int i = 0; i < poolSize(); i++) {
			Block block = pool.get();
			recievedBlocks.add(block);
		}

		recievedBlocks.forEach(pool::giveBack);

		Set<Block> recievedBlocks2 = new HashSet<>();
		for (int i = 0; i < 10; i++) {
			Block block = pool.get();
			recievedBlocks2.add(block);

			// We need to write to make sure the resources are acquired (i.e. file is created)
			try (OutputStream out = block.openOutputStream()) {
				out.write(0);
			}
		}

		assertThat(recievedBlocks).anyMatch(this::wasCleanedUp);
		assertThat(recievedBlocks).anyMatch(block -> wasCleanedUp(block) == false);
		assertThat(recievedBlocks2).allMatch(block -> wasCleanedUp(block) == false);
		assertThat(recievedBlocks).isNotEqualTo(recievedBlocks2);
	}

	@Override
	BlockPool blockPool() {
		return new DynamicBlockPool(new GrowingBlockPool(blockSupplier, poolSize()));
	}

	@Override
	int poolSize() {
		return 10;
	}
}
