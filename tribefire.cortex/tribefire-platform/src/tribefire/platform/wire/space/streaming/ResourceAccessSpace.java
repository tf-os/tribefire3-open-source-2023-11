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
package tribefire.platform.wire.space.streaming;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.Function;

import com.braintribe.cartridge.common.processing.deployment.ReflectBeansForDeployment;
import com.braintribe.cartridge.common.processing.streaming.StandardResourcePostPersistenceEnricher;
import com.braintribe.cartridge.common.processing.streaming.StandardResourcePrePersistenceEnricher;
import com.braintribe.exception.Exceptions;
import com.braintribe.model.extensiondeployment.HardwiredBinaryPersistence;
import com.braintribe.model.extensiondeployment.HardwiredBinaryProcessor;
import com.braintribe.model.extensiondeployment.HardwiredBinaryRetrieval;
import com.braintribe.model.extensiondeployment.HardwiredResourceEnricher;
import com.braintribe.model.processing.resource.basic.TemplateRetrieval;
import com.braintribe.model.processing.resource.filesystem.FileSystemBinaryProcessor;
import com.braintribe.model.processing.resource.filesystem.path.AccessFsPathResolver;
import com.braintribe.model.processing.resource.streaming.access.BasicResourceAccessFactory;
import com.braintribe.model.processing.resource.streaming.access.BasicResourceUrlBuilderSupplier;
import com.braintribe.model.processing.resource.streaming.cache.FSRepresentationCache;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.resourceapi.persistence.BinaryPersistenceRequest;
import com.braintribe.model.resourceapi.persistence.BinaryPersistenceResponse;
import com.braintribe.provider.Holder;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.platform.api.resource.ResourcesBuilder;
import tribefire.platform.impl.gmql.GmqlProcessor;
import tribefire.platform.impl.resourceaccess.ProxyResourceAccessFactory;
import tribefire.platform.impl.streaming.MimeBasedDispatchingResourceEnricher;
import tribefire.platform.impl.streaming.ResourceDownloadProcessor;
import tribefire.platform.impl.streaming.ResourceManipulationProcessor;
import tribefire.platform.wire.space.MasterResourcesSpace;
import tribefire.platform.wire.space.common.BindersSpace;
import tribefire.platform.wire.space.common.EndpointsSpace;
import tribefire.platform.wire.space.common.EnvironmentSpace;
import tribefire.platform.wire.space.common.ResourceProcessingSpace;
import tribefire.platform.wire.space.common.SpecificationsSpace;
import tribefire.platform.wire.space.cortex.GmSessionsSpace;
import tribefire.platform.wire.space.cortex.deployment.DeploymentSpace;
import tribefire.platform.wire.space.rpc.RpcSpace;
import tribefire.platform.wire.space.security.AuthContextSpace;

@Managed
public class ResourceAccessSpace implements WireSpace, ReflectBeansForDeployment {

	// @formatter:off
	@Import	private AuthContextSpace authContext;
	@Import	private BindersSpace binders;
	@Import	private DeploymentSpace deployment;
	@Import	private EndpointsSpace endpoints;
	@Import	private EnvironmentSpace environment;
	@Import	private GmSessionsSpace gmSessions;
	@Import	private GmSessionsSpace sessions;
	@Import	private MasterResourcesSpace resources;
	@Import	private MimeTypeSpace mimeType;
	@Import	private ResourceProcessingSpace resourceProcessing;
	@Import	private RpcSpace rpc;
	@Import	private SpecificationsSpace specifications;
	// @formatter:on	

	@Managed
	public ProxyResourceAccessFactory dynamicResourceAccessFactory() {
		ProxyResourceAccessFactory bean = new ProxyResourceAccessFactory();
		bean.setDeployedComponentResolver(deployment.proxyingDeployedComponentResolver());
		return bean;
	}

	@Managed
	public BasicResourceAccessFactory resourceAccessFactory() {
		BasicResourceAccessFactory bean = new BasicResourceAccessFactory();
		bean.setUrlBuilderSupplier(resourceUrlBuilderSupplier());
		bean.setStreamPipeFactory(resourceProcessing.streamPipeFactory());
		return bean;
	}

