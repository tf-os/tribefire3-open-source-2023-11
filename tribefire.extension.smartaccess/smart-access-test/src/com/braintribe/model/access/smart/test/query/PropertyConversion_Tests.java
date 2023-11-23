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

import static com.braintribe.utils.lcd.CollectionTools2.asSet;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;

import com.braintribe.model.access.smart.query.fluent.SmartSelectQueryBuilder;
import com.braintribe.model.processing.query.smart.test.model.accessA.PersonA;
import com.braintribe.model.processing.query.smart.test.model.accessB.PersonB;
import com.braintribe.model.processing.query.smart.test.model.accessB.StandardIdEntity;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartPersonB;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartStringIdEntity;
import com.braintribe.model.processing.query.smart.test.setup.base.SmartMappingSetup;
import com.braintribe.model.processing.smart.query.planner.PropertyConversion_PlannerTests;
import com.braintribe.model.query.JoinType;
import com.braintribe.model.query.SelectQuery;

/**
 * @see PropertyConversion_PlannerTests
 */
public class PropertyConversion_Tests extends AbstractSmartQueryTests {

	public static final long YEAR_IN_MILLIS = 365L * 24 * 60 * 60 * 1000;

	private static final SimpleDateFormat sdf = new SimpleDateFormat(SmartMappingSetup.DATE_PATTERN);

	// #########################################
	// ## . . . . . Simple Property . . . . . ##
	// #########################################

