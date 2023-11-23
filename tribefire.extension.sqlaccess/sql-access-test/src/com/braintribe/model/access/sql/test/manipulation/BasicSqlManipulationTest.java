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
package com.braintribe.model.access.sql.test.manipulation;

import java.math.BigDecimal;
import java.util.Date;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.model.access.sql.test.model.basic.BasicColor;
import com.braintribe.model.access.sql.test.model.basic.BasicEntity;
import com.braintribe.model.access.sql.test.model.basic.BasicScalarEntity;
import com.braintribe.testing.category.KnownIssue;

/**
 * @author peter.gazdik
 */
@Category(KnownIssue.class)
public class BasicSqlManipulationTest extends AbstractSqlAccessManipulationTest {

	@Test
	public void testCreateEntity() throws Exception {
		manipulationElf.apply(session -> {
			session.create(BasicScalarEntity.T);
		});
		
		// TODO make tableName retrievable via higher-level API
		assertNumberOfRows("BasicScalarEntity", 1);
	}
	

	@Test
	public void testCreateTwoEntities() throws Exception {
		manipulationElf.apply(session -> {
			session.create(BasicScalarEntity.T);
			session.create(BasicScalarEntity.T);
		});

		assertNumberOfRows("BasicScalarEntity", 2);
	}
	
	@Test
	public void testCreateEntityAndSetSimpleProperties() throws Exception {
		manipulationElf.apply(session -> {
			BasicScalarEntity e = session.create(BasicScalarEntity.T);
			e.setStringValue("String");
			e.setBooleanValue(true);
			e.setIntegerValue(111);
			e.setLongValue(222L);
			e.setFloatValue(333F);
			e.setDoubleValue(444D);
			e.setDateValue(new Date());
			e.setDecimalValue(new BigDecimal("11223344556677889900"));
			e.setColor(BasicColor.green);
		});
		
		// TODO make tableName retrievable via higher-level API
		assertNumberOfRows("BasicScalarEntity", 1);
		// TODO extend asserts
	}
	
	@Test
	public void testMix() throws Exception {
		manipulationElf.apply(session -> {
			BasicScalarEntity e = session.create(BasicScalarEntity.T);
			e.setStringValue("String");
			e.setBooleanValue(true);
			e.setIntegerValue(111);
			e.setLongValue(222L);
			e.setFloatValue(333F);
			e.setDoubleValue(444D);
			e.setDateValue(new Date());
			e.setDecimalValue(new BigDecimal("11223344556677889900.13456789"));
			e.setColor(BasicColor.green);
			
			BasicEntity yin = session.create(BasicEntity.T);
			yin.setId("yin");
			yin.setName("Yin");

			BasicEntity yang = session.create(BasicEntity.T);
			yang.setId("yang");
			yang.setName("Yang");
			
			yin.setBasicEntity(yang);
			yang.setBasicEntity(yin);
		});
	}
	
	
}
