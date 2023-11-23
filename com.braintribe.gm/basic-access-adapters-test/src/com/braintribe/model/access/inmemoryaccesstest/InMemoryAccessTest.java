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
package com.braintribe.model.access.inmemoryaccesstest;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;
import static com.braintribe.utils.lcd.CollectionTools2.asList;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.impl.persistence.BasicPersistenceGmSession;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.util.meta.NewMetaModelGeneration;

/**
 * The classes in this package are used to reproduce a problem described in BTT-6220.
 * 
 * @author michael.lafite
 */
public class InMemoryAccessTest {

	@Test
	public void testTransitionDecodeToDispatch_slightlyModified() throws Exception {

		// used to not work (see BTT-6220)
		PersistenceGmSession session = new BasicPersistenceGmSession(newInMemoryAccess());
		// always worked
		// PersistenceGmSession session = GmTestTools.newSessionWithSmoodAccessAndTemporaryFile();
		// PersistenceGmSession session = com.braintribe.utils.genericmodel.GmTools.newSessionWithSmoodAccess(new
		// File("temporary_database.xml"));

		InOutFormatGroup_CF inOutFormatGroup = session.create(InOutFormatGroup_CF.T);

		Agreement_CF agreement = session.create(Agreement_CF.T);
		agreement.setExampleProperty("examplePropertyValue");
		agreement.setInOutFormatGroupList(new ArrayList<InOutFormatGroup_CF>());
		agreement.getInOutFormatGroupList().add(inOutFormatGroup);
		session.commit();

		// System.out.println("1:" + GMCoreTools.getDescription((List<? extends GenericEntity>) session.query()
		// .entities(EntityQueryBuilder.from(GenericEntity.class).done()).list()));
		// System.out.println("2:" + GMCoreTools.getDescription((List<? extends GenericEntity>) session.query()
		// .entities(EntityQueryBuilder.from(InOutFormatGroup_CF.class).done()).list()));
		// System.out.println("3:" + GMCoreTools.getDescription((List<? extends GenericEntity>) session.query()
		// .entities(EntityQueryBuilder.from(Agreement_CF.class).done()).list()));
		// System.out
		// .println(
		// "4:" + GMCoreTools
		// .getDescription(
		// (List<? extends GenericEntity>) session.query()
		// .entities(EntityQueryBuilder.from(Agreement_CF.class).where()
		// .property(Agreement_CF.id).eq(agreement.getId()).done())
		// .list()));
		// System.out
		// .println("5:" + GMCoreTools.getDescription((List<? extends GenericEntity>) session.query()
		// .entities(EntityQueryBuilder.from(Agreement_CF.class).where()
		// .property(Agreement_CF.exampleProperty).eq(agreement.getExampleProperty()).done())
		// .list()));
		// System.out.println("6:" + GMCoreTools.getDescription((List<? extends GenericEntity>) session.query()
		// .entities(EntityQueryBuilder.from(Agreement_CF.class).where()
		// .property(Agreement_CF.inOutFormatGroupList).contains().entity(inOutFormatGroup).done())
		// .list()));

		List<Agreement_CF> agreements = session.query().entities(EntityQueryBuilder.from(Agreement_CF.class).where()
				.property(Agreement_CF.inOutFormatGroupList).contains().entity(inOutFormatGroup).done()).list();
		Assert.assertEquals(1, agreements.size());
	}

	@Test
	public void testAssignedIds() throws Exception {

		PersistenceGmSession session = new BasicPersistenceGmSession(newInMemoryAccess());

		Agreement_CF agreement1 = session.create(Agreement_CF.T);
		agreement1.setExampleProperty("examplePropertyValue");
		agreement1.setId(123l);

		session.create(Agreement_CF.T);
		agreement1.setId(456l);

		session.commit();

		assertThat(session.query().entity(Agreement_CF.T, 123l).find()).isNull();
		assertThat(agreement1.<Object> getId()).isEqualTo(456l);
		Agreement_CF agreement1Retrieved = session.query().entity(Agreement_CF.T, agreement1.getId()).refresh();
		Assert.assertEquals(agreement1.getExampleProperty(), agreement1Retrieved.getExampleProperty());
	}

	/**
	 * This test is similar to {@link #testAssignedIds()} but tells the {@link InMemoryAccess} to enforce the id
	 * generation for any instantiated entity even though the entity might get an assigned id from the client. The
	 * expected result is an updated id in the created entity.
	 */
	@Test
	public void testAssignedIdsWithOverride() throws Exception {

		PersistenceGmSession session = new BasicPersistenceGmSession(newInMemoryAccess(true));

		Agreement_CF agreement1 = session.create(Agreement_CF.T);
		agreement1.setExampleProperty("examplePropertyValue");

		// this line basically has no further meaning but accidentally showed a bug in InMemoryAccess's id generation.
		// Before the current milliseconds where used to generate long-id's. Running this test mulitple times in a row
		// had a good chance to fail. After changing InMemoryAccess Id generation this is also working properly.
		session.create(Agreement_CF.T);

		agreement1.setId(123l);
		agreement1.setId(456l);

		session.commit();

		assertThat(agreement1.<Object> getId()).isNotEqualTo(456l);

		// assertThat(session.query().entity(Agreement_CF.class, 123l).find()).isNull();
		assertThat(session.query().entity(Agreement_CF.T, 456l).find()).isNull();

		EntityQuery query = EntityQueryBuilder.from(Agreement_CF.class).where().property(Agreement_CF.id).eq(agreement1.getId()).done();

		List<Agreement_CF> result = session.query().entities(query).list();
		assertThat(result).isNotNull();
		// for (Agreement_CF r : result) {
		// System.out.println(r);
		// }

		Agreement_CF agreement1Retrieved = session.query().entities(query).unique();

		// Agreement_CF agreement1Retrieved = session.query().entity(Agreement_CF.class, agreement1.getId()).refresh();
		Assert.assertEquals(agreement1.getExampleProperty(), agreement1Retrieved.getExampleProperty());
	}

	private static GmMetaModel metaModel = new NewMetaModelGeneration().buildMetaModel("test:InMemoryAccessModel",
			asList(Agreement_CF.T, InOutFormatGroup_CF.T));

	private InMemoryAccess newInMemoryAccess() {
		InMemoryAccess result = new InMemoryAccess();
		result.setMetaModelProvider(() -> metaModel);

		return result;
	}

	private InMemoryAccess newInMemoryAccess(boolean overrideAssignedIds) {
		InMemoryAccess result = newInMemoryAccess();
		result.setOverrideAssignedIds(overrideAssignedIds);

		return result;
	}

	@Test
	public void combinedAssignedIdTest() throws Exception {
		testAssignedIds();
		testAssignedIdsWithOverride();
	}

}
