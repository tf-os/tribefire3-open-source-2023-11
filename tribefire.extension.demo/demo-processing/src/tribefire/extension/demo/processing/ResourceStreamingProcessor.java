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
package tribefire.extension.demo.processing;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.processing.accessrequest.api.AccessRequestContext;
import com.braintribe.model.processing.accessrequest.api.AccessRequestProcessor;
import com.braintribe.model.processing.accessrequest.api.AccessRequestProcessors;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.resource.Resource;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.stream.FileStreamProviders;

import tribefire.extension.demo.model.api.streaming.DownloadResourceFromAccessRequest;
import tribefire.extension.demo.model.api.streaming.DownloadResourceFromFileSystemRequest;
import tribefire.extension.demo.model.api.streaming.DownloadResourceResponse;
import tribefire.extension.demo.model.api.streaming.ResourceStreamingRequest;
import tribefire.extension.demo.model.api.streaming.ResourceStreamingResponse;
import tribefire.extension.demo.model.api.streaming.UploadLocalResourceToFileSystemRequest;
import tribefire.extension.demo.model.api.streaming.UploadResourceResponse;
import tribefire.extension.demo.model.api.streaming.UploadResourceToAccessRequest;
import tribefire.extension.demo.model.api.streaming.UploadResourceToFileSystemRequest;
import tribefire.extension.demo.model.data.UploadedResource;

/**
 * Demonstrates the streaming capabilities of DDSA services by allowing you to upload and download a resource. <br>
 * 
 * It is meant to introduce developers to usage of {@link Resource} to stream resources and how to consume received resource streams (which differs based on where resources are stored - an access or a file system).
 * 
 *
 */
public class ResourceStreamingProcessor implements AccessRequestProcessor<ResourceStreamingRequest, ResourceStreamingResponse> {

	private static final Logger logger = Logger.getLogger(ResourceStreamingProcessor.class);

	private String existingResourcesPath;
	private String uploadPath;

	private AccessRequestProcessor<ResourceStreamingRequest, ResourceStreamingResponse> dispatcher = AccessRequestProcessors
			.dispatcher(config -> {
				config.register(UploadResourceToAccessRequest.T, this::uploadResourceToAccess);
				config.register(UploadResourceToFileSystemRequest.T, this::uploadResourceToFileSystem);
				config.register(UploadLocalResourceToFileSystemRequest.T, this::uploadLocalResourceToFileSystem);
				config.register(DownloadResourceFromAccessRequest.T, this::downloadResourceFromAccess);
				config.register(DownloadResourceFromFileSystemRequest.T, this::downloadResourceFromFileSystem);
			});

	
	@Override
	public ResourceStreamingResponse process(AccessRequestContext<ResourceStreamingRequest> context) {
		return dispatcher.process(context);
	}
	
	/**
	 *  Uploads a resource to an access.
	 */
	private UploadResourceResponse uploadResourceToAccess(
			AccessRequestContext<UploadResourceToAccessRequest> context) {

		UploadResourceToAccessRequest request = context.getRequest();
		Resource uploadedResource = request.getResource();

		PersistenceGmSession session = context.getSession();
		
		logger.info("Uploading resource '" + uploadedResource.getName() + "' to access '" + session.getAccessId() + "'");

		// consuming stream
		try (InputStream is = uploadedResource.openStream()) {
			// if uploaded resource with that name already exists, replace
			UploadedResource resourceHolder = getUploadedResourceByName(session, uploadedResource.getName());
			if(resourceHolder != null) {
				resourceHolder.setResource(uploadedResource);
			} else {
				resourceHolder = session.create(UploadedResource.T);
				resourceHolder.setResource(uploadedResource);
			}
		} catch (IOException e) {
			logger.error("Failed to upload resource '" + uploadedResource.getName() + "' to access '" + session.getAccessId() + "'", e);
			throw new RuntimeException("Failed to upload resource '" + uploadedResource.getName() + "' to access '" + session.getAccessId() + "'", e);
		}

		logger.info("Successfully uploaded resource '" + uploadedResource.getName() + "' to access '" + session.getAccessId() + "'");

		UploadResourceResponse response = UploadResourceResponse.T.create();
		return response;
	}

	/**
	 *  Uploads a resource to a file system.
	 */
	private UploadResourceResponse uploadResourceToFileSystem(
			AccessRequestContext<UploadResourceToFileSystemRequest> context) {

		UploadResourceToFileSystemRequest request = context.getRequest();
		Resource uploadedResource = request.getResource();

		logger.info("Uploading resource '" + uploadedResource.getName() + "' to the file system on path '" + uploadPath + "'");

		// consuming stream
		try (InputStream is = uploadedResource.openStream()) {
			File file = FileTools.createFile(new File(uploadPath + "/" + uploadedResource.getName()));
			IOTools.inputToFile(is, file);
		} catch (IOException e) {
			logger.error("Failed to upload resource '" + uploadedResource.getName() + "' to the file system on path '" + uploadPath + "'", e);
			throw new RuntimeException("Failed to upload resource '" + uploadedResource.getName() + "' to the file system on path '" + uploadPath + "'", e);
		}
		
		logger.info("Successfully uploaded resource '" + uploadedResource.getName() + "' to the file system on path '" + uploadPath + "'");

		UploadResourceResponse response = UploadResourceResponse.T.create();
		return response;
	}
	
