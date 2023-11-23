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
package com.braintribe.model.access.smood.bms;

import static com.braintribe.model.generic.manipulation.util.ManipulationBuilder.compound;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.access.smood.api.ManipulationStorage;
import com.braintribe.model.access.smood.api.ManipulationStorageException;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.processing.dataio.GenericModelInputStream;
import com.braintribe.model.processing.dataio.GenericModelOutputStream;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.stream.CountingInputStream;
import com.braintribe.utils.system.SystemTools;

public class BinaryManipulationStorage implements ManipulationStorage {
	private static final Logger logger = Logger.getLogger(BinaryManipulationStorage.class);
	private File storageFile;
	protected boolean persist = true;

	@Required
	public void setStorageFile(File storageFile) {
		this.storageFile = storageFile;
		boolean debug = logger.isDebugEnabled();
		
		if (FileTools.isDevNull(storageFile)) {
			
			if (debug) {
				logger.debug("Persistence will be disabled (/dev/null)");
			}
			this.persist = false;
		} else {
			
			if (debug) {
				logger.debug("Persistence will be enabled ("+storageFile.getAbsolutePath()+")");
			}
			
		}
	}

	@Override
	public void appendManipulation(Manipulation manipulation) throws ManipulationStorageException {
		if (!this.persist) {
			return;
		}
		GenericModelOutputStream gmOut = null;

		try {
			if (!storageFile.exists()) {
				ensureParentFolderExists(storageFile);
			}

			gmOut = new GenericModelOutputStream(new BufferedOutputStream(new FileOutputStream(storageFile, true)));
			gmOut.writeGmEntity(manipulation);
			gmOut.flush();
		}
		catch (Exception e) {
			throw new ManipulationStorageException("error while writing manipulation to file "+this.storageFile, e);
		}
		finally {
			ensureClose(gmOut);
		}
	}

	@Override
	public void reset() throws ManipulationStorageException {
		if (!this.persist) {
			return;
		}
		if (storageFile.exists()) {
			storageFile.delete();
		}
	}

	@Override
	public Manipulation getAccumulatedManipulation() throws ManipulationStorageException {
		if (!this.persist) {
			return null;
		}
		List<Manipulation> manipulations = new ArrayList<Manipulation>(); 
		FileInputStream fileIn = null; 
		CountingInputStream cis = null;
		GenericModelInputStream gmIn = null;
				
		long debugInterval = 2000L;
		
		try {
			if (storageFile.exists()) {
				
				boolean debug = logger.isDebugEnabled();
				long fileLength = this.storageFile.length();
				int manipulationCount = 0;
				long nextDebugOutput = System.currentTimeMillis() + debugInterval;
			
				fileIn = new FileInputStream(storageFile);
				cis = new CountingInputStream(fileIn, true);
				
				gmIn = new GenericModelInputStream(cis);
				
				Manipulation manipulation;
				while ((manipulation = (Manipulation)gmIn.readGmEntity(null)) != null) {
					manipulations.add(manipulation);

					manipulationCount++;

					if (debug && ((manipulationCount % 10) == 0)) {
						long now = System.currentTimeMillis();

						if (now > nextDebugOutput) {
							long count = cis.getCount();
							double percentage = 100 * (((double) count) / ((double) fileLength));
							String percentageString = String.format("%.2f", percentage);
							logger.debug("Read "+manipulationCount+" manipulations. ("+cis.getCount()+" of "+fileLength+" bytes; "+percentageString+" %)");
							
							nextDebugOutput = now + debugInterval;
						}
					}
				}
			}

			switch (manipulations.size()) {
				case 0:
					return null;
				case 1:
					return manipulations.get(0);
				default:
					return compound(manipulations);
			}
		}
		catch (Exception e) {
			throw new ManipulationStorageException("Error while reading manipulations from file "+this.storageFile, e);
		}
		finally {
			ensureClose(cis, gmIn, fileIn);
		}
	}

	@Override
	public long getSize() {
		if (!this.persist) {
			return 0L;
		}
		return storageFile.exists()? 
				storageFile.length(): 
					0L;
	}

	/**
	 * Ensure {@link Closeable#close()} is called for all the given {@link Closeable}(s)
	 */
	private static void ensureClose(Closeable ... closeables) {

		if (closeables == null || closeables.length == 0)
			return;

		for (Closeable closeable : closeables) {
			if (closeable != null) {
				try {
					closeable.close();
				} catch (Throwable e) {
					logger.error("error while closing closeable "+closeable+": "+e.getMessage(), e);
				}
			}
		}
	}

	private static void ensureParentFolderExists(File f) throws IOException {
		File parentFolder = f.getAbsoluteFile().getParentFile();
		if (parentFolder != null) {
			FileTools.ensureDirectoryExists(parentFolder);
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("BinaryManipulationStorage[path=");
		if (storageFile != null) {
			sb.append(storageFile.getAbsolutePath());
			if (storageFile.exists()) {
				sb.append(";size=");
				sb.append(String.format("%,d", storageFile.length()));
			} else {
				sb.append(";nonexistent");
			}
			String freeSpace = SystemTools.getPrettyPrintFreeSpaceOnDiskDevice(storageFile);
			if (freeSpace != null) {
				sb.append(";Free Space: ");
				sb.append(freeSpace);
			}
		} else {
			sb.append("No storageFile set.");
		}
		sb.append("]");
		return sb.toString();
	}
}
