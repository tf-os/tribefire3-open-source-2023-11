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
package com.braintribe.model.processing.query.smart.test.builder.special;

import com.braintribe.model.processing.query.smart.test.builder.AbstractBuilder;
import com.braintribe.model.processing.query.smart.test.builder.SmartDataBuilder;
import com.braintribe.model.processing.query.smart.test.builder.repo.RepositoryDriver;
import com.braintribe.model.processing.query.smart.test.model.accessB.special.BookB;

/**
 * 
 */
public class BookBBuilder extends AbstractBuilder<BookB, BookBBuilder> {

	public static BookBBuilder newInstance(SmartDataBuilder dataBuilder) {
		return new BookBBuilder(dataBuilder.repoDriver());
	}

	public BookBBuilder(RepositoryDriver repoDriver) {
		super(BookB.class, repoDriver);
	}

	public BookBBuilder titleB(String value) {
		instance.setTitleB(value);
		return this;
	}

	public BookBBuilder author(String value) {
		instance.setAuthor(value);
		return this;
	}

	public BookBBuilder isbn(String value) {
		instance.setIsbn(value);
		return this;
	}

}
