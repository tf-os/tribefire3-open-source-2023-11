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
package com.braintribe.model.access.smart.test.query2._base;

import static com.braintribe.model.generic.typecondition.TypeConditions.isKind;
import static com.braintribe.model.generic.typecondition.TypeConditions.or;
import static com.braintribe.utils.lcd.CollectionTools2.asMap;
import static com.braintribe.utils.lcd.CollectionTools2.asSet;

import java.util.Map;

import org.junit.Before;

import com.braintribe.common.lcd.EmptyReadWriteLock;
import com.braintribe.model.access.smart.SmartAccess;
import com.braintribe.model.access.smood.basic.SmoodAccess;
import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.typecondition.basic.TypeKind;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.query.smart.test.base.MultiPartitionSmoodAccess;
import com.braintribe.model.processing.query.smart.test.model.deployment.MoodAccess;
import com.braintribe.model.processing.query.smart.test2._common.SmartModelTestSetup;
import com.braintribe.model.processing.smood.Smood;
import com.braintribe.testing.tools.gm.access.TransientNonIncrementalAccess;
import com.braintribe.testing.tools.gm.builder.EntityBuilderFactory;

/**
 * @author peter.gazdik
 */
public abstract class AbstractSmartAccessSetupForTest {

	protected SmartAccess smartAccess;

	protected Smood smoodA;
	protected Smood smoodB;

	protected EntityBuilderFactory bA;
	protected EntityBuilderFactory bB;

	private SmoodAccess delegateA;
	private SmoodAccess delegateB;

	@Before
	public void deploySmartAccess() {
		SmartModelTestSetup setup = getSmartModelTestSetup();

		delegateA = configureSmoodAccess(setup.modelA, "accessA", "accessA");
		delegateB = configureSmoodAccess(setup.modelB, "accessB", "accessB");

		smoodA = delegateA.getDatabase();
		smoodB = delegateB.getDatabase();

		bA = new EntityBuilderFactory(null, e -> registerEntity(e, smoodA, delegateA.getAccessId()));
		bB = new EntityBuilderFactory(null, e -> registerEntity(e, smoodB, delegateB.getAccessId()));

		smartAccess = newSmartAccess(setup);
	}

	private void registerEntity(GenericEntity e, Smood smood, String defaultPartition) {
		if (e.getPartition() == null)
			e.setPartition(defaultPartition);

		smood.registerEntity(e, true);
	}

	private SmartAccess newSmartAccess(SmartModelTestSetup setup) {
		MoodAccess denotationA = configureSmoodDenotation(delegateA);
		MoodAccess denotationB = configureSmoodDenotation(delegateB);
		com.braintribe.model.accessdeployment.smart.SmartAccess denotationSmart = newAccess(com.braintribe.model.accessdeployment.smart.SmartAccess.T,
				"smartAccess", setup.modelS);

		SmartAccess result = new SmartAccess();

		result.setAccessMapping(asMap(denotationA, delegateA, denotationB, delegateB));
		result.setSmartDenotation(denotationSmart);
		result.setMetaModel(setup.modelS);
		result.setDefaultTraversingCriteria(defaultSmartTraversingCriteria());

		return result;
	}

	protected SmoodAccess configureSmoodAccess(GmMetaModel metaModel, String name, String... partitions) {
		SmoodAccess result = newSmoodAccess(partitions);
		result.setAccessId(name);
		result.setDataDelegate(new TransientNonIncrementalAccess(metaModel));
		result.setReadWriteLock(EmptyReadWriteLock.INSTANCE);
		return result;
	}

	public static <T extends IncrementalAccess> T newAccess(EntityType<T> entityType, String name, GmMetaModel model) {
		T result = entityType.createPlain();
		result.setExternalId(name);
		result.setMetaModel(model);

		return result;
	}

	protected SmoodAccess newSmoodAccess(String... partitions) {
		return new MultiPartitionSmoodAccess(asSet(partitions));
	}

	private MoodAccess configureSmoodDenotation(SmoodAccess smoodAccess) {
		MoodAccess result = MoodAccess.T.create();
		result.setMetaModel(smoodAccess.getMetaModel());
		result.setExternalId(smoodAccess.getAccessId());

		return result;
	}

	/** We stop on any entity- or collection- property */
	private Map<Class<? extends GenericEntity>, TraversingCriterion> defaultSmartTraversingCriteria() {
		// @formatter:off
		TraversingCriterion defaultTc = TC.create()
					.typeCondition(
							or(
								isKind(TypeKind.entityType),
								isKind(TypeKind.collectionType)
							)
					)
				.done();
		// @formatter:on

		return asMap(GenericEntity.class, defaultTc);
	}

	protected abstract SmartModelTestSetup getSmartModelTestSetup();
}
