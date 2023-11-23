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
package com.braintribe.model.processing.am;

import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;

import com.braintribe.common.lcd.EmptyReadWriteLock;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.SetType;
import com.braintribe.model.processing.session.impl.persistence.BasicPersistenceGmSession;
import com.braintribe.model.processing.session.impl.session.collection.EnhancedSet;
import com.braintribe.model.processing.smood.Smood;

public class AssemblyMonitoringTest extends AbstractMonitoringTest {

	@Override
	@Before
	public void prepare() {
		smood = new Smood(EmptyReadWriteLock.INSTANCE);
		session = new BasicPersistenceGmSession();
		session.setIncrementalAccess(smood);
	}

	@Override
	protected Node create(String name) {
		Node node = session.create(Node.T);
		node.setName(name);
		return node;
	}

	/**
	 * @param name
	 *            name
	 */
	protected Node create2(String name) {
		return null;
	}

	
	/* NOTE this does not do any monitoring now, as collection without owner does not have manipulation tracking.
	 * 
	 * This worked in 1.1 though, because AssemblyMonitorin itself sets the collection as a property of some entity.
	 * Before, there was the CollectionEnhancingPAI that would then set the owner of this collection as that entity, but
	 * now we just use the NonNullCollectionEnsuringPAI, which does not do this. If there is no owner, there is no
	 * manipulation tracking. 
	 * 
	 * EDIT 10.8.2015: manipulation is actually tracked, that causes an NPE. Will have to review with Dirk.
	 * */
	@Test
	public void testPlainCollection() throws Exception {
		Node n0 = create("n0");
		Node n1 = create("n1");
		Node n2 = create("n2");

		SetType collectionType = GMF.getTypeReflection().getSetType(GenericEntity.T);
		EnhancedSet<GenericEntity> set = new EnhancedSet<GenericEntity>(collectionType, new HashSet<GenericEntity>());

		set.add(n0);
		set.add(n1);
		set.add(n2);

		initMonitoring(set);

		monitoring.addEntityMigrationListener(new EntityMigrationListener() {

			@Override
			public void onLeave(GenericEntity entity) {
				println(entity + " left");
			}

			@Override
			public void onJoin(GenericEntity entity) {
				println(entity + " joined");
			}
		});

		// This would throw NPE, so we rather avoid it now.
		// set.remove(n0);
	}

	@Test
	public void testWithCollection() throws Exception {
		Node n0 = create("n0");
		Node n1 = create("n1");
		Node n2 = create("n2");

		initMonitoring(n0);

		n0.setLinkSet(new HashSet<Node>());
		n0.getLinkSet().add(n1);
		n0.getLinkSet().remove(n1);
		n1.setLink1(n2);

		// printMonitoringState();
	}

	@Test
	public void test() throws Exception {
		Node n0 = create("n0");
		Node n1 = create("n1");
		Node n2 = create("n2");
		Node n3 = create("n3");
		Node n4 = create("n4");
		Node n5 = create("n5");
		Node n6 = create("n6");
		Node n7 = create("n7");
		Node n8 = create("n8");
		Node n9 = create("n9");
		Node na = create("na");
		Node nb = create("nb");
		Node nc = create("nc");
		Node nd = create("nd");
		Node ne = create("ne");
		Node nf = create("nf");

		// circle one
		n0.setLink1(n1);
		n1.setLink1(n2);
		n2.setLink1(n0);

		// circle on subcircle
		n3.setLink1(n4);
		n4.setLink1(n5);
		n4.setLink2(n5);
		n5.setLink1(n3);
		n5.setLink2(n3);

		n0.setLink2(n3);

		// circle two
		n6.setLink1(n7);
		n7.setLink1(n8);
		n8.setLink1(n6);

		// circle three
		n9.setLink1(na);
		na.setLink1(nb);
		nb.setLink1(n9);

		// tree one
		nc.setLink1(nd);
		nc.setLink1(ne);
		ne.setLink1(nf);

		initMonitoring(n0);

		monitoring.addEntityMigrationListener(new EntityMigrationListener() {

			@Override
			public void onLeave(GenericEntity entity) {
				println(entity + " left");
			}

			@Override
			public void onJoin(GenericEntity entity) {
				println(entity + " joined");
			}
		});

		// printMonitoringState();

		// n1.setLink2(n6);
		// println(monitoring.getReferenceMap());
		n4.setLink2(null);
		println("N4 deleted 1");
		// printMonitoringState();
		n4.setLink1(null);
		println("N4 deleted 2");
		// printMonitoringState();
		// n4.setLink1(n5);
		// printMonitoringState();

		// n0.setLink1(null);
		// System.out.println(monitoring.getReferenceMap());
		// n0.setLink2(null);
		// System.out.println(monitoring.getReferenceMap());
	}

}
