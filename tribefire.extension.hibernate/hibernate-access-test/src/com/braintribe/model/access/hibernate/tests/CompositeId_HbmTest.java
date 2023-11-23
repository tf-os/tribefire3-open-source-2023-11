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
import static com.braintribe.utils.lcd.CollectionTools2.asSet;

import org.junit.Test;

import com.braintribe.model.access.TmpQueryResultDepth;
import com.braintribe.model.access.hibernate.base.HibernateAccessRecyclingTestBase;
import com.braintribe.model.access.hibernate.base.model.simple.BasicEntity;
import com.braintribe.model.access.hibernate.base.wire.space.HibernateModelsSpace;
import com.braintribe.model.access.hibernate.gm.CompositeIdValues;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.processing.query.tools.PreparedTcs;
import com.braintribe.model.processing.traversing.engine.impl.clone.legacy.CloningContextBasedBasicModelWalkerCustomization;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.utils.collection.impl.AttributeContexts;

/**
 * This just shows how these tests work.
 * 
 * @see HibernateAccessRecyclingTestBase
 * 
 * @author peter.gazdik
 */
public class CompositeId_HbmTest extends HibernateAccessRecyclingTestBase {

	static final Property integerValueP = BasicEntity.T.getProperty(BasicEntity.integerValue);
	static final Property stringValueP = BasicEntity.T.getProperty(BasicEntity.stringValue);
	static final Property nameP = BasicEntity.T.getProperty(BasicEntity.name);

	@Override
	protected GmMetaModel model() {
		return HibernateAccessRecyclingTestBase.hibernateModels.compositeId();
	}

	@Test
	public void createEntity() throws Exception {
		BasicEntity entity = createWithCompositeIdExplicitly();
		assertThat(entity.<String> getId()).isNotEmpty();
	}

	@Test
	public void deleteEntity() throws Exception {
		BasicEntity entity = createWithCompositeIdExplicitly();

		session.deleteEntity(entity);
		session.commit();

		resetGmSession();

		BasicEntity entity2 = session.query().select(queryById(entity)).unique();
		assertThat(entity2).isNull();
	}

	/**
	 * Tests that lazy loading (which is done via {@link PropertyQuery}) is handled properly.
	 * <p>
	 * This also shows a limitation of our testing framework. Our id is mapped to <code>integerValue</code> and <code>stringValue</code> columns (see
	 * {@link HibernateModelsSpace#compositeId()}), thus assigning an id sets those values and subsequent queries for those two properties deliver the
	 * value. Our mock implementation which never does a real commit will not do that and the two properties remain <code>null</code>.
	 */
	@Test
	public void propertyQuery_LazyLoading() throws Exception {
		BasicEntity entity = createWithCompositeIdExplicitly();

		nameP.setAbsenceInformation(entity, GMF.absenceInformation());
		assertThat(entity.getName()).isEqualTo("CompositeId Entity");

		/* This part would be different in a real situation we would get value based on the id (123), as that part of the composite id is mapped to
		 * the "integerValue" column. */
		integerValueP.setAbsenceInformation(entity, GMF.absenceInformation());
		assertThat(entity.getIntegerValue()).isNull();
	}

	/** Tests that the String id is converted to {@link CompositeIdValues} properly when given in a {@link PersistentEntityReference}. */
	@Test
	public void queryByReference() throws Exception {
		BasicEntity entity = createWithCompositeIdExplicitly();

		SelectQuery sq = new SelectQueryBuilder().from(BasicEntity.T, "e") //
				.select("e") //
				.where().entity("e").eq().entityReference(entity.reference()) //
				.done();

		BasicEntity entity2 = session.query().select(sq).unique();
		assertThat(entity2).isEqualTo(entity);
	}

	/** Tests that the String id is converted to {@link CompositeIdValues} properly when given as a condition on id property alone. */
	@Test
	public void queryByIdEquals() throws Exception {
		BasicEntity entity = createWithCompositeIdExplicitly();

		SelectQuery sq = new SelectQueryBuilder().from(BasicEntity.T, "e") //
				.select("e") //
				.where().property("e", GenericEntity.id).eq(entity.getId()) //
				.done();

		BasicEntity entity2 = session.query().select(sq).unique();
		assertThat(entity2).isSameAs(entity);
	}

	@Test
	public void queryByIdEquals_ReverseOperandsOrder() throws Exception {
		BasicEntity entity = createWithCompositeIdExplicitly();

		SelectQuery sq = queryById(entity);

		BasicEntity entity2 = session.query().select(sq).unique();
		assertThat(entity2).isSameAs(entity);
	}

	private SelectQuery queryById(BasicEntity entity) {
		return new SelectQueryBuilder().from(BasicEntity.T, "e") //
				.select("e") //
				.where().value(entity.getId()).eq().property("e", GenericEntity.id) //
				.done();
	}

	@Test
	public void queryByIdIn() throws Exception {
		BasicEntity entity = createWithCompositeIdExplicitly();

		SelectQuery sq = new SelectQueryBuilder().from(BasicEntity.T, "e") //
				.select("e") //
				.where().property("e", GenericEntity.id).in(asSet(entity.<String> getId())) //
				.done();

		BasicEntity entity2 = session.query().select(sq).unique();
		assertThat(entity2).isEqualTo(entity);
	}

	@Test
	public void queryByIdContains() throws Exception {
		BasicEntity entity = createWithCompositeIdExplicitly();

		SelectQuery sq = new SelectQueryBuilder().from(BasicEntity.T, "e") //
				.select("e") //
				.where().value(asSet(entity.<String> getId())).contains().property("e", GenericEntity.id) //
				.done();

		BasicEntity entity2 = session.query().select(sq).unique();
		assertThat(entity2).isSameAs(entity);
	}

	/**
	 * Because compositeId is handled by CC.preProcessInstanceToBeCloned, we test whether that code is being used by the BFS cloning. This is
	 * implemented with {@link CloningContextBasedBasicModelWalkerCustomization#substitute}
	 */
	@Test
	public void query_DepthFirst() throws Exception {
		AttributeContexts.push(AttributeContexts.derivePeek().set(TmpQueryResultDepth.class, 2).build());

		try {
			run_query_DepthFirst();
		} finally {
			AttributeContexts.pop();
		}

	}

	private void run_query_DepthFirst() throws Exception {
		BasicEntity entity = createWithCompositeIdExplicitly();

		SelectQuery sq = new SelectQueryBuilder().from(BasicEntity.T, "e") //
				.select("e") //
				.where().value(asSet(entity.<String> getId())).contains().property("e", GenericEntity.id) //
				.tc(PreparedTcs.everythingTc) //
				.done();

		BasicEntity entity2 = session.query().select(sq).unique();
		assertThat(entity2).isSameAs(entity);
	}

	private BasicEntity createWithCompositeIdExplicitly() {
		BasicEntity entity = session.create(BasicEntity.T);
		entity.setId("123,'Duck'");
		entity.setPartition("test");
		entity.setName("CompositeId Entity");
		session.commit();

		return entity;
	}

}
