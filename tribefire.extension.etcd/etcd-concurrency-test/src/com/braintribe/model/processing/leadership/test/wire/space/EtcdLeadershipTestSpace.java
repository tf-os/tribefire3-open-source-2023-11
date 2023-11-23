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
package com.braintribe.model.processing.leadership.test.wire.space;

import java.util.Arrays;
import java.util.List;

import com.braintribe.integration.etcd.supplier.ClientSupplier;
import com.braintribe.model.processing.leadership.api.LeadershipManager;
import com.braintribe.model.processing.leadership.etcd.EtcdLeadershipManager;
import com.braintribe.model.processing.leadership.test.wire.contract.EtcdLeadershipTestContract;
import com.braintribe.model.service.api.InstanceId;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.context.WireContextConfiguration;

@Managed
public class EtcdLeadershipTestSpace implements EtcdLeadershipTestContract {

	@Override
	public void onLoaded(WireContextConfiguration configuration) {
		EtcdLeadershipTestContract.super.onLoaded(configuration);

		// This initializes the leadership manager and with it the db table
		leadershipManager();
	}

	@Override
	@Managed
	public Integer failProbability() {
		return 10;
	}

	@Override
	@Managed
	public LeadershipManager leadershipManager() {
		EtcdLeadershipManager bean = new EtcdLeadershipManager();
		// TODO: add authentication case here if needed
		bean.setClientSupplier(new ClientSupplier(endpointUrls(), null, null));
		bean.setLocalInstanceId(instanceId());
		return bean;
	}

	@Managed
	private InstanceId instanceId() {
		InstanceId bean = InstanceId.T.create();
		bean.setNodeId("test-node");
		bean.setApplicationId("test-app");
		return bean;
	}

	@Override
	@Managed
	public Long writeInterval() {
		return 1_000L;
	}

	@Override
	@Managed
	public String host() {
		return "localhost";
	}

	@Override
	@Managed
	public Integer port() {
		return 2048;
	}

	@Override
	@Managed
	public Integer workerCount() {
		return 2;
	}

	@Override
	public List<String> endpointUrls() {
		return Arrays.asList("http://localhost:2379");
	}

}
