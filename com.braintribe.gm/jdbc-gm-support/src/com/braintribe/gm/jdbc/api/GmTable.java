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
package com.braintribe.gm.jdbc.api;

import static com.braintribe.utils.lcd.CollectionTools2.asMap;
import static com.braintribe.utils.lcd.CollectionTools2.asSet;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.sql.Connection;
import java.util.Map;
import java.util.Set;

/**
 * @see GmDb
 * 
 * @author peter.gazdik
 */
public interface GmTable {

	String getName();

	/** In order they were provided to the {@link GmTableBuilder}. */
	Set<GmColumn<?>> getColumns();

	/** In order they were provided to the {@link GmTableBuilder}. */
	Set<GmIndex> getIndices();

	void ensure();

	default void insert(Object... columnsAndValues) {
		insert(asMap(columnsAndValues));
	}

	default void insert(Map<GmColumn<?>, Object> columnsToValues) {
		insert(null, columnsToValues);
	}

	default void insert(Connection c, Object... columnsAndValues) {
		insert(c, asMap(columnsAndValues));
	}

	void insert(Connection c, Map<GmColumn<?>, Object> columnsToValues);

	default GmSelectBuilder select() {
		return select(newSet(getColumns()));
	}

	default GmSelectBuilder select(GmColumn<?>... columns) {
		return select(asSet(columns));
	}

	GmSelectBuilder select(Set<GmColumn<?>> columns);

	default GmUpdateBuilder update(Object... columnsAndValues) {
		return update(asMap(columnsAndValues));
	}

	GmUpdateBuilder update(Map<GmColumn<?>, Object> columnsToValues);

}
