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
package tribefire.platform.impl.streaming;

import static com.braintribe.utils.lcd.CollectionTools2.isEmpty;
import static java.util.Collections.emptyList;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.extensiondeployment.BinaryPersistence;
import com.braintribe.model.extensiondeployment.ResourceEnricher;
import com.braintribe.model.extensiondeployment.meta.PersistResourceWith;
import com.braintribe.model.extensiondeployment.meta.PreEnrichResourceWith;
import com.braintribe.model.extensiondeployment.meta.UploadWith;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.meta.data.EntityTypeMetaData;
import com.braintribe.model.processing.accessrequest.api.AbstractDispatchingAccessRequestProcessor;
import com.braintribe.model.processing.accessrequest.api.AccessRequestContext;
import com.braintribe.model.processing.accessrequest.api.DispatchConfiguration;
import com.braintribe.model.processing.session.api.common.GmSessions;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.source.ResourceSource;
import com.braintribe.model.resourceapi.base.ResourceRequest;
import com.braintribe.model.resourceapi.base.ResourceSourceRequest;
import com.braintribe.model.resourceapi.enrichment.EnrichResource;
import com.braintribe.model.resourceapi.enrichment.EnrichResourceResponse;
import com.braintribe.model.resourceapi.persistence.DeleteBinary;
import com.braintribe.model.resourceapi.persistence.DeleteResource;
import com.braintribe.model.resourceapi.persistence.DeleteResourceResponse;
import com.braintribe.model.resourceapi.persistence.DeleteSource;
import com.braintribe.model.resourceapi.persistence.DeleteSourceResponse;
import com.braintribe.model.resourceapi.persistence.DeletionScope;
import com.braintribe.model.resourceapi.persistence.ManageResource;
import com.braintribe.model.resourceapi.persistence.ManageResourceResponse;
import com.braintribe.model.resourceapi.persistence.StoreBinary;
import com.braintribe.model.resourceapi.persistence.StoreBinaryResponse;
import com.braintribe.model.resourceapi.persistence.UpdateResource;
import com.braintribe.model.resourceapi.persistence.UploadResource;
import com.braintribe.model.resourceapi.persistence.UploadResourceResponse;
import com.braintribe.model.resourceapi.persistence.UploadResources;
import com.braintribe.model.resourceapi.persistence.UploadResourcesResponse;
import com.braintribe.model.resourceapi.persistence.UploadSource;
import com.braintribe.model.resourceapi.persistence.UploadSourceResponse;
import com.braintribe.model.resourceapi.stream.DownloadResource;
import com.braintribe.model.service.api.ServiceRequest;

/**
 * <p>
 * Central processor of {@link ManageResource} requests like {@link UploadResource} and {@link DownloadResource}.
 * 
 */
public class ResourceManipulationProcessor extends AbstractDispatchingAccessRequestProcessor<ResourceRequest, ManageResourceResponse> {

	private static final Logger log = Logger.getLogger(ResourceManipulationProcessor.class);
	
	private Evaluator<ServiceRequest> systemEvaluator;

	@Override
	protected void configureDispatching(DispatchConfiguration dispatching) {
		dispatching.register(UploadResource.T, this::upload);
		dispatching.register(UpdateResource.T, this::update);
		dispatching.register(DeleteResource.T, this::delete);

//		TODO: These yet unused requests don't work with the new security. Find a way to support them again or delete them
//		dispatching.register(DeleteSource.T, this::deleteSource);
//		dispatching.register(UploadSource.T, this::uploadSource);

		dispatching.register(UploadResources.T, this::bulkUpload);
	}
	
	@Configurable
	@Required
	public void setSystemEvaluator(Evaluator<ServiceRequest> systemEvaluator) {
		this.systemEvaluator = systemEvaluator;
	}

	private DeleteSourceResponse deleteSource(AccessRequestContext<DeleteSource> context) {
		DeleteSource originalRequest = context.getOriginalRequest();

		DeletionScope deletionScope = originalRequest.getDeleteSourceEntity() //
				? DeletionScope.source //
				: DeletionScope.binary;

		DeleteResource deleteBinary = delegateRequest(originalRequest, DeleteResource.T);
		deleteBinary.setDeletionScope(deletionScope);

		deleteBinary.eval(context.getSession()).get();

		return DeleteSourceResponse.T.create();
	}

