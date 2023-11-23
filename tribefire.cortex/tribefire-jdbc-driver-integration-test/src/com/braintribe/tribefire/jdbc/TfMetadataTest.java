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
package com.braintribe.tribefire.jdbc;

import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.testing.category.SpecialEnvironment;

@Category(SpecialEnvironment.class)
public class TfMetadataTest {

	private static TfConnection con;
	private static DatabaseMetaData metaData;
//	private static Map<String, EntityType<?>> typeMap;

	@BeforeClass
	public static void initialize() throws Exception {
		Class.forName("com.braintribe.tribefire.jdbc.TfDriver");
		con = (TfConnection) DriverManager
				.getConnection("jdbc:tribefire://localhost:8443/tribefire-services?accessId=auth", "cortex", "cortex");

		metaData = con.getMetaData();
//		typeMap = con.getEntityTypeMap();
	}

	@Test
	public void testMetaDataTableName() throws Exception {

		try {
			ResultSet rs = metaData.getTables(null, null, null, null);
			while (rs.next()) {
				String tableName = rs.getString("TABLE_NAME");
				System.out.println("Table: " + tableName);
			}

		} catch (Exception e) {
			throw new Exception("Error while getting meta data", e);
		}
	}

	@Test
	public void testTfAssociationTables() { // TODO
		con.getAssociationTableMap().get("User").iterator().next();
	}

	@Test
	public void testMetaDataGeneral() throws Exception {

		Connection con = DriverManager.getConnection("jdbc:tribefire://localhost:8443/tribefire-services?accessId=auth",
				"cortex", "cortex");

		try {
			DatabaseMetaData metaData = con.getMetaData();
			ResultSet rs = metaData.getColumns(null, null, "User", null);

			while (rs.next()) {
				String colName = rs.getString("COLUMN_NAME");
				int dataType = rs.getInt("DATA_TYPE");
				System.out.println("Column: " + colName + ", type=" + dataType);
			}
			System.out.println();

			rs = metaData.getTables(null, null, null, null);
			while (rs.next()) {
				String tableName = rs.getString("TABLE_NAME");
				System.out.println("Table: " + tableName);
			}
			System.out.println();

			rs = metaData.getTableTypes();
			ResultSetMetaData metaData2 = rs.getMetaData();
			int colCount = metaData2.getColumnCount();
			while (rs.next()) {
				for (int i = 0; i < colCount; ++i) {
					String col = metaData2.getColumnName(i + 1);
					Object o = rs.getObject(i + 1);
					System.out.println(col + ": " + o + " (wasnull: " + rs.wasNull() + ")");
				}
			}
			System.out.println();

			rs = metaData.getSchemas();
			metaData2 = rs.getMetaData();
			colCount = metaData2.getColumnCount();
			while (rs.next()) {
				String schema = rs.getString(1);
				String cat = rs.getString(2);
				System.out.println("Catalog: " + cat + ", Schema: " + schema + " (wasnull: " + rs.wasNull() + ")");
			}
			System.out.println();

		} catch (Exception e) {
			throw new Exception("Error while getting meta data", e);
		}
	}

	@Test
	public void testMetaDataFK() throws SQLException {
		String table = "User";
		ResultSet grs = metaData.getImportedKeys(null, con.getAccessId(), table);
		assertTrue(grs.next());
	}
}
