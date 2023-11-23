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
package com.braintribe.model.processing.query.smart.test.model.accessA.special;

import java.util.Set;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.processing.query.smart.test.model.accessA.StandardIdentifiableA;
import com.braintribe.model.processing.query.smart.test.model.smart.special.SmartReaderA;

/**
 * @see SmartReaderA
 */

public interface ReaderA extends StandardIdentifiableA {

	EntityType<ReaderA> T = EntityTypes.T(ReaderA.class);

	// @formatter:off
	String getName();
	void setName(String name);

	String getFavoritePublicationTitle();
	void setFavoritePublicationTitle(String favoritePublicationTitle);

	Set<String> getFavoritePublicationTitles();
	void setFavoritePublicationTitles(Set<String> favoritePublicationTitles);

	ManualA getFavoriteManual();
	void setFavoriteManual(ManualA favoriteManual);

	// mapped via IKPA - this.ikpaPublicationTitle = SmartPublicationB.title
	String getIkpaPublicationTitle();
	void setIkpaPublicationTitle(String ikpaPublicationTitle);

	// ##################################################
	// ## . . . . . . Weak-type Properties . . . . . . ##
	// ##################################################

	String getFavoriteManualTitle();
	void setFavoriteManualTitle(String favoriteManualTitle);

	Set<String> getFavoriteManualTitles();
	void setFavoriteManualTitles(Set<String> favoriteManualTitles);
	// @formatter:on

}
