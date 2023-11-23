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
package com.braintribe.model.processing.deployment.hibernate;

import org.junit.Test;

import com.braintribe.model.dbs.DbColumn;
import com.braintribe.utils.junit.assertions.BtAssertions;

/**
 * @see DbTableProcessingUtils
 */
public class DbTableProcessingUtilsTests {

	@Test
	public void propertyNameDerivations() throws Exception {
		assertPropertyName("my_property", "myProperty");
		assertPropertyName("MyProperty", "myProperty");
		assertPropertyName("ID", "id");
		assertPropertyName("iD", "id");
		assertPropertyName("URL", "url");
		assertPropertyName("uRL", "url");
		assertPropertyName("URL_property", "urlProperty");
		assertPropertyName("URL property", "urlProperty");
		assertPropertyName("URL Property", "urlProperty");
		assertPropertyName("AVI2MPG", "avi2MPG");
	}

	private void assertPropertyName(String columnName, String expectedPropertyName) {
		DbColumn dbColumn = DbColumn.T.create();
		dbColumn.setName(columnName);

		String actualPropertyName = DbTableProcessingUtils.getPropertyName(dbColumn);

		BtAssertions.assertThat(actualPropertyName).isEqualTo(expectedPropertyName);
	}

}