	private UploadSourceResponse uploadSource(AccessRequestContext<UploadSource> context) {
		UploadSource originalRequest = context.getOriginalRequest();

		UploadResource uploadResource = delegateRequest(originalRequest, UploadResource.T);
		uploadResource.setSourceType(originalRequest.getSourceType());
		uploadResource.setUseCase(originalRequest.getUseCase());

		UploadResourceResponse uploadResourceResponse = uploadResource.eval(context.getSession()).get();

		UploadSourceResponse response = UploadSourceResponse.T.create();
		response.setResource(uploadResourceResponse.getResource());

		return response;
	}

	private <T extends ManageResource> T delegateRequest(ResourceSourceRequest originalRequest, EntityType<T> delegateRequestType) {
		Resource resource = Resource.T.create();
		resource.setResourceSource(originalRequest.getResourceSource());

		T delegateRequest = delegateRequestType.create();
		delegateRequest.setResource(resource);

		return delegateRequest;
	}

	protected UploadResourceResponse upload(AccessRequestContext<UploadResource> context) {
		PersistenceGmSession session = context.getSession();
		UploadResource originalRequest = context.getOriginalRequest();
		StoreBinaryResponse storeResponse = _upload(originalRequest, originalRequest.getResource(), context);

		Resource clonedResource = GmSessions.cloneIntoSession(storeResponse.getResource(), session);

		UploadResourceResponse response = UploadResourceResponse.T.create();
		response.setResource(clonedResource);

		return response;
	}

	protected UploadResourcesResponse bulkUpload(AccessRequestContext<UploadResources> context) {
		UploadResources originalRequest = context.getOriginalRequest();
		Boolean detectMimeType = originalRequest.getDetectMimeType();

		UploadResourcesResponse response = UploadResourcesResponse.T.create();

		for (Resource resource : originalRequest.getResources()) {
			UploadResource uploadResource = UploadResource.T.create();
			uploadResource.setDomainId(context.getDomainId());
			uploadResource.setResource(resource);
			uploadResource.setSourceType(originalRequest.getSourceType());
			uploadResource.setUseCase(originalRequest.getUseCase());

			UploadResourceResponse uploadResourceResponse = evalUploadResource(context.getSession(), resource, uploadResource, detectMimeType);

			response.getResources().add(uploadResourceResponse.getResource());
		}

		return response;
	}

	private UploadResourceResponse evalUploadResource(PersistenceGmSession session, Resource resource, UploadResource uploadResource,
			Boolean detectMimeType) {
		String requestResourceMimeType = resource.getMimeType();

		if (detectMimeType == Boolean.TRUE) {
			resource.setMimeType(requestResourceMimeType);
		} else if (detectMimeType == Boolean.FALSE && resource.getMimeType() == null) {
			resource.setMimeType("application/octet-stream");
		}

		UploadResourceResponse uploadResourceResponse = uploadResource.eval(session).get();

		Resource responseResource = uploadResourceResponse.getResource();

		if (detectMimeType == Boolean.TRUE && responseResource != null && responseResource.getMimeType() == null) {
			responseResource.setMimeType(requestResourceMimeType);
		}

		return uploadResourceResponse;
	}

	private StoreBinaryResponse _upload(UploadResource originalRequest, Resource resource, AccessRequestContext<?> context) {
		PersistenceGmSession session = context.getSession();

		EntityType<? extends ResourceSource> sourceType;

		if (originalRequest.getSourceType() == null) {
			sourceType = ResourceSource.T;
			log.trace(() -> "No source type given, defaulting to " + ResourceSource.T);
		} else {
			sourceType = GMF.getTypeReflection().getEntityType(originalRequest.getSourceType());
		}

		String requestUseCase = originalRequest.getUseCase();
		PersistResourceWith persistWith = resolvePersistWith(session, requestUseCase, sourceType);

		StoreBinary storeRequest = StoreBinary.T.create();
		storeRequest.setServiceId(persistWith.getPersistence().getExternalId());
		storeRequest.setCreateFrom(resource);
		storeRequest.setPersistResource(false);
		storeRequest.setDomainId(context.getDomainId());

		List<ResourceEnricher> preEnrichers = resovePreEnrichers(session, sourceType, requestUseCase);
		storeRequest = preEnrich(session, preEnrichers, storeRequest);

		StoreBinaryResponse storeResponse = storeRequest.eval(systemEvaluator).get();

		ensureUseCase(requestUseCase, storeResponse);

		UploadWith uploadWith = resolveMd(UploadWith.T, session, requestUseCase, sourceType);
		postEnrich(session, uploadWith, storeResponse);

		return storeResponse;
	}

