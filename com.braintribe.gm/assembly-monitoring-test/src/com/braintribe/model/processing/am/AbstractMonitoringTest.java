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

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.junit.Before;

import com.braintribe.common.lcd.EmptyReadWriteLock;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.session.impl.persistence.BasicPersistenceGmSession;
import com.braintribe.model.processing.session.impl.session.collection.EnhancedList;
import com.braintribe.model.processing.session.impl.session.collection.EnhancedMap;
import com.braintribe.model.processing.session.impl.session.collection.EnhancedSet;
import com.braintribe.model.processing.smood.Smood;

public abstract class AbstractMonitoringTest {

	protected Smood smood;
	protected BasicPersistenceGmSession session;
	protected AssemblyMonitoring monitoring;

	protected Node n0;
	protected Node n1;
	protected Node n2;
	protected Node n3;
	protected Node n4;
	protected Node n5;
	protected Node n6;
	protected Node n7;
	protected Node n8;
	protected Node n9;
	protected Node na;
	protected Node nb;
	protected Node nc;
	protected Node nd;
	protected Node ne;
	protected Node nf;

	protected boolean consolePrintingEnabled = false;

	@Before
	public void prepare() {
		smood = new Smood(EmptyReadWriteLock.INSTANCE);
		session = new BasicPersistenceGmSession();
		session.setIncrementalAccess(smood);

		createInstances();
	}

	protected void createInstances() {
		n0 = create("n0");
		n1 = create("n1");
		n2 = create("n2");
		n3 = create("n3");
		n4 = create("n4");
		n5 = create("n5");
		n6 = create("n6");
		n7 = create("n7");
		n8 = create("n8");
		n9 = create("n9");
		na = create("na");
		nb = create("nb");
		nc = create("nc");
		nd = create("nd");
		ne = create("ne");
		nf = create("nf");
	}

	protected Node create(String name) {
		Node node = session.create(Node.T);
		node.setName(name);
		return node;
	}

	protected void println(String s) {
		if (consolePrintingEnabled)
			System.out.println(s);
	}

	protected void printMonitoringState() {
		Map<GenericEntity, RefereeData> map = monitoring.getReferenceMap();
		Map<String, RefereeData> strMap = new TreeMap<String, RefereeData>();

		for (Entry<GenericEntity, RefereeData> entry: map.entrySet()) {
			Node node = (Node) entry.getKey();
			strMap.put(node.getName(), entry.getValue());
		}

		System.out.println(strMap);
	}

	protected void initMonitoring(GenericEntity entity) {
		monitoring = AssemblyMonitoring.newInstance().build(session, entity);
	}

	protected void initMonitoring(EnhancedList<?> collection) {
		monitoring = AssemblyMonitoring.newInstance().build(session, collection);
	}

	protected void initMonitoring(EnhancedSet<?> collection) {
		monitoring = AssemblyMonitoring.newInstance().build(session, collection);
	}

	protected void initMonitoring(EnhancedMap<?, ?> map) {
		monitoring = AssemblyMonitoring.newInstance().build(session, map);
	}

	// ##########################################################################################
	// ## . . . . . . . . . . . . . . Preparing Graph . . . . . . . . . . . . . . . . . . . . .##
	// ##########################################################################################

	protected void chain(Node... nodes) {
		chainHelper(Arrays.asList(nodes));
	}

	protected void circle(Node... nodes) {
		ArrayList<Node> list = new ArrayList<Node>(Arrays.asList(nodes));
		list.add(nodes[0]);

		chainHelper(list);
	}

	private void chainHelper(List<Node> nodes) {
		for (int i = nodes.size() - 1; i > 0; i--) {
			Node prev = nodes.get(i - 1);
			Node next = nodes.get(i);

			prev.setLink1(next);
		}
	}

	// ##########################################################################################
	// ## . . . . . . . . . . . . . . . . Assertions . . . . . . . . . . . . . . . . . . . . . ##
	// ##########################################################################################

	protected void assertReachableEntities(GenericEntity... nodes) {
		assertThat(monitoring.getEntities()).isNotNull().isNotEmpty().containsOnly(nodes);
	}

	protected void assertReferenceDegree(GenericEntity referee, GenericEntity reference, int degree) {
		assertThat(monitoring.getReferenceMap()).isNotNull().containsKeys(reference);

		RefereeData refereeData = monitoring.getReferenceMap().get(reference);
		assertThat(refereeData.referees).isNotNull().containsKeys(referee);

		Counter counter = refereeData.referees.get(referee);
		assertThat(counter.count).isEqualTo(degree);
	}
}
