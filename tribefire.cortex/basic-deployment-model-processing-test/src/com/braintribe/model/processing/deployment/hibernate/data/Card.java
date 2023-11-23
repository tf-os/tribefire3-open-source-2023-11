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
package com.braintribe.model.processing.deployment.hibernate.data;

import java.util.Date;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

interface Card extends GenericEntity {

	EntityType<Card> T = EntityTypes.T(Card.class);

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
