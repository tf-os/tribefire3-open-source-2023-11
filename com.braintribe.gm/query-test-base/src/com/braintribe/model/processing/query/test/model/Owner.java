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
package com.braintribe.model.processing.query.test.model;

import java.util.List;
import java.util.Map;
import java.util.Set;


import com.braintribe.model.generic.annotation.ToStringInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

@ToStringInformation("Owner[$;]")
public interface Owner extends Person {

	EntityType<Owner> T = EntityTypes.T(Owner.class);

	Set<Company> getCompanySet();
	void setCompanySet(Set<Company> companySet);

	List<Company> getCompanyList();
	void setCompanyList(List<Company> companyList);

	Map<String, Company> getCompanyMap();
	void setCompanyMap(Map<String, Company> companyMap);

	Map<String, String> getCompanyTypeMap();
	void setCompanyTypeMap(Map<String, String> companyTypeMap);

	Map<Company, Integer> getCompanyValueMap();
	void setCompanyValueMap(Map<Company, Integer> companyValueMap);

}
