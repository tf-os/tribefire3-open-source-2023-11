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
package com.braintribe.model.processing.accessory.impl;

import static com.braintribe.model.generic.typecondition.TypeConditions.isAssignableTo;
import static com.braintribe.model.generic.typecondition.TypeConditions.isKind;
import static com.braintribe.model.generic.typecondition.TypeConditions.or;
import static com.braintribe.utils.lcd.CollectionTools2.first;

import java.util.List;

import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.descriptive.HasExternalId;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.typecondition.basic.TypeKind;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.selector.MetaDataSelector;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.processing.query.tools.PreparedTcs;
import com.braintribe.model.processing.session.api.collaboration.CollaborativeAccess;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.service.domain.ServiceDomain;

/**
 * @author peter.gazdik
 */
public class ModelAccessoryHelper {

	/**
	 * This TC is used when loading the model behind the model accessory, i.e. also the one which backs it's {@link ModelOracle} and
	 * {@link CmdResolver}. It loads everything except for complex properties of any {@link HasExternalId} instance (e.g. {@link Deployable}s) to
	 * avoid loading too much. This of course means certain {@link MetaDataSelector MD selectors} would not work it hey are targeting complex
	 * properties of such entities.
	 */
	// Cutting-off every complex property of any HasExternalId entity instance in the assembly (e.g.: Deployables)
	// @formatter:off
	public static TraversingCriterion maModelTc = 
			TC.create()
			.pattern()
				.typeCondition(
						isAssignableTo(HasExternalId.T))
				.conjunction()
					.property()
					.typeCondition(
						or(
							isKind(TypeKind.collectionType),
							isKind(TypeKind.entityType)
						))
					.close()
			.close()
		.done();
	// @formatter:on

	public static GmMetaModel queryModelForMa(com.braintribe.model.access.IncrementalAccess cortex, String modelName) {
		List<GenericEntity> models = cortex.queryEntities(modelByNameWithTc(modelName)).getEntities();

		if (models.isEmpty())
			throw new IllegalStateException("No model model found with name: " + modelName);

		if (models.size() > 1)
			throw new IllegalStateException("More than one model found with name: " + modelName);

		return first(models);
	}

	public static EntityQuery modelByNameWithTc(String modelName) {
		// @formatter:off
		EntityQuery query = EntityQueryBuilder
			.from(GmMetaModel.T)
			.where()
				.property("name").eq(modelName)
			.tc(maModelTc)
		.done();
		// @formatter:on
		return query;
	}

	public static TraversingCriterion accessTc() {
		return scalarAndModelOfRootTc(IncrementalAccess.metaModel);
	}

	public static TraversingCriterion serviceDomainTc() {
		return scalarAndModelOfRootTc(ServiceDomain.serviceModel);
	}

	private static TraversingCriterion scalarAndModelOfRootTc(String modelPropertyName) {
		// @formatter:off
		return TC.create()
				.conjunction()
					.criterion(PreparedTcs.scalarOnlyTc)
					.negation()
						.pattern()
							.root()
							.entity()
							.property(modelPropertyName)
						.close()
				.close()
				.done(); 
		// @formatter:on
	}

	public static <T extends HasExternalId> T queryComponentForMa(ManagedGmSession cortexSession, EntityType<T> componentType, String externalId,
			TraversingCriterion tc) {

		return cortexSession.query().select(componentQuery(componentType, externalId, tc)).unique();
	}

	private static SelectQuery componentQuery(EntityType<? extends HasExternalId> baseComponentType, String externalId, TraversingCriterion tc) {
		// @formatter:off
		return new SelectQueryBuilder()
				.from(baseComponentType, "e")
				.select("e")
				.where()
					.property(HasExternalId.externalId).eq(externalId)
				.tc(tc)
				.done();
		// @formatter:on
	}

	public static String queryCortexModelname(CollaborativeAccess cortexAccess) {
		return cortexAccess.readWithCsaSession(session -> session.query().select(cortexModelNameQuery()).unique());
	}

	private static SelectQuery cortexModelNameQuery() {
		// @formatter:off
		return new SelectQueryBuilder()
				.from(IncrementalAccess.T, "a")
				.select("a", "metaModel.name")
				.where()
					.property(HasExternalId.externalId).eq("cortex")
				.done();
		// @formatter:on
	}

}
