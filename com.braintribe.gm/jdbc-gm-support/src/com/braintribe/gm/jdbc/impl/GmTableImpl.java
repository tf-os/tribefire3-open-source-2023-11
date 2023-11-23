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
package com.braintribe.gm.jdbc.impl;

import static com.braintribe.utils.lcd.CollectionTools2.newLinkedSet;
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static java.util.Collections.unmodifiableSet;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.braintribe.exception.Exceptions;
import com.braintribe.gm.jdbc.api.GmColumn;
import com.braintribe.gm.jdbc.api.GmDb;
import com.braintribe.gm.jdbc.api.GmIndex;
import com.braintribe.gm.jdbc.api.GmSelectBuilder;
import com.braintribe.gm.jdbc.api.GmTable;
import com.braintribe.gm.jdbc.api.GmTableBuilder;
import com.braintribe.gm.jdbc.api.GmUpdateBuilder;
import com.braintribe.gm.jdbc.impl.column.AbstractGmColumn;
import com.braintribe.logging.Logger;
import com.braintribe.util.jdbc.JdbcTools;

/**
 * @author peter.gazdik
 */
public class GmTableImpl implements GmTable, GmTableBuilder {

	private static final Logger log = Logger.getLogger(GmTableImpl.class);

	protected final String tableName;
	protected final GmDb db;
	protected GmColumn<?> primaryKeyColumn;

	private final List<GmColumn<?>> columns = newList();
	private final List<GmColumn<?>> notNullColumns = newList();
	private final List<GmIndex> indices = newList();

	private Set<GmColumn<?>> readOnlyColumns;
	private Set<GmIndex> readOnlyIndices;

	private final Map<String, GmColumn<?>> columnsByName = newMap();
	private final Map<String, GmIndex> indicesByName = newMap();

	public GmTableImpl(String tableName, GmDb db) {
		this.tableName = tableName;
		this.db = db;
	}

	// ####################################################
	// ## . . . . . . . . GmTableBuilder . . . . . . . . ##
	// ####################################################

	@Override
	public GmTableBuilder withColumns(GmColumn<?>... columns) {
		return withColumns(Arrays.asList(columns));
	}

	@Override
	public GmTableBuilder withColumns(Collection<GmColumn<?>> newColumns) {
		adopt(newColumns);

		columns.addAll(newColumns);
		notNullColumns.addAll(findNotNullColumns(newColumns));

		return this;
	}

	private void adopt(Collection<GmColumn<?>> newColumns) {
		for (GmColumn<?> gmColumn : newColumns)
			((AbstractGmColumn<?>) gmColumn).setTable(this);
	}

	private List<GmColumn<?>> findNotNullColumns(Collection<GmColumn<?>> newColumns) {
		return newColumns.stream() //
				.filter(GmColumn::isNotNull) //
				.collect(Collectors.toList());
	}

	@Override
	public GmTableBuilder withIndices(GmIndex... indices) {
		return withIndices(Arrays.asList(indices));
	}

	@Override
	public GmTableBuilder withIndices(Collection<GmIndex> indices) {
		this.indices.addAll(indices);
		return this;
	}

	@Override
	public GmTable done() {
		this.readOnlyColumns = unmodifiableSet(newLinkedSet(columns));
		this.readOnlyIndices = unmodifiableSet(newLinkedSet(indices));

		index();
		return this;
	}

	private void index() {
		for (GmColumn<?> column : columns) {
			GmColumn<?> otherColumn = columnsByName.put(column.getGmName(), column);
			if (otherColumn != null)
				throw new IllegalStateException("Multiple columns registered with the same gm name: " + column.getGmName() + ". FIRST: " + column
						+ ", SECOND: " + otherColumn);

			if (column.isPrimaryKey())
				primaryKeyColumn = column;
		}

		for (GmIndex index : indices) {
			GmIndex otherIndex = indicesByName.put(index.getName(), index);
			if (otherIndex != null)
				throw new IllegalStateException(
						"Multiple indices registered with the same gm name: " + index.getName() + ". FIRST: " + index + ", SECOND: " + otherIndex);
		}
	}

	// ####################################################
	// ## . . . . . . . . . . GmTable . . . . . . . . . .##
	// ####################################################

	// @formatter:off
	@Override public String getName() { return tableName; }
	@Override public Set<GmColumn<?>> getColumns() { return readOnlyColumns; }
	@Override public Set<GmIndex> getIndices() { return readOnlyIndices; }
	// @formatter:on

	@Override
	public void ensure() {
		JdbcTools.withManualCommitConnection(db.dataSource, () -> "Ensuring table: " + getName(), this::ensureExists);
	}