	/** @see PropertyConversion_PlannerTests#selectConvertedProperty() */
	@Test
	public void selectConvertedProperty() {
		Date d1 = new Date(10 * YEAR_IN_MILLIS);
		Date d2 = new Date(20 * YEAR_IN_MILLIS);
		Date d3 = new Date(30 * YEAR_IN_MILLIS);

		bB.personB("person1").birthDate(convert(d1)).create();
		bB.personB("person2").birthDate(convert(d2)).create();
		bB.personB("person3").birthDate(convert(d3)).create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonB.class, "p")
				.select("p", "convertedBirthDate")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(d1);
		assertResultContains(d2);
		assertResultContains(d3);
		assertNoMoreResults();
	}

	/** Selecting a simple property directly from the delegate retrieves the property directly, without any conversion. */
	@Test
	public void selectPropertyFromDelegateWithoutConversion() {
		String stringDate1 = "1.1.1970";
		String stringDate2 = "1.1.1980";
		String stringDate3 = "1.1.1990";

		bB.personB("person1").birthDate(stringDate1).create();
		bB.personB("person2").birthDate(stringDate2).create();
		bB.personB("person3").birthDate(stringDate3).create();

		// @formatter:off
		SelectQuery selectQuery = new SmartSelectQueryBuilder()
				.selectDelegateProperty("p", "birthDate")
				.from(SmartPersonB.class, "p")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(stringDate1);
		assertResultContains(stringDate2);
		assertResultContains(stringDate3);
		assertNoMoreResults();
	}

	/** @see PropertyConversion_PlannerTests#filterByConvertedProperty_Equals() */
	@Test
	public void filterByConvertedProperty_Equals() {
		Date d1 = new Date(10 * YEAR_IN_MILLIS);
		Date d2 = new Date(20 * YEAR_IN_MILLIS);
		Date d3 = new Date(30 * YEAR_IN_MILLIS);

		PersonB p;
		p = bB.personB("person1").birthDate(convert(d1)).create();
		p = bB.personB("person2").birthDate(convert(d2)).create();
		p = bB.personB("person3").birthDate(convert(d3)).create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonB.class, "p")
				.select("p")
				.where()
					.property("p", "convertedBirthDate").eq(d3)
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(smartPerson(p));
		assertNoMoreResults();
	}

	/** @see PropertyConversion_PlannerTests#filterByConvertedProperty_Inequality_NonDelegatable() */
	@SuppressWarnings("unused")
	@Test
	public void filterByConvertedProperty_Inequality_NonDelegatable() {
		Date d1 = new Date(10 * YEAR_IN_MILLIS);
		Date d2 = new Date(20 * YEAR_IN_MILLIS);
		Date d3 = new Date(30 * YEAR_IN_MILLIS);

		/* If we looked at the strings, we would see that d1 > d2 > d3, though the dates are sorted in reverse order (d1
		 * is the smallest). So if this query provides the right result, we have probably handled the
		 * conversion/condition correctly. */

		PersonB p1 = bB.personB("person1").birthDate(convert(d1)).create();
		PersonB p2 = bB.personB("person2").birthDate(convert(d2)).create();
		PersonB p3 = bB.personB("person3").birthDate(convert(d3)).create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonB.class, "p")
				.select("p")
				.where()
					.property("p", "convertedBirthDate").gt(d1)
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(smartPerson(p2));
		assertResultContains(smartPerson(p3));
		assertNoMoreResults();
	}

	/** @see PropertyConversion_PlannerTests#filterByConvertedProperty_In() */
	@Test
	public void filterByConvertedProperty_In() {
		Date d1 = new Date(10 * YEAR_IN_MILLIS);
		Date d2 = new Date(20 * YEAR_IN_MILLIS);
		Date d3 = new Date(30 * YEAR_IN_MILLIS);

		bB.personB("p1").birthDate(convert(d1)).create();
		bB.personB("p2").birthDate(convert(d2)).create();
		bB.personB("p3").birthDate(convert(d3)).create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonB.class, "p")
				.select("p", "nameB")
				.where()
					.property("p", "convertedBirthDate").in(asSet(d1, d2))
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains("p1");
		assertResultContains("p2");
		assertNoMoreResults();
	}

	/** @see PropertyConversion_PlannerTests#delegateWhenConvertedToSmartString_Like() */
	@SuppressWarnings("unused")
	@Test
	public void delegateWhenConvertedToSmartString_Like() {
		StandardIdEntity e1 = bB.standardIdEntity("e1").id(15).create();
		StandardIdEntity e2 = bB.standardIdEntity("e2").id(28).create();
		StandardIdEntity e3 = bB.standardIdEntity("e3").id(38).create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartStringIdEntity.class, "c")
				.select("c")
				.where()
					.property("c", "id").like("*8")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(smartStringIdEntity(e2));
		assertResultContains(smartStringIdEntity(e3));
		assertNoMoreResults();

	}

	// #########################################
	// ## . . . . . . KPA Property . . . . . .##
	// #########################################

	/** @see PropertyConversion_PlannerTests#selectConvertedProperty_Kpa() */
	@Test
	public void selectConvertedProperty_Kpa() {
		PersonA pA1 = bA.personA("pA1").create();
		PersonA pA2 = bA.personA("pA2").create();
		PersonA pA3 = bA.personA("pA3").create();

		bB.personB("pB1").parentA("" + pA1.getId()).create();
		bB.personB("pB2").parentA("" + pA2.getId()).create();
		bB.personB("pB3").parentA("" + pA3.getId()).create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonB.class, "p")
					.join("p", "convertedSmartParentA", "cp", JoinType.inner)
				.select("p", "nameB")
				.select("cp")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains("pB1", smartPerson(pA1));
		assertResultContains("pB2", smartPerson(pA2));
		assertResultContains("pB3", smartPerson(pA3));
		assertNoMoreResults();
	}

	/** @see PropertyConversion_PlannerTests#selectConvertedProperty_Kpa_Left() */
	@Test
	public void selectConvertedProperty_Kpa_Left() {
		PersonA pA1 = bA.personA("pA1").create();
		PersonA pA2 = bA.personA("pA2").create();
		PersonA pA3 = bA.personA("pA3").create();

		bB.personB("pB1").parentA("" + pA1.getId()).create();
		bB.personB("pB2").parentA("" + pA2.getId()).create();
		bB.personB("pB3").parentA("" + pA3.getId()).create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonB.class, "p")
				.select("p", "nameB")
				.select("p", "convertedSmartParentA")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains("pB1", smartPerson(pA1));
		assertResultContains("pB2", smartPerson(pA2));
		assertResultContains("pB3", smartPerson(pA3));
		assertNoMoreResults();
	}

	// #########################################
	// ## . . . . Collection Property . . . . ##
	// #########################################

	/** @see PropertyConversion_PlannerTests#selectConvertedCollectionProperty() */
	@Test
	public void selectConvertedCollectionProperty() {
		Date d1 = new Date(10L * YEAR_IN_MILLIS);
		Date d2 = new Date(20 * YEAR_IN_MILLIS);
		Date d3 = new Date(30 * YEAR_IN_MILLIS);

		bB.personB("person1").dates(convert(d1), convert(d2)).create();
		bB.personB("person2").dates(convert(d3)).create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonB.class, "p")
				.select("p", "convertedDates")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(d1);
		assertResultContains(d2);
		assertResultContains(d3);
		assertNoMoreResults();
	}

	/** @see PropertyConversion_PlannerTests#filterByConvertedCollectionProperty() */
	@Test
	public void filterByConvertedCollectionProperty() {
		Date d1 = new Date(10L * YEAR_IN_MILLIS);
		Date d2 = new Date(20 * YEAR_IN_MILLIS);
		Date d3 = new Date(30 * YEAR_IN_MILLIS);

		PersonB p;
		p = bB.personB("person2").dates(convert(d3)).create();
		p = bB.personB("person1").dates(convert(d1), convert(d2)).create();

		SmartPersonB sp = smartPerson(p);

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonB.class, "p")
				.select("p")
				.where()
					.value(d2).in().property("p", "convertedDates")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(sp);
		assertNoMoreResults();
	}

	/** @see PropertyConversion_PlannerTests#filterByConvertedCollectionProperty_Inequality_NonDelegatable() */
	@SuppressWarnings("unused")
	@Test
	public void filterByConvertedCollectionProperty_Inequality_NonDelegatable() {
		Date d0 = new Date(0L * YEAR_IN_MILLIS);
		Date d1 = new Date(10L * YEAR_IN_MILLIS);
		Date d2 = new Date(20 * YEAR_IN_MILLIS);
		Date d3 = new Date(30 * YEAR_IN_MILLIS);

		PersonB p0 = bB.personB("person0").dates(convert(d0)).create();
		PersonB p1 = bB.personB("person1").dates(convert(d1), convert(d2)).create();
		PersonB p2 = bB.personB("person2").dates(convert(d3)).create();

		SmartPersonB sp1 = smartPerson(p1);
		SmartPersonB sp2 = smartPerson(p2);

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonB.class, "p")
					.join("p", "convertedDates", "d")
				.select("p")
				.where()
					.entity("d").gt(d1)
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(sp1);
		assertResultContains(sp2);
		assertNoMoreResults();
	}

	// #########################################
	// ## . . . . IdProperty Property . . . . ##
	// #########################################

	/** @see PropertyConversion_PlannerTests#entityPropertyComparison() */
	@SuppressWarnings("unused")
	@Test
	public void entityPropertyComparison() {
		StandardIdEntity e1 = bB.standardIdEntity("e1").parent(null).create();
		StandardIdEntity e2 = bB.standardIdEntity("e2").parent(e1).create();
		StandardIdEntity e3 = bB.standardIdEntity("e3").parent(e2).create();

		SmartStringIdEntity ce1 = smartStringIdEntity(e1);
		SmartStringIdEntity ce2 = smartStringIdEntity(e2);

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartStringIdEntity.class, "c")
				.select("c")
				.where()
					.property("c", "parent").eq().entity(ce1)
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(ce2);
		assertNoMoreResults();
	}

	/** @see PropertyConversion_PlannerTests#entityCollection_InCondition() */
	@Test
	public void entityCollection_InCondition() {
		StandardIdEntity c11 = bB.standardIdEntity("c11").create();
		StandardIdEntity c12 = bB.standardIdEntity("c12").create();
		StandardIdEntity c21 = bB.standardIdEntity("c21").create();
		StandardIdEntity c22 = bB.standardIdEntity("c22").create();

		StandardIdEntity e;
		e = bB.standardIdEntity("e1").children(c21, c22).create();
		e = bB.standardIdEntity("e1").children(c11, c12).create();

		SmartStringIdEntity se = smartStringIdEntity(e);

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartStringIdEntity.class, "c")
				.select("c")
				.where()
					.property("c", "children").contains().entity(smartStringIdEntity(c11))
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(se);
		assertNoMoreResults();
	}

	// ###################################
	// ## . . . . . HELPERS . . . . . . ##
	// ###################################

	public static String convert(Date date) {
		return sdf.format(date);
	}

}
