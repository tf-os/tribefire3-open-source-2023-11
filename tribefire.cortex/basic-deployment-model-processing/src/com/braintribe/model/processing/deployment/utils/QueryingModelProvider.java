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
package com.braintribe.model.processing.deployment.utils;

import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.EntityQuery;


/**
 * Provides an instance of {@link GmMetaModel} with given name from given {@link PersistenceGmSession}.
 */
public class QueryingModelProvider implements Supplier<GmMetaModel> {
	private static final Logger logger = Logger.getLogger(QueryingModelProvider.class);
	
	private Supplier<PersistenceGmSession> sessionProvider;
	private String modelName;
	private GmMetaModel metaModel;
	
	private boolean ensureModelTypes;
	private boolean cacheModel = true;
	
	@Configurable @Required
	public void setSessionProvider(Supplier<PersistenceGmSession> sessionProvider) {
		this.sessionProvider = sessionProvider;
	}

	@Configurable @Required
	public void setModelName(String modelName) {
		this.modelName = modelName;
	}
	
	@Configurable
	public void setEnsureModelTypes(boolean ensureModelTypes) {
		this.ensureModelTypes = ensureModelTypes;
	}

	public void setCacheModel(boolean cacheModel) {
		this.cacheModel = cacheModel;
	}
	
	@Override
	public GmMetaModel get() throws RuntimeException {
		if (!cacheModel || metaModel == null) {
			metaModel = getMetaModel();
		}

		return metaModel;
	}

	private GmMetaModel getMetaModel() throws RuntimeException {
		try {
			EntityQuery eq = EntityQueryBuilder.from(GmMetaModel.class).where().property("name").eq(modelName).tc().negation().joker().limit(1).done();
			GmMetaModel model = sessionProvider.get().query().entities(eq).first();
			
			if (model == null) {
				logger.debug("No model found in session for name: "+modelName);
			}
			if (ensureModelTypes) {
				GMF.getTypeReflection().deploy(model);
				logger.debug("Successfully ensured types from model: "+model);
			}
			return model;
			

		} catch (Exception e) {
			throw new RuntimeException("Error while querying GmMetaModel with name: " + modelName, e);
		}
	}
}
