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
package tribefire.extension.elastic.templates.api;

import java.util.function.Function;

import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.deployment.Cartridge;
import com.braintribe.model.deployment.Module;
import com.braintribe.model.descriptive.HasExternalId;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.utils.StringTools;
import com.braintribe.wire.api.scope.InstanceConfiguration;
import com.braintribe.wire.api.scope.InstanceQualification;

public class ElasticTemplateContextImpl implements ElasticTemplateContext, ElasticTemplateContextBuilder {

	private String idPrefix;
	private Function<String, ? extends GenericEntity> lookupFunction;
	private Function<EntityType<?>, GenericEntity> entityFactory = EntityType::create;
	private Cartridge elasticCartridge;
	private String index;
	private String name;
	private String clusterName;
	private boolean clusterSniff = false;
	private String connectorHost = "127.0.0.1";
	private int connectorPort = 9300;
	private boolean deployConnector = true;
	private int maxFulltextResultSize = 1000;
	private int maxResultWindow = 100000;
	private IncrementalAccess access;
	private int indexingThreadCount = 2;
	private int indexingQueueSize = 1000;
	private int httpPort = 9200;
	private Module elasticModule;

	@Override
	public ElasticTemplateContextBuilder setIdPrefix(String idPrefix) {
		this.idPrefix = idPrefix;
		return this;
	}

	@Override
	public String getIdPrefix() {
		return idPrefix;
	}

	@Override
	public ElasticTemplateContextBuilder setElasticCartridge(Cartridge elasticCartridge) {
		this.elasticCartridge = elasticCartridge;
		return this;
	}

	@Override
	public Cartridge getElasticCartridge() {
		return elasticCartridge;
	}

	@Override
	public ElasticTemplateContextBuilder setIndex(String index) {
		this.index = index;
		return this;
	}

	@Override
	public String getIndex() {
		return index;
	}

	@Override
	public ElasticTemplateContextBuilder setName(String name) {
		this.name = name;
		return this;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public ElasticTemplateContextBuilder setClusterName(String clusterName) {
		this.clusterName = clusterName;
		return this;
	}

	@Override
	public String getClusterName() {
		return clusterName;
	}

	@Override
	public ElasticTemplateContextBuilder setClusterSniff(boolean clusterSniff) {
		this.clusterSniff = clusterSniff;
		return this;
	}

	@Override
	public boolean getClusterSniff() {
		return clusterSniff;
	}

	@Override
	public ElasticTemplateContextBuilder setConnectorHost(String connectorHost) {
		this.connectorHost = connectorHost;
		return this;
	}

	@Override
	public String getConnectorHost() {
		return connectorHost;
	}

	@Override
	public ElasticTemplateContextBuilder setConnectorPort(int connectorPort) {
		this.connectorPort = connectorPort;
		return this;
	}

	@Override
	public int getConnectorPort() {
		return connectorPort;
	}

	@Override
	public ElasticTemplateContextBuilder setHttpPort(int httpPort) {
		this.httpPort = httpPort;
		return this;
	}

	@Override
	public int getHttpPort() {
		return httpPort;
	}

	@Override
	public ElasticTemplateContextBuilder setDeployConnector(boolean deployConnector) {
		this.deployConnector = deployConnector;
		return this;
	}

	@Override
	public boolean getDeployConnector() {
		return deployConnector;
	}

	@Override
	public ElasticTemplateContextBuilder setMaxFulltextResultSize(int maxFulltextResultSize) {
		this.maxFulltextResultSize = maxFulltextResultSize;
		return this;
	}

	@Override
	public int getMaxFulltextResultSize() {
		return maxFulltextResultSize;
	}

	@Override
	public ElasticTemplateContextBuilder setMaxResultWindow(int maxResultWindow) {
		this.maxResultWindow = maxResultWindow;
		return this;
	}

	@Override
	public int getMaxResultWindow() {
		return maxResultWindow;
	}

	@Override
	public ElasticTemplateContextBuilder setAccess(IncrementalAccess access) {
		this.access = access;
		return this;
	}

	@Override
	public IncrementalAccess getAccess() {
		return access;
	}

	@Override
	public ElasticTemplateContextBuilder setIndexingThreadCount(int indexingThreadCount) {
		this.indexingThreadCount = indexingThreadCount;
		return this;
	}

	@Override
	public int getIndexingThreadCount() {
		return indexingThreadCount;
	}

	@Override
	public ElasticTemplateContextBuilder setIndexingQueueSize(int indexingQueueSize) {
		this.indexingQueueSize = indexingQueueSize;
		return this;
	}

	@Override
	public int getIndexingQueueSize() {
		return indexingQueueSize;
	}

	@Override
	public <T extends GenericEntity> T lookup(String globalId) {
		return (T) lookupFunction.apply(globalId);
	}

	@Override
	public ElasticTemplateContextBuilder setEntityFactory(Function<EntityType<?>, GenericEntity> entityFactory) {
		this.entityFactory = entityFactory;
		return this;
	}

	@Override
	public ElasticTemplateContextBuilder setLookupFunction(Function<String, ? extends GenericEntity> lookupFunction) {
		this.lookupFunction = lookupFunction;
		return this;
	}

	@Override
	public ElasticTemplateContextBuilder setElasticModule(Module elasticModule) {
		this.elasticModule = elasticModule;
		return this;
	}

	@Override
	public Module getElasticModule() {
		return this.elasticModule;
	}

	@Override
	public ElasticTemplateContext build() {
		return this;
	}

	@Override
	public int hashCode() {
		return idPrefix.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ElasticTemplateContext) {
			return ((ElasticTemplateContext) obj).getIdPrefix().equals(this.idPrefix);
		}
		return super.equals(obj);
	}

	@Override
	public <T extends GenericEntity> T create(EntityType<T> entityType, InstanceConfiguration instanceConfiguration) {

		T entity = (T) entityFactory.apply(entityType);

		if (idPrefix == null) {
			throw new IllegalStateException("You have to specify a idPrefix.");
		}

		InstanceQualification qualification = instanceConfiguration.qualification();

		String globalId = "wire://" + idPrefix + "/" + qualification.space().getClass().getSimpleName() + "/" + qualification.name();

		entity.setGlobalId(globalId);

		if (entity instanceof HasExternalId) {
			HasExternalId eid = (HasExternalId) entity;

			String externalId = StringTools.camelCaseToDashSeparated(entityType.getShortName()) + "." + StringTools.camelCaseToDashSeparated(idPrefix)
					+ "." + StringTools.camelCaseToDashSeparated(qualification.space().getClass().getSimpleName()) + "."
					+ StringTools.camelCaseToDashSeparated(qualification.name());

			eid.setExternalId(externalId);
		}

		return entity;
	}

}
