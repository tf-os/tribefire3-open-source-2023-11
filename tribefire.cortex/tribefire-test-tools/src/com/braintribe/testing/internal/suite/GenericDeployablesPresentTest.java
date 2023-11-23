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
package com.braintribe.testing.internal.suite;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import com.braintribe.logging.Logger;
import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.deployment.DeploymentStatus;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.product.rat.imp.ImpApi;
import com.braintribe.product.rat.imp.ImpApiFactory;
import com.braintribe.utils.lcd.CommonTools;

/**
 * checks if all expected deployables are present and deployed, as well as expected demo entities are present
 *
 */
public class GenericDeployablesPresentTest  {

	private static Logger logger = Logger.getLogger(GenericDeployablesPresentTest.class);

	private final ImpApi imp;
	public GenericDeployablesPresentTest(PersistenceGmSessionFactory factory) {
		PersistenceGmSession session = factory.newSession("cortex");
		imp = new ImpApiFactory().factory(factory).build();
	}

	/**
	 *
	 * @param externalId
	 *            String
	 * @param deployableType
	 *            EntityType
	 *
	 * @throws GmSessionException
	 *             if no deployable with respective externalId and type is present AND deployed
	 */
	public void assertThatDeployableIsPresentAndDeployed(String externalId, EntityType<? extends Deployable> deployableType) {

		Deployable deployable = imp.deployable(deployableType, externalId).get();
		
		boolean isDeployed = deployable.getDeploymentStatus().equals(DeploymentStatus.deployed);
		String assertionFailureMessage = deployableType.getShortName() + " '" + externalId + "' is not deployed";

		if (!isDeployed) {
			throw new GmSessionException(assertionFailureMessage);
		}
	}


	/**
	 * makes sure there is at least one entity of every given type present
	 */
	public static void testEntitiesPresent(PersistenceGmSession accessSessoin, EntityType<?>... types) {
		logger.info("Test if at least one instance of each expected entity type was instantiated...");
		
		for (EntityType<?> type: CommonTools.getSet(types)){
			logger.info("Check if at least one instance of " + type.getTypeSignature() + " was instantiated.");
			EntityQuery entityQuery = EntityQueryBuilder.from(type).done();
			List<?> list = accessSessoin.query().entities(entityQuery).list();

			assertThat(!list.isEmpty());
		}
		
		logger.info("Test succeeded!");

	}

}
