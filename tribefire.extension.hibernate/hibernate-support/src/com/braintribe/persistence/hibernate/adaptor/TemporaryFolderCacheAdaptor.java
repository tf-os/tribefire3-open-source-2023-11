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
package com.braintribe.persistence.hibernate.adaptor;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.DestructionAware;
import com.braintribe.logging.Logger;
import com.braintribe.utils.FileTools;

public class TemporaryFolderCacheAdaptor extends XPathAdaptor implements DestructionAware {

	protected static Logger logger = Logger.getLogger(TemporaryFolderCacheAdaptor.class);

	protected File temporaryCacheFolder = null;

	public TemporaryFolderCacheAdaptor() throws Exception {
		try {
			this.temporaryCacheFolder = File.createTempFile("ehcache", null);
			this.temporaryCacheFolder.delete();
			if (!this.temporaryCacheFolder.mkdirs()) {
				throw new Exception("Could not create directory "+this.temporaryCacheFolder);
			}
			if (logger.isDebugEnabled()) logger.debug("Created cache directory: "+this.temporaryCacheFolder.getAbsolutePath());
		} catch(Exception e) {
			throw new Exception("Error while creating temporary cache folder.", e);
		}

		super.valueMap = new HashMap<String,String>();
		super.valueMap.put("/ehcache/diskStore/@path", this.temporaryCacheFolder.getAbsolutePath());
	}

	@Override
	public void preDestroy() {
		if (this.temporaryCacheFolder != null) {
			try {
				if (logger.isDebugEnabled()) logger.debug("Cleaning directory: "+this.temporaryCacheFolder.getAbsolutePath());
				FileTools.deleteDirectoryRecursivelyOnExit(this.temporaryCacheFolder);
			} catch(Exception e) {
				logger.error("Error while cleaning up temporary cache folder: "+this.temporaryCacheFolder, e);
			}
			this.temporaryCacheFolder = null;
		}
	}
	
	@Override
	public void cleanup() {
		this.preDestroy();
	}

	@Override
	@Configurable
	public void setValueMap(Map<String, String> valueMap) {
		this.valueMap = valueMap;
	}

	@Configurable
	public void setTemporaryCacheFolder(File temporaryCacheFolder) {
		this.temporaryCacheFolder = temporaryCacheFolder;
	}

	public static String getBuildVersion() {
		return "$Build_Version$ $Id: TemporaryFolderCacheAdaptor.java 99995 2017-07-24 14:49:31Z andre.goncalves $";
	}

}