	@Managed
	public BasicResourceUrlBuilderSupplier resourceUrlBuilderSupplier() {
		BasicResourceUrlBuilderSupplier bean = new BasicResourceUrlBuilderSupplier();
		bean.setBaseStreamingUrl(endpoints.streamingUrl());
		bean.setSessionIdProvider(authContext.currentUser().userSessionIdProvider()::get);
		bean.setResponseMimeType("application/json");
		return bean;
	}

	@Managed
	public FileSystemBinaryProcessor fileSystemBinaryProcessor() {
		FileSystemBinaryProcessor bean = new FileSystemBinaryProcessor();
		bean.setFsPathResolver(accessPathResolver());
		bean.setTimestampPathFormat(DateTimeFormatter.ofPattern("yyMM/ddHH/mmss").withLocale(Locale.US));
		return bean;
	}

	@Managed
	public AccessFsPathResolver accessPathResolver() {
		AccessFsPathResolver bean = new AccessFsPathResolver();
		bean.setAccessPathSupplier(accessIdToResourcesBasePath());
		bean.setPathResolver(environment::resolve);

		return bean;
	}

	@Managed
	public Function<String, Path> accessIdToResourcesBasePath() {
		return accessId -> resourcesDirOf(accessId).asPath();
	}

	@Managed
	public Function<String, File> accessIdToResourcesBaseFile() {
		return accessId -> resourcesDirOf(accessId).asFile();
	}

	private ResourcesBuilder resourcesDirOf(String accessId) {
		return resources.database(accessId + "/resources");
	}

	@Managed
	public HardwiredBinaryProcessor fileSystemBinaryProcessorDeployable() {
		HardwiredBinaryProcessor bean = HardwiredBinaryProcessor.T.create();
		bean.setName("File System Binary Processor");
		bean.setExternalId("binaryProcessor.fileSystem");
		bean.setGlobalId("hardwired:service/" + bean.getExternalId());
		return bean;
	}

	public ServiceProcessor<? super BinaryPersistenceRequest, ? super BinaryPersistenceResponse> defaultBinaryPeristenceProcessor() {
		return fileSystemBinaryProcessor();
	}

	@Managed
	public HardwiredBinaryPersistence defaultBinaryPersistenceDeployable() {
		HardwiredBinaryPersistence bean = HardwiredBinaryPersistence.T.create();
		bean.setName("Default Binary Persistence");
		bean.setExternalId("binaryPersistence.default");
		bean.setGlobalId("hardwired:service/" + bean.getExternalId());
		return bean;
	}

	@Managed
	public HardwiredBinaryRetrieval templateBinaryRetrievalDeployable() {
		HardwiredBinaryRetrieval bean = HardwiredBinaryRetrieval.T.create();
		bean.setName("Template Binary Retrieval");
		bean.setExternalId("binaryRetrieval.template");
		bean.setGlobalId("hardwired:service/" + bean.getExternalId());
		return bean;
	}

	@Managed
	public MimeBasedDispatchingResourceEnricher mimeBasedDispatchingResourceEnricher() {
		MimeBasedDispatchingResourceEnricher bean = new MimeBasedDispatchingResourceEnricher();
		bean.setSystemModelAccessoryFactory(sessions.systemModelAccessoryFactory());
		return bean;
	}

	@Managed
	public HardwiredResourceEnricher mimeBasedDispatchingResourceEnricherDeployable() {
		HardwiredResourceEnricher bean = HardwiredResourceEnricher.T.create();
		bean.setName("MIME Based Dispatching Resource Enricher");
		bean.setExternalId(MimeBasedDispatchingResourceEnricher.externalId);
		bean.setGlobalId(MimeBasedDispatchingResourceEnricher.globalId);
		return bean;
	}

	@Managed
	public StandardResourcePrePersistenceEnricher mimeTypeDetectingEnricher() {
		StandardResourcePrePersistenceEnricher bean = new StandardResourcePrePersistenceEnricher();
		bean.setMimeTypeDetector(mimeType.detector());
		return bean;
	}

	public static final String mimeTypeDetectingEnricherExternalId = "resourceEnricher.prePersistence.mimeTypeDetector";
	public static final String mimeTypeDetectingEnricherGlobalId = "hardwired:service/" + mimeTypeDetectingEnricherExternalId;

