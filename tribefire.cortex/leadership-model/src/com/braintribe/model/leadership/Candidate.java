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
package com.braintribe.model.leadership;

import java.util.Date;

import com.braintribe.model.generic.StandardStringIdentifiable;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.service.api.InstanceId;


public interface Candidate extends StandardStringIdentifiable {

	final EntityType<Candidate> T = EntityTypes.T(Candidate.class);

	public final static String domainId = "domainId";
	public final static String instanceId = "instanceId";
	public final static String candidateId = "candidateId";
	public final static String candidateType = "candidateType";
	public final static String priority = "priority";
	public final static String isLeader = "isLeader";
	public final static String pingTimestamp = "pingTimestamp";
	public final static String leadershipPingTimestamp = "leadershipPingTimestamp";
	
	void setDomainId(String domainId);
	String getDomainId();

	void setInstanceId(InstanceId instanceId);
	InstanceId getInstanceId();
	
	void setCandidateId(String candidateId);
	String getCandidateId();

	void setCandidateType(CandidateType candidateType);
	CandidateType getCandidateType();

	void setPriority(int priority);
	int getPriority();
	
	void setIsLeader(boolean isLeader);
	boolean getIsLeader();
	
	void setPingTimestamp(Date pingTimestamp);
	Date getPingTimestamp();

	void setLeadershipPingTimestamp(Date leadershipPingTimestamp);
	Date getLeadershipPingTimestamp();

}