	private void ensureExists(Connection c) {
		String table = JdbcTools.tableExists(c, tableName);
		if (table == null) {
			createTableWithAllColumns(c);
			addMissingIndices(c, tableName);
		} else {
			addMissingColumns(c, table);
			addMissingIndices(c, table);
		}
	}

	private void createTableWithAllColumns(Connection c) {
		String createStatement = createTableStatement();
		JdbcTools.withStatement(c, () -> "Creating table: " + tableName + " with statement: " + createStatement, s -> {
			try {
				log.debug(() -> "Creating table with statement: " + createStatement);
				s.executeUpdate(createStatement);
				log.debug(() -> "Successfully created table: " + tableName);

			} catch (Exception e) {
				try {
					if (JdbcTools.tableExists(c, tableName) != null)
						return;
				} catch (Exception e2) {
					e.addSuppressed(e2);
				}
				throw e;
			}
		});
	}

	private String createTableStatement() {
		StringJoiner sj = new StringJoiner(", ", "create table " + tableName + " (", ")");
		for (GmColumn<?> column : columns)
			column.streamSqlColumnDeclarations().forEach(sj::add);

		return sj.toString();
	}

	private void addMissingColumns(Connection c, String sqlTableName) {
		Set<String> existingColumns = JdbcTools.columnsExist(c, sqlTableName, columnNames());

		List<String> columnDeclarations = columnNames().stream() //
				.filter(column -> !existingColumns.contains(column)) //
				.map(this::getColumn) //
				.flatMap(GmColumn::streamSqlColumnDeclarations) //
				.collect(Collectors.toList());

		JdbcTools.withStatement(c, () -> "Adding missing columns for table: " + tableName, s -> {
			for (String declaration : columnDeclarations) {
				String st = "alter table " + sqlTableName + " add " + declaration;
				log.debug("Executing update statement: " + st);
				executeUpdate(s, st);
				log.debug("Successfully updated " + tableName + ".");
			}
		});
	}

	private void addMissingIndices(Connection c, String sqlTableName) {
		Set<String> existingIndices = JdbcTools.indicesExist(c, sqlTableName, indexNames());

		List<GmIndex> missingIndices = indexNames().stream() //
				.filter(index -> !existingIndices.contains(index)) //
				.map(this::getIndex) //
				.collect(Collectors.toList());

		createIndices(c, missingIndices, () -> "Adding missing indices to table: " + tableName);
	}

	private void createIndices(Connection c, List<GmIndex> indices, Supplier<String> detailsSupplier) {
		JdbcTools.withStatement(c, detailsSupplier, s -> {
			for (GmIndex index : indices) {
				String indexStatement = "create index " + index.getName() + " on " + tableName + " (" + index.getColumn().getSingleSqlColumn() + ")";

				try {
					log.debug("Creating index statement: " + indexStatement);
					executeUpdate(s, indexStatement);
					log.debug("Successfully added index:" + index.getName());

				} catch (Exception e) {
					throw Exceptions.contextualize(e, "Creating index for table [" + tableName + "] failed. "
							+ "If the error says this index already exists, maybe it exists on a different table, as we already checked indices on thsi table.");
				}
			}
		});
	}

	/* Some RDBSes like Oracle don't even tell you which column/index was the problem. */
	private void executeUpdate(Statement s, String sql) throws Exception {
		try {
			s.executeUpdate(sql);
		} catch (Exception e) {
			throw Exceptions.contextualize(e, "Executing statement: " + sql);
		}
	}

	private Set<String> columnNames() {
		return columnsByName.keySet();
	}

	private GmColumn<?> getColumn(String gmName) {
		return columnsByName.computeIfAbsent(gmName, n -> {
			throw new NoSuchElementException("No column found for name: " + gmName);
		});
	}

	private Set<String> indexNames() {
		return indicesByName.keySet();
	}

	private GmIndex getIndex(String name) {
		return indicesByName.computeIfAbsent(name, n -> {
			throw new NoSuchElementException("No index found for name: " + name);
		});
	}

	@Override
	public void insert(Connection connection, Map<GmColumn<?>, Object> values) {
		GmInsertImpl.insert(this, values, connection);
	}

	@Override
	public GmSelectBuilder select(Set<GmColumn<?>> columns) {
		return new GmSelectBuilderImpl(this, columns);
	}

	@Override
	public GmUpdateBuilder update(Map<GmColumn<?>, Object> columnsToValues) {
		return new GmUpdateBuilderImpl(this, columnsToValues);
	}

	public List<GmColumn<?>> notNullColumns() {
		return notNullColumns;
	}

}
