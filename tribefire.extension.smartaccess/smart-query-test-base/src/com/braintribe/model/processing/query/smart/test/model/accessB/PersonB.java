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
package com.braintribe.model.processing.query.smart.test.model.accessB;

import java.util.List;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * 
 */

public interface PersonB extends StandardIdentifiableB {

	final EntityType<PersonB> T = EntityTypes.T(PersonB.class);

	// @formatter:off
	/** such property is defined for both A and B */
	String getNameB();
	void setNameB(String nameB);

	int getAgeB();
	void setAgeB(int ageB);

	String getParentA();
	void setParentA(String parentA);

	String getCompanyNameB();
	void setCompanyNameB(String companyNameB);

	String getBirthDate();
	void setBirthDate(String birthDate);

	List<String> getDates();
	void setDates(List<String> dates);
	// @formatter:on

}
