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

import static java.util.Objects.requireNonNull;

import java.nio.file.Path;
import java.util.function.Function;

import com.braintribe.cfg.Required;
import com.braintribe.model.processing.resource.streaming.AbstractFsBasedBinaryRetriever;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.source.ResourceSource;
import com.braintribe.model.resourceapi.base.BinaryRequest;
import com.braintribe.model.resourceapi.persistence.BinaryPersistenceRequest;
import com.braintribe.model.resourceapi.persistence.DeleteBinary;
import com.braintribe.model.resourceapi.persistence.DeleteBinaryResponse;
import com.braintribe.model.resourceapi.persistence.StoreBinary;
import com.braintribe.model.resourceapi.persistence.StoreBinaryResponse;
import com.braintribe.model.resourceapi.stream.BinaryRetrievalRequest;

import tribefire.module.model.resource.ModuleSource;
import tribefire.module.wire.contract.ModuleResourcesContract;

/**
 * A {@link ServiceProcessor } for {@link BinaryRetrievalRequest} for {@link Resource}s backed by {@link ModuleSource}s.
 * <p>
 * Technically it is a processor for all {@link BinaryRequest}s, but {@link BinaryPersistenceRequest}s throw an {@link UnsupportedOperationException}.
 * 
 * @author peter.gazdik
 */
public class ModuleSourceBinaryRetrieval extends AbstractFsBasedBinaryRetriever {

	private Function<String, ModuleResourcesContract> moduleResourcesContractResolver;

	@Required
	public void setModuleResourcesContractResolver(Function<String, ModuleResourcesContract> moduleResourcesContractResolver) {
		this.moduleResourcesContractResolver = moduleResourcesContractResolver;
	}

	@Override
	protected DeleteBinaryResponse delete(ServiceRequestContext context, DeleteBinary originalRequest) {
		return throwReadOnly("delete");
	}

	@Override
	protected StoreBinaryResponse store(ServiceRequestContext context, StoreBinary request) {
		return throwReadOnly("store");
	}

	private <T> T throwReadOnly(String method) {
		throw new UnsupportedOperationException(
				"Method [ModuleSourceBinaryRetrieval." + method + "] is not supported, module sources are read-only!");
	}

	@Override
	protected Path resolvePathForRetrieval(BinaryRetrievalRequest request) {
		Resource resource = request.getResource();

		ModuleSource source = retrieveModuleSource(resource);

		ModuleResourcesContract resourcesContract = resolveResourcesContract(source);

		return resourcesContract.resource(source.getPath()).asPath();
	}

	private ModuleSource retrieveModuleSource(Resource resource) {
		requireNonNull(resource, "Cannot stream null Resource");

		ResourceSource source = resource.getResourceSource();
		requireNonNull(source, () -> "Cannot stream resource with null source: " + resource);

		if (source instanceof ModuleSource)
			return (ModuleSource) source;
		else
			throw new IllegalStateException(
					"ModuleSourceBinaryRetrieval should only be configured for ModuleSource, not: " + source.entityType().getTypeSignature());
	}

	private ModuleResourcesContract resolveResourcesContract(ModuleSource source) {
		return moduleResourcesContractResolver.apply(source.getModuleName());
	}

}
