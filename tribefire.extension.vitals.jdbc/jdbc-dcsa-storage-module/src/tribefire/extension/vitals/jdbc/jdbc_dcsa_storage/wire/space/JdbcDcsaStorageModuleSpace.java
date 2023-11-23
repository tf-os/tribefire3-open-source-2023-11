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
package tribefire.extension.vitals.jdbc.jdbc_dcsa_storage.wire.space;

import javax.sql.DataSource;

import com.braintribe.codec.marshaller.json.JsonStreamMarshaller;
import com.braintribe.gm.jdbc.api.GmDb;
import com.braintribe.logging.Logger;
import com.braintribe.model.access.collaboration.distributed.api.JdbcDcsaStorage;
import com.braintribe.model.deployment.database.pool.DatabaseConnectionPool;
import com.braintribe.model.deployment.database.pool.HikariCpConnectionPool;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.processing.deployment.api.ExpertContext;
import com.braintribe.model.processing.deployment.api.binding.DenotationBindingBuilder;
import com.braintribe.model.processing.lock.api.LockManager;
import com.braintribe.model.processing.lock.db.impl.DbLockManager;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.extension.jdbc.dcsa.model.deployment.JdbcDcsaSharedStorage;
import tribefire.extension.jdbc.gmdb.dcsa.GmDbDcsaSharedStorage;
import tribefire.extension.jdbc.gmdb.dcsa.TemporaryJdbc2GmDbSharedStorage;
import tribefire.extension.vitals.jdbc.jdbc_dcsa_storage.processor.SharedStorageRequestProcessor;
import tribefire.extension.vitals.jdbc.model.migration.SharedStorageRequest;
import tribefire.module.wire.contract.ClusterBindersContract;
import tribefire.module.wire.contract.ResourceProcessingContract;
import tribefire.module.wire.contract.TribefireModuleContract;
import tribefire.module.wire.contract.TribefireWebPlatformContract;

/**
 * This modules binds the {@link JdbcDcsaStorage} implementation for {@link JdbcDcsaSharedStorage} denotation type.
 */
@Managed
public class JdbcDcsaStorageModuleSpace implements TribefireModuleContract {

	private final static Logger logger = Logger.getLogger(JdbcDcsaStorageModuleSpace.class);

	private final static int DEFAULT_THREADPOOL_SIZE = 10;

	@Import
	private TribefireWebPlatformContract tfPlatform;

	@Import
	private ClusterBindersContract clusterBinders;

	@Import
	private ResourceProcessingContract resourceProcessing;

	private TemporaryJdbc2GmDbSharedStorage sharedStorage;

	@Override
	public void bindHardwired() {
		tfPlatform.hardwiredDeployables().bindOnExistingServiceDomain("cortex") //
				.serviceProcessor( //
						"migrate-dcsa-shared-storeage", //
						"Shared Storage Request Processor", //
						SharedStorageRequest.T, //
						sharedStorageRequestProcessor());
	}

	private SharedStorageRequestProcessor sharedStorageRequestProcessor() {
		SharedStorageRequestProcessor bean = new SharedStorageRequestProcessor();
		bean.setSharedStorageSupplier(() -> sharedStorage);
		bean.setResourceBuilder(tfPlatform.resourceProcessing().transientResourceBuilder());

		return bean;
	}

	@Override
	public void bindDeployables(DenotationBindingBuilder bindings) {
		bindings.bind(JdbcDcsaSharedStorage.T) //
				.component(clusterBinders.dcsaSharedStorage()) //
				.expertFactory(this::jdbc2GmDbSharedStoreage);
	}

	@Managed
	private TemporaryJdbc2GmDbSharedStorage jdbc2GmDbSharedStoreage(ExpertContext<JdbcDcsaSharedStorage> context) {
		DcsaDataSourceInfo dataSourceInfo = dataSourceInfo(context);

		TemporaryJdbc2GmDbSharedStorage bean = new TemporaryJdbc2GmDbSharedStorage();
		bean.setDataSource(dataSourceInfo.dataSource);
		bean.setJdbcStorage(jdbcDcsaSharedStorage(context));
		bean.setGmDbStorage(gmDbDcsaSharedStorage(context));

		// Temporary dirty hack to inject shared storage to the request processor
		if (sharedStorage == null)
			sharedStorage = bean;

		return bean;
	}

	@Managed
	private GmDbDcsaSharedStorage gmDbDcsaSharedStorage(ExpertContext<JdbcDcsaSharedStorage> context) {
		JdbcDcsaSharedStorage deployable = context.getDeployable();

		DcsaDataSourceInfo dataSourceInfo = dataSourceInfo(context);

		GmDbDcsaSharedStorage bean = new GmDbDcsaSharedStorage();
		bean.setProjectId(deployable.getProject());
		bean.setGmDb(gmDb(context));
		bean.setLockManager(dataSourceInfo.lockManager);
		bean.setAutoUpdateSchema(false);

		return bean;
	}

