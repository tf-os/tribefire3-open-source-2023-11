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
package com.braintribe.model.access.smart.test.query.business;

import static com.braintribe.model.processing.query.smart.test.setup.base.SmartMappingSetup.accessIdB;

import com.braintribe.model.access.smart.test.query.AbstractSmartQueryTests;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.query.smart.test.model.accessA.business.CustomerA;
import com.braintribe.model.processing.query.smart.test.model.accessB.business.JdeInventoryB;
import com.braintribe.model.processing.query.smart.test.model.accessB.business.SapInventoryB;
import com.braintribe.model.processing.smood.Smood;

/**
 * This is a base class for business-tests. It is a random collection of special use-cases that occurred in real-life and were causing
 * problems.
 */
public class AbstractBuisnessTests extends AbstractSmartQueryTests {

	protected CustomerA customerA(String ucn) {
		CustomerA result =  CustomerA.T.create();
		result.setUcn(ucn);
		result.setPartition(setup.getAccessA().getAccessId());

		return register(smoodA, result);
	}

	protected JdeInventoryB jde(String ucn, String info) {
		JdeInventoryB result = JdeInventoryB.T.create();
		result.setUcn(ucn);
		result.setProductInfo(info);
		result.setPartition(accessIdB);

		return register(smoodB, result);
	}

	protected SapInventoryB sap(String ucn, String info) {
		SapInventoryB result = SapInventoryB.T.create();
		result.setUcn(ucn);
		result.setProductInfo(info);
		result.setPartition(accessIdB);

		return register(smoodB, result);
	}

	private <T extends GenericEntity> T register(Smood smood, T entity) {
		smood.registerEntity(entity, true);
		return entity;
	}

}
