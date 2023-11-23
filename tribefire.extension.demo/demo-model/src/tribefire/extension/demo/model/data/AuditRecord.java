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
package tribefire.extension.demo.model.data;

import java.util.Date;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

@SelectiveInformation("${date} - ${info}")
public interface AuditRecord extends GenericEntity {
	
	
	final EntityType<AuditRecord> T = EntityTypes.T(AuditRecord.class);
	
	// TODO: Dynamic initializer now() currently removed because fo causing an issue in the produced cortex manipulation stack. 
	// (issue after cartridge sync and restart)
	//@Initializer("now()")
	Date getDate();
	//@Initializer("now()")
	void setDate(Date date);
	
	@Mandatory
	String getInfo();
	void setInfo(String info);
}
