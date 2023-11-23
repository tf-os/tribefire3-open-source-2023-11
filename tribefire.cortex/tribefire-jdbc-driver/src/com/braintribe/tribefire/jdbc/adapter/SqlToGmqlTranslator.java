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
package com.braintribe.tribefire.jdbc.adapter;

import java.sql.SQLFeatureNotSupportedException;

import org.apache.commons.lang.StringUtils;

import com.braintribe.logging.Logger;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.tribefire.jdbc.TfAssociationTable;
import com.braintribe.tribefire.jdbc.TfConnection;
import com.braintribe.tribefire.jdbc.TfMetadata;

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;

/**
 * SqlToGmqlTranslator is a helper class implementing various mapping methods to
 * compensate for philosophical differences in approach between SQL and GMQL.
 *
 */
public class SqlToGmqlTranslator {

	public static final String VIRTUAL_PREFIX = ""; // "@reserved%VIRT@";
	public static final String SYSTEM_PREFIX = "@reserved%VIRT@";
	private TfConnection connection;
	private Logger logger = Logger.getLogger(SqlToGmqlTranslator.class);
	// private Map<String, String> virtualAliasMap = new HashMap<>();

	/**
	 * Instantiates a new sql to gmql translator.
	 *
	 * @param connection
	 *            the connection
	 */
	public SqlToGmqlTranslator(TfConnection connection) {
		this.connection = connection;
	}

	/**
	 * Translate sql select to gmql.
	 *
	 * @param slct
	 *            the original select
	 * @return the modified select
	 */
	public Select translateSqlSelectItemsToGmql(Select slct) {
		PlainSelect slb = (PlainSelect) slct.getSelectBody();

		// Translate Select items
		if (slb.getSelectItems() != null) {
			for (SelectItem item : slb.getSelectItems()) {
				if (item instanceof SelectExpressionItem) {
					SelectExpressionItem sei = (SelectExpressionItem) item;
					if (sei.getExpression() instanceof Column) {
						Column col = (Column) sei.getExpression();
						col.setTable(translateColumnAndTable(slb, col));
					}
				}
			}
		}

		return slct;
	}

	/**
	 * Translate column and table. Allows for virtual tables as well as two-level
	 * reserved dot notation like alias.property.childProperty
	 *
	 * @param pSelect
	 *            the select
	 * @param col
	 *            the col
	 * @return the table
	 */
	public Table translateColumnAndTable(PlainSelect pSelect, Column col) {
		if (null == pSelect) {
			throw new IllegalArgumentException("The select statement must not be null.");
		}

		// correct schema and name, resolve aliases
		Table table = col.getTable();
		// alias in schema field? double dot reference format...
		Table aliasTable = getTableByAlias(pSelect, table.getSchemaName());
		if (aliasTable == null) {
			// alias in table name?
			aliasTable = getTableByAlias(pSelect, table.getName());
		}

		if (aliasTable == null && !StringUtils.isEmpty(table.getName())) {
			// probably just default alias and the table name should be part of the property
			// name
			col.setColumnName(table.getName() + "." + col.getColumnName());
			col.setTable(null);
		}

		// schema is usually misplaced with name and name with property prefix
		if (StringUtils.isNotEmpty(table.getSchemaName())) {
			col.setColumnName(table.getName() + "." + col.getColumnName());
			table.setName(table.getSchemaName());
			table.setSchemaName(null);
		}

		if (null != aliasTable && null != connection.getAssociationTableByName(aliasTable.getName())) {
			col.setColumnName(VIRTUAL_PREFIX + col.getColumnName());
		}

		// safer to get the table by alias
		if (aliasTable != null) {
			col.setTable(aliasTable);
			return aliasTable;
		} else {
			return table;
		}
	}

