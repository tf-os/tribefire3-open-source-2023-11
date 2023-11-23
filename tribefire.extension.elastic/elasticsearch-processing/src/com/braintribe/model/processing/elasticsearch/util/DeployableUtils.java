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
package com.braintribe.model.processing.elasticsearch.util;

import java.util.List;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.accessdeployment.aspect.AspectConfiguration;
import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.elasticsearchdeployment.ElasticsearchConnector;
import com.braintribe.model.elasticsearchdeployment.IndexedElasticsearchConnector;
import com.braintribe.model.elasticsearchdeployment.aspect.ExtendedFulltextAspect;
import com.braintribe.model.elasticsearchdeployment.indexing.ElasticsearchIndexingWorker;
import com.braintribe.model.extensiondeployment.AccessAspect;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.processing.action.api.ActionProcessingException;
import com.braintribe.model.processing.deployment.api.DeployRegistry;
import com.braintribe.model.processing.deployment.api.DeployedUnit;
import com.braintribe.model.processing.elasticsearch.IndexedElasticsearchConnectorImpl;
import com.braintribe.model.processing.elasticsearch.indexing.ElasticsearchIndexingWorkerImpl;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.SelectQuery;

public class DeployableUtils {

	private final static Logger logger = Logger.getLogger(DeployableUtils.class);

	private DeployRegistry deployRegistry;
	private Supplier<PersistenceGmSession> cortexSessionFactory;

	public ElasticsearchIndexingWorkerImpl getElasticsearchWorker(ElasticsearchIndexingWorker workerDeployable) throws Exception {

		DeployedUnit unit = deployRegistry.resolve(workerDeployable);

		if (unit == null) {
			throw new ActionProcessingException("Elasticsearch Worker deployedUnit is null.");
		}
		ElasticsearchIndexingWorkerImpl impl = unit.getComponent(com.braintribe.model.elasticsearchdeployment.indexing.ElasticsearchIndexingWorker.T);
		return impl;
	}

	public IndexedElasticsearchConnectorImpl getIndexedElasticsearchConnector(ElasticsearchIndexingWorker worker) throws Exception {

		com.braintribe.model.elasticsearchdeployment.IndexedElasticsearchConnector connectorDeployable = worker.getElasticsearchConnector();
		DeployedUnit unit = deployRegistry.resolve(connectorDeployable);

		if (unit == null) {
			throw new ActionProcessingException("Elasticsearch Connector deployedUnit is null.");
		}
		IndexedElasticsearchConnectorImpl con = unit.getComponent(com.braintribe.model.elasticsearchdeployment.IndexedElasticsearchConnector.T);
		return con;

	}

	public ExtendedFulltextAspect getFulltextAspectForAccess(String accessId) {

		if (accessId == null) {
			throw new IllegalArgumentException("The access Id must be provided.");
		}

		PersistenceGmSession cortexSession = this.cortexSessionFactory.get();

		logger.debug(() -> "Searching for access " + accessId);

		// formatter:off
		SelectQuery query = new SelectQueryBuilder().from(IncrementalAccess.T, "a").join("a", IncrementalAccess.aspectConfiguration, "c").where()
				.property("a", IncrementalAccess.externalId).eq(accessId).select("c").done();
		// formatter:on

		AspectConfiguration aspectConfiguration = cortexSession.query().select(query).first();
		if (aspectConfiguration == null) {
			logger.debug(() -> "Access " + accessId + " does not contain an aspect configuration.");
		}

		List<AccessAspect> aspects = aspectConfiguration.getAspects();
		for (AccessAspect aa : aspects) {
			if (aa instanceof ExtendedFulltextAspect) {
				return (ExtendedFulltextAspect) aa;
			}
		}
		logger.debug(() -> "Could not find an aspect of type ExtendedFulltextAspect in access " + accessId);
		return null;

	}

	public com.braintribe.model.processing.elasticsearch.ElasticsearchConnector getConnector(String accessId) {
		ExtendedFulltextAspect aspect = getFulltextAspectForAccess(accessId);

		if (aspect == null) {
			throw new IllegalArgumentException("The access " + accessId + " does not contain a fulltext aspect.");
		}

		IndexedElasticsearchConnector elasticsearchConnector = aspect.getElasticsearchConnector();
		if (elasticsearchConnector == null) {
			throw new IllegalStateException("The fulltext aspect of access " + accessId + " has no connector configured.");
		}

		DeployedUnit deployedUnit = deployRegistry.resolve(elasticsearchConnector.getExternalId());
		com.braintribe.model.processing.elasticsearch.ElasticsearchConnector connector = deployedUnit.findComponent(IndexedElasticsearchConnector.T);
		if (connector == null) {
			throw new IllegalStateException(
					"The Elastic connector of access " + accessId + " (" + elasticsearchConnector.getExternalId() + ") could not be resolved.");
		}
		return connector;
	}

	public com.braintribe.model.processing.elasticsearch.ElasticsearchConnector findConnectorOrDefault(String externalId) {

		com.braintribe.model.processing.elasticsearch.ElasticsearchConnector con = null;

		if (externalId != null) {
			DeployedUnit deployedUnit = deployRegistry.resolve(externalId);
			con = deployedUnit.findComponent(ElasticsearchConnector.T);
			if (con == null) {
				con = deployedUnit.findComponent(IndexedElasticsearchConnector.T);
			}
		} else {
			List<Deployable> deployables = deployRegistry.getDeployables();
			if (deployables != null) {
				for (Deployable deployable : deployables) {
					if (deployable instanceof ElasticsearchConnector) {

						DeployedUnit deployedUnit = deployRegistry.resolve(deployable);
						con = deployedUnit.findComponent(ElasticsearchConnector.T);
						if (con == null) {
							con = deployedUnit.findComponent(IndexedElasticsearchConnector.T);
						}
						if (con != null) {
							break;
						}
					}
				}
			}
		}

		return con;
	}

	public ElasticsearchIndexingWorker queryWorker(String accessId) throws GmSessionException, Exception {

		PersistenceGmSession cortexSession = this.cortexSessionFactory.get();

		logger.debug(() -> "Searching for worker for access " + accessId);

		// formatter:off
		SelectQuery query = new SelectQueryBuilder().from(ElasticsearchIndexingWorker.T, "w").join("w", ElasticsearchIndexingWorker.access, "a")
				.where().property("a", IncrementalAccess.externalId).eq(accessId).select("w").done();
		// formatter:on

		ElasticsearchIndexingWorker worker = cortexSession.query().select(query).unique();
		return worker;
	}

	@Configurable
	@Required
	public void setDeployRegistry(DeployRegistry deployRegistry) {
		this.deployRegistry = deployRegistry;
	}
	@Configurable
	@Required
	public void setCortexSessionFactory(Supplier<PersistenceGmSession> cortexSessionFactory) {
		this.cortexSessionFactory = cortexSessionFactory;
	}

}
