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
package com.braintribe.utils;

import java.io.File;
import java.io.IOException;
import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.braintribe.logging.Logger;

/**
 * This is a helper class that is used by {@link FileTools#deleteFileWhenOrphaned(File)}. Please read the description of
 * the {@link #deleteFileWhenOrphaned(File)} method carefully.
 */
public class FileAutoDeletion implements AutoCloseable {

	private static Logger logger = Logger.getLogger(FileAutoDeletion.class);

	private static final ReferenceQueue<Object> filesRegisteredForDeletionQueue = new ReferenceQueue<>();
	private static final ConcurrentMap<PhantomReference<Object>, String> refs = new ConcurrentHashMap<>();
	private static boolean cleanupMechanismsInstalled = false;
	private static Object cleanupMechanismsLockObject = new Object();

	private static boolean run = true;
	private static Thread daemonThread;

	/**
	 * Please read this description carefully! This method takes as a parameter the file that should be deleted once there
	 * is not reference to it anymore. This should always be a fallback mechanism as files should be deleted as soon as they
	 * are not needed anymore. There are, however, circumstances where this is out of the control of the code that creates
	 * the file. So, when the provided File object is no longer referenced (i.e., it is orphaned and about to be garbage
	 * collected), this code will make sure that the file gets deleted first. This should only be used for temporary files!
	 * A word of caution: use this method with meticulous care. If, for example, a FileInputStream is constructed on this
	 * file, this FileInputStream does NOT hold a reference to this file. Hence, this would mean that the file could get
	 * deleted while there is still an FileInputStream open on this file. If in doubt, don't use this method.
	 *
	 * @param tempFile
	 *            The file that should be monitored for getting an orphan.
	 * @see FileTools#deleteFileWhenOrphaned(File)
	 */
	protected static void deleteFileWhenOrphaned(File tempFile) {
		if (tempFile == null) {
			return;
		}
		String filePath = null;
		try {
			filePath = tempFile.getCanonicalPath();
		} catch (IOException e) {
			logger.error("Could not get canonical path of file " + tempFile.getAbsolutePath(), e);
			return;
		}

		ensureCleanupMechanisms();

		refs.put(new PhantomReference<Object>(tempFile, filesRegisteredForDeletionQueue), filePath);
	}

	/**
	 * Initializes the shutdown hook and the cleanup thread that are necessary to delete orphaned files. This initialization
	 * only happens once. Hence, this method can be called multiple times with only taking effect once.
	 */
	private static void ensureCleanupMechanisms() {
		if (!cleanupMechanismsInstalled) {
			synchronized (cleanupMechanismsLockObject) {
				if (!cleanupMechanismsInstalled) {
					Runtime.getRuntime().addShutdownHook(new Thread("FileAutoDeletion::shutdownHook") {
						@Override
						public void run() {
							for (String filePath : refs.values()) {
								FileTools.deleteFileSilently(filePath);
							}
							run = false;
						}
					});
					cleanupMechanismsInstalled = true;

					daemonThread = new Thread(FileAutoDeletion::deleteEnqueuedFiles);
					daemonThread.setDaemon(true);
					daemonThread.setName("FileAutoDeletion::deleteEnqueuedFiles");
					daemonThread.start();
				}
			}
		}
	}

	/**
	 * The method that watches for PhantomReferences to be enqueued. If will then try to delete the file in question.
	 */
	private static void deleteEnqueuedFiles() {
		logger.pushContext("FileAutoDeletion::deleteEnqueuedFiles");
		try {
			while (run) {
				Reference<?> ref = filesRegisteredForDeletionQueue.remove();
				String blobFilePath = refs.remove(ref);
				if (blobFilePath != null) {
					logger.debug(() -> "Deleting file that is no longer referenced: " + blobFilePath);
					FileTools.deleteFileSilently(blobFilePath);
				}
			}
		} catch (InterruptedException ignore) {
			logger.debug(() -> "FileAutoDeletion::deleteEnqueuedFiles shuts down due to an interrupt");
			// we're done
		} finally {
			logger.popContext();
			run = true;
			cleanupMechanismsInstalled = false;
		}
		logger.debug(() -> "FileAutoDeletion::deleteEnqueuedFiles stopped");
	}

	@Override
	public void close() throws Exception {
		run = false;
		if (daemonThread != null) {
			try {
				daemonThread.interrupt();
			} catch (Exception e) {
				logger.debug("Error while trying to interrupt daemon thread.", e);
			}
		}
	}

}
