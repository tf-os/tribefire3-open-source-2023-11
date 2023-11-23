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
package com.braintribe.model.access.hibernate.schema;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.function.Supplier;

import javax.sql.DataSource;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.exception.Exceptions;

/**
 * Schema provider supplied by JDBC functionality
 * 
 *
 */
public class SimpleSchemaProvider implements Supplier<String> {

	private DataSource dataSource;

	private String schema;

	@Override
	public String get() {
		if (schema == null) {
			try (Connection connection = dataSource.getConnection()) {
				DatabaseMetaData metaData = connection.getMetaData();
				String userName = metaData.getUserName();
				// TODO: rework
				// String url = metaData.getURL();
				// schema = connection.getSchema();
				schema = userName;
			} catch (Exception e) {
				throw Exceptions.unchecked(e, "Could not get 'schema'");
			}
		}
		return schema;
	}

	// -----------------------------------------------------------------------
	// GETTER & SETTER
	// -----------------------------------------------------------------------

	@Configurable
	@Required
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

}
