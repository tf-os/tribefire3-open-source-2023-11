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
package com.braintribe.model.access.sql.manipulation;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.braintribe.model.access.sql.SqlAccess;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.AtomicManipulation;
import com.braintribe.model.generic.manipulation.InstantiationManipulation;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.VdHolder;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.processing.session.api.managed.ManipulationMode;
import com.braintribe.model.processing.session.api.managed.ManipulationReport;
import com.braintribe.model.processing.session.impl.managed.BasicManagedGmSession;

/**
 * @author peter.gazdik
 */
public class SqlManipulationApplicator {

	public static SqlManipulationReportImpl appy(SqlAccess sqlAccess, Manipulation manipulation) {
		return new SqlManipulationApplicator(sqlAccess, manipulation).apply();
	}

	private final Manipulation manipulation;
	private final String defaultPartition;

	private final BasicManagedGmSession session = newSession();
	private final SqlManipulationReportImpl report = new SqlManipulationReportImpl();

	public SqlManipulationApplicator(SqlAccess sqlAccess, Manipulation manipulation) {
		this.manipulation = manipulation;
		this.defaultPartition = sqlAccess.getAccessId();

	}

	private BasicManagedGmSession newSession() {
		BasicManagedGmSession result = new BasicManagedGmSession();
		result.listeners().add(this::noticeManipulation);

		return result;
	}

	private void noticeManipulation(Manipulation manipulation) {
		switch (manipulation.manipulationType()) {
			case INSTANTIATION:
				report.newEntities.add(((InstantiationManipulation) manipulation).getEntity());
				return;
			default:
				return;

		}
	}

	private SqlManipulationReportImpl apply() {
		loadShallowInstancesForPersistentEntities();
		applyManipulationLocally();
		generateIdentifyingPropertiesForNewEntities();

		return report;
	}

	private void loadShallowInstancesForPersistentEntities() {
		List<AtomicManipulation> manipulations = manipulation.inline();

		Set<PersistentEntityReference> references = PersistentReferenceScanner.findPersistentReferences(manipulations);

		for (PersistentEntityReference ref : references) {
			EntityType<?> entityType = ref.valueType();

			GenericEntity entity = entityType.createRaw();
			entity.setId(ref.getRefId());
			entity.setPartition(ref.getRefPartition());

			for (Property p : entityType.getProperties()) {
				if (p.isIdentifying() || p.isGlobalId())
					continue;

				p.setDirectUnsafe(entity, getValueFor(p));
			}

			session.attach(entity);
			report.existingEntities.add(entity);
		}
	}

	private ManipulationReport applyManipulationLocally() {
		return session.manipulate().mode(ManipulationMode.REMOTE).apply(manipulation);
	}

	private Object getValueFor(Property p) {
		if (p.getType().isCollection())
			// TODO for collections we want a special type-compatible collection which just remembers which property was
			// touched so we later find the manipulations
			return VdHolder.standardAiHolder;

		if (p.getType().isBase())
			/* TODO for objects we probably want to use a that touch-aware collection as well, in case somebody is doing
			 * adds/removes on an object property. This would however mean that later we have to find out the existing
			 * value type by a query. */
			return VdHolder.standardAiHolder;

		return VdHolder.standardAiHolder;
	}

	private void generateIdentifyingPropertiesForNewEntities() {
		for (GenericEntity newEntity : report.newEntities) {
			generateIdsIfNeeded(newEntity);
			generatePartitionIfNeeded(newEntity);

		}
	}

	private void generateIdsIfNeeded(GenericEntity newEntity) {
		String typeSignature = newEntity.entityType().getTypeSignature();

		Object _id = newEntity.getId();

		if (_id != null && !(_id instanceof String)) {
			throw new GenericModelException("Id must be a string in SqlAccess. Assigned value was: " + _id + " of type "
					+ _id.getClass().getSimpleName() + ", for entity of type: " + typeSignature);
		}

		String id = (String) _id;
		String globalId = newEntity.getGlobalId();

		if (id != null && globalId != null) {
			if (!id.equals(globalId))
				throw new GenericModelException("Id and GlobalId must be the same in SqlAcceess. Assigned values were: id='" + id + "', globalId='"
						+ globalId + "' for entity of type: " + typeSignature);
			return;
		}

		if (id != null) {
			// id!=null, globalId==null
			assignGlobalId(newEntity, id);

		} else if (globalId != null) {
			// id==null, globalId!=null
			assignId(newEntity, globalId);

		} else {
			// id==null, globalId==null
			String newId = UUID.randomUUID().toString();
			assignId(newEntity, newId);
			assignGlobalId(newEntity, newId);
		}
	}

	private void assignId(GenericEntity newEntity, String newId) {
		newEntity.setId(newId);
		report.assignedIds.put(newEntity, newId);
	}

	private void assignGlobalId(GenericEntity newEntity, String newId) {
		newEntity.setGlobalId(newId);
		report.assignedGlobalIds.put(newEntity, newId);
	}

	private void generatePartitionIfNeeded(GenericEntity newEntity) {
		if (newEntity.getPartition() == null) {
			newEntity.setPartition(defaultPartition);
			report.assignedPartitions.put(newEntity, defaultPartition);
		}
	}

}
