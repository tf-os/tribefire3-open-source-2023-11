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
package tribefire.platform.wire.space.cortex.deployment.deployables.streaming;

import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

import javax.sql.DataSource;

import com.braintribe.model.cache.CacheOptions;
import com.braintribe.model.deployment.database.pool.DatabaseConnectionPool;
import com.braintribe.model.deployment.resource.sql.SqlBinaryTableMapping;
import com.braintribe.model.processing.deployment.api.ExpertContext;
import com.braintribe.model.processing.resource.filesystem.FileSystemBinaryProcessor;
import com.braintribe.model.processing.resource.filesystem.path.FsPathResolver;
import com.braintribe.model.processing.resource.filesystem.path.StaticFsPathResolver;
import com.braintribe.model.processing.resource.sql.FixSqlSourcesProcessor;
import com.braintribe.model.processing.resource.sql.JdbcSqlBinaryProcessor;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.platform.wire.space.common.ResourceProcessingSpace;
import tribefire.platform.wire.space.cortex.deployment.deployables.DeployableBaseSpace;
import tribefire.platform.wire.space.cortex.services.ClusterSpace;
import tribefire.platform.wire.space.rpc.RpcSpace;
import tribefire.platform.wire.space.streaming.ResourceAccessSpace;

@Managed
public class BinaryProcessorsSpace extends DeployableBaseSpace {

	@Import
	private ClusterSpace cluster;

	@Import
	private ResourceAccessSpace resourceAccess;

	@Import
	private ResourceProcessingSpace resourceProcessing;

	@Import
	private RpcSpace rpc;

	@Managed
	public FileSystemBinaryProcessor fileSystem(
			ExpertContext<com.braintribe.model.deployment.resource.filesystem.FileSystemBinaryProcessor> context) {

		com.braintribe.model.deployment.resource.filesystem.FileSystemBinaryProcessor deployable = context.getDeployable();

		FileSystemBinaryProcessor bean = new FileSystemBinaryProcessor();
		bean.setFsPathResolver(pathResolver(deployable));

		DateTimeFormatter timestampPathFormat = DateTimeFormatter.ofPattern("yyMM/ddHH/mmss").withLocale(Locale.US);
		bean.setTimestampPathFormat(timestampPathFormat);

		CacheOptions cacheOptions = deployable.getCacheOptions();
		if (cacheOptions != null) {
			bean.setCacheType(cacheOptions.getType());
			bean.setCacheMaxAge(cacheOptions.getMaxAge());
			bean.setCacheMustRevalidate(cacheOptions.getMustRevalidate());
		}

		return bean;
	}

	private FsPathResolver pathResolver(com.braintribe.model.deployment.resource.filesystem.FileSystemBinaryProcessor deployable) {
		StaticFsPathResolver bean = new StaticFsPathResolver();

		String resolvedBasePath = environment
				.resolve(Objects.requireNonNull(deployable.getBasePath(), () -> "BasePath of " + deployable + " is null!"));
		bean.setBasePath(Paths.get(resolvedBasePath));

		return bean;
	}

	@Managed
	public JdbcSqlBinaryProcessor sql(ExpertContext<com.braintribe.model.deployment.resource.sql.SqlBinaryProcessor> context) {
		// Once the JDBC part is stable, we get rid of the hibernate version
		com.braintribe.model.deployment.resource.sql.SqlBinaryProcessor deployable = context.getDeployable();

		JdbcSqlBinaryProcessor bean = new JdbcSqlBinaryProcessor();
		bean.setExternalId(deployable.getExternalId());
		bean.setDataSource(resolveDataSource(context));
		bean.setLocking(cluster.locking());
		bean.setEvaluator(rpc.serviceRequestEvaluator());

		SqlBinaryTableMapping tableMapping = deployable.getTableMapping();
		if (tableMapping != null) {
			bean.setTableName(tableMapping.getTableName());
			bean.setIdColumnName(tableMapping.getPrimaryKeyColumnName());
			bean.setBlobColumnName(tableMapping.getBlobColumnName());
		}

		CacheOptions cacheOptions = deployable.getCacheOptions();
		if (cacheOptions != null) {
			bean.setCacheType(cacheOptions.getType());
			bean.setCacheMaxAge(cacheOptions.getMaxAge());
			bean.setCacheMustRevalidate(cacheOptions.getMustRevalidate());
		}

		return bean;
	}

	private DataSource resolveDataSource(ExpertContext<com.braintribe.model.deployment.resource.sql.SqlBinaryProcessor> context) {
		com.braintribe.model.deployment.resource.sql.SqlBinaryProcessor binaryProcessor = context.getDeployable();
		DatabaseConnectionPool connectionPool = binaryProcessor.getConnectionPool();
		if (connectionPool == null)
			throw new IllegalStateException("The connection pool of SqlBinaryProcessor " + binaryProcessor.getName() + " ("
					+ binaryProcessor.getExternalId() + ") is not set.");

		return context.resolve(connectionPool, DatabaseConnectionPool.T);
	}

	@Managed
	public FixSqlSourcesProcessor fixSqlSourcesProcessor() {
		FixSqlSourcesProcessor bean = new FixSqlSourcesProcessor();
		return bean;
	}

}
