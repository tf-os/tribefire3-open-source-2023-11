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

import org.junit.Test;

import com.braintribe.model.processing.service.common.topology.InstanceIdHashingComparator;
import com.braintribe.model.service.api.InstanceId;

public class InstanceIdHashingComparatorTest {

	@Test
	public void testSimple() throws Exception {

		InstanceIdHashingComparator comp = InstanceIdHashingComparator.instance;
		
		
		assertThat(comp.compare(createInstanceId(null, null), createInstanceId(null, null))).isTrue();
		assertThat(comp.compare(createInstanceId("n1", null), createInstanceId("n1", null))).isTrue();
		assertThat(comp.compare(createInstanceId(null, "a1"), createInstanceId(null, "a1"))).isTrue();
		
		assertThat(comp.compare(createInstanceId("n1", null), createInstanceId("n2", null))).isFalse();
		assertThat(comp.compare(createInstanceId(null, "a1"), createInstanceId(null, "a2"))).isFalse();

		assertThat(comp.compare(createInstanceId("n1", null), createInstanceId("n2", null))).isFalse();
		assertThat(comp.compare(createInstanceId(null, "a1"), createInstanceId(null, "a2"))).isFalse();

	}

	private static InstanceId createInstanceId(String nodeId, String appId) {
		InstanceId iid = InstanceId.T.create();
		iid.setNodeId(nodeId);
		iid.setApplicationId(appId);
		return iid;
	}
}
