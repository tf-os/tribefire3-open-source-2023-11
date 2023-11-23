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
package com.braintribe.model.processing.resource.streaming.cache;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Function;

import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.processing.resource.cache.ResourceCache;
import com.braintribe.model.processing.resource.cache.ResourceCacheEntry;
import com.braintribe.model.processing.resource.md5.Md5Tools;
import com.braintribe.model.processing.resource.streaming.ResourceStreamException;
import com.braintribe.model.resource.source.ResourceSource;

import com.braintribe.utils.FileTools;

/**
 * A FileSystem based variant of the {@link ResourceCache}.
 * 
 * @author gunther.schenk
 *
 */
public class FSRepresentationCache implements ResourceCache {

	private static Logger logger = Logger.getLogger(
			FSRepresentationCache.class);

	private File rootDirectory;
	private Function<String, File> folderBuilder = new SplittedGuidFolderBuilder();
	private String dataFileExtension = ".data";
	private String inProgressFileExtension = ".inProgress";

	private final long waitInterval = 1000;
	private final long waitTimeout = 300000; // 5min

	private Function<ResourceSource, String> cacheKeyProvider = new CacheKeyProvider();
	
	// **************************************************************************
	// Constructor
	// **************************************************************************

	public FSRepresentationCache() {
	}

	// **************************************************************************
	// Getter/Setter
	// **************************************************************************

	@Required
	public void setRootDirectory(File rootDirectory) {
		this.rootDirectory = rootDirectory;
	}

	public File getRootDirectory() {
		return rootDirectory;
	}

	/**
	 * optional the {@link Function} which build a File that acts as a directory
	 * default is the {@link SplittedGuidFolderBuilder}
	 */
	public void setFolderBuilder(Function<String, File> folderBuilder) {
		this.folderBuilder = folderBuilder;
	}
	
	public Function<String, File> getFolderBuilder() {
		return folderBuilder;
	}

	public void setDataFileExtension(String dataFileExtension) {
		this.dataFileExtension = dataFileExtension;
	}

	public String getDataFileExtension() {
		return dataFileExtension;
	}

	public void setInProgressFileExtension(String inProgressFileExtension) {
		this.inProgressFileExtension = inProgressFileExtension;
	}

	public String getInProgressFileExtension() {
		return inProgressFileExtension;
	}

	public void setCacheKeyProvider(Function<ResourceSource, String> cacheKeyProvider) {
		this.cacheKeyProvider = cacheKeyProvider;
	}

	// **************************************************************************
	// Interface methods
	// **************************************************************************
	 

	@Override
	public ResourceCacheEntry acquireCacheEntry(ResourceSource source) throws ResourceStreamException {
		return acquireEntry(getFSResourceCacheEntry(source));
	}

	@Override
	public ResourceCacheEntry acquireCacheEntry(String cacheKey, String domain)	throws ResourceStreamException {
		return acquireEntry(getFSResourceCacheEntry(cacheKey, domain));
	}
	
	@Override
	public ResourceCacheEntry getCacheEntry (ResourceSource source) throws ResourceStreamException {
		return getFSResourceCacheEntry(source);
	}

	@Override
	public ResourceCacheEntry getCacheEntry (String cacheKey, String domain) throws ResourceStreamException {
		return getFSResourceCacheEntry(cacheKey, domain);
	}

	// **************************************************************************
	// Helper methods
	// **************************************************************************


	/**
	 * Returns a new {@link FSCacheEntry} instance for the given source.
	 */
	protected FSCacheEntry getFSResourceCacheEntry (ResourceSource source) throws ResourceStreamException {
		return new FSCacheEntry(provideCacheKey(source), source.entityType().getShortName());
	}

	/**
	 * Returns a new {@link FSCacheEntry} instance for the given cacheKey and domain.
	 */
	protected FSCacheEntry getFSResourceCacheEntry (String cacheKey, String domain) throws ResourceStreamException {
		return new FSCacheEntry(cacheKey, domain);
	}

	/**
	 * Uses the {@link #cacheKeyProvider} to provide a string representation of the given RepresentationSource
	 */
	private String provideCacheKey(ResourceSource source) throws ResourceStreamException {
		try {
			return cacheKeyProvider.apply(source);
		} catch (RuntimeException e) {
			throw new ResourceStreamException("Could not provide cacheKey for representation source with id: "
					+ source.getId(), e);
		}
	}

	/**
	 * Returns an already cached entry or triggers creation or blocks until an already triggered 
	 * creation is finished. 
	 * @return A valid cache entry. 
	 * @throws CreationTimeoutException  
	 *         ResourceStreamException 
	 */
	private ResourceCacheEntry acquireEntry(FSCacheEntry entry) throws ResourceStreamException {
		if (entry.isCached()) {
			return entry;
		}
		try {
			entry.setInProgress();
		} catch (AlreadyInProgressException ae) {
			entry.waitForCreation();
		}
		return entry;
	}

