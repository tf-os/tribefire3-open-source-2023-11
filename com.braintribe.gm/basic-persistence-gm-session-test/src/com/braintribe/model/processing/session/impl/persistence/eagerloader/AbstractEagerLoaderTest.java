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
package com.braintribe.model.processing.session.impl.persistence.eagerloader;

import java.util.List;
import java.util.function.Consumer;

import org.assertj.core.api.Assertions;
import org.junit.Before;

import com.braintribe.model.access.smood.basic.SmoodAccess;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.impl.persistence.EagerLoader;
import com.braintribe.model.processing.session.impl.persistence.eagerloader.helpers.EagerLoaderTestPersistenceSession;
import com.braintribe.model.processing.session.impl.persistence.eagerloader.helpers.QueryCounter;
import com.braintribe.model.processing.session.impl.persistence.eagerloader.helpers.QueryTrackingAccess;
import com.braintribe.model.processing.session.impl.persistence.eagerloader.model.EagerItem;
import com.braintribe.model.processing.session.impl.persistence.eagerloader.model.EagerLoaderTestModel;
import com.braintribe.model.processing.session.impl.persistence.eagerloader.model.EagerOwner;
import com.braintribe.model.processing.smood.Smood;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.testing.tools.gm.GmTestTools;

/**
 * Tes
 * 
 * @author peter.gazdik
 */
public abstract class AbstractEagerLoaderTest {

	static final int NUMBER_OF_OWNERS = EagerLoader.DEFAULT_BULK_SIZE;

	protected Smood smood;
	protected QueryCounter queryCounter;

	protected QueryTrackingAccess access;
	protected PersistenceGmSession session;

	protected List<EagerOwner> owners;

	@Before
	public void init() {
		SmoodAccess smoodAccess = GmTestTools.newSmoodAccessMemoryOnly("test.EagerLoader", EagerLoaderTestModel.raw());
		smood = smoodAccess.getDatabase();

		queryCounter = new QueryCounter();

		access = new QueryTrackingAccess(smoodAccess, queryCounter);
		session = new EagerLoaderTestPersistenceSession(access);
	}

	protected void newOwner(int i, Consumer<EagerOwner> initializer) {
		String name = getOwnerName(i);

		EagerOwner owner = EagerOwner.T.create();
		owner.setGlobalId(name);
		owner.setId(i);
		owner.setName(name);

		initializer.accept(owner);

		smood.registerEntity(owner, false);
	}

	private String getOwnerName(int i) {
		// make sure each number has two digits
		String number = i < 10 ? "0" + i : "" + i;
		return "owner_" + number;
	}

	protected EagerItem newItemOf(EagerOwner owner, int i) {
		String name = getItemName(owner, i);

		EagerItem item = EagerItem.T.create();
		item.setGlobalId(name);
		item.setId(name);
		item.setName(name);

		return item;
	}

	protected String getItemName(EagerOwner owner, int i) {
		return owner.getName() + "_item_" + i;
	}

	protected void loadOwners() {
		EntityQuery ownerQuery = EntityQueryBuilder.from(EagerOwner.T).done();
		owners = session.query().entities(ownerQuery).list();
		queryCounter.reset();
	}

	protected void assertJustOneQuery() {
		Assertions.assertThat(queryCounter.totalCount).isEqualTo(1);
	}

}
