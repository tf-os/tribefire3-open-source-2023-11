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
package tribefire.extension.simple.model.data;

import com.braintribe.model.generic.StandardIdentifiable;
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * An <code>Address</code> describes a location. It consists of information such as the {@link #getStreet() street}, {@link #getCity() city} and
 * {@link #getCountry() country}.
 */
@SelectiveInformation("${street} ${streetNumber}, ${postalCode} ${city} (${country})")
public interface Address extends StandardIdentifiable {

	// Constant to conveniently access the entity type.
	EntityType<Address> T = EntityTypes.T(Address.class);

	/* Constants which provide convenient access to all property names, which is e.g. useful for queries. */
	String street = "street";
	String streetNumber = "streetNumber";
	String postalCode = "postalCode";
	String city = "city";
	String country = "country";

	/**
	 * The street name.
	 */
	String getStreet();
	void setStreet(String street);

	/**
	 * The street number. (For the sake of simplicity this is just a number, although there could be street numbers like <code>21b</code>.)
	 */
	Integer getStreetNumber();
	void setStreetNumber(Integer streetNumber);

	/**
	 * The postal code.
	 */
	String getPostalCode();
	void setPostalCode(String postalCode);

	/**
	 * The city (name).
	 */
	String getCity();
	void setCity(String city);

	/**
	 * The country (name).
	 */
	String getCountry();
	void setCountry(String country);

}
