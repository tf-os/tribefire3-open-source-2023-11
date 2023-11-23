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
package com.braintribe.model.access.sql.tools;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

import javax.sql.DataSource;

import com.braintribe.model.access.sql.api.ThrowingConsumer;
import com.braintribe.model.access.sql.api.ThrowingFunction;

/**
 * @author peter.gazdik
 */
public class JdbcTools {

	public static void doStatement(DataSource dataSource, ThrowingConsumer<Statement> consumer) {
		try {
			tryDoStatement(dataSource, consumer);

		} catch (Exception e) {
			throw new RuntimeException("Error while executing SQL statement(s).", e);
		}
	}

	public static void tryDoStatement(DataSource dataSource, ThrowingConsumer<Statement> consumer) throws Exception {
		try (Connection connection = dataSource.getConnection()) {
			tryDoStatement(connection, consumer);
		}
	}

	public static void tryDoStatement(Connection connection, ThrowingConsumer<Statement> consumer) throws Exception {
		try (Statement statement = connection.createStatement()) {
			consumer.accept(statement);
		}
	}

	public static <R> R tryComputeStatement(DataSource dataSource, ThrowingFunction<Statement, R> consumer) throws Exception {
		try (Connection connection = dataSource.getConnection()) {
			try (Statement statement = connection.createStatement()) {
				return consumer.apply(statement);
			}
		}
	}

	public static void tryDoPreparedStatement(Connection connection, String sql, ThrowingConsumer<PreparedStatement> consumer) throws Exception {
		try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			consumer.accept(preparedStatement);
		}
	}

	public static <R> R tryComputePreparedStatement(Connection connection, String sql, ThrowingFunction<PreparedStatement, R> consumer)
			throws Exception {

		try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			return consumer.apply(preparedStatement);
		}
	}

}