	@Managed
	private GmDb gmDb(ExpertContext<JdbcDcsaSharedStorage> context) {
		DcsaDataSourceInfo dataSourceInfo = dataSourceInfo(context);

		GmDb bean = GmDb.newDb(dataSourceInfo.dataSource) //
				.withDefaultCodec(new JsonStreamMarshaller()) //
				.withExecutorPoolSize(getParallelFetchThreads(context.getDeployable())) //
				.withStreamPipeFactory(resourceProcessing.streamPipeFactory()) //
				.done();

		return bean;
	}

	@Managed
	@SuppressWarnings("deprecation")
	private JdbcDcsaStorage jdbcDcsaSharedStorage(ExpertContext<JdbcDcsaSharedStorage> context) {
		JdbcDcsaSharedStorage deployable = context.getDeployable();

		DcsaDataSourceInfo dataSourceInfo = dataSourceInfo(context);

		JdbcDcsaStorage storage = new JdbcDcsaStorage();
		storage.setProjectId(deployable.getProject());
		storage.setAutoUpdateSchema(!Boolean.FALSE.equals(deployable.getAutoUpdateSchema()));
		storage.setDataSource(dataSourceInfo.dataSource);
		storage.setLockManager(dataSourceInfo.lockManager);
		storage.setMarshaller(new com.braintribe.codec.marshaller.json.JsonStreamMarshaller());
		storage.setParallelFetchThreads(getParallelFetchThreads(deployable));
		storage.setAutoUpdateSchema(false);

		return storage;
	}

	protected int getParallelFetchThreads(JdbcDcsaSharedStorage dss) {
		// this runtime property is just an experimental feature, which has been used for some performance tests.
		// if we want to make this official, it should be renamed and properly documented.
		String runtimeProperty = "EXPERIMENTAL_TRIBEFIRE_JDBC_DCSA_STORAGE_PARALLEL_FETCH_THREADS";
		String runtimePropertyValue = TribefireRuntime.getProperty(runtimeProperty, null);
		if (runtimePropertyValue != null) {
			int threads = Integer.parseInt(runtimePropertyValue);
			logger.debug(() -> "Using " + threads + " parallel fetch threads (configured via runtime property " + runtimeProperty + ")");
			return threads;
		}

		if (dss.getParallelFetchThreads() != null && dss.getParallelFetchThreads() > 0) {
			int threads = dss.getParallelFetchThreads();
			logger.debug(() -> "Using " + threads + " parallel fetch threads (configured via " + JdbcDcsaSharedStorage.T.getShortName() + ")");
			return threads;
		}

		DatabaseConnectionPool databaseConnectionPool = dss.getConnectionPool();
		if (databaseConnectionPool instanceof HikariCpConnectionPool) {
			HikariCpConnectionPool hcpd = (HikariCpConnectionPool) databaseConnectionPool;

			Integer poolSize = hcpd.getMaxPoolSize();
			if (poolSize != null && poolSize > 0) {
				// we set the thread count based on the pool size. general idea is that we have one thread per connection.
				// tests have shown though that we are a bit faster with a few additional threads, hence factor 1.5.
				final int maxThreads = 30;
				final int threads = Math.min((int) (poolSize * 1.5), maxThreads);

				if (threads <= maxThreads) {
					logger.debug(() -> "Using " + threads + " parallel fetch threads (based on connection pool size " + poolSize + ")");
				} else {
					logger.debug(() -> "Using " + threads + " parallel fetch threads (based on maximum thread count " + maxThreads
							+ " when deriving from connection pool size; connection pool size is " + poolSize + ")");
				}
				return threads;
			}
		}

		int threads = DEFAULT_THREADPOOL_SIZE;
		logger.debug(() -> "Using " + threads + " parallel fetch threads (based on default thread count)");
		return threads;
	}

	private static class DcsaDataSourceInfo {
		public DataSource dataSource;
		public LockManager lockManager;
	}

	@Managed
	private DcsaDataSourceInfo dataSourceInfo(ExpertContext<JdbcDcsaSharedStorage> context) {
		DcsaDataSourceInfo bean = new DcsaDataSourceInfo();
		bean.dataSource = resolveDataSource(context);
		bean.lockManager = lockManager(bean.dataSource);

		return bean;
	}

	private DataSource resolveDataSource(ExpertContext<JdbcDcsaSharedStorage> context) {
		return context.resolve(context.getDeployable().getConnectionPool(), DatabaseConnectionPool.T);
	}

	private LockManager lockManager(DataSource dataSource) {
		DbLockManager bean = new DbLockManager();
		bean.setDataSource(dataSource);
		bean.postConstruct();
		return bean;
	}

}
