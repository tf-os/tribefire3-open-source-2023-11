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
package com.braintribe.model.processing.aws.service;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.LifecycleAware;
import com.braintribe.cfg.Required;
import com.braintribe.exception.Exceptions;
import com.braintribe.logging.Logger;
import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.aws.api.ConnectionStatistics;
import com.braintribe.model.aws.deployment.S3Connector;
import com.braintribe.model.aws.deployment.cloudfront.CloudFrontConfiguration;
import com.braintribe.model.aws.deployment.processor.S3BinaryProcessor;
import com.braintribe.model.aws.resource.S3Source;
import com.braintribe.model.aws.service.AcquireCloudFrontKeyPair;
import com.braintribe.model.aws.service.AcquiredCloudFrontKeyPair;
import com.braintribe.model.aws.service.AwsRequest;
import com.braintribe.model.aws.service.AwsResult;
import com.braintribe.model.aws.service.CheckConnection;
import com.braintribe.model.aws.service.CloudFrontUrl;
import com.braintribe.model.aws.service.ConnectionStatus;
import com.braintribe.model.aws.service.CreateCloudFrontUrlForResource;
import com.braintribe.model.aws.service.CreatePresignedUrlForResource;
import com.braintribe.model.aws.service.PresignedUrl;
import com.braintribe.model.extensiondeployment.BinaryRetrieval;
import com.braintribe.model.extensiondeployment.meta.BinaryProcessWith;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.aws.util.AwsUtil;
import com.braintribe.model.processing.aws.util.Keys;
import com.braintribe.model.processing.deployment.api.DeployRegistry;
import com.braintribe.model.processing.deployment.api.DeployedUnit;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.meta.cmd.CmdResolverImpl;
import com.braintribe.model.processing.meta.oracle.BasicModelOracle;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.impl.AbstractDispatchingServiceProcessor;
import com.braintribe.model.processing.service.impl.DispatchConfiguration;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.resource.Resource;
import com.braintribe.utils.lcd.StringTools;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

public class AwsServiceProcessor extends AbstractDispatchingServiceProcessor<AwsRequest, AwsResult> implements LifecycleAware {

	private final static Logger logger = Logger.getLogger(AwsServiceProcessor.class);

	private DeployRegistry deployRegistry;

	private Supplier<PersistenceGmSession> cortexSessionSupplier;
	private PersistenceGmSessionFactory sessionFactory;
	private Cache<String, S3Connector> connectorsPerAccess = null;

