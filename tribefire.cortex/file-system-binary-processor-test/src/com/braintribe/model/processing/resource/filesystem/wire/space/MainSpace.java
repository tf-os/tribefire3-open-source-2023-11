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
package com.braintribe.model.processing.resource.filesystem.wire.space;

import static com.braintribe.utils.lcd.CollectionTools2.asMap;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.braintribe.gm.service.wire.common.contract.CommonServiceProcessingContract;
import com.braintribe.gm.service.wire.common.contract.ServiceProcessingConfigurationContract;
import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.cache.CacheOptions;
import com.braintribe.model.cache.CacheType;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.processing.resource.filesystem.BinaryProcessorTestCommons;
import com.braintribe.model.processing.resource.filesystem.FileSystemBinaryProcessor;
import com.braintribe.model.processing.resource.filesystem.common.ProcessorConfig;
import com.braintribe.model.processing.resource.filesystem.common.ServiceIdDispatchingProcessor;
import com.braintribe.model.processing.resource.filesystem.common.TestFile;
import com.braintribe.model.processing.resource.filesystem.common.TestSessionFactory;
import com.braintribe.model.processing.resource.filesystem.path.AccessFsPathResolver;
import com.braintribe.model.processing.resource.filesystem.path.FsPathResolver;
import com.braintribe.model.processing.resource.filesystem.wire.contract.MainContract;
import com.braintribe.model.processing.service.common.ConfigurableDispatchingServiceProcessor;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.processing.smood.Smood;
import com.braintribe.model.resourceapi.base.BinaryRequest;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.provider.Holder;
import com.braintribe.provider.Hub;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.context.WireContextConfiguration;

@Managed
public class MainSpace implements MainContract, BinaryProcessorTestCommons {

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
		bean.register(SERVICE_ID_SIMPLE, simpleFileSystemBinaryProcessor());
		bean.register(SERVICE_ID_ENRICHING, enrichingFileSystemBinaryProcessor());
		return bean;
	}
	
	@Managed
	private PersistenceGmSessionFactory sessionFactory() {
		TestSessionFactory bean = new TestSessionFactory(evaluator());
		bean.addAccess(access1());
		bean.addAccess(access2());
		return bean;
	}

	
	@Managed
	@Override
	public Hub<Path> tempPathHolder() {
		return new Holder<>();
	}

	@Managed
	public String tempPath() {
		return tempPathHolder().get().toString();
	}

	@Override
	@Managed
	public Path access1Path() {
		return createTempPath(access1().getAccessId());
	}

	@Override
	@Managed
	public IncrementalAccess access1() {
		Smood bean = new Smood(new ReentrantReadWriteLock());
		bean.setAccessId("test-access-1");
		return bean;
	}

	@Override
	@Managed
	public Path access2Path() {
		return createTempPath(access2().getAccessId());
	}

	@Override
	@Managed
	public IncrementalAccess access2() {
		Smood bean = new Smood(new ReentrantReadWriteLock());
		bean.setAccessId("test-access-2");
		return bean;
	}

	@Override
	@Managed
	public ProcessorConfig simpleFileSystemBinaryProcessorConfig() {
		ProcessorConfig bean = new ProcessorConfig();

		CacheOptions cacheOptions = CacheOptions.T.create();
		cacheOptions.setMaxAge(60);
		cacheOptions.setMustRevalidate(true);
		cacheOptions.setType(CacheType.privateCache);

		bean.setCacheOptions(cacheOptions);

		return bean;
	}

	@Managed
	@Override
	public FileSystemBinaryProcessor simpleFileSystemBinaryProcessor() {
		FileSystemBinaryProcessor bean = new FileSystemBinaryProcessor();

		configProcessor(bean, simpleFileSystemBinaryProcessorConfig());

		return bean;
	}

	@Override
	@Managed
	public ProcessorConfig enrichingFileSystemBinaryProcessorConfig() {
		ProcessorConfig bean = new ProcessorConfig();

		CacheOptions cacheOptions = CacheOptions.T.create();
		cacheOptions.setMaxAge(30);
		cacheOptions.setMustRevalidate(false);
		cacheOptions.setType(CacheType.noStore);

		bean.setCacheOptions(cacheOptions);

		return bean;
	}

	@Override
	@Managed
	public FileSystemBinaryProcessor enrichingFileSystemBinaryProcessor() {
		FileSystemBinaryProcessor bean = new FileSystemBinaryProcessor();

		configProcessor(bean, enrichingFileSystemBinaryProcessorConfig());

		return bean;
	}

	@Override
	@Managed
	public Map<String, TestFile> testFiles() {
		final String cpPath = "res/uploadFiles/";

		return Stream.of("test-resource-01.pdf", "test-resource-02.zip", "test-resource-03.docx", "test-resource-04.jpg") //
				.map(fileName -> TestFile.create(cpPath + "/" + fileName)) //
				.collect(Collectors.toMap( //
						TestFile::extension, //
						Function.identity() //
				));
	}

	protected void configProcessor(FileSystemBinaryProcessor bean, ProcessorConfig config) {
		bean.setCacheMaxAge(config.getCacheOptions().getMaxAge());
		bean.setCacheMustRevalidate(config.getCacheOptions().getMustRevalidate());
		bean.setCacheType(config.getCacheOptions().getType());

		DateTimeFormatter timestampPathFormat = DateTimeFormatter.ofPattern("yyMM/ddHH/mmss").withLocale(Locale.US);
		bean.setTimestampPathFormat(timestampPathFormat);

		bean.setFsPathResolver(fsPathResolver());
	}

	private FsPathResolver fsPathResolver() {
		Map<String, Path> accessIdToPath = asMap(//
				access1().getAccessId(), access1Path(), //
				access2().getAccessId(), access2Path() //
		);

		AccessFsPathResolver result = new AccessFsPathResolver();
		result.setAccessPathSupplier(accessIdToPath::get);

		return result;
	}

	protected Path createTempPath(String path) {
		Path root = tempPathHolder().get();
		Path bean = root.resolve(path);
		try {
			Files.createDirectories(bean);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		return bean;
	}

}
