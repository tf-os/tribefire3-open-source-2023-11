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
package com.braintribe.model.processing.query.smart.test.base;

import static com.braintribe.model.processing.query.smart.test.setup.base.SmartMappingSetup.accessIdA;
import static com.braintribe.model.processing.query.smart.test.setup.base.SmartMappingSetup.accessIdB;
import static com.braintribe.utils.lcd.CollectionTools2.asSet;

import com.braintribe.common.lcd.EmptyReadWriteLock;
import com.braintribe.model.access.smood.basic.SmoodAccess;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.query.smart.test.model.deployment.MoodAccess;
import com.braintribe.model.processing.query.smart.test.setup.base.SmartMappingSetup;
import com.braintribe.model.processing.session.impl.persistence.BasicPersistenceGmSession;
import com.braintribe.testing.tools.gm.access.TransientNonIncrementalAccess;

/**
 * 
 */
public class DelegateAccessSetup {

	protected final SmartMappingSetup setup;

	protected BasicPersistenceGmSession session;
	protected SmoodAccess smoodAccessA;
	protected SmoodAccess smoodAccessB;
	protected MoodAccess smoodDenotationA;
	protected MoodAccess smoodDenotationB;

	public DelegateAccessSetup(SmartMappingSetup setup) {
		this.setup = setup;
	}

	// ######################################
	// ## . . . . . Smood Accesses . . . . ##
	// ######################################

	public SmoodAccess getAccessA() {
		if (smoodAccessA == null) {
			smoodAccessA = configureSmoodAccess(setup.modelA, accessIdA, accessIdA);
		}
		return smoodAccessA;
	}

	public SmoodAccess getAccessB() {
		if (smoodAccessB == null) {
			smoodAccessB = configureSmoodAccess(setup.modelB, accessIdB, accessIdB);
		}
		return smoodAccessB;
	}

	public MoodAccess getDenotationAccessA() {
		if (smoodDenotationA == null) {
			smoodDenotationA = configureSmoodDenotation(accessIdA, setup.modelA);
		}
		return smoodDenotationA;
	}

	public MoodAccess getDenotationAccessB() {
		if (smoodDenotationB == null) {
			smoodDenotationB = configureSmoodDenotation(accessIdB, setup.modelB);
		}
		return smoodDenotationB;
	}

	protected SmoodAccess configureSmoodAccess(GmMetaModel metaModel, String name, String... partitions) {
		SmoodAccess result = newSmoodAccess(partitions);
		result.setAccessId(name);
		result.setDataDelegate(new TransientNonIncrementalAccess(metaModel));
		result.setReadWriteLock(EmptyReadWriteLock.INSTANCE);
		result.setDefaultTraversingCriteria(null);
		return result;
	}

	protected SmoodAccess newSmoodAccess(String... partitions) {
		return new MultiPartitionSmoodAccess(asSet(partitions));
	}

	private MoodAccess configureSmoodDenotation(String name, GmMetaModel model) {
		MoodAccess result = MoodAccess.T.createPlain();
		result.setMetaModel(model);
		result.setExternalId(name);

		return result;
	}

}
