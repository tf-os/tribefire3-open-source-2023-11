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
package com.braintribe.model.processing.deployment.hibernate.testmodel.inheritance.single;

import java.util.Date;

import com.braintribe.model.generic.StandardIdentifiable;
import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.annotation.ToStringInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

@ToStringInformation("Card branded by ${brand} + issued by ${issuer} international coverage is ${internationalCoverage}")
@Abstract
public interface Card extends StandardIdentifiable {

	EntityType<Card> T = EntityTypes.T(Card.class);

	// private static Long numberSequence = 10000L;

	// @formatter:off
	String getNumber();
	void setNumber(String number);

	CardCompany getBrand();
	void setBrand(CardCompany brand);

	Bank getIssuer();
	void setIssuer(Bank issuer);

	CardInternationalCoverage getInternationalCoverage();
	void setInternationalCoverage(CardInternationalCoverage internationalCoverage);

	Date getDate();
	void setDate(Date date);
	// @formatter:on

}
