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
package com.braintribe.utils.genericmodel;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.impl.managed.history.BasicTransaction;
import com.braintribe.testing.category.KnownIssue;
import com.braintribe.testing.model.test.technical.features.CollectionEntity;
import com.braintribe.testing.model.test.technical.features.SimpleEntity;
import com.braintribe.testing.tools.gm.GmTestTools;
import com.braintribe.utils.CollectionTools;
import com.braintribe.utils.ReflectionTools;

/**
 * Provides Tests for {@link GmTools}.
 * 
 * @author michael.lafite
 */
public class GmToolsTest {

	private static String LENIENTDECODINGTEST_FILEPATH = "res/lenientDecodingTest.xml";

	@Test
	public void testDecodeLeniently() throws Exception {
		PersistenceGmSession session = GmTools.decodeLeniently(new File(LENIENTDECODINGTEST_FILEPATH));
		GmMetaModel model = session.query()
				.entities(EntityQueryBuilder.from(GmMetaModel.class).tc().negation().joker().done()).first();
		assertThat(model.getName()).isEqualTo("com.braintribe.model.test:ExampleModel#1.0");
	}

	@Test
	@Category(KnownIssue.class)
	public void testEnsureModelTypes() throws Exception {
		final String lenientDecodingTestEntityTypeTypeSignature = "com.braintribe.model.test.LenientDecodingTestEntityType";
		assertThat(ReflectionTools.classExists(lenientDecodingTestEntityTypeTypeSignature)).isFalse();
		GmTools.ensureModelTypes(new File(LENIENTDECODINGTEST_FILEPATH), "THERE_IS_NO_MODEL_WITH_THIS_NAME");
		assertThat(ReflectionTools.classExists(lenientDecodingTestEntityTypeTypeSignature)).isFalse();
		GmTools.ensureModelTypes(new File(LENIENTDECODINGTEST_FILEPATH), "com.braintribe.model.test:ExampleModel#*");
		assertThat(ReflectionTools.classExists(lenientDecodingTestEntityTypeTypeSignature)).isTrue();

		PersistenceGmSession session = GmTools.decodeLeniently(new File(LENIENTDECODINGTEST_FILEPATH));
		GenericEntity entity = session.query()
				.entities(EntityQueryBuilder.from(lenientDecodingTestEntityTypeTypeSignature).done()).first();
		assertThat(entity).isNotNull();
	}

	@Test
	@Category(KnownIssue.class)
	public void testPrintDoneManipulations() throws Exception {
		PersistenceGmSession session = GmTestTools.newSessionWithSmoodAccessAndTemporaryFile();
		CollectionEntity collectionEntity = session.create(CollectionEntity.T);

		SimpleEntity simpleEntity = session.create(SimpleEntity.T);
		collectionEntity.setSimpleEntitySet(new HashSet<SimpleEntity>());

		session.commit();

		collectionEntity.getSimpleEntitySet().add(simpleEntity);

		List<?> listeners = (List<?>) ReflectionTools.getFieldValue("manipulationListeners", session);
		BasicTransaction transaction = CollectionTools.getSingleElement(listeners, BasicTransaction.class);
		session.commit();
		
		assertThat(transaction).isNotNull();
	}

}
