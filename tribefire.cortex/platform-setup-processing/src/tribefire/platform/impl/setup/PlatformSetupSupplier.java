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
package tribefire.platform.impl.setup;

import java.time.Instant;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.model.platformsetup.PlatformSetup;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.date.NanoClock;

public class PlatformSetupSupplier implements Supplier<PlatformSetup> {

	private static final Logger logger = Logger.getLogger(PlatformSetupSupplier.class);

	private Supplier<PersistenceGmSession> setupSessionFactory;

	protected boolean initialized = false;
	protected PlatformSetup platformSetup = null;


	protected static TraversingCriterion allTc = TC.create()
			.negation().joker()
			.done();	

	@Override
	public PlatformSetup get() {

		if (initialized) {
			return platformSetup;
		}
		initialized = true;

		logger.debug(() -> "Loading the PlatformSetup");
		Instant start = NanoClock.INSTANCE.instant();

		try {
			PersistenceGmSession session = setupSessionFactory.get();

			EntityQuery query = EntityQueryBuilder.from(PlatformSetup.T).tc(allTc).done();
			platformSetup = session.query().entities(query).first();
		} catch(Exception e) {
			logger.warn(() -> "Error while trying to get the PlatformSetup entity.", e);
		} finally {
			logger.debug(() -> "Loading the PlatformSetup took "+StringTools.prettyPrintDuration(start, true, null)+". Result available: "+(platformSetup != null));
		}

		return platformSetup;
	}

	@Required
	@Configurable
	public void setSetupSessionFactory(Supplier<PersistenceGmSession> setupSessionFactory) {
		this.setupSessionFactory = setupSessionFactory;
	}
}
