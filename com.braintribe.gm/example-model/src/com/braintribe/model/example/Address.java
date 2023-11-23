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
package com.braintribe.model.example;

import com.braintribe.model.generic.StandardIdentifiable;
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

@SelectiveInformation("${street} ${zip} ${place}")

public interface Address extends StandardIdentifiable {

	EntityType<Address> T = EntityTypes.T(Address.class);

	String street = "street";
	String place = "place";
	String zip = "zip";
	String city = "city";
	String country = "country";

	String getStreet();
	void setStreet(String street);

	String getPlace();
	void setPlace(String place);

	String getZip();
	void setZip(String zip);

	String getCity();
	void setCity(String city);

	String getCountry();
	void setCountry(String country);

}
