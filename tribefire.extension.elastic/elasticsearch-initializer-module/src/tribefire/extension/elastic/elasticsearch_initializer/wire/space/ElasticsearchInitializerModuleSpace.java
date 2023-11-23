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
package tribefire.extension.elastic.elasticsearch_initializer.wire.space;

import static com.braintribe.utils.lcd.CollectionTools2.asSet;

import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.accessdeployment.aspect.AspectConfiguration;
import com.braintribe.model.accessdeployment.smood.SmoodAccess;
import com.braintribe.model.elasticsearchdeployment.ElasticsearchService;
import com.braintribe.model.elasticsearchdeployment.admin.ElasticsearchAdminServlet;
import com.braintribe.model.elasticsearchdeployment.aspect.ExtendedFulltextAspect;
import com.braintribe.model.elasticsearchdeployment.service.ElasticServiceProcessor;
import com.braintribe.model.elasticsearchdeployment.service.HealthCheckProcessor;
import com.braintribe.model.extensiondeployment.check.CheckBundle;
import com.braintribe.model.extensiondeployment.meta.ProcessWith;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.cortex.initializer.support.wire.space.AbstractInitializerSpace;
import tribefire.cortex.model.check.CheckCoverage;
import tribefire.cortex.model.check.CheckWeight;
import tribefire.extension.elastic.elasticsearch_initializer.wire.contract.ElasticsearchInitializerModuleContract;
import tribefire.extension.elastic.elasticsearch_initializer.wire.contract.ElasticsearchInitializerModuleModelsContract;
import tribefire.extension.elastic.elasticsearch_initializer.wire.contract.ExistingInstancesContract;
import tribefire.extension.elastic.elasticsearch_initializer.wire.contract.RuntimePropertiesContract;
import tribefire.extension.elastic.templates.api.ElasticTemplateContext;
import tribefire.extension.elastic.templates.wire.contract.ElasticTemplatesContract;

/**
 * @see ElasticsearchInitializerModuleContract
 */
@Managed
public class ElasticsearchInitializerModuleSpace extends AbstractInitializerSpace implements ElasticsearchInitializerModuleContract {

	@Import
	ElasticsearchInitializerModuleModelsContract models;

	@Import
	private RuntimePropertiesContract properties;

	@Import
	private ElasticTemplatesContract templates;

	@Import
	private ExistingInstancesContract existingInstances;

	@Override
	@Managed
	public ElasticsearchService service() {
		ElasticsearchService bean = create(ElasticsearchService.T);
		bean.setModule(existingInstances.module());
		bean.setExternalId("elasticsearch.service");
		bean.setName("Elasticsearch Service");
		bean.setBasePath(properties.ELASTIC_SERVICE_BASE_PATH());
		bean.setDataPath(properties.ELASTIC_SERVICE_DATA_PATH());
		bean.setClusterName(properties.ELASTIC_CLUSTER_NAME());
		bean.setPluginClasses(asSet("org.elasticsearch.ingest.attachment.IngestAttachmentPlugin"));
		bean.setPort(properties.ELASTIC_PORT());
		bean.setHttpPort(properties.ELASTIC_HTTP_PORT());
		bean.setElasticPath("resources/elastic");
		bean.setPathIdentifier("elastic.service");
		bean.setAutoDeploy(properties.ELASTIC_RUN_SERVICE());
		bean.setBindHosts(properties.ELASTIC_BIND_HOSTS());
		bean.setPublishHost(properties.ELASTIC_PUBLISH_HOST());
		bean.setRepositoryPaths(properties.ELASTIC_REPOSITORY_PATHS());
		bean.setRecoverAfterNodes(properties.ELASTIC_RECOVER_AFTER_NODES());
		bean.setRecoverAfterTimeInS(properties.ELASTIC_RECOVER_AFTER_TIME_IN_MS());
		bean.setExpectedNodes(properties.ELASTIC_EXPECTED_NODES());
		bean.setClusterNodes(properties.ELASTIC_CLUSTER_NODES());

		return bean;
	}

	@Override
	@Managed
	public ElasticServiceProcessor serviceRequestProcessor() {
		ElasticServiceProcessor bean = create(ElasticServiceProcessor.T);
		bean.setModule(existingInstances.module());
		bean.setExternalId("elasticsearch.serviceProcessor");
		bean.setName("Elasticsearch Service Processor");

		return bean;
	}

	@Override
	@Managed
	public ElasticsearchAdminServlet adminServlet() {
		ElasticsearchAdminServlet bean = create(ElasticsearchAdminServlet.T);
		bean.setModule(existingInstances.module());
		bean.setExternalId("elasticsearch.adminServlet");
		bean.setName("Elasticsearch Admin Servlet");
		bean.setPathIdentifier("elastic.admin");

		return bean;
	}

	@Override
	@Managed
	public HealthCheckProcessor healthCheckProcessor() {
		HealthCheckProcessor bean = create(HealthCheckProcessor.T);
		bean.setModule(existingInstances.module());
		bean.setName("Elastic Check Processor");
		bean.setExternalId("elasticsearch.healthzProcessor");

		return bean;
	}

	@Override
	@Managed
	public CheckBundle functionalCheckBundle() {
		CheckBundle bean = create(CheckBundle.T);
		bean.setModule(existingInstances.module());
		bean.getChecks().add(healthCheckProcessor());
		bean.setName("Elastic Checks");
		bean.setWeight(CheckWeight.under1s);
		bean.setCoverage(CheckCoverage.functional);
		bean.setIsPlatformRelevant(false);

		return bean;
	}

	@Managed
	@Override
	public ProcessWith serviceProcessWith() {
		ProcessWith bean = create(ProcessWith.T);
		bean.setProcessor(serviceRequestProcessor());

		return bean;
	}

	@Managed
	@Override
	public ElasticTemplateContext defaultElasticTemplateContext() {
		ElasticTemplateContext bean = ElasticTemplateContext.builder().setIdPrefix("Elastic.Default").setElasticModule(existingInstances.module())
				.setEntityFactory(super::create).setLookupFunction(super::lookup).setClusterName("elasticsearch").setClusterSniff(false)
				.setConnectorHost(properties.ELASTIC_HOST("127.0.0.1")).setConnectorPort(properties.ELASTIC_PORT())
				.setDeployConnector(properties.ELASTIC_RUN_SERVICE()).setIndex(properties.ELASTIC_ACCESS_INDEX("default-index"))
				.setAccess(demoAccess()).setName("Default").setIndexingQueueSize(1000).setIndexingThreadCount(2).build();

		return bean;
	}

	@Managed
	@Override
	public IncrementalAccess demoAccess() {
		SmoodAccess bean = create(SmoodAccess.T);
		bean.setName("Elastic Fulltext Demo Access");
		bean.setMetaModel(models.demoDocumentModel());
		bean.setExternalId("demo.document.elastic.access");

		AspectConfiguration aspectConfiguration = create(AspectConfiguration.T);
		bean.setAspectConfiguration(aspectConfiguration);

		ExtendedFulltextAspect fulltextAspect = templates.fulltextAspect(defaultElasticTemplateContext());
		bean.getAspectConfiguration().getAspects().add(fulltextAspect);

		return bean;
	}
}