	public AwsServiceProcessor() {
		connectorsPerAccess = Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.DAYS).maximumSize(100).build();
	}

	@Override
	protected void configureDispatching(DispatchConfiguration<AwsRequest, AwsResult> dispatching) {
		dispatching.register(CheckConnection.T, this::checkConnection);
		dispatching.register(CreatePresignedUrlForResource.T, this::createPresignedUrlForResource);
		dispatching.register(CreateCloudFrontUrlForResource.T, this::createCloudFrontUrlForResource);
		dispatching.register(AcquireCloudFrontKeyPair.T, this::acquireCloudFrontKeyPair);
	}

	private AcquiredCloudFrontKeyPair acquireCloudFrontKeyPair(@SuppressWarnings("unused") ServiceRequestContext context,
			AcquireCloudFrontKeyPair request) {

		PersistenceGmSession cortexSession = cortexSessionSupplier.get();
		String accessId = request.getAccessId();
		S3Connector connector = getConnectorForAccess(cortexSession, accessId);

		CloudFrontConfiguration configuration = connector.getCloudFrontConfiguration();
		if (configuration == null) {
			Keys keys = AwsUtil.generateKeyPair(request.getKeysize());

			configuration = cortexSession.create(CloudFrontConfiguration.T);
			configuration.setKeyGroupId(request.getKeyGroupId());
			configuration.setPrivateKey(keys.getPrivateKeyBase64());
			configuration.setPublicKey(keys.getPublicKeyBase64());
			configuration.setPublicKeyPem(keys.getPublicKeyPem());
			cortexSession.commit();
		}

		AcquiredCloudFrontKeyPair result = AcquiredCloudFrontKeyPair.T.create();
		result.setKeyGroupId(configuration.getKeyGroupId());
		result.setPublicKeyPem(configuration.getPublicKeyPem());
		result.setSuccess(true);
		return result;
	}

	private CloudFrontUrl createCloudFrontUrlForResource(@SuppressWarnings("unused") ServiceRequestContext context,
			CreateCloudFrontUrlForResource request) {

		PersistenceGmSession cortexSession = cortexSessionSupplier.get();
		String accessId = request.getAccessId();
		S3Connector connector = getConnectorForAccess(cortexSession, accessId);
		CloudFrontConfiguration configuration = connector.getCloudFrontConfiguration();

		if (configuration == null) {
			throw new IllegalStateException("Could not find a matching CloudFrontConfiguration.");
		}

		String keyGroupId = configuration.getKeyGroupId();
		String privateKeybase64 = configuration.getPrivateKey();

		Boolean preSignUrl = request.getPreSignUrl();
		boolean preSignConfigAvailable = !StringTools.isAnyBlank(keyGroupId, privateKeybase64);
		boolean preSignRequested = preSignUrl != null && preSignUrl.booleanValue();

		if (!preSignConfigAvailable && preSignRequested) {
			throw new IllegalStateException("The CloudFront configuration for access " + accessId
					+ " does not have a valid CloudFront configuration. Either the private key or the key group Id is missing.");
		}

		String baseUrl = configuration.getBaseUrl();
		if (!baseUrl.endsWith("/")) {
			baseUrl += "/";
		}
		String resourceId = request.getResourceId();
		S3Source s3Source = getS3SourceForResource(accessId, resourceId);
		String url = baseUrl + s3Source.getKey();

		CloudFrontUrl result = CloudFrontUrl.T.create();

		if (preSignRequested || (preSignUrl == null && preSignConfigAvailable)) {

			int timeToLiveInMin = request.getTimeToLiveInMin();
			if (timeToLiveInMin <= 0) {
				throw new IllegalArgumentException("The TTL must be a positive number.");
			}
			GregorianCalendar gc = new GregorianCalendar();
			gc.add(Calendar.MINUTE, timeToLiveInMin);

			PrivateKey privateKey = getPrivateKeyFromBase64Encoded(privateKeybase64);
			Date expiry = gc.getTime();

			String signedUrl = AwsUtil.getSignedUrlWithCannedPolicy(url, keyGroupId, privateKey, expiry);

			result.setUrl(signedUrl);
			result.setIsPreSigned(true);

		} else {
			result.setUrl(url);
			result.setIsPreSigned(false);
		}

		return result;
	}

	private PrivateKey getPrivateKeyFromBase64Encoded(String base64Encoded) {

		byte[] privKeyBytes = Base64.getDecoder().decode(base64Encoded);
		try {
			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privKeyBytes);
			KeyFactory kf = KeyFactory.getInstance("RSA");
			PrivateKey privKey = kf.generatePrivate(keySpec);
			return privKey;
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Could not decode private key");
		}

	}

	private PresignedUrl createPresignedUrlForResource(@SuppressWarnings("unused") ServiceRequestContext context,
			CreatePresignedUrlForResource request) {

		PresignedUrl result = PresignedUrl.T.create();

		String accessId = request.getAccessId();
		String resourceId = request.getResourceId();
		long timeToLiveInMs = request.getTimeToLiveInMs();

		S3Source s3Source = getS3SourceForResource(accessId, resourceId);
		PersistenceGmSession cortexSession = cortexSessionSupplier.get();
		S3Connector connector = getConnectorForAccess(cortexSession, accessId);

		String bucketName = s3Source.getBucketName();
		String key = s3Source.getKey();

		DeployedUnit resolve = deployRegistry.resolve(connector);
		if (resolve != null) {
			com.braintribe.model.processing.aws.connect.S3Connector connectorImpl = (com.braintribe.model.processing.aws.connect.S3Connector) resolve
					.findComponent(S3Connector.T);
			String url = connectorImpl.generatePresignedUrl(bucketName, key, timeToLiveInMs);
			result.setPreSignedUrl(url);
		}

		return result;
	}

	private S3Connector getConnectorForAccess(PersistenceGmSession cortexSession, String accessId) {
		return connectorsPerAccess.get(accessId, a -> {
			IncrementalAccess access = null;
			try {
				EntityQuery query = EntityQueryBuilder.from(IncrementalAccess.T).where().property(IncrementalAccess.externalId).eq(accessId).done();
				access = cortexSession.query().entities(query).first();
			} catch (Exception e) {
				throw Exceptions.unchecked(e, "Could not obtain access " + accessId);
			}
			BinaryProcessWith processWith = null;
			try {
				GmMetaModel metaModel = access.getMetaModel();
				ModelOracle oracle = new BasicModelOracle(metaModel);
				CmdResolver cmdResolver = CmdResolverImpl.create(oracle).done();
				processWith = cmdResolver.getMetaData().entityType(S3Source.T).access(accessId).meta(BinaryProcessWith.T).exclusive();
			} catch (Exception e) {
				throw Exceptions.unchecked(e,
						"Error while trying to get the BinaryProcessWith metadata that is supposed to be attached to access " + accessId);
			}
			if (processWith == null) {
				throw new IllegalStateException("Could not get the BinaryProcessWith metadata that is supposed to be attached to access " + accessId);
			}
			BinaryRetrieval retrieval = processWith.getRetrieval();
			if (!(retrieval instanceof S3BinaryProcessor)) {
				throw new IllegalStateException(
						"The BinaryProcessWith (" + retrieval + ") that is attached to access " + accessId + " is not of type S3BinaryProcessor");
			}
			try {
				S3BinaryProcessor s3Retrieval = (S3BinaryProcessor) retrieval;
				S3Connector connector = s3Retrieval.getConnection();
				return connector;
			} catch (Exception e) {
				throw Exceptions.unchecked(e, "Could not get the connector attached to access " + accessId);
			}
		});
	}

	private S3Source getS3SourceForResource(String accessId, String resourceId) {
		try {
			PersistenceGmSession session = sessionFactory.newSession(accessId);
			EntityQuery query = EntityQueryBuilder.from(Resource.T).where().property(Resource.id).eq(resourceId).done();
			Resource resource = session.query().entities(query).first();
			S3Source resourceSource = (S3Source) resource.getResourceSource();
			return resourceSource;
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Could not obtain resource " + resourceId + " from access " + accessId);
		}
	}

	private ConnectionStatus checkConnection(@SuppressWarnings("unused") ServiceRequestContext context, CheckConnection request) {

		ConnectionStatus result = ConnectionStatus.T.create();

		S3Connector connector = request.getConnector();

		DeployedUnit resolve = deployRegistry.resolve(connector);

		long start = System.currentTimeMillis();
		if (resolve != null) {
			com.braintribe.model.processing.aws.connect.S3Connector connectorImpl = (com.braintribe.model.processing.aws.connect.S3Connector) resolve
					.findComponent(S3Connector.T);

			int totalCount = 0;
			Set<String> bucketsList = connectorImpl.getBucketsList();
			if (bucketsList != null) {
				totalCount = bucketsList.size();
			}

			result.setBucketCount(totalCount);

			Map<String, ConnectionStatistics> stats = connectorImpl.getStatisticsPerRegion();
			result.setStatisticsPerRegion(stats);
		}

		result.setDurationInMs(System.currentTimeMillis() - start);

		return result;
	}

	@Override
	public void postConstruct() {
		logger.debug(() -> AwsServiceProcessor.class.getSimpleName() + " deployed.");
	}

	@Override
	public void preDestroy() {
		logger.debug(() -> AwsServiceProcessor.class.getSimpleName() + " undeployed.");
	}

	@Configurable
	@Required
	public void setDeployRegistry(DeployRegistry deployRegistry) {
		this.deployRegistry = deployRegistry;
	}
	@Required
	@Configurable
	public void setCortexSessionSupplier(Supplier<PersistenceGmSession> cortexSessionSupplier) {
		this.cortexSessionSupplier = cortexSessionSupplier;
	}
	@Required
	@Configurable
	public void setSessionFactory(PersistenceGmSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

}
