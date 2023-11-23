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
package com.braintribe.model.access.hibernate.tests;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Date;

import org.junit.Test;

import com.braintribe.model.access.hibernate.base.HibernateAccessRecyclingTestBase;
import com.braintribe.model.access.hibernate.base.HibernateBaseModelTestBase;
import com.braintribe.model.access.hibernate.base.model.simple.BasicColor;
import com.braintribe.model.access.hibernate.base.model.simple.BasicScalarEntity;
import com.braintribe.model.access.hibernate.base.model.simple.StringIdEntity;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.query.SelectQuery;

/**
 * Basic tests for entity with scalar properties only.
 * 
 * @see HibernateAccessRecyclingTestBase
 * 
 * @author peter.gazdik
 */
public class Scalars_HbmTest extends HibernateBaseModelTestBase {

	static final String _string = "string";
	static final int _integer = 123;
	static final long _long = 9_876_543_210L;
	static final float _float = 0.123F;
	static final double _double = 1.012_345_678_9D;
	static final Date date = new Date(10_000_000L);

	@Test
	public void storesAndLoadsScalarEntity() throws Exception {
		prepareSclarEntity();

		BasicScalarEntity bse = accessDriver.requireEntityByProperty(BasicScalarEntity.T, BasicScalarEntity.name, "BSE 1");
		assertThat(bse.getPartition()).isNotEmpty().isEqualTo(access.getAccessId());
		assertThat(bse.getStringValue()).isEqualTo(_string);
		assertThat(bse.getIntegerValue()).isEqualTo(_integer);
		assertThat(bse.getLongValue()).isEqualTo(_long);
		assertThat(bse.getFloatValue()).isEqualTo(_float);
		assertThat(bse.getDoubleValue()).isEqualTo(_double);
		assertThat(bse.getBooleanValue()).isEqualTo(Boolean.TRUE);
		assertThat(bse.getDateValue()).isEqualTo(date);
		assertThat(bse.getDecimalValue()).isEqualTo(BigDecimal.TEN);
		assertThat(bse.getColor()).isEqualTo(BasicColor.green);
	}

	@Test
	public void findsByScalarProperty() throws Exception {
		prepareSclarEntity();

		findsBseByProperty("stringValue", _string);
		findsBseByProperty("integerValue", _integer);
		findsBseByProperty("longValue", _long);
		findsBseByProperty("floatValue", _float);
		findsBseByProperty("doubleValue", _double);
		findsBseByProperty("booleanValue", Boolean.TRUE);
		findsBseByProperty("dateValue", date);
		findsBseByProperty("decimalValue", BigDecimal.TEN);
		findsBseByProperty("color", BasicColor.green);
	}

	private void findsBseByProperty(String propertyName, Object propertyValue) {
		BasicScalarEntity bse = session.query().select(queryBseByProperty(propertyName, propertyValue)).first();
		assertThat(bse).isNotNull();
	}

	private SelectQuery queryBseByProperty(String propertyName, Object propertyValue) {
		return new SelectQueryBuilder() //
				.from(BasicScalarEntity.T, "e") //
				.where().property("e", propertyName).eq(propertyValue) //
				.done();
	}

	private void prepareSclarEntity() {
		BasicScalarEntity bse = session.create(BasicScalarEntity.T);
		bse.setName("BSE 1");
		bse.setStringValue(_string);
		bse.setIntegerValue(_integer);
		bse.setLongValue(_long);
		bse.setFloatValue(_float);
		bse.setDoubleValue(_double);
		bse.setBooleanValue(true);
		bse.setDateValue(date);
		bse.setDecimalValue(BigDecimal.TEN);
		bse.setColor(BasicColor.green);

		session.commit();

		resetGmSession();
	}

	@Test
	public void withExplicitId() throws Exception {
		StringIdEntity sie = session.create(StringIdEntity.T);
		sie.setId("ONE");
		sie.setName("SIE-1");

		session.commit();

		resetGmSession();

		sie = accessDriver.requireEntityByProperty(StringIdEntity.T, BasicScalarEntity.name, "SIE-1");
		assertThat(sie.<String> getId()).isEqualTo("ONE");
		assertThat(sie.getPartition()).isNotEmpty().isEqualTo(access.getAccessId());

	}

	/**
	 * This makes sense despite partition not being mapped. We make sure the handling of partition change is handled correctly for the references used
	 * internally.
	 */
	@Test
	public void withExplicitIdAndPartition() throws Exception {
		assertThat(access.getAccessId()).isNotEmpty();

		StringIdEntity sie = session.create(StringIdEntity.T);
		sie.setId("ONE");
		sie.setPartition(access.getAccessId());
		sie.setName("SIE-1");

		session.commit();

		resetGmSession();

		sie = accessDriver.requireEntityByProperty(StringIdEntity.T, BasicScalarEntity.name, "SIE-1");
		assertThat(sie.<String> getId()).isEqualTo("ONE");
		assertThat(sie.getPartition()).isEqualTo(access.getAccessId());

	}
}