	private List<ResourceEnricher> resovePreEnrichers(PersistenceGmSession session, EntityType<? extends ResourceSource> sourceType, String useCase) {
		PreEnrichResourceWith preEnrichWith = resolveMd(PreEnrichResourceWith.T, session, useCase, sourceType);
		if (preEnrichWith != null)
			return preEnrichWith.getPrePersistenceEnrichers();

		UploadWith uploadWith = resolveMd(UploadWith.T, session, useCase, sourceType);
		if (uploadWith != null)
			return uploadWith.getPrePersistenceEnrichers();

		return emptyList();
	}

	private void ensureUseCase(String requestUseCase, StoreBinaryResponse storeResponse) {
		if (requestUseCase != null) {
			ResourceSource resourceSource = storeResponse.getResource().getResourceSource();

			if (resourceSource.getUseCase() == null)
				resourceSource.setUseCase(requestUseCase);
		}
	}

	protected UploadResourceResponse update(AccessRequestContext<UpdateResource> context) {
		PersistenceGmSession session = context.getSession();
		UpdateResource originalRequest = context.getOriginalRequest();
		Resource resource = originalRequest.getResource();

		StoreBinaryResponse storeResponse = _upload(originalRequest, resource, context);

		Resource persistedResource = session.query().entity(Resource.T, resource.getId()).find();

		if (originalRequest.getDeleteOldResourceSource())
			_delete(persistedResource, DeletionScope.source, context);

		Resource enrichedResource = storeResponse.getResource();
		transferMetadata(enrichedResource, persistedResource);

		ResourceSource clonedResourceSource = GmSessions.cloneIntoSession(enrichedResource.getResourceSource(), session);
		persistedResource.setResourceSource(clonedResourceSource);

		UploadResourceResponse response = UploadResourceResponse.T.create();
		response.setResource(persistedResource);

		return response;

	}

	protected DeleteResourceResponse delete(AccessRequestContext<DeleteResource> context) {
		DeleteResource request = context.getOriginalRequest();

		return _delete(getResource(context), request.getDeletionScope(), context);
	}
	
	private Resource getResource(AccessRequestContext<? extends ManageResource> context) {
		// The resource needs to be queried again because the caller might send invalid or malicious parts with the request, i.e. the ResourceSource
		Resource requestResource = context.getOriginalRequest().getResource();
		return context.getSession().query().entity(requestResource).require();
	}

	private DeleteResourceResponse _delete(Resource resource, DeletionScope deletionScope, AccessRequestContext<?> context) {
		EntityType<? extends ResourceSource> sourceType = resource.getResourceSource() != null ? resource.getResourceSource().entityType() : null;
		if (sourceType == null)
			throw new IllegalStateException("The resource does not have a source: " + resource);

		PersistenceGmSession session = context.getSession();
		PersistResourceWith persistWith = resolvePersistWith(session, resource.getResourceSource().getUseCase(), sourceType);

		DeleteBinary deleteRequest = DeleteBinary.T.create();
		deleteRequest.setServiceId(persistWith.getPersistence().getExternalId());
		deleteRequest.setResource(resource);
		deleteRequest.setDomainId(context.getDomainId());

		deleteRequest.eval(systemEvaluator).get();

		if (deletionScope != null) {
			switch (deletionScope) {
				case resource:
					session.deleteEntity(resource);
					//$FALL-THROUGH$
				case source:
					ResourceSource resourceSource = resource.getResourceSource();
					if (resourceSource != null)
						session.deleteEntity(resourceSource);
					break;
				default:
					// Do nothing
			}
		}

		return DeleteResourceResponse.T.create();
	}

	protected PersistResourceWith resolvePersistWith(PersistenceGmSession session, String useCase, EntityType<? extends ResourceSource> sourceType) {
		PersistResourceWith result = resolveMd(PersistResourceWith.T, session, useCase, sourceType);

		if (result == null)
			throw new GmSessionException("Unable to retrieve the upload processor. No " + PersistResourceWith.T.getTypeSignature() + " resolved for "
					+ sourceType.getTypeSignature() + (useCase != null ? " and use case " + useCase : ""));

		BinaryPersistence persistence = result.getPersistence();

		if (persistence == null)
			throw new GmSessionException(
					"Unable to retrieve the persistence processor. No " + BinaryPersistence.T.getTypeSignature() + " set on metadata: " + result);

		log.trace(() -> descResolution(result, sourceType, useCase));

		return result;
	}

