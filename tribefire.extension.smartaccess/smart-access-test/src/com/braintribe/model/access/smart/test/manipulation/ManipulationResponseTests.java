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
package com.braintribe.model.access.smart.test.manipulation;

import static com.braintribe.utils.lcd.CollectionTools2.asSet;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.builder.vd.VdBuilder;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.processing.query.smart.test.model.accessA.CompanyA;
import com.braintribe.model.processing.query.smart.test.model.accessA.PersonA;
import com.braintribe.model.processing.query.smart.test.model.accessB.PersonB;
import com.braintribe.model.processing.query.smart.test.model.accessB.StandardIdEntity;
import com.braintribe.model.processing.query.smart.test.model.smart.Company;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartPersonA;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartPersonB;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartStringIdEntity;
import com.braintribe.model.processing.query.smart.test.setup.base.SmartMappingSetup;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.utils.junit.assertions.BtAssertions;

/**
 * 
 */
public class ManipulationResponseTests extends AbstractManipulationResponseTests {

	private static final SimpleDateFormat sdf = new SimpleDateFormat(SmartMappingSetup.DATE_PATTERN);

	private SmartPersonA spA;
	private SmartPersonB spB;

	// ####################################
	// ## . . . . CHANGE VALUE . . . . . ##
	// ####################################

	@Test
	public void simpleChangeValue() throws Exception {
		initA();

		spA.setNameA("spA"); // just so we have something to commit
		recordResponseA(session -> {
			PersonA personA = personaA(spA.getId(), session);
			personA.setCompanyNameA("companyA");
		});

		commit();

		BtAssertions.assertThat(spA.getCompanyNameA()).isEqualTo("companyA");
	}

	@Test
	public void simpleChangeValue_ConvertedProperty() throws Exception {
		initB();

		final Date date = newDateRoundedToSeconds();

		spB.setNameB("spB"); // just so we have something to commit
		recordResponseB(session -> {
			PersonB personB = personB(spB.getId(), session);
			personB.setBirthDate(convert(date));
		});

		commit();

		BtAssertions.assertThat(spB.getConvertedBirthDate()).isEqualTo(date);
	}

	@Test
	public void changeValueForNonManipulatedEntity() throws Exception {
		initA();

		final SmartPersonA otherSp = newSmartPersonA();
		commit();

		spA.setNameA("spA"); // just so we have something to commit
		recordResponseA(session -> {
			PersonA personA = personaA(otherSp.getId(), session);
			personA.setCompanyNameA("companyA");
		});

		commit();

		BtAssertions.assertThat(otherSp.getCompanyNameA()).isEqualTo("companyA");
	}

	@Test
	public void changeValueForNonManipulatedEntity_ConvertedId() throws Exception {
		initB();

		final SmartStringIdEntity ssie = newStandardStringIdEntity();
		commit();

		spB.setNameB("spB"); // just so we have something to commit
		recordResponseB(session -> {
			StandardIdEntity sie = standardIdEntity(Long.parseLong(ssie.getId()), session);
			sie.setName("sie");
		});

		commit();

		BtAssertions.assertThat(ssie.getName()).isEqualTo("sie");
	}

	// ####################################
	// ## . . CHANGE ENTITy VALUE . . . .##
	// ####################################

	@Test
	public void changeValue_Entity() throws Exception {
		initA();

		Company company = newCompany();
		company.setNameA("CompanyA");
		commit();

		spA.setNameA("spA"); // just so we have something to commit
		recordResponseA(session -> {
			CompanyA companyA = companyA(company.getId(), session);

			PersonA personA = personaA(spA.getId(), session);
			personA.setCompanyA(companyA);
		});

		commit();

		BtAssertions.assertThat(spA.getCompanyA()).isSameAs(company);
	}
	// ####################################
	// ## . . . . . ADD/REMOVE . . . . . ##
	// ####################################

	@Test
	public void simpleAdd() throws Exception {
		initA();

		spA.setNickNamesSetA(asSet("n1", "n2"));
		commit();

		spA.setNameA("spA"); // just so we have something to commit
		recordResponseA(session -> {
			PersonA personA = personaA(spA.getId(), session);
			personA.getNickNamesSetA().add("n3");
		});

		commit();

		BtAssertions.assertThat(spA.getNickNamesSetA()).hasSize(3).containsOnly("n1", "n2", "n3");
	}

