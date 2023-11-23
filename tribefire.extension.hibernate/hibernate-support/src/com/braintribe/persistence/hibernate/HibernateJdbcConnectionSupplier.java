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
package com.braintribe.persistence.hibernate;

import java.sql.Connection;
import java.util.function.Supplier;

import javax.sql.DataSource;

import org.hibernate.SessionFactory;

import com.braintribe.exception.Exceptions;
import com.braintribe.utils.lcd.LazyInitialization;

/**
 * A  {@link Connection JDBC connection} {@link Supplier} that ensures the schema is created by Hibernate before connection is used.
 */
public class HibernateJdbcConnectionSupplier implements Supplier<Connection> {

	private final SessionFactory sessionFactory;
	private final DataSource dataSource;
	private final LazyInitialization schemaUpdate = new LazyInitialization(this::updateSchema);

	public HibernateJdbcConnectionSupplier(SessionFactory sessionFactory, DataSource dataSource) {
		this.sessionFactory = sessionFactory;
		this.dataSource = dataSource;
	}

	@Override
	public Connection get() {
		schemaUpdate.run();

		try {
			return dataSource.getConnection();
		} catch (Exception e) {
			throw Exceptions.unchecked(e);
		}
	}

	private void updateSchema() {
		sessionFactory.openStatelessSession().close();
	}
}