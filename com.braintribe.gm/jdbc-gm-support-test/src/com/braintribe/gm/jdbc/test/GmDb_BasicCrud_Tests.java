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
package com.braintribe.gm.jdbc.test;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;
import static com.braintribe.utils.lcd.CollectionTools2.first;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

import org.junit.Test;

import com.braintribe.common.db.DbVendor;
import com.braintribe.gm.jdbc.api.GmColumn;
import com.braintribe.gm.jdbc.api.GmRow;
import com.braintribe.gm.jdbc.api.GmTable;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.ManipulationType;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.resource.Resource;
import com.braintribe.utils.RandomTools;

/**
 * @author peter.gazdik
 */
public class GmDb_BasicCrud_Tests extends AbstractGmDbTestBase {

	public GmDb_BasicCrud_Tests(DbVendor vendor) {
		super(vendor);
	}

	// ## Column definitions

	private final GmColumn<String> colIdStr = gmDb.shortString255("id").primaryKey().notNull().done();

	private final GmColumn<Boolean> colBoolean = gmDb.booleanCol("boolVal").done();
	private final GmColumn<Integer> colInt = gmDb.intCol("intVal").done();
	private final GmColumn<Long> colLong = gmDb.longCol("longVal").done();
	private final GmColumn<Float> colFloat = gmDb.floatCol("floatVal").done();
	private final GmColumn<Double> colDouble = gmDb.doubleCol("doubleVal").done();
	private final GmColumn<BigDecimal> colDecimal = gmDb.bigDecimal("decimalVal").done();

	private final GmColumn<Date> colDate = gmDb.date("dateVal").done();
	private final GmColumn<Date> colTimestamp = gmDb.date("timestampVal").done();

	private final GmColumn<String> colString255 = gmDb.shortString255("str255").done();
	private final GmColumn<String> colShortString = gmDb.string("strShort").done();
	private final GmColumn<String> col1KString = gmDb.string("str1K").done();
	private final GmColumn<String> col4KString = gmDb.string("str4K").done();
	private final GmColumn<String> colLongString = gmDb.string("longNvarchar").done();

	private final GmColumn<ManipulationType> colEnum = gmDb.enumCol("enum", ManipulationType.class).done();
	private final GmColumn<GenericEntity> colEntityAsString = gmDb.entityAsString("entity", GenericEntity.T).done();

	private final GmColumn<Resource> colTextResource = gmDb.resource("textResource").done();

	// ## Column values

	private final String _id = RandomTools.timeStamp();

	private final Boolean _bolean = Boolean.TRUE;
	private final Integer _int = 123;
	private final Long _long = 123_456_789_000L;
	private final Float _float = 0.25f;
	private final Double _double = 45e120;
	private final BigDecimal _decimal = new BigDecimal(5000);

	private final Date _date = new Date();
	private final Date _timestamp = new Timestamp(_date.getTime());

	private final String _str255 = "Some Varchar Value";
	private final String _strShort = str8Chars();
	private final String _str1K = str1K();
	private final String _str4K = str4K();
	private final String _stringLong = _str4K + _str4K + " And more chars...";
	private final ManipulationType _enum = ManipulationType.ACQUIRE;
	private final GmEntityType _entityAsString = entityAsString();
	private final Resource _textResource = textResource(_strShort);

	private static GmEntityType entityAsString() {
		GmEntityType result = GmEntityType.T.create();
		result.setTypeSignature("Type Signature");

		return result;
	}

	private static GmEntityType entityAsString2() {
		GmEntityType result = GmEntityType.T.create();
		result.setTypeSignature("Type Signature 2");

		return result;
	}

	// ##################################################
	// ## . . . . . . . . Actual Tests . . . . . . . . ##
	// ##################################################

	@Test
	public void testCreateAndRead() {
		final String TABLE_NAME = "basic_cr" + tmSfx;
		GmTable table = ensureTableAndInsertOneRow(TABLE_NAME);

		collectResult(table.select().rows());

		// ## Assertions

		assertResultSize(1);

		GmRow row = first(queryResult);

		assertThat(row.getValue(colIdStr)).isEqualTo(_id);

		assertThat(row.getValue(colBoolean)).isEqualTo(_bolean);

		assertThat(row.getValue(colInt)).isEqualTo(_int);
		assertThat(row.getValue(colLong)).isEqualTo(_long);
		assertThat(row.getValue(colFloat)).isEqualTo(_float);
		assertThat(row.getValue(colDouble)).isEqualTo(_double);
		assertThat(row.getValue(colDecimal).longValue()).isEqualTo(_decimal.longValue());

		assertThat(row.getValue(colDate)).isEqualTo(_date);
		assertThat(row.getValue(colTimestamp)).isEqualTo(_timestamp);

		assertThat(row.getValue(colString255)).isEqualTo(_str255);
		assertThat(row.getValue(colShortString)).isEqualTo(_strShort);
		assertThat(row.getValue(col1KString)).isEqualTo(_str1K);
		assertThat(row.getValue(col4KString)).isEqualTo(_str4K);
		assertThat(row.getValue(colLongString)).isEqualTo(_stringLong);

		assertThat(row.getValue(colEnum)).isEqualTo(_enum);

		GmEntityType entityStr = (GmEntityType) row.getValue(colEntityAsString);
		assertThat(entityStr).isNotNull();
		assertThat(entityStr.getTypeSignature()).isEqualTo(_entityAsString.getTypeSignature());

		assertSameResource(row.getValue(colTextResource), _textResource);
	}

