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
package com.braintribe.zarathud.model.forensics;

import java.util.List;
import java.util.Map;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.zarathud.model.forensics.findings.ComparisonIssueType;

/**
 * a finger print as generated by the forensic experts for each issue
 * @author pit
 *
 */
/**
 * 
 * @author pit
 */
public interface FingerPrint extends GenericEntity {
	
	EntityType<FingerPrint> T = EntityTypes.T(FingerPrint.class);

	String slots = "slots";
	String issueData = "issueData";
	String comparisonIssueData = "comparisonIssueData";
	String comparisonIssue = "comparisonIssue";
	String entitySource = "entitySource";
	String entityComparisonTarget = "entityComparisonTarget";
	String origin = "origin";

	/**
	 * @return - a map of the slot keys and values
	 */
	Map<String,String> getSlots();
	void setSlots( Map<String,String> map);
	
	/**
	 * @return - additional string data attached to an issue 
	 */
	List<String> getIssueData();
	void setIssueData( List<String> data);
	
	
	/**
	 * @return - additional data added by the comparator (mainly ZedEntities, MethodEntity, FieldEntity et al)
	 */
	List<GenericEntity> getComparisonIssueData();
	void setComparisonIssueData(List<GenericEntity> value);
	
	/**
	 * @return - the actual issue the comparator found
	 */
	ComparisonIssueType getComparisonIssueType();
	void setComparisonIssueType(ComparisonIssueType value);


	/**
	 * @return - the actually entity that was the source/center of the issue
	 */
	GenericEntity getEntitySource();
	void setEntitySource( GenericEntity entity);
	
	/**
	 * @return - an optional entity that was involved (most comparison)
	 */
	GenericEntity getEntityComparisonTarget();
	void setEntityComparisonTarget( GenericEntity entity);
	

	/**
	 * @return - the {@link FingerPrintOrigin}, i.e. where the {@link FingerPrint} has been created (extracted or injected)
	 */
	FingerPrintOrigin getOrigin();
	void setOrigin(FingerPrintOrigin value);

	
}
