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
package com.braintribe.model.processing.query.smart.test.model.smart;

import java.util.Date;
import java.util.List;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.processing.query.smart.test.model.accessB.PersonB;

/**
 * Mapped to {@link PersonB}
 */
public interface SmartPersonB extends StandardSmartIdentifiable, BasicSmartEntity {

	EntityType<SmartPersonB> T = EntityTypes.T(SmartPersonB.class);

	String getNameB();
	void setNameB(String nameB);

	int getAgeB();
	void setAgeB(int ageB);

	String getCompanyNameB();
	void setCompanyNameB(String companyNameB);

	Company getCompanyB();
	void setCompanyB(Company companyB);

	// based on keyProperty - PesonB.parentA = PersonA.nameA
	SmartPersonA getSmartParentA();
	void setSmartParentA(SmartPersonA smartParentA);

	// based on keyProperty - PesonB.parentA = PersonA.id
	SmartPersonA getConvertedSmartParentA();
	void setConvertedSmartParentA(SmartPersonA convertedSmartParentA);

	Date getConvertedBirthDate();
	void setConvertedBirthDate(Date convertedBirthDate);

	List<Date> getConvertedDates();
	void setConvertedDates(List<Date> convertedDates);

}
