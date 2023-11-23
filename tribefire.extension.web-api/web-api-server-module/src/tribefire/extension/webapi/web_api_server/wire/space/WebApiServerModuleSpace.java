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
package tribefire.extension.webapi.web_api_server.wire.space;

import com.braintribe.ddra.endpoints.api.api.v1.DdraMappings;
import com.braintribe.model.ddra.DdraConfiguration;
import com.braintribe.model.ddra.DdraMapping;
import com.braintribe.model.extensiondeployment.HardwiredServicePostProcessor;
import com.braintribe.model.extensiondeployment.HardwiredServicePreProcessor;
import com.braintribe.model.extensiondeployment.StateChangeProcessor;
import com.braintribe.model.extensiondeployment.meta.OnChange;
import com.braintribe.model.extensiondeployment.meta.OnCreate;
import com.braintribe.model.extensiondeployment.meta.OnDelete;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.ddra.endpoints.DdraEndpointsExceptionHandler;
import com.braintribe.model.processing.ddra.endpoints.api.v1.ApiV1RestServletUtils;
import com.braintribe.model.processing.ddra.endpoints.api.v1.DdraConfigurationStateChangeProcessor;
import com.braintribe.model.processing.ddra.endpoints.api.v1.WebApiV1Server;
import com.braintribe.model.processing.ddra.endpoints.interceptors.HttpStreamingPostProcessor;
import com.braintribe.model.processing.ddra.endpoints.interceptors.HttpStreamingPreProcessor;
import com.braintribe.model.processing.ddra.endpoints.rest.v2.RestV2Server;
import com.braintribe.model.processing.deployment.api.binding.DenotationBindingBuilder;
import com.braintribe.model.processing.meta.editor.ModelMetaDataEditor;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.web.api.registry.FilterConfiguration;
import com.braintribe.web.api.registry.WebRegistries;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.cortex.initializer.support.integrity.wire.contract.CoreInstancesContract;
import tribefire.module.api.InitializerBindingBuilder;
import tribefire.module.api.WebRegistryConfiguration;
import tribefire.module.api.WireContractBindingBuilder;
import tribefire.module.wire.contract.TribefireModuleContract;
import tribefire.module.wire.contract.TribefireWebPlatformContract;
import tribefire.module.wire.contract.WebPlatformHardwiredDeployablesContract;

/**
 * This module's javadoc is yet to be written.
 */
@Managed
public class WebApiServerModuleSpace implements TribefireModuleContract {

	private static final String GROUP_ID = "tribefire.extension.web-api";
	private static final String MODEL_NAME_WEB_API_CONFIGURATION = GROUP_ID + ":web-api-configuration-model";
	private static final String WEB_API_V2_SERVLET_PATTERN = "/api/v1/*";
	private static final String REST_V2_SERVLET_PATTERN = "/rest/v2/*";
	private static final String DEFAULT_MIME_TYPE = "application/json";

	@Import
	private TribefireWebPlatformContract tfPlatform;

	@Import
	private RestV2HandlerSpace restV2Handler;
	
	@Import
	private TcSpace tc;
	
	//
	// WireContracts
	//

	@Override
	public void bindWireContracts(WireContractBindingBuilder bindings) {
		// Bind wire contracts to make them available for other modules.
		// Note that the Contract class cannot be defined in this module, but must be in a gm-api artifact.
	}

	//
	// Hardwired deployables
	//

	@Override
	public void bindHardwired() {
		WebPlatformHardwiredDeployablesContract hardwiredDeployables = tfPlatform.hardwiredDeployables();
		WebRegistryConfiguration webRegistry = hardwiredDeployables.webRegistry();
		
		webRegistry.addServlet( //
				 WebRegistries.servlet() //
				 .name("web-api-v2-server") //
				 .instance(apiV2Server()) //
				 .pattern(WEB_API_V2_SERVLET_PATTERN) //
		);
		
		webRegistry.lenientAuthFilter().addPattern(WEB_API_V2_SERVLET_PATTERN);
		
		webRegistry.addServlet( //
				WebRegistries.servlet() //
				.name("rest-v2-server") //
				.instance(restV2Server()) //
				.pattern(REST_V2_SERVLET_PATTERN) //
				.multipart() //
				);
		
		webRegistry.strictAuthFilter().addPattern(REST_V2_SERVLET_PATTERN);

		FilterConfiguration compressionFilter = webRegistry.compressionFilter();
		compressionFilter.addPattern(REST_V2_SERVLET_PATTERN);
		compressionFilter.addPattern(WEB_API_V2_SERVLET_PATTERN);
		
		FilterConfiguration threadRenamingFilter = webRegistry.threadRenamingFilter();
		threadRenamingFilter.addPattern(REST_V2_SERVLET_PATTERN);
		threadRenamingFilter.addPattern(WEB_API_V2_SERVLET_PATTERN);
		
		hardwiredDeployables //
			.bind(httpStreamingPreProcessorDeployable()) //
			.component(tfPlatform.binders().servicePreProcessor(), this::httpStreamingPreProcessor);
		
		hardwiredDeployables //
			.bind(httpStreamingPostProcessorDeployable()) //
			.component(tfPlatform.binders().servicePostProcessor(), this::httpStreamingPostProcessor);
		
		hardwiredDeployables //
			.bindStateChangeProcessor("web-api-configuration-monitor", "Web Api Configuration Monitor", this::ddraMappingsScp).getGlobalId();
	}
	

