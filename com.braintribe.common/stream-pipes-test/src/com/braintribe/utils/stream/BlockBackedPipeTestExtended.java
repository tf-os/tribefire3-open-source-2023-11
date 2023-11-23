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
package com.braintribe.utils.stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TemporaryFolder;

import com.braintribe.logging.Logger;
import com.braintribe.testing.category.Slow;
import com.braintribe.testing.category.SpecialEnvironment;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.stream.api.StreamPipe;
import com.braintribe.utils.stream.pools.CompoundBlockPool;
import com.braintribe.utils.stream.pools.CompoundBlockPoolBuilder;

public class BlockBackedPipeTestExtended {

	private static final int NUMBER_OF_INMEMORY_BLOCKS_PER_POOL = 10;

	public static final Logger log = Logger.getLogger(BlockBackedPipeTestExtended.class);

	private static byte[] FIRST_LINE = "first line".getBytes();
	private static byte[] SECOND_LINE = "second line".getBytes();

	private CompoundBlockPool blockPool;

	@ClassRule
	public static TemporaryFolder tempFolder = new TemporaryFolder();

	@Before
	public void initBlockPool() {
		blockPool = CompoundBlockPoolBuilder.start() //
				.appendInMemoryBlockPool(15, NUMBER_OF_INMEMORY_BLOCKS_PER_POOL) //
				.appendInMemoryBlockPool(15, NUMBER_OF_INMEMORY_BLOCKS_PER_POOL) //
				.build();
	}

	@Test
	public void singleThreadedTest() throws IOException {
		byte buffer[] = new byte[FIRST_LINE.length];
		byte buffer2[] = new byte[SECOND_LINE.length];

		StreamPipe pipe = blockPool.newPipe("test");

		InputStream inBefore = pipe.openInputStream();

		OutputStream out = pipe.openOutputStream();

		out.write(FIRST_LINE);

		InputStream inBetween = pipe.openInputStream();

		IOTools.readFully(inBefore, buffer);

		assertThat(buffer).as("unexpected bytes read from inBefore").isEqualTo(FIRST_LINE);

		out.write(SECOND_LINE);

		IOTools.readFully(inBefore, buffer2);
		assertThat(buffer2).as("unexpected bytes read from inBefore").isEqualTo(SECOND_LINE);

		out.close();

		assertThat(inBefore.read()).as("eof expected").isEqualTo(-1);

		IOTools.readFully(inBetween, buffer);
		IOTools.readFully(inBetween, buffer2);
		assertThat(buffer).as("unexpected bytes read from inBetween").isEqualTo(FIRST_LINE);
		assertThat(buffer2).as("unexpected bytes read from inBetween").isEqualTo(SECOND_LINE);
		assertThat(inBetween.read()).as("eof expected").isEqualTo(-1);

		InputStream inAfter = pipe.openInputStream();

		IOTools.readFully(inAfter, buffer);
		IOTools.readFully(inAfter, buffer2);
		assertThat(buffer).as("unexpected bytes read from inAfter").isEqualTo(FIRST_LINE);
		assertThat(buffer2).as("unexpected bytes read from inAfter").isEqualTo(SECOND_LINE);
		assertThat(inAfter.read()).as("eof expected").isEqualTo(-1);

	}

	/**
	 * SpecialEnvironment?
	 * <p>
	 * This test fails in CI randomly.
	 * <p>
	 * When passing the parameter "false", it relies on calling {@code System.gc()} to cleanup blocks. But when GC doesn't cleanup what the test needs an
	 * {@link IllegalStateException} is thrown.
	 */
	@Test
	@Category(SpecialEnvironment.class)
	public void testBlockReleasing() throws Exception {
		testBlockReleasing(false);
		assertThatThrownBy(() -> testBlockReleasing(true)).isInstanceOf(IllegalStateException.class);
	}

