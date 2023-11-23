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

import static com.braintribe.gm.jdbc.api.GmLobLoadingMode.NO_LOB;
import static com.braintribe.gm.jdbc.api.GmLobLoadingMode.ONLY_LOB;
import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static com.braintribe.utils.lcd.CollectionTools2.first;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import com.braintribe.common.db.DbVendor;
import com.braintribe.gm.jdbc.api.GmColumn;
import com.braintribe.gm.jdbc.api.GmRow;
import com.braintribe.gm.jdbc.api.GmTable;
import com.braintribe.model.resource.Resource;

/**
 * @author peter.gazdik
 */
public class GmDb_Advanced_Tests extends AbstractGmDbTestBase {

	public GmDb_Advanced_Tests(DbVendor vendor) {
		super(vendor);
	}

	// ## Column definitions

	private final GmColumn<Long> colIdLong = gmDb.longCol("id").primaryKey().notNull().done();
	private final GmColumn<String> colString = gmDb.string("str").done();

	@Test
	public void testPagination() {
		final String TABLE_NAME = "pagination" + tmSfx;

		GmTable table = gmDb.newTable(TABLE_NAME).withColumns(colIdLong, colString).done();

		table.ensure();

		for (int i = 0; i < 10; i++) {
			table.insert( //
					colIdLong, (long) i, //
					colString, "John" + i //
			);
		}

		collectResult(table.select() //
				.orderBy("id asc") //
				.limit(1) //
				.rows());

		assertResultSize(1);
		assertIds(0L);

		collectResult(table.select() //
				.orderBy("id asc") //
				.limitAndOffset(2, 4) //
				.rows());

		assertResultSize(2);
		assertIds(4L, 5L);
	}

	@Test
	public void testBatchLoading() {
		final String TABLE_NAME = "batch_loading" + tmSfx;

		GmTable table = gmDb.newTable(TABLE_NAME).withColumns(colIdLong, colString).done();

		table.ensure();

		for (int i = 0; i < 10; i++) {
			table.insert( //
					colIdLong, (long) i, //
					colString, "John" + i //
			);
		}

		collectResult(table.select() //
				.orderBy("id asc") //
				.limitAndOffset(5, 1) //
				.rowsInBatchesOf(2) //
				.values());

		assertResultSize(5);
		assertIds(1L, 2L, 3L, 4L, 5L);

		collectResult(table.select() //
				.orderBy("id asc") //
				.limitAndOffset(4, 1) //
				.rowsInBatchesOf(2) //
				.values());

		assertResultSize(4);
		assertIds(1L, 2L, 3L, 4L);
	}

	@Test
	public void testLongTextResource() {
		final String TABLE_NAME = "long_text_res" + tmSfx;

		long _id = 1L;
		Resource _text4K = textResource(str4K());
		Resource _text64KPlus = textResource(str64K() + "HELLO");

		GmColumn<Resource> col4K = resColumn("res4K");
		GmColumn<Resource> col64KPlus = resColumn("res64Kplus");

		GmTable table = gmDb.newTable(TABLE_NAME).withColumns(colIdLong, col4K, col64KPlus).done();

		table.ensure();

		table.insert( //
				colIdLong, _id, //
				col4K, _text4K, //
				col64KPlus, _text64KPlus //
		);

		collectResult(table.select().rows());

		assertResultSize(1);

		GmRow row = first(queryResult);

		assertThat(row.getValue(colIdLong)).isEqualTo(_id);
		assertSameResource(row.getValue(col4K), _text4K);
		assertSameResource(row.getValue(col64KPlus), _text64KPlus);
	}

	private GmColumn<Resource> resColumn(String name) {
		// MySQL has a limit on total row size, so let's make the columns smaller.
		return vendor == DbVendor.mysql ? gmDb.resource(name, 5000).done() : gmDb.resource(name).done();
	}

	@Test
	public void testLobLoadingMode() {
		// This test relies on the knowledge of the underlying representation of CLOBs / long strings.
		if (vendor != DbVendor.derby)
			return;

		final String TABLE_NAME = "loab_loading" + tmSfx;

		long _id = 1L;

		String shortText = "Short Text";
		String longText = str64K();
		Resource shortRes = textResource(shortText);
		Resource longRes = textResource(longText);

		GmColumn<String> colShortStr = gmDb.string("strK").done();
		GmColumn<String> colLongStr = gmDb.string("str64K").done();

		GmColumn<Resource> colShortRes = gmDb.resource("res4K", 4000).done();
		GmColumn<Resource> colLongRes = gmDb.resource("res64Kplus").done();

		GmTable table = gmDb.newTable(TABLE_NAME).withColumns( //
				colIdLong, //
				colShortStr, colLongStr, //
				colShortRes, colLongRes).done();

		table.ensure();

		table.insert( //
				colIdLong, _id, //
				colShortStr, shortText, //
				colLongStr, longText, //
				colShortRes, shortRes, //
				colLongRes, longRes //
		);

		GmRow row;

		// ###############################################
		// ## . . NOTHING LOADED DUE TO LOB LOADING . .##
		// ###############################################

		collectResult(table.select() //
				.lobLoading(colShortStr, NO_LOB).lobLoading(colLongStr, ONLY_LOB) //
				.lobLoading(colShortRes, NO_LOB).lobLoading(colLongRes, ONLY_LOB) //
				.rows());

		assertResultSize(1);
		row = first(queryResult);

		assertEquals(row.getValue(colShortStr), shortText);
		assertEquals(row.getValue(colLongStr), longText);
		assertSameResource(row.getValue(colShortRes), shortRes);
		assertSameResource(row.getValue(colLongRes), longRes);

		// ###############################################
		// ## . . NOTHING LOADED DUE TO LOB LOADING . .##
		// ###############################################

		collectResult(table.select() //
				.lobLoading(colShortStr, ONLY_LOB).lobLoading(colLongStr, NO_LOB) //
				.lobLoading(colShortRes, ONLY_LOB).lobLoading(colLongRes, NO_LOB) //
				.rows());

		assertResultSize(1);
		row = first(queryResult);

		assertNull(row.getValue(colShortStr));
		assertNull(row.getValue(colLongStr));
		assertNull(row.getValue(colShortRes));
		assertNull(row.getValue(colLongRes));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testMultiColumnNonNull() {
		final String TABLE_NAME = "non_null_multi" + tmSfx;

		long _id = 1L;

		// resource is reserved at oracle
		GmColumn<Resource> colShortRes = gmDb.resource("res0urce").notNull().done();

		GmTable table = gmDb.newTable(TABLE_NAME).withColumns( //
				colIdLong, //
				colShortRes).done();

		table.ensure();

		table.insert( //
				colIdLong, _id
		);
	}

	private void assertIds(Long... ids) {
		List<Long> expectedIds = asList(ids);
		List<Long> actualIds = queryResult.stream() //
				.map(r -> r.getValue(colIdLong)) //
				.collect(Collectors.toList());

		assertThat(actualIds).isEqualTo(expectedIds);
	}

}