	@Managed
	private HttpStreamingPreProcessor httpStreamingPreProcessor() {
		return new HttpStreamingPreProcessor();
	}
	
	@Managed
	private HttpStreamingPostProcessor httpStreamingPostProcessor() {
		return new HttpStreamingPostProcessor();
	}

	// TODO: ask Gunther about usage of these Deployables
	@Managed
	public HardwiredServicePreProcessor httpStreamingPreProcessorDeployable() {
		HardwiredServicePreProcessor bean = HardwiredServicePreProcessor.T.create();
		bean.setName("HTTP Streaming PreProcessor");
		bean.setExternalId("preprocessor.http.streaming");
		bean.setGlobalId("hardwired:preprocessor/http.streaming");
		return bean;
	}
	
	@Managed
	public HardwiredServicePostProcessor httpStreamingPostProcessorDeployable() {
		HardwiredServicePostProcessor bean = HardwiredServicePostProcessor.T.create();
		bean.setName("HTTP Streaming PostProcessor");
		bean.setExternalId("postprocessor.http.streaming");
		bean.setGlobalId("hardwired:postprocessor/http.streaming");
		return bean;
	}


	@Managed
	private RestV2Server restV2Server() {
		RestV2Server bean = new RestV2Server();
		bean.setEvaluator(tfPlatform.requestUserRelated().evaluator());
		bean.setMarshallerRegistry(tfPlatform.marshalling().registry());
		bean.setExceptionHandler(exceptionHandler());
		bean.setHandlers(restV2Handler.handlers());
		bean.setModelAccessoryFactory(tfPlatform.requestUserRelated().modelAccessoryFactory());
		bean.setTraversingCriteriaMap(tc.criteriaMap());
		bean.setUsersSessionFactory(tfPlatform.requestUserRelated().sessionFactory());
		bean.setSystemSessionFactory(tfPlatform.systemUserRelated().sessionFactory());
		bean.setAccessAvailability(tfPlatform.deployment().deployRegistry()::isDeployed);
		return bean;
	}
	
	@Managed
	public DdraConfigurationStateChangeProcessor ddraMappingsScp() {
		DdraConfigurationStateChangeProcessor bean = new DdraConfigurationStateChangeProcessor();
		bean.setMappings(ddraMappings());
		return bean;
	}
	
	@Managed
	private WebApiV1Server apiV2Server() {
		WebApiV1Server bean = new WebApiV1Server();
		
		bean.setEvaluator(tfPlatform.requestUserRelated().evaluator());
		bean.setMarshallerRegistry(tfPlatform.marshalling().registry());
		bean.setExceptionHandler(exceptionHandler());
		bean.setMappings(ddraMappings());
		bean.setTraversingCriteriaMap(tc.criteriaMap());
		bean.setUsersSessionFactory(tfPlatform.requestUserRelated().sessionFactory());
		bean.setSystemSessionFactory(tfPlatform.systemUserRelated().sessionFactory());
		bean.setSessionIdProvider(tfPlatform.requestUserRelated().userSessionIdSupplier());
		bean.setModelAccessoryFactory(tfPlatform.requestUserRelated().modelAccessoryFactory());
		bean.setStreamPipeFactory(tfPlatform.resourceProcessing().streamPipeFactory());
		bean.setRestServletUtils(restServletUtils());
		
		return bean;
	}
	
	@Managed
	private ApiV1RestServletUtils restServletUtils() {
		ApiV1RestServletUtils bean = new ApiV1RestServletUtils();
		bean.setMimeTypeRegistry(tfPlatform.resourceProcessing().mimeTypeRegistry());
		return bean;
	}

	
	@Managed
	private DdraMappings ddraMappings() {
		return new DdraMappings();
	}

	
	@Managed
	private DdraEndpointsExceptionHandler exceptionHandler() {
		DdraEndpointsExceptionHandler handler = new DdraEndpointsExceptionHandler();
		handler.setIncludeDebugInformation(true);
		handler.setDefaultMarshaller(tfPlatform.marshalling().jsonMarshaller());
		handler.setDefaultMimeType(DEFAULT_MIME_TYPE);

		return handler;
	}
	
	//
	// Deployables
	//

	@Override
	public void bindDeployables(DenotationBindingBuilder bindings) {
		// Bind deployment experts for deployable denotation types.
		// Note that the basic component binders (for e.g. serviceProcessor or incrementalAccess) can be found via tfPlatform.deployment().binders(). 
	}

	@Override
	public void bindInitializers(InitializerBindingBuilder bindings) {
		bindings.bind(this::initDdraConfiguration);
	}
	
	private void initDdraConfiguration(PersistenceInitializationContext context) {
		context.getSession().create(DdraConfiguration.T, "ddra:config");
	}
	
}