	/**
	 * Gets the table by alias, in case the source is not clear.
	 *
	 * @param pSelect
	 *            the select
	 * @param aliasName
	 *            the alias name
	 * @return the table by alias
	 */
	public Table getTableByAlias(PlainSelect pSelect, String aliasName) {
		if (null == pSelect) {
			throw new IllegalArgumentException("The select statement must not be null.");
		}

		if (pSelect.getFromItem() != null && pSelect.getFromItem().getAlias() != null
				&& pSelect.getFromItem().getAlias().getName().equals(aliasName)) {
			return (Table) pSelect.getFromItem();
		}

		if (pSelect.getJoins() != null) {
			for (Join jn : pSelect.getJoins()) {
				if (jn.getRightItem() instanceof Table) {
					Table joinTable = (Table) jn.getRightItem();
					if (joinTable.getAlias().getName().equals(aliasName)) {
						return joinTable;
					}
				}
			}
		}

		return null;
	}

	/**
	 * Gets the table by name, in case the table object is absent.
	 *
	 * @param pSelect
	 *            the select
	 * @param tableName
	 *            the table name
	 * @return the table by name
	 */
	public Table getTableByName(PlainSelect pSelect, String tableName) {
		if (null == pSelect) {
			throw new IllegalArgumentException("The select statement must not be null.");
		}

		if (pSelect.getFromItem() != null && pSelect.getFromItem().getAlias() != null
				&& ((Table) pSelect.getFromItem()).getName().equals(tableName)) {
			return (Table) pSelect.getFromItem();
		}

		if (pSelect.getJoins() != null) {
			for (Join jn : pSelect.getJoins()) {
				if (jn.getRightItem() instanceof Table) {
					Table joinTable = (Table) jn.getRightItem();
					if (joinTable.getName().equals(tableName)) {
						return joinTable;
					}
				}
			}
		}

		return null;
	}

	/**
	 * Translate the full select. Currently only the select items are converted at
	 * this stage.
	 *
	 * @param statement
	 *            the statement
	 * @return the select
	 * @throws SQLFeatureNotSupportedException
	 *             the SQL feature not supported exception
	 */
	public Select translateSelect(Statement statement) throws SQLFeatureNotSupportedException {
		if (statement instanceof Select) {
			return translateSqlSelectItemsToGmql((Select) statement);
		} else {
			throw new SQLFeatureNotSupportedException(
					"Only select statements are supported in version " + TfMetadata.DRIVER_VERSION);
		}
	}

	/**
	 * Translate virtual table query.
	 *
	 * @param query
	 *            the query
	 * @param queryParser
	 *            the query parser
	 * @return the select query
	 */
	// TODO unused
	// public SelectQuery translateVirtualTableQuery(SelectQuery query,
	// SqlQueryParser queryParser) {
	//
	// Set<From> fromsToProcess = query.getFroms().stream()
	// .filter(from -> null != connection.getAssociationTableByName(from.getName()))
	// .collect(Collectors.toSet());
	//
	// for (From from : fromsToProcess) {
	// TfAssociationTable assTab =
	// connection.getAssociationTableByName(from.getName());
	// query.getFroms().remove(from);
	// From newFrom = From.T.create();
	// newFrom.setEntityTypeSignature(assTab.getEntity().getTypeSignature());
	// newFrom.setName(from.getName());
	// query.getFroms().add(newFrom);
	//
	// // From newFrom = From.T.create();
	// // newFrom.setEntityTypeSignature(assTab.getEntity().getTypeSignature());
	// // newFrom.setName(from.getName());
	// // query.getFroms().add(targetFrom)
	// }
	//
	// return query;
	// }

