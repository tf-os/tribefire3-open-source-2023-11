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
import java.io.FileWriter;
import java.nio.file.Files;

import org.junit.Test;

import com.braintribe.testing.junit.assertions.assertj.core.api.Assertions;
import com.braintribe.utils.stream.blocks.FileBlock;
import com.braintribe.utils.stream.pools.CompoundBlockPool;
import com.braintribe.utils.stream.pools.CompoundBlockPoolBuilder;

public class BlockPoolBuilderTest {
	/**
	 * Asserts that files within the provided rootDir for a FileBlock backed pool are recycled
	 */
	@Test
	public void testFileBlocks() throws Exception {
		final String CONTENT = "I exist";
		File rootDir = Files.createTempDirectory(BlockPoolBuilderTest.class.getSimpleName()).toFile();

		File subDir = new File(rootDir, "1");
		subDir.mkdirs();
		File orphanedBlockFile = new File(subDir, "orphan");

		try (FileWriter fileWriter = new FileWriter(orphanedBlockFile)) {
			fileWriter.write(CONTENT);
		}

		CompoundBlockPool pool = CompoundBlockPoolBuilder.start().appendDynamicFileBlockPool(rootDir).build();

		// The first Block's file should be our orphaned one
		FileBlock block = (FileBlock) pool.blockSupplier().get();

		File file = block.getFile();
		Assertions.assertThat(file) //
				.hasSameAbsolutePathAs(orphanedBlockFile) //
				.hasContent(CONTENT); //

		// The second Block's file should have been generated newly
		block = (FileBlock) pool.blockSupplier().get();

		Assertions.assertThat(block.getFile()) //
				.isNotNull() //
				.doesNotHaveSameAbsolutePathAs(orphanedBlockFile) //
				.doesNotExist() //
				.hasAncestor(rootDir);
	}
}
