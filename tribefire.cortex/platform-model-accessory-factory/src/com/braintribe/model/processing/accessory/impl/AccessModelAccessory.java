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
package com.braintribe.model.processing.accessory.impl;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.gm.model.persistence.reflection.api.GetModelEnvironment;
import com.braintribe.logging.Logger;
import com.braintribe.model.accessapi.ModelEnvironment;
import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.meta.cmd.ResolutionContextBuilder;
import com.braintribe.model.processing.meta.cmd.context.aspects.AccessAspect;
import com.braintribe.model.processing.meta.cmd.context.aspects.AccessTypeAspect;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.impl.persistence.BasicPersistenceGmSession;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.record.ListRecord;
import com.braintribe.utils.lcd.StopWatch;

/**
 * <p>
 * Standard access bound {@link AbstractDerivingModelAccessory}.
 * 
 */
public class AccessModelAccessory extends AbstractDerivingModelAccessory {

	// constants
	private static final Logger log = Logger.getLogger(AccessModelAccessory.class);

	// configurable
	private String accessId;

	// cached
	private String modelName;
	private String accessType;

	public AccessModelAccessory() {
	}

	@Required
	@Configurable
	public void setAccessId(String accessId) {
		this.accessId = accessId;
	}

	@Override
	protected String getModelName() {
		if (modelName != null) {
			return modelName;
		}

		StopWatch sw = new StopWatch();

		PersistenceGmSession cortexSession = cortexSessionProvider.get();
		
		// This is a ugly workaround that should only be necessary in combination with cartridges and can be removed as soon as cartridges are
		// The issue is that sometimes in cartridges we get a session without an evaluator which should not be allowed anyway
		if (cortexSession instanceof BasicPersistenceGmSession && ((BasicPersistenceGmSession)cortexSession).getRequestEvaluator() == null)
			queryModelEnvironment(cortexSession);
		else
			evaluateModelEnvironment(cortexSession);
		
		log.debug(() -> "Fetched for accessId='" + accessId + "', modelName='" + modelName + "' and accessType='" + accessType + "' in "  + sw.getElapsedTime() + " ms.");

		if (modelName == null)
			throw new IllegalStateException("Model name for access '" + accessId + "' is null");

		return modelName;

	}

	private void evaluateModelEnvironment(PersistenceGmSession cortexSession) {
		GetModelEnvironment getModelEnvironment = GetModelEnvironment.T.create();
		getModelEnvironment.setAccessId(accessId);
		
		ModelEnvironment modelEnvironment = getModelEnvironment.eval(cortexSession).get();
		
		modelName = modelEnvironment.getMetaModelName();
		accessType = modelEnvironment.getDataAccessDenotationType();
	}

	private void queryModelEnvironment(PersistenceGmSession cortexSession) {
		// @formatter:off
		SelectQuery query = new SelectQueryBuilder()
				.select("a", IncrementalAccess.metaModel+"."+GmMetaModel.name)
				.select().entitySignature().entity("a")
				.from(IncrementalAccess.T, "a")
				.where()
					.property("a", IncrementalAccess.externalId).eq(accessId)
			.done();
		// @formatter:on

		log.debug(() -> "Fetching model and accessType for access='" + accessId);

		ListRecord row = cortexSession.query().select(query).unique();
		if (row == null)
			throw new IllegalStateException("Could not find an Access with external Id "+accessId+", or it does not reference a model with a name.");
		
		modelName = (String) row.getValues().get(0);
		accessType = (String) row.getValues().get(1);
	}

	@Override
	protected void initializeContextBuilder(ResolutionContextBuilder rcb) {
		rcb.addStaticAspect(AccessAspect.class, accessId);
		rcb.addStaticAspect(AccessTypeAspect.class, accessType);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "@" + Integer.toHexString(hashCode()) + " [accessId='" + accessId + "', modelName='" + modelName + "']";
	}

}
