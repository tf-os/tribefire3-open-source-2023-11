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
package com.braintribe.model.processing.leadership.etcd;

import java.net.URLEncoder;

import com.braintribe.integration.etcd.EtcdProcessing;
import com.braintribe.logging.Logger;
import com.braintribe.model.processing.leadership.api.LeadershipHandle;

public class EtcdLeadershipHandle implements LeadershipHandle {

	private static Logger logger = Logger.getLogger(EtcdLeadershipHandle.class);
	
	private String identification;
	private EtcdProcessing etcdProcessing;
	private String domainId;

	public EtcdLeadershipHandle(String domainId, String identification, EtcdProcessing etcdProcessing) {
		this.domainId = domainId;
		this.identification = identification;
		this.etcdProcessing = etcdProcessing;
	}
	
	@Override
	public String getIdentification() {
		return identification;
	}

	@Override
	public void release() {
		try {
			String key = EtcdLeadershipManager.leadershipPrefix+URLEncoder.encode(domainId, "UTF-8");
			etcdProcessing.deleteIfMatches(key, identification);
		} catch(Exception e) {
			logger.error("Error while releasing leadership.", e);
		}
	}

}