	@Test
	public void testCreateAndReadNulls() {
		final String TABLE_NAME = "basic_cr_nulls" + tmSfx;

		GmTable table = newGmTable(TABLE_NAME);
		table.ensure();

		table.insert( //
				colIdStr, _id, //
				colBoolean, null //
		);

		collectResult(table.select().rows());

		// ## Assertions

		assertResultSize(1);

		GmRow row = first(queryResult);

		assertThat(row.getValue(colIdStr)).isEqualTo(_id);

		assertThat(row.getValue(colBoolean)).isNull();

		assertThat(row.getValue(colInt)).isNull();
		assertThat(row.getValue(colLong)).isNull();
		assertThat(row.getValue(colFloat)).isNull();
		assertThat(row.getValue(colDouble)).isNull();
		assertThat(row.getValue(colDecimal)).isNull();

		assertThat(row.getValue(colDate)).isNull();
		assertThat(row.getValue(colTimestamp)).isNull();

		assertThat(row.getValue(colString255)).isNull();
		assertThat(row.getValue(colShortString)).isNull();
		assertThat(row.getValue(col1KString)).isNull();
		assertThat(row.getValue(col4KString)).isNull();
		assertThat(row.getValue(colLongString)).isNull();

		assertThat(row.getValue(colEnum)).isNull();
		assertThat(row.getValue(colEntityAsString)).isNull();

		assertThat(row.getValue(colTextResource)).isNull();
	}

	@Test
	public void testConditionalReads() {
		final String TABLE_NAME = "conditions" + tmSfx;
		GmTable table = ensureTableAndInsertOneRow(TABLE_NAME);

		// test condition is considered - no value with wrong condition
		assertRowsCountForColumnCondition(table, 0, "intVal", _int + 1);

		assertRowsCountForColumnCondition(table, 1, "intVal", _int);
		assertRowsCountForColumnCondition(table, 1, "longVal", _long);
		assertRowsCountForColumnCondition(table, 1, "floatVal", _float);
		assertRowsCountForColumnCondition(table, 1, "doubleVal", _double);
		assertRowsCountForColumnCondition(table, 1, "dateVal", _date);
		assertRowsCountForColumnCondition(table, 1, "timestampVal", _timestamp);
		assertRowsCountForColumnCondition(table, 1, "decimalVal", _decimal);
		assertRowsCountForColumnCondition(table, 1, "str255", _str255);
		assertRowsCountForColumnCondition(table, 1, "enum", _enum);

		// Same thing but using
		assertRowsCountForColumnCondition(table, 1, colInt, _int);
	}

	@Test
	public void testMultiConditionalRead() {
		final String TABLE_NAME = "multiCondition" + tmSfx;

		GmTable table = newGmTable(TABLE_NAME);
		table.ensure();

		table.insert(colIdStr, "multi-A", colInt, 1, colLong, 1_000L);
		table.insert(colIdStr, "multi-B", colInt, 1, colLong, 2_000L);

		collectResult(table.select().whereColumn(colInt, 1).rows());
		assertResultSize(2);

		collectResult(table.select().whereColumn(colInt, 1).whereColumn(colLong, 1_000L).rows());
		assertResultSize(1);
	}

	@Test
	public void testRead_UsingIn() {
		final String TABLE_NAME = "read_in" + tmSfx;

		GmTable table = newGmTable(TABLE_NAME);
		table.ensure();

		table.insert(colIdStr, "id1", colInt, 1);
		table.insert(colIdStr, "id2", colInt, 2);
		table.insert(colIdStr, "id3", colInt, 3);

		collectResult(table.select().whereColumnIn(colInt, 1, 2).rows());
		assertResultSize(2);

		assertThat(resultValues(colIdStr)).containsExactlyInAnyOrder("id1", "id2");
	}

	private final Boolean _bolean2 = Boolean.TRUE;
	private final Integer _int2 = 10 * _int;
	private final Long _long2 = 10 * _long;
	private final Float _float2 = 10 * _float;
	private final Double _double2 = 10 * _double;
	private final BigDecimal _decimal2 = _decimal.multiply(BigDecimal.TEN);

	private final Date _date2 = new Date(_date.getTime() + 1000);
	private final Date _timestamp2 = new Timestamp(_date2.getTime());

