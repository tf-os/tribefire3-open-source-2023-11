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

import com.braintribe.model.ddra.endpoints.IdentityManagementMode;
import com.braintribe.model.ddra.endpoints.OutputPrettiness;
import com.braintribe.model.ddra.endpoints.TypeExplicitness;
import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

@Abstract
public interface DdraEndpoint extends DdraEndpointHeaders {

	EntityType<DdraEndpoint> T = EntityTypes.T(DdraEndpoint.class);
	
	@Initializer(value="enum(com.braintribe.model.ddra.endpoints.OutputPrettiness,mid)")
	OutputPrettiness getPrettiness();
	void setPrettiness(OutputPrettiness prettiness);

	@Initializer("'3'")
	String getDepth();
	void setDepth(String depth);

	boolean getStabilizeOrder();
	void setStabilizeOrder(boolean stabilizeOrder);

	DdraEndpointDepth getComputedDepth();
	void setComputedDepth(DdraEndpointDepth computedDepth);

	boolean getWriteEmptyProperties();
	void setWriteEmptyProperties(boolean writeEmptyProperties);
	
	boolean getWriteAbsenceInformation();
	void setWriteAbsenceInformation(boolean writeAbsenceInformation);
	
	boolean getInferResponseType();
	void setInferResponseType(boolean inferResponseType);
	
	@Initializer(value="enum(com.braintribe.model.ddra.endpoints.TypeExplicitness,auto)")
	TypeExplicitness getTypeExplicitness();
	void setTypeExplicitness(TypeExplicitness typeExplicitness);
	
	@Initializer("0")
	Integer getEntityRecurrenceDepth();
	void setEntityRecurrenceDepth(Integer entityRecurrenceDepth);
	
	@Initializer(value="enum(com.braintribe.model.ddra.endpoints.IdentityManagementMode,auto)")
	IdentityManagementMode getIdentityManagementMode();
	void setIdentityManagementMode(IdentityManagementMode identityManagementMode);
	
	boolean getDownloadResource();
	void setDownloadResource(boolean downloadResource);
	
	boolean getSaveLocally();
	void setSaveLocally(boolean saveLocally);
	
	String getResponseFilename();
	void setResponseFilename(String responseFilename);
	
	String getResponseContentType();
	void setResponseContentType(String responseContentType);
	
}
