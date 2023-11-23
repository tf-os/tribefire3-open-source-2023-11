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
package com.braintribe.model;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * Note: Probably better remove this class and its usages...
 * Only serviceDomain and typeSignature seem to be actually used and its implemented confusingly 
 */ 
public interface DdraBaseUrlPathParameters extends GenericEntity {
	
	EntityType<DdraBaseUrlPathParameters> T = EntityTypes.T(DdraBaseUrlPathParameters.class);
	
	String getServiceDomain();
	void setServiceDomain(String accessId);
	
	String getTypeSignature();
	void setTypeSignature(String typeSignature);
	
	String getSessionId();
	void setSessionId(String sessionId);
	
	@Initializer("true")
	boolean getIsDomainExplicit();
	void setIsDomainExplicit(boolean isDomainExplicit);
	
	

}
