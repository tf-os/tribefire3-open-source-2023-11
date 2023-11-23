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
package tribefire.platform.impl.preprocess;

import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.InitializationAware;
import com.braintribe.cfg.Required;
import com.braintribe.exception.Exceptions;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

public class ModelExposureEnsurer implements InitializationAware {

	protected Supplier<PersistenceGmSession> sessionProvider;
	private boolean runOnStartup = false;
	
	public ModelExposureEnsurer() {
		
	}

	@Required @Configurable
	public void setSessionProvider(Supplier<PersistenceGmSession> sessionProvider) {
		this.sessionProvider = sessionProvider;
	}
	
	@Configurable
	public void setRunOnStartup(boolean runOnStartup) {
		this.runOnStartup = runOnStartup;
	}
	
	@Override
	public void postConstruct() {
		if (runOnStartup) {
			try {
				ensureModelExposure();
			} catch (Exception e) {
				throw Exceptions.unchecked(e, "Error while ensuring model exposure.");
			}
		}
	}

	public void ensureModelExposure() throws Exception {
		throw new UnsupportedOperationException("model Exposure no longer supported");
		
//		if (logger.isDebugEnabled())
//			logger.debug("Searching for unset model exposures on GmMetaModel instances in cortex.");
//		
//		PersistenceGmSession cortexSession = sessionProvider.provide();
//		EntityQuery query = EntityQueryBuilder.from(GmMetaModel.class).where().property("modelExposure").eq(null).done();
//		List<GmMetaModel> result = cortexSession.query().entities(query).list();
//		
//		int size = result.size();
//		
//		if (size == 0) {
//			if (logger.isDebugEnabled()) {
//				logger.debug("All instances of GmMetaModel have a modelExposure set.");
//			}
//			return;
//		}
//		
//		if (logger.isDebugEnabled())
//			logger.debug("Found: "+size+" instances of GmMetaModel that doesn't have a modelExposure set.");
//		
//		
//		for (GmMetaModel model : result) {
//			model.setModelExposure(defaultModelExposure);
//			if (logger.isTraceEnabled())
//				logger.trace("ModelExposure value for model: "+model.getName()+" set to: "+defaultModelExposure);
//			
//		}
//
//		if (cortexSession.getTransaction().hasManipulations()) {
//			cortexSession.commit();
//			if (logger.isDebugEnabled())
//				logger.debug("Successfully ensured modelExposures for: "+size+" models.");
//		}
		
		
	}

	
}
