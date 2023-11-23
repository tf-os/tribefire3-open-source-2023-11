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

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.InitializationAware;
import com.braintribe.cfg.Required;
import com.braintribe.exception.Exceptions;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.EntityQuery;

public class GlobalIdEnsurer implements InitializationAware {
	
	private static final Logger logger = Logger.getLogger(GlobalIdEnsurer.class);
	protected Supplier<PersistenceGmSession> sessionProvider;
	private boolean runOnStartup = false;
	
	public GlobalIdEnsurer() {
		
	}

	@Required @Configurable
	public void setSessionProvider(
			Supplier<PersistenceGmSession> sessionProvider) {
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
				ensureGlobalIds();
			} catch (Exception e) {
				throw Exceptions.unchecked(e, "Error while ensuring global Ids.");
			}
		}
	}
	
	public void ensureGlobalIds() throws Exception {
		
		if (logger.isDebugEnabled())
			logger.debug("Searching for unset Global Ids in cortex.");
		
		PersistenceGmSession cortexSession = sessionProvider.get();
		EntityQuery query = EntityQueryBuilder.from(GenericEntity.class).where().property(GenericEntity.globalId).eq(null).done();
		List<GenericEntity> result = cortexSession.query().entities(query).list();
		
		int size = result.size();
		
		if (size == 0) {
			if (logger.isDebugEnabled()) {
				logger.debug("All instances of HasGlobalId have a globalId set.");
			}
			return;
		}
		
		if (logger.isDebugEnabled())
			logger.debug("Found: "+size+" instances of HasGlobalId that doesn't have a globalId set.");
		
		
		for (GenericEntity hasGlobalId : result) {
			String globalId = UUID.randomUUID().toString();
			hasGlobalId.setGlobalId(globalId);

			if (logger.isTraceEnabled())
				logger.trace("GlobalId value for instance: "+hasGlobalId+" set to: "+globalId);
			
		}

		if (cortexSession.getTransaction().hasManipulations()) {
			cortexSession.commit();
			if (logger.isDebugEnabled())
				logger.debug("Successfully ensured globalId values for: "+size+" instances.");
		}

	}
	
	
}
