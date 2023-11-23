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
package com.braintribe.model.processing.core.commons.selectiveinfo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.access.ModelAccessException;
import com.braintribe.model.access.smood.basic.SmoodAccess;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.processing.core.commons.SelectiveInformationSupport;
import com.braintribe.model.processing.core.commons.selectiveinfo.model.Address;
import com.braintribe.model.processing.core.commons.selectiveinfo.model.GmCoreCommonsTestModelProvider;
import com.braintribe.model.processing.core.commons.selectiveinfo.model.House;
import com.braintribe.model.processing.core.commons.selectiveinfo.model.HouseOwner;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.SelectQueryResult;
import com.braintribe.testing.tools.gm.GmTestTools;

/**
 * @author peter.gazdik
 */
public class SelectiveInformationTcTest {

	private final SmoodAccess smoodAccess = GmTestTools.newSmoodAccessMemoryOnly("test", GmCoreCommonsTestModelProvider.model);

	@Before
	public void initData() throws Exception {
		Address ownerAddress = Address.T.create();
		ownerAddress.setId(1L);
		ownerAddress.setStreet("Owner Street");

		HouseOwner houseOwner = HouseOwner.T.create();
		houseOwner.setId(2L);
		houseOwner.setAddress(ownerAddress);

		Address houseAddress = Address.T.create();
		houseAddress.setId(3L);
		houseAddress.setStreet("House Street");

		House house = House.T.create();
		house.setOwner(houseOwner);
		house.setAddress(houseAddress);

		smoodAccess.getDatabase().initialize(Arrays.asList(ownerAddress, houseOwner, houseAddress, house));
	}

	@Test
	public void notUsedPropertiesAbsent() throws Exception {
		House house = queryHouseWhenSelectiveInfoIs("Static");

		assertPropertyAbsent(house, "address");
		assertPropertyAbsent(house, "owner");
	}

	@Test
	public void usedPropertyLoaded() throws Exception {
		House house = queryHouseWhenSelectiveInfoIs("house in ${address}");

		assertPropertyPresent(house, "id");
		assertPropertyPresent(house, GenericEntity.partition);
		assertPropertyPresent(house, "address");
		assertPropertyAbsent(house, "owner");
	}

	@Test
	public void usedPropertyWithPathLoaded() throws Exception {
		House house = queryHouseWhenSelectiveInfoIs("house in ${address.street}");

		assertPropertyPresent(house, "id");
		assertPropertyPresent(house, GenericEntity.partition);
		assertPropertyPresent(house, "address");
		assertPropertyAbsent(house, "owner");
	}

	@Test
	public void usedPropertyWithTwoLevelPathLoaded() throws Exception {
		House house = queryHouseWhenSelectiveInfoIs("house in ${owner.address}");

		assertPropertyPresent(house, "id");
		assertPropertyPresent(house, GenericEntity.partition);
		assertPropertyAbsent(house, "address");
		assertPropertyPresent(house, "owner");

		HouseOwner owner = house.getOwner();
		assertPropertyPresent(owner, "id");
		assertPropertyPresent(owner, "address");
	}

	private House queryHouseWhenSelectiveInfoIs(String selectiveInformation) {
		TraversingCriterion tc = createTcForSelectiveInfoString(selectiveInformation);

		SelectQuery query = new SelectQueryBuilder().from(House.class, "h").tc(tc).done();

		List<?> entities = query(query).getResults();
		return (House) entities.get(0);
	}

	private static TraversingCriterion createTcForSelectiveInfoString(String selectiveInformation) {
		List<String[]> propertyChains = SelectiveInformationSupport.extractPropertyChains(selectiveInformation);
		return SelectiveInformationSupport.buildTcFor(propertyChains);
	}

	private SelectQueryResult query(SelectQuery query) {
		try {
			return smoodAccess.query(query);

		} catch (ModelAccessException e) {
			throw new RuntimeException("Query evaluation failed.", e);
		}
	}

	private void assertPropertyAbsent(GenericEntity entity, String propertyName) {
		Property property = entity.entityType().getProperty(propertyName);
		assertThat(property.getAbsenceInformation(entity)).isNotNull();
	}

	private void assertPropertyPresent(GenericEntity entity, String propertyName) {
		Property property = entity.entityType().getProperty(propertyName);
		assertThat(property.getAbsenceInformation(entity)).isNull();
	}

}
