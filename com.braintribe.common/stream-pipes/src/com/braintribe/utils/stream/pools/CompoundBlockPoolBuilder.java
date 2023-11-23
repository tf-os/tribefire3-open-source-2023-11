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
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import com.braintribe.logging.Logger;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.stream.api.StreamPipeManager;
import com.braintribe.utils.stream.blocks.Block;
import com.braintribe.utils.stream.blocks.FileBlock;
import com.braintribe.utils.stream.blocks.InMemoryBlock;

/**
 * Builder for {@link CompoundBlockPool}s
 * 
 * @author Neidhart.Orlich
 *
 */
public class CompoundBlockPoolBuilder implements StreamPipeManager {
	private static Logger logger = Logger.getLogger(CompoundBlockPoolBuilder.class);

	private static ReentrantLock folderLock = new ReentrantLock();
	private final List<BlockPool> pools = new ArrayList<>();

	private static final int BLOCK_SUBFOLDERS = 1000;

	public static CompoundBlockPoolBuilder start() {
		return new CompoundBlockPoolBuilder();
	}

	/**
	 * Shortcut for {@link #appendDynamicFileBlockPool(File, int)} with unlimited size.
	 * 
	 * @see #appendDynamicFileBlockPool(File, int)
	 */
	public CompoundBlockPoolBuilder appendDynamicFileBlockPool(File rootDir) {
		return appendDynamicFileBlockPool(rootDir, -1);
	}

	/**
	 * Appends a new {@link DynamicBlockPool} - pool which can shrink again if there are too many unused blocks. This pool
	 * uses with {@link FileBlock}s which results in a scalable block size
	 * 
	 * @param maxNumBlocks
	 *            Max number of blocks that this pool can create
	 * @param rootDir
	 *            Root directory inside which file block files are stored. This directory must not be shared with any other
	 *            class, application and especially no other FileBlock backed pool. Files can be cleaned up or reused if you
	 *            reuse this folder after a restart. If no rootDir supplied, new temp files will be generated for the file
	 *            blocks. The directory must not be touched by other applications or manually and especially not by
	 *            automated cleanup tasks.
	 */
	public CompoundBlockPoolBuilder appendDynamicFileBlockPool(File rootDir, int maxNumBlocks) {
		BlockPool blockPool = new DynamicBlockPool(new GrowingBlockPool(fileBlockSupplier(rootDir), maxNumBlocks));
		pools.add(blockPool);
		return this;
	}

	/**
	 * Appends a new {@link SoftReferencingBlockPool} - pool which can free unused buffers from memory on high load. This
	 * pool uses {@link InMemoryBlock}s and thus is very fast and doesn't clutter the file system
	 * 
	 * @param blockSize
	 *            Fixed Block buffer size in bytes
	 * @param number
	 *            Max number of blocks that this pool can create
	 */
	public CompoundBlockPoolBuilder appendSoftReferencedInMemoryBlockPool(int blockSize, int number) {
		BlockPool blockPool = new SoftReferencingBlockPool(new InMemoryBlockSupplier(blockSize), number);
		pools.add(blockPool);
		return this;
	}

	/**
	 * Appends a new {@link GrowingBlockPool} - pool which guarantees to keep all it's Blocks' resources available until
	 * {@link CompoundBlockPool#shutdown()}. In this case this means that allocated byte buffers will not be freed and thus
	 * also don't have to be recreated when again needed. This pool uses {@link InMemoryBlock}s and thus is very fast and
	 * doesn't clutter the file system
	 * 
	 * @param blockSize
	 *            Fixed Block buffer size in bytes
	 * @param number
	 *            Max number of blocks that this pool can create
	 */
	public CompoundBlockPoolBuilder appendInMemoryBlockPool(int blockSize, int number) {
		BlockPool blockPool = new GrowingBlockPool(new InMemoryBlockSupplier(blockSize), number);
		pools.add(blockPool);
		return this;
	}

