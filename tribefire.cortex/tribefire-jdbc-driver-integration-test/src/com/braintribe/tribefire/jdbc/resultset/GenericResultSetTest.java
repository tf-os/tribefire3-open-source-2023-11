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
package com.braintribe.tribefire.jdbc.resultset;

import org.junit.Test;

public class GenericResultSetTest {

	@Test
	public void simpleTests() throws Exception {
		GenericResultSet grs = new GenericResultSet("TABLE_SCHEM", "TABLE_CATALOG");
		grs.addData("tableScheme1", null);
		grs.addData("tableScheme2", "catalog2");
		
		while (grs.next()) {
			String s = grs.getString(1);
			String c = grs.getString("TABLE_CATALOG");
			System.out.println("Schema: "+s+", Catalog: "+c);
		}
		grs.close();
	}
	
	@Test
	public void emptySetTest() throws Exception {
		GenericResultSet grs = new GenericResultSet("TABLE_SCHEM", "TABLE_CATALOG");
		try {
			while (grs.next()) {
				throw new Exception("Unexpected.");
			}
		} finally {
			grs.close();
		}
	}
}
