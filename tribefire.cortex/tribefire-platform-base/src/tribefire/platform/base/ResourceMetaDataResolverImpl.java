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
package tribefire.platform.base;

import static java.util.Collections.emptyMap;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.logging.Logger;
import com.braintribe.model.resource.Resource;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.resource.ResourceMetaDataTools;

import tribefire.module.api.ResourceMetaDataResolver;
import tribefire.module.model.resource.ModuleSource;
import tribefire.module.wire.contract.ModuleResourcesContract;

/**
 * @author peter.gazdik
 */
public class ResourceMetaDataResolverImpl implements ResourceMetaDataResolver {

	private final ModuleResourcesContract moduleResources;
	private final String creator;

	private volatile ConcurrentMap<String, Resource> resourceIndex;
	private ReentrantLock resourceIndexLock = new ReentrantLock();

	private static final Logger log = Logger.getLogger(ResourceMetaDataResolverImpl.class);

	public ResourceMetaDataResolverImpl(ModuleResourcesContract moduleResources, String creator) {
		this.moduleResources = moduleResources;
		this.creator = creator;
	}

	@Override
	public Resource resolve(String path) {
		if (resourceIndex == null)
			indexResources();

		return resourceIndex.computeIfAbsent(path, this::computeFor);
	}

	private void indexResources() {
		if (resourceIndex == null) {
			resourceIndexLock.lock();
			try {
				if (resourceIndex == null) {
					resourceIndex = new ConcurrentHashMap<>(resolveIndex());
				}
			} finally {
				resourceIndexLock.unlock();
			}
		}
	}

	private Map<String, Resource> resolveIndex() {

		File indexFile = moduleResources.resource(ModuleSource.INDEX_FILE_NAME).asFile();

		if (indexFile.exists())
			try {
				return (Map<String, Resource>) FileTools.read(indexFile).fromInputStream(new YamlMarshaller()::unmarshall);

			} catch (RuntimeException e) {
				log.warn("Error while parsing module resources index. Will calculate resource meta-data dynamically, on demand. File: "
						+ indexFile.getAbsolutePath(), e);
			}

		return emptyMap();
	}

	private Resource computeFor(String path) {
		File file = moduleResources.resource(path).asFile();
		if (!file.exists())
			throw new IllegalArgumentException("No module resource found for relative path: " + path);

		return ResourceMetaDataTools.fileToResource(creator, file);
	}

}
