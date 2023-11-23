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
package com.braintribe.model.processing.leadership;

import com.braintribe.logging.Logger;
import com.braintribe.model.leadership.CandidateType;
import com.braintribe.model.service.api.InstanceId;
import com.braintribe.utils.DigestGenerator;

public class CandidateIdentification {

	private static final Logger logger = Logger.getLogger(CandidateIdentification.class);

	protected String domainId = null;
	protected InstanceId instanceId = null;
	protected String candidateId = null;
	protected CandidateType candidateType = null;

	public CandidateIdentification(String domainId, InstanceId instanceId, String candidateId, CandidateType candidateType) {
		this.instanceId = instanceId;
		this.candidateId = candidateId;
		if (candidateId == null) {
			throw new NullPointerException("The candidate ID must not be null.");
		}
		this.domainId = domainId;
		if (domainId == null) {
			throw new NullPointerException("The domain ID must not be null.");
		}
		this.domainId = truncateId(this.domainId);

		if (candidateType == null) {
			this.candidateType = CandidateType.Dbl;
		} else {
			this.candidateType = candidateType;
		}
	}

	protected String truncateId(String id) {
		if (id.length() > 240) {
			String md5;
			try {
				md5 = DigestGenerator.stringDigestAsString(id, "MD5");
			} catch (Exception e) {
				logger.error("Could not generate an MD5 sum of ID " + id, e);
				md5 = "";
			}
			String cutId = id.substring(0, 200);
			String newId = cutId.concat("#").concat(md5);
			return newId;
		}
		return id;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof CandidateIdentification)) {
			return false;
		}
		CandidateIdentification other = (CandidateIdentification) obj;
		return this.candidateId.equals(other.candidateId);
	}

	@Override
	public int hashCode() {
		return this.candidateId.hashCode();
	}

	public String getDomainId() {
		return domainId;
	}
	public InstanceId getInstanceId() {
		return instanceId;
	}
	public String getCandidateId() {
		return candidateId;
	}

	public CandidateType getCandidateType() {
		return candidateType;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.candidateId);
		sb.append(" (domain: ");
		sb.append(this.domainId);
		if (instanceId != null) {
			sb.append(", instance: ");
			sb.append(this.instanceId);
			sb.append(")");
		}
		return sb.toString();
	}
}
