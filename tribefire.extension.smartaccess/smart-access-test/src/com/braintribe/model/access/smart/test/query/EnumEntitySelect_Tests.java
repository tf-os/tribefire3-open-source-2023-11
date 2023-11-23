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
package com.braintribe.model.access.smart.test.query;

import org.junit.Test;

import com.braintribe.model.access.smart.query.fluent.SmartSelectQueryBuilder;
import com.braintribe.model.processing.query.smart.test.model.accessB.ItemTypeB;
import com.braintribe.model.processing.query.smart.test.model.smart.ItemType;
import com.braintribe.model.processing.query.smart.test.model.smart.ItemType_Identity;
import com.braintribe.model.processing.query.smart.test.model.smart.ItemType_String;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartEnumEntityB;
import com.braintribe.model.processing.smart.query.planner.EnumEntitySelection_PlannerTests;
import com.braintribe.model.query.SelectQuery;

/**
 * 
 */
public class EnumEntitySelect_Tests extends AbstractSmartQueryTests {

	/** @see EnumEntitySelection_PlannerTests#enumSelection_Identity */
	@Test
	public void enumSelection_Identity() {
		bB.enumEntityB("e1").enumIdentity(ItemType_Identity.MAGIC).create();
		bB.enumEntityB("e2").enumIdentity(ItemType_Identity.TOOL).create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartEnumEntityB.class, "e")
				.select("e", "enumIdentity")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(ItemType_Identity.MAGIC);
		assertResultContains(ItemType_Identity.TOOL);
		assertNoMoreResults();
	}

	/**
	 * Querying enum property from delegate directly still converts the enum to the right smart type.See also
	 * {@link PropertyConversion_Tests#selectPropertyFromDelegateWithoutConversion()}.
	 */
	@Test
	public void enumSelection_Identity_DirectlyFromDelegate() {
		bB.enumEntityB("e1").enumIdentity(ItemType_Identity.MAGIC).create();
		bB.enumEntityB("e2").enumIdentity(ItemType_Identity.TOOL).create();

		// @formatter:off
		SelectQuery selectQuery = new SmartSelectQueryBuilder()
				.selectDelegateProperty("e", "enumIdentity")
				.from(SmartEnumEntityB.class, "e")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(ItemType_Identity.MAGIC);
		assertResultContains(ItemType_Identity.TOOL);
		assertNoMoreResults();
	}

	@Test
	public void enumSelection_DelegateEnumConversion() {
		bB.enumEntityB("e1").enumAsDelegate(ItemTypeB.MAGIC_B).create();
		bB.enumEntityB("e2").enumAsDelegate(ItemTypeB.TOOL_B).create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartEnumEntityB.class, "e")
				.select("e", "enumAsDelegate")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(ItemType.MAGIC);
		assertResultContains(ItemType.TOOL);
		assertNoMoreResults();
	}

	/**
	 * Querying enum property from delegate directly prevents conversion in this case too. See also
	 * {@link PropertyConversion_Tests#selectPropertyFromDelegateWithoutConversion()}.
	 */
	@Test
	public void enumSelection_DelegateEnumConversion_DirectlyFromDelegate() {
		bB.enumEntityB("e1").enumAsDelegate(ItemTypeB.MAGIC_B).create();
		bB.enumEntityB("e2").enumAsDelegate(ItemTypeB.TOOL_B).create();

		// @formatter:off
		SelectQuery selectQuery = new SmartSelectQueryBuilder()
				.selectDelegateProperty("e", "enumAsDelegate")
				.from(SmartEnumEntityB.class, "e")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(ItemTypeB.MAGIC_B);
		assertResultContains(ItemTypeB.TOOL_B);
		assertNoMoreResults();
	}

	/** @see EnumEntitySelection_PlannerTests#enumSelection_StringConversion */
	@Test
	public void enumSelection_StringConversion() {
		bB.enumEntityB("e1").enumAsString("MAGIC_B").create();
		bB.enumEntityB("e2").enumAsString("TOOL_B").create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartEnumEntityB.class, "e")
				.select("e", "enumAsString")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(ItemType_String.MAGIC);
		assertResultContains(ItemType_String.TOOL);
		assertNoMoreResults();
	}

	/** @see EnumEntitySelection_PlannerTests#enumSelection_CustomConversion() */
	@Test
	public void enumSelection_CustomConversion() {
		bB.enumEntityB("e1").enumCustomConverted(ItemType.MAGIC.ordinal()).create();
		bB.enumEntityB("e2").enumCustomConverted(ItemType.TOOL.ordinal()).create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartEnumEntityB.class, "e")
				.select("e", "enumCustomConverted")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(ItemType.MAGIC);
		assertResultContains(ItemType.TOOL);
		assertNoMoreResults();
	}

	/** @see EnumEntitySelection_PlannerTests#enumSelection_StringConversion_Set */
	@Test
	public void enumSelection_StringConversion_Set() {
		bB.enumEntityB("e1").enumAsStringSet("MAGIC_B", "TOOL_B").create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartEnumEntityB.class, "e")
				.select("e", "enumAsStringSet")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(ItemType_String.MAGIC);
		assertResultContains(ItemType_String.TOOL);
		assertNoMoreResults();
	}

	/** @see EnumEntitySelection_PlannerTests#enumSelection_StringConversion_MapValue */
	@Test
	public void enumSelection_StringConversion_MapValue() {
		bB.enumEntityB("e1").enumAsStringMap("COSMETICS_B", "MAGIC_B", "OTHER_B", "TOOL_B").create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartEnumEntityB.class, "e")
				.select("e", "enumAsStringMap")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(ItemType_String.MAGIC);
		assertResultContains(ItemType_String.TOOL);
		assertNoMoreResults();
	}

	/** @see EnumEntitySelection_PlannerTests#enumSelection_StringConversion_MapKey */
	@Test
	public void enumSelection_StringConversion_MapKey() {
		bB.enumEntityB("e1").enumAsStringMap("COSMETICS_B", "MAGIC_B", "OTHER_B", "TOOL_B").create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartEnumEntityB.class, "e")
					.join("e", "enumAsStringMap", "m")
				.select().mapKey("m")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(ItemType_String.COSMETICS);
		assertResultContains(ItemType_String.OTHER);
		assertNoMoreResults();
	}

	/** @see EnumEntitySelection_PlannerTests#enumSelection_StringConversion_MapKey_WhereValueIsEntity */
	@Test
	public void enumSelection_StringConversion_MapKey_WhereValueIsEntity() {
		bB.enumEntityB("e1").enumAsStringMapKey("COSMETICS_B", null, "OTHER_B", null).create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartEnumEntityB.class, "e")
					.join("e", "enumAsStringMapKey", "m")
				.select().mapKey("m")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(ItemType_String.COSMETICS);
		assertResultContains(ItemType_String.OTHER);
		assertNoMoreResults();
	}

	/** @see EnumEntitySelection_PlannerTests#enumSelection_Delegate_MapValue */
	@Test
	public void enumSelection_Delegate_MapValue() {
		bB.enumEntityB("e1").enumAsDelegateMap(ItemTypeB.COSMETICS_B, ItemTypeB.MAGIC_B, ItemTypeB.OTHER_B, ItemTypeB.TOOL_B).create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartEnumEntityB.class, "e")
				.select("e", "enumAsDelegateMap")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(ItemType.MAGIC);
		assertResultContains(ItemType.TOOL);
		assertNoMoreResults();
	}

	/** @see EnumEntitySelection_PlannerTests#enumSelection_Delegate_MapKey */
	@Test
	public void enumSelection_Delegate_MapKey() {
		bB.enumEntityB("e1").enumAsDelegateMap(ItemTypeB.COSMETICS_B, ItemTypeB.MAGIC_B, ItemTypeB.OTHER_B, ItemTypeB.TOOL_B).create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartEnumEntityB.class, "e")
					.join("e", "enumAsDelegateMap", "m")
				.select().mapKey("m")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(ItemType.COSMETICS);
		assertResultContains(ItemType.OTHER);
		assertNoMoreResults();
	}

	/** @see EnumEntitySelection_PlannerTests#enumSelection_Delegate_MapKey_WhereValueIsEntity */
	@Test
	public void enumSelection_Delegate_MapKey_WhereValueIsEntity() {
		bB.enumEntityB("e1").enumAsDelegateMapKey(ItemTypeB.COSMETICS_B, null, ItemTypeB.OTHER_B, null).create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartEnumEntityB.class, "e")
					.join("e", "enumAsDelegateMapKey", "m")
				.select().mapKey("m")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(ItemType.COSMETICS);
		assertResultContains(ItemType.OTHER);
		assertNoMoreResults();
	}

}