	/**
	 * Appends a new {@link GrowingBlockPool} - pool which guarantees to keep all it's Blocks' resources available until
	 * {@link CompoundBlockPool#shutdown()}. In this case this means that created files will not be deleted and thus also
	 * don't have to be recreated when again needed. This pool uses with {@link FileBlock}s which results in a scalable
	 * block size
	 * <p>
	 * 
	 * @param maxNumBlocks
	 *            Max number of blocks that this pool can create
	 * @param rootDir
	 *            Root directory inside which file block files are stored. This directory must not be shared with any other
	 *            class, application and especially no other FileBlock backed pool. Files can be cleaned up or reused if you
	 *            reuse this folder after a restart. If no rootDir supplied, new temp files will be generated for the file
	 *            blocks. The directory must not be touched by other applications or manually and especially not by
	 *            automated cleanup tasks.
	 */
	public CompoundBlockPoolBuilder appendFileBlockPool(File rootDir, int maxNumBlocks) {
		BlockPool blockPool = new GrowingBlockPool(fileBlockSupplier(rootDir), maxNumBlocks);
		pools.add(blockPool);
		return this;
	}

	public CompoundBlockPool build() {
		CompoundBlockPool compoundBlockPool = new CompoundBlockPool(pools);

		return compoundBlockPool;
	}

	@Override
	public CompoundBlockPool createFactory(String name) {
		return build();
	}

	private Supplier<Block> fileBlockSupplier(File blockFileRootDir) {
		Iterator<FileBlock> recycledBlocks = Collections.emptyIterator();

		if (blockFileRootDir == null) {
			return new DisposableFileBlockSupplier();
		}

		recycledBlocks = recyclePresentFiles(blockFileRootDir).iterator();

		return new FileBlockSupplier(recycledBlocks, blockFileRootDir);
	}

	// Needs to be synchronized because FileTools.ensureFolderExists isn't thread safe.
	private static File disposableBlockFilesRootDir() {
		File streampipesTempDir = new File(FileTools.getTempDir(), "streampipes");

		File blockFilesFolder = new File(streampipesTempDir, "disposable-blockfiles");
		if (!blockFilesFolder.exists()) {
			folderLock.lock();
			try {
				if (!blockFilesFolder.exists()) {
					return FileTools.ensureFolderExists(blockFilesFolder);
				}
			} finally {
				folderLock.unlock();
			}
		}
		return blockFilesFolder;
	}

	/* package-private */ static class InMemoryBlockSupplier implements Supplier<Block> {

		private final int size;

		public InMemoryBlockSupplier(int size) {
			super();
			this.size = size;
		}

		@Override
		public InMemoryBlock get() {
			return new InMemoryBlock(size);
		}

		public int getBlockSize() {
			return size;
		}
	}

	/* package-private */ class FileBlockSupplier implements Supplier<Block> {
		Iterator<FileBlock> recycledBlocks;
		File blockFileRootDir;

		public FileBlockSupplier(Iterator<FileBlock> recycledBlocks, File blockFileRootDir) {
			this.recycledBlocks = recycledBlocks;
			this.blockFileRootDir = blockFileRootDir;
		}

		@Override
		public FileBlock get() {
			if (recycledBlocks.hasNext()) {
				return recycledBlocks.next();
			}

			File blockFile = createNewFileForBlock(blockFileRootDir);

			return new FileBlock(blockFile);
		}

		public File getBlockFileRootDir() {
			return blockFileRootDir;
		}

	}

	private class DisposableFileBlockSupplier implements Supplier<Block> {

		@Override
		public FileBlock get() {
			File blockFile;
			try {
				blockFile = Files.createTempFile(disposableBlockFilesRootDir().toPath(), "block-file", null).toFile();
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
			FileTools.deleteFileWhenOrphaned(blockFile);

			return new FileBlock(blockFile);
		}

	}

	public static File createNewFileForBlock(File blockFileRootDir) {
		int random = (int) (Math.random() * BLOCK_SUBFOLDERS);
		File subDir = new File(blockFileRootDir, String.valueOf(random));

		subDir.mkdirs();

		String filename = String.valueOf(System.currentTimeMillis()) + UUID.randomUUID().toString();

		return new File(subDir, filename);
	}

	private static Iterable<FileBlock> recyclePresentFiles(File blockFileRootDir) {
		if (!blockFileRootDir.exists()) {
			return Collections.EMPTY_SET;
		}

		List<FileBlock> blocks = new ArrayList<>();

		for (File folder : blockFileRootDir.listFiles()) {
			if (!folder.isDirectory()) {
				logger.warn("Block file root dir is only supposed to contain directories, but found: " + folder.getAbsolutePath());
				continue;
			}

			for (File file : folder.listFiles()) {
				if (file.isDirectory()) {
					logger.warn("Block file sub dir is only supposed to contain files, but found: " + file.getAbsolutePath());
					continue;
				}
				blocks.add(new FileBlock(file));
			}
		}

		return blocks;
	}
}
