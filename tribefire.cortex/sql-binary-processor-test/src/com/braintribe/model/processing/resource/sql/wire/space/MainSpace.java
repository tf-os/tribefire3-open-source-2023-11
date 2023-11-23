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
package com.braintribe.model.processing.resource.sql.wire.space;

import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.braintribe.cartridge.common.processing.streaming.StandardResourceEnrichingStreamer;
import com.braintribe.cartridge.common.processing.streaming.StandardResourceEnrichingStreamer2;
import com.braintribe.gm.service.wire.common.contract.CommonServiceProcessingContract;
import com.braintribe.gm.service.wire.common.contract.ServiceProcessingConfigurationContract;
import com.braintribe.mimetype.PlatformMimeTypeDetector;
import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.cache.CacheOptions;
import com.braintribe.model.cache.CacheType;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.processing.lock.impl.SemaphoreBasedLocking;
import com.braintribe.model.processing.resource.enrichment.ResourceEnrichingStreamer;
import com.braintribe.model.processing.resource.sql.BinaryProcessorTestConstants;
import com.braintribe.model.processing.resource.sql.JdbcSqlBinaryProcessor;
import com.braintribe.model.processing.resource.sql.common.ProcessorConfig;
import com.braintribe.model.processing.resource.sql.common.ServiceIdDispatchingProcessor;
import com.braintribe.model.processing.resource.sql.common.TestFile;
import com.braintribe.model.processing.resource.sql.common.TestSessionFactory;
import com.braintribe.model.processing.resource.sql.wire.contract.MainContract;
import com.braintribe.model.processing.service.common.ConfigurableDispatchingServiceProcessor;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.processing.smood.Smood;
import com.braintribe.model.resourceapi.base.BinaryRequest;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.utils.stream.file.FileBackedPipeFactory;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.context.WireContextConfiguration;

@Managed
public class MainSpace implements MainContract, BinaryProcessorTestConstants {

	@Import
	private DataSourcesSpace dataSources;

	@Import
	private ServiceProcessingConfigurationContract serviceProcessingConfiguration;

	@Import
	private CommonServiceProcessingContract commonServiceProcessing;

	@Override
	public void onLoaded(WireContextConfiguration configuration) {
		serviceProcessingConfiguration.registerServiceConfigurer(this::configureServices);
	}

	@Override
	public Evaluator<ServiceRequest> evaluator() {
		return commonServiceProcessing.evaluator();
	}

	private void configureServices(ConfigurableDispatchingServiceProcessor bean) {
		bean.removeInterceptor("auth");
		bean.register(BinaryRequest.T, serviceIdDispatchingProcessor());
	}

	@Managed
	private ServiceIdDispatchingProcessor<BinaryRequest> serviceIdDispatchingProcessor() {
		ServiceIdDispatchingProcessor<BinaryRequest> bean = new ServiceIdDispatchingProcessor<>();
		bean.register(SERVICE_ID_SIMPLE, simpleSqlBinaryProcessor());
		bean.register(SERVICE_ID_ENRICHING, enrichingSqlBinaryProcessor());
		return bean;
	}

	@Managed
	private PersistenceGmSessionFactory sessionFactory() {
		TestSessionFactory bean = new TestSessionFactory(evaluator());
		bean.addAccess(access());
		return bean;
	}

	@Managed
	@Override
	public Map<String, TestFile> testFiles() {
		String cpPath = "res/uploadFiles/";
		String[] fileNames = { "test-resource-03.docx", "test-resource-04.jpg" };

		Map<String, TestFile> bean = newMap();

		for (String fileName : fileNames) {
			TestFile file = TestFile.create(cpPath + "/" + fileName);
			bean.put(file.extension(), file);
		}

		return bean;

	}

	@Override
	@Managed
	public IncrementalAccess access() {
		Smood bean = new Smood(new ReentrantReadWriteLock());
		bean.setAccessId("test-access-1");
		return bean;
	}

	@Override
	@Managed
	public JdbcSqlBinaryProcessor simpleSqlBinaryProcessor() {
		JdbcSqlBinaryProcessor bean = new JdbcSqlBinaryProcessor();
		bean.setIdGenerator(r -> "test-S-" + UUID.randomUUID().toString());
		configJdbcProcessor(bean, simpleSqlBinaryProcessorConfig());
		return bean;
	}

	@Managed
	@Override
	public ProcessorConfig simpleSqlBinaryProcessorConfig() {
		ProcessorConfig bean = new ProcessorConfig();

		CacheOptions cacheOptions = CacheOptions.T.create();
		cacheOptions.setMaxAge(60);
		cacheOptions.setMustRevalidate(true);
		cacheOptions.setType(CacheType.privateCache);

		bean.setCacheOptions(cacheOptions);

		return bean;
	}

	@Override
	@Managed
	public JdbcSqlBinaryProcessor enrichingSqlBinaryProcessor() {
		JdbcSqlBinaryProcessor bean = new JdbcSqlBinaryProcessor();
		bean.setIdGenerator(r -> "test-E-" + UUID.randomUUID().toString());
		configJdbcProcessor(bean, enrichingSqlBinaryProcessorConfig());
		return bean;
	}

	@Managed
	@Override
	public ProcessorConfig enrichingSqlBinaryProcessorConfig() {
		ProcessorConfig bean = new ProcessorConfig();

		CacheOptions cacheOptions = CacheOptions.T.create();
		cacheOptions.setMaxAge(30);
		cacheOptions.setMustRevalidate(false);
		cacheOptions.setType(CacheType.publicCache);

		bean.setCacheOptions(cacheOptions);

		return bean;
	}

	@Managed
	private ResourceEnrichingStreamer enrichingStreamer() {
		StandardResourceEnrichingStreamer bean = new StandardResourceEnrichingStreamer();
		bean.setDigestAlgorithm("MD5");
		bean.setMimeTypeDetector(PlatformMimeTypeDetector.instance);
		bean.setStreamPipeFactory(new FileBackedPipeFactory());
		return bean;
	}

	@Managed
	private ResourceEnrichingStreamer enrichingStreamer2() {
		StandardResourceEnrichingStreamer2 bean = new StandardResourceEnrichingStreamer2();
		bean.setDigestAlgorithm("MD5");
		bean.setMimeTypeDetector(PlatformMimeTypeDetector.instance);
		bean.setStreamPipeFactory(new FileBackedPipeFactory());
		return bean;
	}

	protected void configJdbcProcessor(JdbcSqlBinaryProcessor bean, ProcessorConfig config) {
		bean.setDataSource(dataSources.dataSource());
		bean.setExternalId("test.sql.binary.persistence");
		bean.setLocking(new SemaphoreBasedLocking());
		bean.setCacheMaxAge(config.getCacheOptions().getMaxAge());
		bean.setCacheMustRevalidate(config.getCacheOptions().getMustRevalidate());
		bean.setCacheType(config.getCacheOptions().getType());
	}

}
