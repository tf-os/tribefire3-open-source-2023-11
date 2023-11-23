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

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.processing.query.smart.test.model.accessA.StandardIdentifiableA;
import com.braintribe.model.processing.query.smart.test.model.smart.special.SmartBookA;

/**
 * @see SmartBookA
 */

public interface BookA extends StandardIdentifiableA {

	final EntityType<BookA> T = EntityTypes.T(BookA.class);

	// @formatter:off
	String getTitle();
	void setTitle(String title);

	String getAuthor();
	void setAuthor(String author);

	int getNumberOfPages();
	void setNumberOfPages(int numberOfPages);

	String getIsbn();
	void setIsbn(String isbn);
	// @formatter:on

}