	// **************************************************************************
	// Inner Classes
	// **************************************************************************

	/**
	 * The FileSystem variant of an cache entry.
	 * 
	 * @author gunther.schenk
	 */
	public class FSCacheEntry implements ResourceCacheEntry {

		private final String cacheKey;
		private final String domain;
		private File cacheFolder;
		private File dataFile;
		private File inProgressFile;

		// **************************************************************************
		// Constructor
		// **************************************************************************

		public FSCacheEntry(String cacheKey, String domain) {
			this.cacheKey = cacheKey;
			this.domain = domain;
		}

		// **************************************************************************
		// Interface methods
		// **************************************************************************

		@Override
		public boolean isCached() throws ResourceStreamException {
			try {
				return (getDataFile().exists()) && !(getInProgressFile().exists());
			} catch (Exception e) {
				throw new ResourceStreamException(e);
			}
		}

		@Override
		public long getLastModification() throws ResourceStreamException {
			try {
				return getDataFile().lastModified();
			} catch (Exception e) {
				throw new ResourceStreamException(e);
			}
		}

		@Override
		public InputStream openCacheInputStream() throws ResourceStreamException {

			try {
				waitForCreation();
				File cacheDataFile = getDataFile();
				if (!cacheDataFile.exists()) {
					throw new ResourceStreamException("Representation Cache Data file: "
							+ cacheDataFile.getAbsolutePath() + " does not exists.");
				}
				return new FileInputStream(cacheDataFile);
			} catch (CreationTimeoutException te) {
				throw te;
			} catch (Exception e) {
				throw new ResourceStreamException("Could not open cache input stream.", e);
			}
		}

		//@SuppressWarnings("resource")
		@Override
		public OutputStream openCacheOutputStream() throws ResourceStreamException {
			try {
				File cacheDataFile = getDataFile();
				return new BufferedOutputStream(new FileOutputStream(cacheDataFile, false)) {
					@Override
					public void close() throws IOException {
						try {
							super.close();
						} finally {
							try {
								setFinished();
							} catch (Exception e) {
								IOException ioe = new IOException();
								ioe.initCause(e);
								throw ioe;
							}
						}

					}
				};
			} catch (Exception e) {
				throw new ResourceStreamException("Could not open cache output stream.", e);
			}
		}

		@Override
		public void delete() throws ResourceStreamException {
			boolean successfullyDeletedInProgressFile = false;
			boolean successfullyDeletedDataFile = false;
			try {
				successfullyDeletedInProgressFile = deleteFile(getInProgressFile());
			} catch (Exception e) {
				logger.warn("Could not get in progessFile for cacheEntry: " + cacheKey, e);
			}
			try {
				successfullyDeletedDataFile = deleteFile(getDataFile());
			} catch (Exception e) {
				logger.warn("Could not get in dataFile for cacheEntry: " + cacheKey, e);
			}
			if (!successfullyDeletedDataFile || !successfullyDeletedInProgressFile) {
				throw new ResourceStreamException("Could not delete all related resources for cacheEntry: "
						+ cacheKey + " dataFile:" + successfullyDeletedDataFile + ", inProgressFile:"
						+ successfullyDeletedInProgressFile);
			}
			try {
				cleanupEmptyFolders(getCacheFolder());	
			} catch (Exception e) {
				logger.warn("Could not cleanup empt parent cache folders.");
			}
			
		}

		@Override
		public String getDomain() {
			return this.domain;
		}

		@Override
		public String getMd5() throws ResourceStreamException {
			try {
				if (getSize() == 0) {
					return null;
				}
				return Md5Tools.getMd5(getDataFile()).asHexString();
			} catch (Exception e) {
				throw new ResourceStreamException(e);
			}
		}
		
		@Override
		public long getSize() throws ResourceStreamException {
			try {
				return getDataFile().length();
			} catch (Exception e) {
				throw new ResourceStreamException(e);
			}
		}

		// **************************************************************************
		// Helper methods
		// **************************************************************************

		/**
		 * Tries to delete all empty cache folders backwards. 
		 */
		private void cleanupEmptyFolders (File folderToDelete) {
			if (folderToDelete.equals(rootDirectory)) {
				return;
			}
			if (FileTools.isEmpty(folderToDelete)) {
				deleteFile(folderToDelete);
				cleanupEmptyFolders(folderToDelete.getParentFile());
			}
		}
		
		/**
		 * Returns the cache folder for this entry.
		 */
		protected File getCacheFolder() throws Exception {
			if (cacheFolder == null) {
				File domainFolder = new File(rootDirectory, domain);
				File relativeOrAbsoluteFolder = getCalculatedFolder(cacheKey);
				cacheFolder = relativeOrAbsoluteFolder.isAbsolute()?
						relativeOrAbsoluteFolder:
						new File(domainFolder, relativeOrAbsoluteFolder.getPath());
			}
			return cacheFolder;
		}