	@Managed
	public HardwiredResourceEnricher mimeTypeDetectingEnricherDeployable() {
		HardwiredResourceEnricher bean = HardwiredResourceEnricher.T.create();
		bean.setName("Default MimeType Detecting Enricher");
		bean.setExternalId(mimeTypeDetectingEnricherExternalId);
		bean.setGlobalId(mimeTypeDetectingEnricherGlobalId);
		return bean;
	}

	public static final String standardPrePersistenceEnricherExternalId = "resourceEnricher.prePersistence.default";
	public static final String standardPrePersistenceEnricherGlobalId = "hardwired:service/" + standardPrePersistenceEnricherExternalId;

	// These are going to be separate instances soon (NOR, 13/11/2020)
	public static final String standardSpecificationEnricherExternalId = standardPrePersistenceEnricherExternalId;
	public static final String standardSpecificationEnricherGlobalId = standardPrePersistenceEnricherGlobalId;

	// @Managed
	public StandardResourcePrePersistenceEnricher standardSpecificationEnricher() {
		return standardPrePersistenceEnricher();
	}

	// @Managed
	public HardwiredResourceEnricher standardSpecificationEnricherDeployable() {
		return standardPrePersistenceEnricherDeployable();
	}

	@Managed
	public StandardResourcePrePersistenceEnricher standardPrePersistenceEnricher() {
		StandardResourcePrePersistenceEnricher bean = new StandardResourcePrePersistenceEnricher();
		bean.setMimeTypeDetector(mimeType.detector());
		bean.setStreamPipeFactory(resourceProcessing.streamPipeFactory());
		bean.setSpecificationDetector(specifications.standardSpecificationDetector());
		return bean;
	}

	@Managed
	public HardwiredResourceEnricher standardPrePersistenceEnricherDeployable() {
		HardwiredResourceEnricher bean = HardwiredResourceEnricher.T.create();
		bean.setName("Default Pre-persistence Resource Enricher");
		bean.setExternalId(standardPrePersistenceEnricherExternalId);
		bean.setGlobalId(standardPrePersistenceEnricherGlobalId);
		return bean;
	}

	@Managed
	public StandardResourcePostPersistenceEnricher standardPostPersistenceEnricher() {
		StandardResourcePostPersistenceEnricher bean = new StandardResourcePostPersistenceEnricher();
		bean.setSpecificationDetector(specifications.standardSpecificationDetector());
		return bean;
	}

	@Managed
	public HardwiredResourceEnricher standardPostPersistenceEnricherDeployable() {
		HardwiredResourceEnricher bean = HardwiredResourceEnricher.T.create();
		bean.setName("Default Post-persistence Resource Enricher");
		bean.setExternalId("resourceEnricher.postPersistence.default");
		bean.setGlobalId("hardwired:service/" + bean.getExternalId());
		return bean;
	}

	public GmqlProcessor gmqlProcessor() {
		GmqlProcessor bean = new GmqlProcessor();
		bean.setSessionFactory(sessions.sessionFactory());
		return bean;
	}

	@Managed
	public ResourceManipulationProcessor resourceManipulationProcessor() {
		ResourceManipulationProcessor bean = new ResourceManipulationProcessor();

		bean.setSystemEvaluator(rpc.systemServiceRequestEvaluator());

		return bean;
	}

	@Managed
	public ResourceDownloadProcessor resourceDownloadProcessor() {
		ResourceDownloadProcessor bean = new ResourceDownloadProcessor();

		bean.setSystemEvaluator(rpc.systemServiceRequestEvaluator());

		return bean;
	}

	@Managed
	public FSRepresentationCache fsCache() {
		FSRepresentationCache bean = new FSRepresentationCache();
		try {
			bean.setRootDirectory(resources.cache("/").asFile());
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Failed to resolve the cache directory");
		}
		return bean;
	}

	public TemplateRetrieval templateRetrieval() {
		TemplateRetrieval bean = new TemplateRetrieval();
		bean.setRequestSessionFactory(gmSessions.sessionFactory());
		bean.setSystemSessionFactory(gmSessions.systemSessionFactory());

		return bean;
	}

	@Managed
	public Holder<URL> repositoryProvider() {
		Holder<URL> bean = new Holder<>();
		try {
			bean.accept(resources.database("default/resources").asUrl());
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Failed to obtain a default resources URL");
		}
		return bean;
	}

}
