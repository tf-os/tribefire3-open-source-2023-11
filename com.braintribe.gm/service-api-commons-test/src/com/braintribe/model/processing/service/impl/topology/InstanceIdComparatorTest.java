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
package com.braintribe.model.processing.service.impl.topology;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Iterator;
import java.util.Random;
import java.util.TreeSet;

import org.junit.Test;

import com.braintribe.model.processing.service.common.topology.InstanceIdComparator;
import com.braintribe.model.service.api.InstanceId;
import com.braintribe.utils.StringTools;

public class InstanceIdComparatorTest {

	@Test
	public void testComparatorNodeIdSimple() throws Exception {

		TreeSet<InstanceId> set = new TreeSet<>(InstanceIdComparator.instance);

		set.add(createInstanceId("3", "1"));
		set.add(createInstanceId("2", "1"));
		set.add(createInstanceId("1", "1"));

		Iterator<InstanceId> iterator = set.iterator();
		for (int i=1; i<3; ++i) {
			assertThat(iterator.next().getNodeId()).isEqualTo(""+i);
		}
	}

	@Test
	public void testComparatorNodeIdRandom() throws Exception {

		TreeSet<InstanceId> set = new TreeSet<>(InstanceIdComparator.instance);

		int count = 1000;
		Random rnd = new Random();
		for (int i=0; i<count; ++i) {
			set.add(createInstanceId(StringTools.extendStringInFront(""+rnd.nextInt(100000), '0', 7), "1"));

		}

		int latest = -1;
		for (InstanceId iid : set) {
			int current = Integer.parseInt(iid.getNodeId());
			assertThat(current).isGreaterThanOrEqualTo(latest);
			latest = current;
		}
	}

	@Test
	public void testComparatorMixedSimple() throws Exception {

		TreeSet<InstanceId> set = new TreeSet<>(InstanceIdComparator.instance);

		int count = 1000;
		Random rnd = new Random();
		for (int i=0; i<count; ++i) {
			set.add(createInstanceId(StringTools.extendStringInFront(""+rnd.nextInt(100000), '0', 7), StringTools.extendStringInFront(""+rnd.nextInt(100000), '0', 7)));
		}

		int latestNodeId = -1;
		int latestAppId = -1;
		for (InstanceId iid : set) {
			int currentNodeId = Integer.parseInt(iid.getNodeId());
			int currentAppId = Integer.parseInt(iid.getApplicationId());
			
			assertThat(currentNodeId).isGreaterThanOrEqualTo(latestNodeId);
			if (currentNodeId > latestNodeId) {
				latestAppId = -1;
			}
			assertThat(currentAppId).isGreaterThanOrEqualTo(latestAppId);
			
			latestNodeId = currentNodeId;
			latestAppId = currentAppId;
		}
	}

	@Test
	public void testComparatorNoNodeIdSimple() throws Exception {

		TreeSet<InstanceId> set = new TreeSet<>(InstanceIdComparator.instance);

		set.add(createInstanceId(null, "1"));
		set.add(createInstanceId(null, "3"));
		set.add(createInstanceId(null, "2"));

		Iterator<InstanceId> iterator = set.iterator();
		for (int i=1; i<3; ++i) {
			assertThat(iterator.next().getApplicationId()).isEqualTo(""+i);
		}
	}
	
	@Test
	public void testComparatorMixedNodeIdSimple() throws Exception {

		TreeSet<InstanceId> set = new TreeSet<>(InstanceIdComparator.instance);

		set.add(createInstanceId(null, "3"));
		set.add(createInstanceId("1", "1"));
		set.add(createInstanceId(null, "2"));

		Iterator<InstanceId> iterator = set.iterator();
		for (int i=1; i<3; ++i) {
			assertThat(iterator.next().getApplicationId()).isEqualTo(""+i);
		}
	}
	
	private static InstanceId createInstanceId(String nodeId, String appId) {
		InstanceId iid = InstanceId.T.create();
		iid.setNodeId(nodeId);
		iid.setApplicationId(appId);
		return iid;
	}
}