	private final String _str255_2 = "X" + _str255;
	private final String _strShort2 = "X" + _strShort;
	private final String _str1K_2 = "X" + _str1K;
	private final String _str4K_2 = "X" + _str4K;
	private final String _stringLong2 = "X" + _stringLong;
	private final ManipulationType _enum2 = ManipulationType.CHANGE_VALUE;
	private final GmEntityType _entityAsString2 = entityAsString2();
	private final Resource _textResource2 = textResource(_strShort2);

	@Test
	public void testUpdates() {
		final String TABLE_NAME = "updates" + tmSfx;
		GmTable table = ensureTableAndInsertOneRow(TABLE_NAME);

		int updated = table.update( //
				colBoolean, _bolean2, //

				colInt, _int2, //
				colLong, _long2, //
				colFloat, _float2, //
				colDouble, _double2, //
				colDecimal, _decimal2, //

				colDate, _date2, //
				colTimestamp, _timestamp2, //

				colString255, _str255_2, //
				colShortString, _strShort2, //
				col1KString, _str1K_2, //
				col4KString, _str4K_2, //
				colLongString, _stringLong2, //
				colEnum, _enum2, //
				colEntityAsString, _entityAsString2, //

				colTextResource, _textResource2 //
		).whereColumn(colIdStr, _id);

		// ## Assertions

		assertThat(updated).isEqualTo(1);

		collectResult(table.select().rows());

		assertResultSize(1);

		GmRow row = first(queryResult);

		assertThat(row.getValue(colIdStr)).isEqualTo(_id);

		assertThat(row.getValue(colBoolean)).isEqualTo(_bolean2);

		assertThat(row.getValue(colInt)).isEqualTo(_int2);
		assertThat(row.getValue(colLong)).isEqualTo(_long2);
		assertThat(row.getValue(colFloat)).isEqualTo(_float2);
		assertThat(row.getValue(colDouble)).isEqualTo(_double2);
		assertThat(row.getValue(colDecimal).longValue()).isEqualTo(_decimal2.longValue());

		assertThat(row.getValue(colDate)).isEqualTo(_date2);
		assertThat(row.getValue(colTimestamp)).isEqualTo(_timestamp2);

		assertThat(row.getValue(colString255)).isEqualTo(_str255_2);
		assertThat(row.getValue(colShortString)).isEqualTo(_strShort2);
		assertThat(row.getValue(col1KString)).isEqualTo(_str1K_2);
		assertThat(row.getValue(col4KString)).isEqualTo(_str4K_2);
		assertThat(row.getValue(colLongString)).isEqualTo(_stringLong2);

		assertThat(row.getValue(colEnum)).isEqualTo(_enum2);

		GmEntityType entityStr = (GmEntityType) row.getValue(colEntityAsString);
		assertThat(entityStr).isNotNull();
		assertThat(entityStr.getTypeSignature()).isEqualTo(_entityAsString2.getTypeSignature());

		assertSameResource(row.getValue(colTextResource), _textResource2);
	}

	private GmTable ensureTableAndInsertOneRow(final String TABLE_NAME) {
		GmTable table = newGmTable(TABLE_NAME);
		table.ensure();
		doStandardInsert(table);

		return table;
	}

	private GmTable newGmTable(final String TABLE_NAME) {
		if (TABLE_NAME.length() > 30)
			throw new IllegalArgumentException("Table name too long, Oracle only supports length <= 30. Table name: " + TABLE_NAME);

		return gmDb.newTable(TABLE_NAME) //
				.withColumns( //
						colIdStr, //
						colBoolean, //
						colInt, colLong, colFloat, colDouble, colDecimal, //
						colDate, colTimestamp, //
						colString255, colShortString, col1KString, col4KString, colLongString, //
						colEnum, //
						colEntityAsString, //
						colTextResource //
				).done();
	}

	private void doStandardInsert(GmTable table) {
		table.insert( //
				colIdStr, _id, //

				colBoolean, _bolean, //

				colInt, _int, //
				colLong, _long, //
				colFloat, _float, //
				colDouble, _double, //
				colDecimal, _decimal, //

				colDate, _date, //
				colTimestamp, _timestamp, //

				colString255, _str255, //
				colShortString, _strShort, //
				col1KString, _str1K, //
				col4KString, _str4K, //
				colLongString, _stringLong, //
				colEnum, _enum, //
				colEntityAsString, _entityAsString, //

				colTextResource, _textResource //
		);
	}

	private <T> void assertRowsCountForColumnCondition(GmTable table, int expectedResults, GmColumn<T> columnName, T columnValue) {
		collectResult(table.select().whereColumn(columnName, columnValue).rows());
		assertResultSize(expectedResults);
	}

	private void assertRowsCountForColumnCondition(GmTable table, int expectedResults, String columnName, Object columnValue) {
		assertRowsCountForCondition(table, expectedResults, columnName + " = ?", columnValue);
	}

	private void assertRowsCountForCondition(GmTable table, int expectedResults, String whereCondition, Object whereParam) {
		collectResult(table.select().where(whereCondition, whereParam).rows());
		assertResultSize(expectedResults);
	}

}
