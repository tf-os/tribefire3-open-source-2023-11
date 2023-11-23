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
package com.braintribe.testing.internal.suite.crud.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.meta.data.constraint.Mandatory;
import com.braintribe.model.processing.meta.cmd.builders.PropertyMdResolver;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.processing.session.api.transaction.Transaction;
import com.braintribe.product.rat.imp.impl.utils.QueryHelper;

/**
 * See {@link #run(PersistenceGmSession)}
 */
public class MandatoryPropertyTest extends AbstractAccessCRUDTest {
	private static Logger logger = Logger.getLogger(MandatoryPropertyTest.class);

	private final Set<String> mandatoryPropertyNames = new HashSet<>();

	public MandatoryPropertyTest(String accessId, PersistenceGmSessionFactory factory) {
		super(accessId, factory);
	}

	public Set<String> getMandatoryPropertyNames() {
		return mandatoryPropertyNames;
	}

	/**
	 * Goes through all entities and checks if there is a property with 'Mandatory' metadata attached. Tries to set
	 * every found to null and asserts that an exception is thrown <br>
	 * Verification step: Makes sure the entities remained unchanged
	 *
	 * @return list of all discovered entities with property with 'Mandatory' metadata attached
	 */
	@Override
	protected List<GenericEntity> run(PersistenceGmSession session) {
		QueryHelper queryHelper = new QueryHelper(session);
		List<GenericEntity> entitiesWithMandatoryProps = new ArrayList<>();

		for (GenericEntity entity : queryHelper.allPersistedEntities()) {
			for (Property property : entity.entityType().getProperties()) {
				PropertyMdResolver propertyMeta = session.getModelAccessory().getMetaData().entity(entity).property(property);
				if (propertyMeta.is(Mandatory.T)) {
					GenericEntity refreshedEntity = session.query().entity(entity).refresh();
					Object value = property.get(refreshedEntity);
					assertThat(value).as("## initially Mandatory property '%s' of %s(%s) was set to null", property.getName(),
							entity.entityType().getTypeSignature(), entity.getId()).isNotNull();
				}
			}
		}

		for (GenericEntity entity : queryHelper.allPersistedEntities()) {
			for (Property property : entity.entityType().getProperties()) {
				PropertyMdResolver propertyMeta = session.getModelAccessory().getMetaData().entity(entity).property(property);
				if (propertyMeta.is(Mandatory.T)) {
					property.set(entity, null);
					entitiesWithMandatoryProps.add(entity);
					mandatoryPropertyNames.add(property.getName());
				}
			}
		}
		
		if (entitiesWithMandatoryProps.isEmpty()) {
			logger.warn("There was no mandatory property at all in the whole model. Skipped Test");
			return entitiesWithMandatoryProps;
		}
		
		assertThatThrownBy(session::commit).as("Did not complain about setting mandatory property to null");

		Transaction transaction = session.getTransaction();
		transaction.undo(transaction.getManipulationsDone().size());

		for (GenericEntity entity : queryHelper.allPersistedEntities()) {
			for (Property property : entity.entityType().getProperties()) {
				PropertyMdResolver propertyMeta = session.getModelAccessory().getMetaData().entity(entity).property(property);
				if (propertyMeta.is(Mandatory.T)) {
					GenericEntity refreshedEntity = session.query().entity(entity).withTraversingCriterion(TC.create().negation().joker().done())
							.refresh();
					Object value = property.get(refreshedEntity);
					assertThat(value).as("Mandatory property %s  of %s(%s) was set to %s", property.getName(),
							refreshedEntity.entityType().getTypeSignature(), refreshedEntity.getId(), value).isNotNull();
				}
			}
		}

		return entitiesWithMandatoryProps;
	}

	@Override
	protected void verifyResult(Verificator verificator, List<GenericEntity> testResult) {
		verificator.assertEntitiesArePersisted(testResult);

	}
}