	/**
	 * Translates and maps a virtual FROM clause.
	 *
	 * @param builder
	 *            the builder
	 * @param table
	 *            the table
	 * @return the select query builder
	 */
	public SelectQueryBuilder virtualFrom(SelectQueryBuilder builder, Table table) {
		String leftAlias = (null == table.getAlias()) ? SYSTEM_PREFIX + table.getName() : table.getAlias().getName();
		table.setAlias(new Alias(leftAlias));

		if (null != connection.getAssociationTableByName(table.getName())) {
			TfAssociationTable assTab = connection.getAssociationTableByName(table.getName());
			String rightAlias = SYSTEM_PREFIX + "r"; // defining a virtual alias

			builder = builder.select(leftAlias, assTab.getLeftEntity().getIdProperty().getName());

			if (assTab.getRightEntity() != null) {
				builder = builder.select(rightAlias, assTab.getRightEntity().getIdProperty().getName());
			} else {
				// looks like a primitive mapping value
				builder.select(leftAlias, assTab.getLeftProperty().getName());
			}

			builder = builder.from(assTab.getLeftEntity(), leftAlias).join(leftAlias,
					assTab.getLeftProperty().getName(), rightAlias);
			return builder;
		}

		logger.warn("Attempted to translate virtual From on table " + table.getFullyQualifiedName()
				+ " but it was not defined.");

		return builder;
	}

	/**
	 * Translates and maps a virtual JOIN clause.
	 *
	 * @param builder
	 *            the builder
	 * @param virtualJoin
	 *            the virtual join
	 * @param plainSelect
	 *            the plain select
	 * @return the select query builder
	 */
	public SelectQueryBuilder virtualJoin(SelectQueryBuilder builder, Join virtualJoin, PlainSelect plainSelect)
			throws SQLFeatureNotSupportedException {
		Table joinTable = (Table) virtualJoin.getRightItem();
		String joinAlias = (joinTable.getAlias() != null) ? joinTable.getAlias().getName()
				: SYSTEM_PREFIX + joinTable.getName();
		String leftAlias = builder.getFirstSource().getName();
		TfAssociationTable virtualTable = connection.getAssociationTableByName(joinTable.getName());

		if (null != virtualTable) {
			// join with FROM
			if (virtualTable.isForTableAndVirtualTable((Table) plainSelect.getFromItem(), joinTable)) {
				builder.join(leftAlias, virtualTable.getLeftProperty().getName(), joinAlias);
			}

			// also join with other tables if relevant
			for (Join otherJoin : plainSelect.getJoins()) {
				if (connection.getAssociationTableByName(((Table) otherJoin.getRightItem()).getName()) != null) {
					// ignore other virtual tables
					continue;
				}

				// add the join if valid
				if (virtualTable.isForTableAndVirtualTable((Table) otherJoin.getRightItem(), joinTable)) {
					leftAlias = (otherJoin.getRightItem().getAlias() != null)
							? otherJoin.getRightItem().getAlias().getName()
							: SYSTEM_PREFIX + ((Table) otherJoin.getRightItem()).getName();
					String otherJoinAlias = (joinTable.getAlias() != null) ? joinTable.getAlias().getName() + leftAlias
							: SYSTEM_PREFIX + joinTable.getName() + leftAlias;
					Table leftTable = getTableByAlias(plainSelect, leftAlias);
					if (!leftTable.getName().equals(virtualTable.getRightEntityName())) {
						builder.join(leftAlias, virtualTable.getLeftProperty().getName(), otherJoinAlias);
					} else {
						throw new SQLFeatureNotSupportedException(
								"Attempted join in the wrong direction. Virtual association table "
										+ virtualTable.getName() + " cannot be joined from the "
										+ virtualTable.getRightEntityName()
										+ " entity. Aborting the query as results may be affected.");
					}
				}
			}

			return builder;
		}

		logger.warn("Attempted to translate virtual From on table " + joinTable.getFullyQualifiedName()
				+ " but it was not defined.");

		return builder;
	}

	/**
	 * Translates and maps a virtual WHERE clause.
	 *
	 * @param where
	 *            the where
	 * @return the expression
	 */
	public Expression translateWhere(Expression where) {
		// implemented per partes in the SqlQueryParser
		return where;
	}

	// /**
	// * Adds the virtual alias to the alias map.
	// *
	// * @param from
	// * the from
	// * @param to
	// * the to
	// * @return the string
	// */
	// TODO currently unnecessary
	// public String addVirtualAlias(String from, String to) {
	// return virtualAliasMap.put(from, to);
	// }

}