		/**
		 * Returns the data file for this entry.
		 */
		protected File getDataFile() throws Exception {
			if (dataFile == null) {
				dataFile = new File(getCacheFolder(), cacheKey + dataFileExtension);
			}
			return dataFile;
		}

		/**
		 * Returns the inProgress file for this entry.
		 */
		protected File getInProgressFile() throws Exception {
			if (inProgressFile == null) {
				inProgressFile = new File(getCacheFolder(), cacheKey + inProgressFileExtension);
			}
			return inProgressFile;
		}

		/**
		 * Returns the cache folder for this entry and physically 
		 * creates it if the folder does not exist. 
		 */
		protected File acquireCacheEntryFolder() throws Exception {
			File cacheFolder = getCacheFolder();
			if (!cacheFolder.exists() && !cacheFolder.isDirectory()) {
				FileTools.createDirectory(cacheFolder.toString());
			}
			return cacheFolder;
		}

		/**
		 * Returns true if (and only if) the inProgress file physically 
		 * exists.
		 */
		public boolean isInProgress() throws ResourceStreamException {
			try {
				return getInProgressFile().exists();
			} catch (Exception e) {
				throw new ResourceStreamException(e);
			}
		}

		/**
		 * Atomically creates an inProgress file for this entry. 
		 * @throws AlreadyInProgressException in case an inProgress file already exists. 
		 */
		protected void setInProgress() throws ResourceStreamException, AlreadyInProgressException {
			File inProgressFile = null;
			try {
				this.acquireCacheEntryFolder();
				inProgressFile = getInProgressFile();
				File parentDir = inProgressFile.getParentFile();
				parentDir.mkdirs();
				if (!inProgressFile.createNewFile()) {
					throw new AlreadyInProgressException("Could not create inProgress File "+inProgressFile.getAbsolutePath()+" for cache entry: "
							+ cacheKey);
				}
			} catch (AlreadyInProgressException ae) {
				throw ae;
			} catch (Exception e) {
				throw new ResourceStreamException("Could not create file "+((inProgressFile == null) ? "null" : inProgressFile.getAbsolutePath()), e);
			}
		}

		/**
		 * If an inProgress file exists for this entry this method tries to 
		 * delete the inProgress file. 
		 *  
		 * @throws ResourceStreamException if the inProgress file can not be deleted.
		 */
		protected void setFinished() throws Exception {
			if (isInProgress()) {
				this.acquireCacheEntryFolder();
				if (!deleteFile(getInProgressFile())) {
					throw new ResourceStreamException("Could not delete inProgress File for cache entry: "
							+ cacheKey + ". Cache Entry still marked inProgress.");
				}

			}
		}

		/**
		 * This method blocks until an inProgress file exists for this entry
		 * or the waitTimeout is reached. 
		 * 
		 * @throws CreationTimeoutException in case the waitTimout is reached an the inProgress file still exists.
		 */
		protected void waitForCreation() throws ResourceStreamException {
			if (!isCached() && !isInProgress()) {
				throw new ResourceStreamException("No cache entry found for cache entry: " + cacheKey);
			}
			long start = System.currentTimeMillis();
			while (isInProgress()) {
				try {
					if (logger.isTraceEnabled())
						logger.trace("Waiting for creation of cacheEntry: " + cacheKey);
					Thread.sleep(waitInterval);
				} catch (InterruptedException ie) {
					/* ignore */
				}
				long current = System.currentTimeMillis();
				long consumed = current - start;
				if (consumed > waitTimeout) {
					throw new CreationTimeoutException("Timeout exceeded! Cache entry: " + cacheKey
							+ " is still in progress. Timeout=" + waitTimeout + "ms.");
				}
			}
		}

		/**
		 * Creates the relative folder name(s) for the given id using the
		 * folderBuilder and variable replacing. 
		 */
		protected File getCalculatedFolder(String id) throws RuntimeException {
			return folderBuilder.apply(id);
		}

		/**
		 * Tries to delete the given file with retries.
		 */
		protected boolean deleteFile(File file) {
			if (!file.exists()) {
				return true;
			}
			int retries = 3;
			boolean fileExists = true;
			for (int i = 0; i < retries; i++) {
				file.delete();
				if (!file.exists()) {
					logger.trace("Successfully deleted file: " + file.getAbsolutePath());
					fileExists = false;
					break;
				} else {
					logger.trace("Could not delete file: " + file.getAbsolutePath() + " in try: " + (i + 1));
				}
			}
			if (fileExists) {
				logger.warn("Could not delete file: " + file.getAbsolutePath() + " after " + retries
						+ " tries. Must be cleaned manually.");
			}
			return (!fileExists);
		}

	}

	public static String getBuildVersion() {
		return "$Build_Version$ $Id: FSRepresentationCache.java 102880 2018-01-18 11:36:53Z roman.kurmanowytsch $";
	}
}
