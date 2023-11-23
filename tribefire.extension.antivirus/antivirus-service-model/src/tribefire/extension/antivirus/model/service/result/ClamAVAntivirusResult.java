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
package tribefire.extension.antivirus.model.service.result;

import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.annotation.meta.Unmodifiable;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * 
 * ClamAV Provider result
 * 
 *
 */
public interface ClamAVAntivirusResult extends AbstractAntivirusResult {

	EntityType<? extends ClamAVAntivirusResult> T = EntityTypes.T(ClamAVAntivirusResult.class);

	@Name("Status")
	@Description("Status of the antivirus scanning - PASSED, FAILED, ERROR")
	@Unmodifiable
	String getStatus();
	void setStatus(String status);
	
	@Name("Signature")
	@Description("Signature of the virus")
	@Unmodifiable
	String getSignature();
	void setSignature(String signature);
	
	@Name("Exception")
	@Description("Exception in case of error during the scanning")
	@Unmodifiable
	String getException();
	void setException(String exception);
	
	@Override
	default String getDetails() {
		switch (Status.valueOf(this.getStatus())) {
			case PASSED: 
				return String.format("No virus found in content.");
			case FAILED: 
				return String.format("Status: %s, Signature of the virus: %s", getStatus(), getSignature());
			case ERROR: 
				return String.format("Status: %s, Received error: %s", getStatus(), getException());
			default:
				throw new IllegalArgumentException("Invalid status received: " + getStatus());	
		}				
	}
	
	enum Status {PASSED, FAILED, ERROR}
}