	private void testBlockReleasing(boolean keepDanglingReferences) throws IOException {
		// So the test can only succeed if blocks are returned to pool
		int runs = 10 * NUMBER_OF_INMEMORY_BLOCKS_PER_POOL;

		List<InputStream> danglings = new ArrayList<InputStream>();
		
		for (int i = 0; i < runs; i++) {
			byte buffer[] = new byte[FIRST_LINE.length];
			byte buffer2[] = new byte[SECOND_LINE.length];
			
			StreamPipe pipe = blockPool.newPipe("test");

			InputStream inBefore = pipe.openInputStream();

			OutputStream out = pipe.openOutputStream();

			out.write(FIRST_LINE);

			InputStream inBetween = pipe.openInputStream();

			IOTools.readFully(inBefore, buffer);

			assertThat(buffer).as("unexpected bytes read from inBefore").isEqualTo(FIRST_LINE);

			out.write(SECOND_LINE);

			IOTools.readFully(inBefore, buffer2);
			assertThat(buffer2).as("unexpected bytes read from inBefore").isEqualTo(SECOND_LINE);

			out.close();

			assertThat(inBefore.read()).as("eof expected").isEqualTo(-1);

			IOTools.readFully(inBetween, buffer);
			IOTools.readFully(inBetween, buffer2);
			assertThat(buffer).as("unexpected bytes read from inBetween").isEqualTo(FIRST_LINE);
			assertThat(buffer2).as("unexpected bytes read from inBetween").isEqualTo(SECOND_LINE);
			assertThat(inBetween.read()).as("eof expected").isEqualTo(-1);

			InputStream inAfter = pipe.openInputStream();

			IOTools.readFully(inAfter, buffer);
			IOTools.readFully(inAfter, buffer2);
			assertThat(buffer).as("unexpected bytes read from inAfter").isEqualTo(FIRST_LINE);
			assertThat(buffer2).as("unexpected bytes read from inAfter").isEqualTo(SECOND_LINE);
			assertThat(inAfter.read()).as("eof expected").isEqualTo(-1);

			if (keepDanglingReferences) {
				InputStream inDangling = pipe.openInputStream();
				danglings.add(inDangling);
			}
			
			pipe = null;
			inBefore = null;
			inBetween = null;
			inAfter = null;
			out = null;
			System.gc(); // This causes problems!!! It's not certain that calling this actually cleans up anything.
		}
	}
	
	@Test
	public void testPipeClosing() throws Exception {
		
		// 2 pools with only one 15-byte block each
		blockPool = CompoundBlockPoolBuilder.start() //
				.appendInMemoryBlockPool(15, 1) //
				.appendInMemoryBlockPool(15, 1) //
				.build();
		
		StreamPipe pipe = blockPool.newPipe("test");
		
		OutputStream out = pipe.openOutputStream();
		out.write(FIRST_LINE);
		out.write(SECOND_LINE);
		out.close();
		
		InputStream in = pipe.openInputStream();
		in.close();
		
		assertThatThrownBy(() -> {
			StreamPipe newPipe = blockPool.newPipe("test");
			OutputStream o = newPipe.openOutputStream();
			o.write(0); // Write anything to trigger Block-acquiration (which should fail because there aren't any left)
		}).isExactlyInstanceOf(IllegalStateException.class);
		
		// Closing the pipe instantly frees the Blocks so now we can write again to any new pipe
		pipe.close();
		
		StreamPipe pipe2 = blockPool.newPipe("test");
		
		out = pipe2.openOutputStream();
		out.write(FIRST_LINE);
		out.write(SECOND_LINE);
		
		// Closing the pipe should fail now because there is still an open output stream
		assertThatThrownBy(pipe2::close).isExactlyInstanceOf(IllegalStateException.class);
		
		out.close();
		in = pipe2.openInputStream();
		
		// Closing the pipe should fail now because there is still an open input stream
		assertThatThrownBy(pipe2::close).isExactlyInstanceOf(IllegalStateException.class);
		
		in.close();
		
		// Closing the pipe should now succeed
		pipe2.close();
		
		// Opening InputStreams should not be possible any more
		assertThatThrownBy(pipe2::openInputStream).isExactlyInstanceOf(IllegalStateException.class);

		// The OutputStream of the pipe should be closed for good.
		assertThatThrownBy(() -> pipe2.acquireOutputStream()).isExactlyInstanceOf(IllegalStateException.class);
	}
	
}