	/**
	 *  Uploads a resource already existing in a file system to another directory by triggering {@link UploadResourceToFileSystemRequest} service request internally.
	 */
	private UploadResourceResponse uploadLocalResourceToFileSystem(
			AccessRequestContext<UploadLocalResourceToFileSystemRequest> context) {

		PersistenceGmSession session = context.getSession();

		Resource callResource = Resource.createTransient(FileStreamProviders.from(FileTools.newInputStream(new File(existingResourcesPath + "/AddressMap.png"))));
		callResource.setName("AddressMap.png");

		UploadResourceToFileSystemRequest uploadResourceToAccessReq = UploadResourceToFileSystemRequest.T.create();
		uploadResourceToAccessReq.setResource(callResource);
		uploadResourceToAccessReq.eval(session).get();

		UploadResourceResponse response = UploadResourceResponse.T.create();
		return response;
	}

	/**
	 *  Downloads a resource from an access.
	 */
	private DownloadResourceResponse downloadResourceFromAccess(
			AccessRequestContext<DownloadResourceFromAccessRequest> context) {

		DownloadResourceFromAccessRequest request = context.getRequest();
		String uploadedResourceName = request.getUploadedResourceName();
		
		PersistenceGmSession session = context.getSession();
		
		logger.info("Downloading resource '" + uploadedResourceName + "' from access '" + session.getAccessId() + "'");

		UploadedResource uploadedResource = getUploadedResourceByName(session, uploadedResourceName);
		
		if(uploadedResource == null) {
			logger.error("Failed to download resource from the access. Resource '" + uploadedResourceName + "' deoesn't exist on path '" + uploadPath + "'");
			throw new RuntimeException("Failed to download resource from the access. Resource '" + uploadedResourceName + "' deoesn't exist on path '" + uploadPath + "'");
		}

		DownloadResourceResponse response = DownloadResourceResponse.T.create();
		response.setResource(uploadedResource.getResource());

		logger.info("Successfully downloaded resource '" + uploadedResourceName + "' from access '" + session.getAccessId() + "'");

		return response;
	}

	/**
	 *  Downloads a resource from a file system. <br>
	 *  
	 *  Note: The Tribefire Explorer and Control Center don't yet support content type 'multipart/form-data'. For now, you can use client like Postman to test it out.
	 */
	private DownloadResourceResponse downloadResourceFromFileSystem(
			AccessRequestContext<DownloadResourceFromFileSystemRequest> context) {
		
		DownloadResourceFromFileSystemRequest request = context.getRequest();
		String uploadedResourceName = request.getUploadedResourceName();

		logger.info("Downloading resource '" + uploadedResourceName + "' from the file system on path '" + uploadPath + "'");

		Resource callResource = null;
		try {
			callResource = Resource.createTransient(FileStreamProviders.from(FileTools.newInputStream(new File(uploadPath + "/" + uploadedResourceName))));
		} catch(Exception e) {
			logger.error("Failed to download resource from the file system. Resource '" + uploadedResourceName + "' deoesn't exist on path '" + uploadPath + "'", e);
			throw new RuntimeException("Failed to download resource from the file system. Resource '" + uploadedResourceName + "' deoesn't exist on path '" + uploadPath + "'", e);
		}

		// 
		DownloadResourceResponse response = DownloadResourceResponse.T.create();
		response.setResource(callResource);

		logger.info("Successfully downloaded resource '" + uploadedResourceName + "' from the file system on path '" + uploadPath + "'");
		
		return response;
	}

	// -----------------------------------------------------------------------
	// HELPER METHODS
	// -----------------------------------------------------------------------

	private UploadedResource getUploadedResourceByName(PersistenceGmSession session, String uploadedResourceName) {
		// @formatter:off
		EntityQuery query = EntityQueryBuilder
					.from(UploadedResource.T)
						.where()
							.property(UploadedResource.resource_name)
							.eq(uploadedResourceName)
					.done();
		// @formatter:on
		
		UploadedResource uploadedResource = session.query().entities(query).first();
		return uploadedResource;
	}

	// -----------------------------------------------------------------------
	// GETTER & SETTER
	// -----------------------------------------------------------------------

	@Configurable
	@Required
	public void setExistingResourcesPath(String existingResourcesPath) {
		this.existingResourcesPath = existingResourcesPath;
	}
	
	@Configurable
	@Required
	public void setUploadPath(String uploadPath) {
		this.uploadPath = uploadPath;
	}
}
