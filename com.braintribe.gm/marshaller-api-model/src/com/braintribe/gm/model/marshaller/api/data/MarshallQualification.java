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
package com.braintribe.gm.model.marshaller.api.data;

import com.braintribe.codec.marshaller.api.OutputPrettiness;
import com.braintribe.codec.marshaller.api.TypeExplicitness;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

@Abstract
public interface MarshallQualification extends GenericEntity {
	EntityType<MarshallQualification> T = EntityTypes.T(MarshallQualification.class);
	
	String getResourceName();
	void setResourceName(String resource);
	
	@Mandatory
    String getMimeType();
    void setMimeType(String mimeType);
    
    boolean getStabilizeOrder();
	void setStabilizeOrder(boolean stabilizeOrder);

    boolean getWriteEmptyProperties();
	void setWriteEmptyProperties(boolean writeEmptyProperties);
	
	boolean getWriteAbsenceInformation();
	void setWriteAbsenceInformation(boolean writeAbsenceInformation);

    TypeExplicitness getTypeExplicitness();
	void setTypeExplicitness(TypeExplicitness typeExplicitness);
	
	@Initializer(value="enum(com.braintribe.codec.marshaller.api.OutputPrettiness,mid)")
	OutputPrettiness getPrettiness();
	void setPrettiness(OutputPrettiness prettiness);
	
	@Initializer("0")
	Integer getEntityRecurrenceDepth();
	void setEntityRecurrenceDepth(Integer entityRecurrenceDepth);
}
