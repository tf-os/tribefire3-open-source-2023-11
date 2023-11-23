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

import java.util.Date;

import com.braintribe.model.leadership.Candidate;
import com.braintribe.model.processing.leadership.api.LeadershipListener;

public class LeadershipWrapper {
	
	protected String domainId;
	protected String candidateId;
	protected Candidate candidate;
	protected LeadershipListener listener;
	
	public LeadershipWrapper(String domainId, String candidateId, LeadershipListener listener, Candidate candidate) {
		super();
		this.domainId = domainId;
		this.candidateId = candidateId;
		this.listener = listener;
		this.candidate = candidate;
	}

	
	public String getDomainId() {
		return domainId;
	}
	public String getCandidateId() {
		return candidateId;
	}
	public LeadershipListener getListener() {
		return listener;
	}
	public Candidate getCandidate() {
		return candidate;
	}
	
	@Override
	public String toString() {
		return ""+domainId+"/"+candidateId+": candidate: "+candidate;
	}


	public boolean isLeader() {
		return candidate.getIsLeader();
	}
	public void setLeader(boolean b) {
		candidate.setIsLeader(b);
	}
	public long getMsSinceLastPing() {
		Date pingTimestamp = candidate.getPingTimestamp();
		long msSinceLastPing = System.currentTimeMillis() - pingTimestamp.getTime();
		return msSinceLastPing;
	}
	public void updatePingTimestamp() {
		candidate.setPingTimestamp(new Date());
	}
	public void updateLeadershipPingTimestamp() {
		candidate.setLeadershipPingTimestamp(new Date());
	}

}
