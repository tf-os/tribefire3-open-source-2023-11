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
package tribefire.extension.audit.model.deployment;

import java.util.Set;

import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.extensiondeployment.AccessAspect;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

@Description("AuditAspect tracks lifecycle and property manipulations in the IncrementalAccess it is assigned to. "
		+ "It uses the Audited, AuditedPreserved, Unaudited metadata to determine the actual requirement for tracking on certain types and properties.")
public interface AuditAspect extends AccessAspect {
	EntityType<AuditAspect> T = EntityTypes.T(AuditAspect.class);
	
	String auditAccess = "auditAccess"; 
	String untrackedRoles = "untrackedRoles"; 
	
	@Description("The access in which the ManipulationRecords are to be stored. "
			+ "If not given the ManipulationRecords will be stored in the IncrementalAccess that is being audited.")
	IncrementalAccess getAuditAccess();
	void setAuditAccess(IncrementalAccess auditAccess);
	
	Set<String> getUntrackedRoles();
	void setUntrackedRoles(Set<String> untrackedRoles);
	
}
