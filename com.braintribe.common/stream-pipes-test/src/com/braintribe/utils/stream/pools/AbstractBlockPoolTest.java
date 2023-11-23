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

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.assertj.core.util.Files;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import com.braintribe.exception.Exceptions;
import com.braintribe.utils.stream.NullOutputStream;
import com.braintribe.utils.stream.blocks.Block;
import com.braintribe.utils.stream.blocks.FileBlock;
import com.braintribe.utils.stream.blocks.InMemoryBlock;

@RunWith(Parameterized.class)
public abstract class AbstractBlockPoolTest {

	abstract BlockPool blockPool();
	abstract int poolSize();

	@Parameter(0)
	public Supplier<Block> blockSupplier;

	@Parameters
	public static Iterable<Object[]> data() {
		File dir = Files.newTemporaryFolder();
		return blockSuppliers( //
				() -> new TestBlock(), //
				() -> new InMemoryBlock(1), //
				() -> new FileBlock(CompoundBlockPoolBuilder.createNewFileForBlock(dir))); //
	}

	private static List<Object[]> blockSuppliers(Supplier<Block>... s) {
		return Arrays.stream(s) //
				.map(e -> new Object[] { e }) //
				.collect(Collectors.toList());
	}

	boolean wasCleanedUp(Block block) {
		try {
			if (block instanceof TestBlock) {
				TestBlock testBlock = (TestBlock) block;
				return testBlock.destroyed;
			}

			if (block instanceof InMemoryBlock) {
				InMemoryBlock inMemoryBlock = (InMemoryBlock) block;

				return inMemoryBlock.getBuffer() == null;
			}

			if (block instanceof FileBlock) {
				FileBlock fileBlock = (FileBlock) block;

				return !fileBlock.getFile().exists();
			}

			throw new IllegalArgumentException("Unknow block type: " + block.getClass());
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Could not inspect blocks during test.");
		}
	}

	/**
	 * Assert that the maximum number of blocks can be retrieved from the pool but not more
	 */
	@Test
	public void testMaxSize() {
		BlockPool pool = blockPool();

		for (int i = 0; i < 10; i++) {
			Block block = pool.get();

			assertThat(block).isNotNull();
		}

		assertThat(pool.get()).isNull();

	}

	/**
	 * Assert that a block is reused after returning it to the pool.
	 */
	@Test
	public void testReturning() {

		BlockPool pool = blockPool();

		Set<Block> recievedBlocks = new HashSet<>();

		for (int i = 0; i < poolSize(); i++) {
			Block block = pool.get();
			recievedBlocks.add(block);
		}

		assertThat(pool.get()).isNull(); // pool is now empty

		recievedBlocks.forEach(block -> {
			pool.giveBack(block);
			assertThat(block).isEqualTo(pool.get());
		});

	}

	/**
	 * Assert that after shutdown all blocks <i>remaining</i> in pool are being destroyed
	 */
	@Test
	public void testShutdown() throws Exception {
		BlockPool pool = blockPool();

		Set<Block> recievedBlocks = new HashSet<>();

		for (int i = 0; i < poolSize(); i++) {
			Block block = pool.get();
			recievedBlocks.add(block);

			// We need to write to make sure the resources are acquired (i.e. file is created)
			try (OutputStream out = block.openOutputStream()) {
				out.write(0);
			}
		}

		recievedBlocks.forEach(block -> assertThat(wasCleanedUp(block)).isFalse());
		recievedBlocks.forEach(pool::giveBack);
		pool.shutDown();
		recievedBlocks.forEach(block -> assertThat(wasCleanedUp(block)).isTrue());

		assertThat(pool.get()).isNull();
	}

	/**
	 * Assert that after shutdown all blocks <i>returned</i> to pool are being destroyed
	 */
	@Test
	public void testShutdown2() throws Exception {
		BlockPool pool = blockPool();

		Set<Block> recievedBlocks = new HashSet<>();

		for (int i = 0; i < poolSize(); i++) {
			Block block = pool.get();
			recievedBlocks.add(block);

			// We need to write to make sure the resources are acquired (i.e. file is created)
			try (OutputStream out = block.openOutputStream()) {
				out.write(0);
			}
		}

		recievedBlocks.forEach(block -> assertThat(wasCleanedUp(block)).isFalse());
		pool.shutDown();
		recievedBlocks.forEach(pool::giveBack);
		recievedBlocks.forEach(block -> assertThat(wasCleanedUp(block)).isTrue());

		assertThat(pool.get()).isNull();
	}

	public static class TestBlock extends Block {
		boolean destroyed;

		@Override
		public OutputStream openOutputStream() {
			return new NullOutputStream();
		}

		@Override
		public InputStream openRawInputStream() {
			return null;
		}

		@Override
		public int getTreshold() {
			return 0;
		}

		@Override
		public void destroy() {
			destroyed = true;
		}

		@Override
		public void autoBufferInputStreams(int bufferSize) {
			// ignore
		}

		@Override
		public boolean isAutoBuffered() {
			return false;
		}

		@Override
		public long getBytesAllocated() {
			throw new UnsupportedOperationException();
		}
	}
}