	@Test
	public void simpleRemove() throws Exception {
		initA();

		spA.setNickNamesSetA(asSet("n1", "n2"));
		commit();

		spA.setNameA("spA"); // just so we have something to commit
		recordResponseA(session -> {
			PersonA personA = personaA(spA.getId(), session);
			personA.getNickNamesSetA().remove("n2");
		});

		commit();

		BtAssertions.assertThat(spA.getNickNamesSetA()).hasSize(1).containsOnly("n1");
	}

	@Test
	public void clearCollection() throws Exception {
		initA();

		spA.setNickNamesSetA(asSet("n1", "n2"));
		commit();

		spA.setNameA("spA"); // just so we have something to commit
		recordResponseA(session -> {
			PersonA personA = personaA(spA.getId(), session);
			personA.getNickNamesSetA().clear();
		});

		commit();

		BtAssertions.assertThat(spA.getNickNamesSetA()).isEmpty();
	}

	// ####################################
	// ## . . . . . . DELETE . . . . . . ##
	// ####################################

	@Test
	public void deleteKnownEntity() throws Exception {
		initB();

		BtAssertions.assertThat(smartPersonBLocal(spB.getId(), spB.getPartition())).isNotNull();

		spB.setNameB("spB"); // just so we have something to commit
		recordResponseB(session -> {
			PersonB pb = personB(spB.getId(), session);
			session.deleteEntity(pb);
		});

		commit();

		BtAssertions.assertThat(smartPersonBLocal(spB.getId(), spB.getPartition())).isNull();
	}

	private SmartPersonB smartPersonBLocal(Long id, String partition) throws GmSessionException {
		PersistentEntityReference ref = VdBuilder.persistentReference(SmartPersonB.class.getName(), id, partition);

		return (SmartPersonB) session.queryCache().entity(ref).find();
	}

	@Test
	public void deleteUknownEntity() throws Exception {
		initB();

		PersonB pb = PersonB.T.create();
		pb.setId(spB.<Long> getId() + 1);

		smoodB.initialize(pb);

		spB.setNameB("spB"); // just so we have something to commit
		recordResponseB(session -> {
			PersonB pb1 = personB(spB.<Long> getId() + 1, session);
			session.deleteEntity(pb1);
		});

		commit();

		// We cannot really do any check here
	}

	// ###################################
	// ## . . . . . HELPERS . . . . . . ##
	// ###################################

	/** This is needed cause {@link SmartMappingSetup#DATE_PATTERN} is configured to seconds granularity. */
	private Date newDateRoundedToSeconds() {
		long t = System.currentTimeMillis();
		return new Date(t - (t % 1000));
	}

	private PersonA personaA(Long id, PersistenceGmSession session) {
		return queryEntity(PersonA.T, id, session);
	}

	private PersonB personB(Long id, PersistenceGmSession session) {
		return queryEntity(PersonB.T, id, session);
	}

	private CompanyA companyA(Long id, PersistenceGmSession session) {
		return queryEntity(CompanyA.T, id, session);
	}

	private StandardIdEntity standardIdEntity(Long id, PersistenceGmSession session) {
		return queryEntity(StandardIdEntity.T, id, session);
	}

	private <T extends GenericEntity> T queryEntity(EntityType<T> entityType, Long id, PersistenceGmSession session) {
		try {
			return session.query().entity(entityType, id).find();
		} catch (GmSessionException e) {
			throw new RuntimeException("Query failed!", e);
		}
	}

	private void initA() throws Exception {
		spA = newSmartPersonA();
		commit();

		BtAssertions.assertThat(spA.<Object> getId()).isNotNull();
		BtAssertions.assertThat(countPersonA()).isEqualTo(1);
	}

	private void initB() throws Exception {
		spB = newSmartPersonB();
		commit();

		BtAssertions.assertThat(spB.<Object> getId()).isNotNull();
		BtAssertions.assertThat(countPersonB()).isEqualTo(1);
	}

	private static String convert(Date date) {
		return sdf.format(date);
	}

}