	private <MD extends EntityTypeMetaData> MD resolveMd(EntityType<MD> mdType, PersistenceGmSession session, String useCase,
			EntityType<? extends ResourceSource> sourceType) {
		return session.getModelAccessory().getCmdResolver().getMetaData() //
				.entityType(sourceType) //
				.useCase(useCase) //
				.meta(mdType) //
				.exclusive();
	}

	protected StoreBinary preEnrich(PersistenceGmSession session, List<ResourceEnricher> preEnrichers, StoreBinary storeRequest) {
		if (isEmpty(preEnrichers))
			return storeRequest;

		Resource createFrom = storeRequest.getCreateFrom();

		for (ResourceEnricher enricher : preEnrichers) {
			EnrichResource request = EnrichResource.T.create();
			request.setResource(createFrom);
			request.setServiceId(enricher.getExternalId());

			EnrichResourceResponse response = request.eval(session).get();

			Resource enrichedResource = response.getResource();

			if (enrichedResource != null)
				createFrom = enrichedResource;
		}

		storeRequest.setCreateFrom(createFrom);

		return storeRequest;
	}

	protected void postEnrich(PersistenceGmSession session, UploadWith uploadWith, StoreBinaryResponse storeResponse) {
		if (uploadWith == null)
			return;

		List<ResourceEnricher> enrichers = uploadWith.getPostPersistenceEnrichers();
		if (isEmpty(enrichers))
			return;

		Resource storedResource = storeResponse.getResource();

		for (ResourceEnricher enricher : enrichers) {
			EnrichResource entichRequest = EnrichResource.T.create();
			entichRequest.setResource(storedResource);
			entichRequest.setServiceId(enricher.getExternalId());

			EnrichResourceResponse response = entichRequest.eval(session).get();

			Resource enrichedResource = response.getResource();

			if (enrichedResource != null)
				transferMetadata(enrichedResource, storedResource);
		}
	}

	protected void transferMetadata(Resource source, Resource target) {
		if (source.getMd5() != null)
			target.setMd5(source.getMd5());

		if (source.getName() != null)
			target.setName(source.getName());

		if (source.getFileSize() != null)
			target.setFileSize(source.getFileSize());

		if (source.getMimeType() != null)
			target.setMimeType(source.getMimeType());

		if (source.getCreated() != null)
			target.setCreated(source.getCreated());

		if (source.getCreator() != null)
			target.setCreator(source.getCreator());

		if (source.getSpecification() != null) {
			PersistenceGmSession targetSession = (PersistenceGmSession) target.session();
			target.setSpecification(GmSessions.cloneIntoSession(source.getSpecification(), targetSession));
		}

		if (source.getTags() != null && !source.getTags().isEmpty()) {
			if (target.getTags() == null) {
				Set<String> tags = new HashSet<>();
				tags.addAll(source.getTags());
				target.setTags(tags);
			} else {
				target.getTags().addAll(source.getTags());
			}
		}

	}

	private String descResolution(EntityTypeMetaData md, EntityType<? extends ResourceSource> sourceType, String useCase) {
		StringBuilder sb = new StringBuilder();

		sb.append("Resolved resoruce processing meta-data for source type [");
		sb.append(sourceType.getTypeSignature());
		sb.append("]");
		if (useCase != null) {
			sb.append(" and use case [").append(useCase).append("]");
		}
		if (md instanceof PersistResourceWith) {
			sb.append("Binary persistence: ");
			sb.append(((PersistResourceWith) md).getPersistence());
		}
		sb.append(". ");

		if (md instanceof PreEnrichResourceWith) {
			PreEnrichResourceWith preEnrichWith = (PreEnrichResourceWith) md;
			if (preEnrichWith.getPrePersistenceEnrichers().isEmpty()) {
				sb.append("No pre-persistence enrichers. ");
			} else {
				sb.append("Pre-persistence enrichers: ");
				sb.append(preEnrichWith.getPrePersistenceEnrichers());
				sb.append(" ");
			}
		}

		if (md instanceof UploadWith) {
			UploadWith uploadWith = (UploadWith) md;
			if (uploadWith.getPostPersistenceEnrichers().isEmpty()) {
				sb.append("No post-persistence enrichers. ");
			} else {
				sb.append("Post-persistence enrichers: ");
				sb.append(uploadWith.getPostPersistenceEnrichers());
			}
		}

		return sb.toString();

	}

}
