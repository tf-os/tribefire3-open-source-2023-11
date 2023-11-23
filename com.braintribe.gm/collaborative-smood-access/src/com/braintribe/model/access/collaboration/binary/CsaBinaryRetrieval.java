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
package com.braintribe.model.access.collaboration.binary;

import java.io.File;
import java.util.Objects;
import java.util.function.Function;

import com.braintribe.cfg.Required;
import com.braintribe.exception.Exceptions;
import com.braintribe.model.access.collaboration.CollaborativeSmoodAccess;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.source.FileSystemSource;
import com.braintribe.model.resource.source.ResourceSource;
import com.braintribe.model.resourceapi.stream.BinaryRetrievalRequest;
import com.braintribe.model.resourceapi.stream.BinaryRetrievalResponse;

/**
 * A special {@link ServiceProcessor} for {@link BinaryRetrievalRequest}s of {@link FileSystemSource}(s).
 */
public class CsaBinaryRetrieval implements ServiceProcessor<BinaryRetrievalRequest, BinaryRetrievalResponse> {

	private ServiceProcessor<BinaryRetrievalRequest, BinaryRetrievalResponse> retrievalDelegate;
	private Function<String, File> accessIdToResourcesBase;
	private Function<String, CollaborativeSmoodAccess> csaAccessResolver;

	@Required
	public void setRetrievalDelegate(ServiceProcessor<? super BinaryRetrievalRequest, ? super BinaryRetrievalResponse> retrievalDelegate) {
		this.retrievalDelegate = (ServiceProcessor<BinaryRetrievalRequest, BinaryRetrievalResponse>) retrievalDelegate;
	}

	/** Configures a function which for given <tt>accessId</tt> returns the (absolute) resources base folder. */
	@Required
	public void setAccessIdToResourcesBase(Function<String, File> accessIdToResourcesBase) {
		this.accessIdToResourcesBase = accessIdToResourcesBase;
	}

	@Required
	public void setDeployRegistry(Function<String, CollaborativeSmoodAccess> csaAccessResolver) {
		this.csaAccessResolver = csaAccessResolver;
	}

	@Override
	public BinaryRetrievalResponse process(ServiceRequestContext context, BinaryRetrievalRequest request) {
		Resource resource = request.getResource();
		FileSystemSource source = retrieveFileSystemSource(resource);
		if (source != null)
			lazyLoadFileSystemSourceIfNeeded(request.getDomainId(), source);

		return retrievalDelegate.process(context, request);
	}

	private static FileSystemSource retrieveFileSystemSource(Resource resource) {
		Objects.requireNonNull(resource, "Cannot stream null Resource");

		ResourceSource source = resource.getResourceSource();
		Objects.requireNonNull(source, () -> "Cannot stream resource with null source: " + resource);

		if (source instanceof FileSystemSource)
			return (FileSystemSource) source;
		else
			return null;
	}

	private void lazyLoadFileSystemSourceIfNeeded(String accessId, FileSystemSource source) {
		File sourceFile = resolveSourcePath(accessId, source);
		if (sourceFile.exists())
			return;

		CollaborativeSmoodAccess csa = resolveCsa(accessId, source);

		if (!csa.experimentalLazyLoad(source))
			throw new IllegalStateException(
					"Resource data not found (locally nor via lazy-loading). Access:" + accessId + ", path: " + source.getPath());
	}

	private CollaborativeSmoodAccess resolveCsa(String accessId, FileSystemSource source) {
		try {
			return csaAccessResolver.apply(accessId);

		} catch (RuntimeException e) {
			throw Exceptions.contextualize(e,
					"Resolution of access '" + accessId + "' failed, thus won't be able to lazy-load missing source: " + source.getPath());
		}
	}

	private File resolveSourcePath(String accessId, FileSystemSource source) {
		File absoluteResourcesBase = accessIdToResourcesBase.apply(accessId);
		String relativePath = source.getPath();
		return new File(absoluteResourcesBase, relativePath);
	}

}
