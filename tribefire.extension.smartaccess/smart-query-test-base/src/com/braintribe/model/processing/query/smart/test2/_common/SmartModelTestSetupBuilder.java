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
package com.braintribe.model.processing.query.smart.test2._common;

import static com.braintribe.model.processing.query.smart.test2._common.SmartTestSetupConstants.modelAName;
import static com.braintribe.model.processing.query.smart.test2._common.SmartTestSetupConstants.modelBName;
import static com.braintribe.model.processing.query.smart.test2._common.SmartTestSetupConstants.modelSName;
import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static com.braintribe.utils.lcd.CollectionTools2.isEmpty;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.smart.mapping.api.SmartMappingEditor;
import com.braintribe.model.processing.smart.mapping.impl.BasicSmartMappingEditor;
import com.braintribe.model.util.meta.NewMetaModelGeneration;
import com.braintribe.utils.lcd.CollectionTools;

/**
 * @author peter.gazdik
 */
public class SmartModelTestSetupBuilder {

	public static SmartModelTestSetup build(List<EntityType<?>> aEntities, List<EntityType<?>> bEntities, List<EntityType<?>> sEntities,
			Function<EntityType<?>, GenericEntity> entityFactory, Consumer<SmartMappingEditor> mappingConfigurer, String globalIdPrefix) {
		
		return build(null, aEntities, bEntities, sEntities, entityFactory, mappingConfigurer, globalIdPrefix);
	}

	// TODO replace with fluent builder
	public static SmartModelTestSetup build(List<EntityType<?>> sharedEntities, List<EntityType<?>> aEntities, List<EntityType<?>> bEntities,
			List<EntityType<?>> sEntities, Function<EntityType<?>, GenericEntity> entityFactory, Consumer<SmartMappingEditor> mappingConfigurer,
			String globalIdPrefix) {

		NewMetaModelGeneration mmg = new NewMetaModelGeneration();

		GmMetaModel commonDependency = isEmpty(sharedEntities) ? mmg.rootMetaModel() : mmg.buildMetaModel("Shared", sharedEntities); 
		
		SmartModelTestSetup result = new SmartModelTestSetup();
		result.modelA = mmg.buildMetaModel(modelAName, aEntities, asList(commonDependency));
		result.modelB = mmg.buildMetaModel(modelBName, bEntities, asList(commonDependency));

		// Build raw smart model
		List<GmMetaModel> deps = asList(mmg.rootMetaModel());		
		if (sharedEntities != null)
			addIfNeeded(deps, commonDependency, sharedEntities, sEntities);
		addIfNeeded(deps, result.modelA, aEntities, sEntities);
		addIfNeeded(deps, result.modelB, bEntities, sEntities);
		result.modelS = mmg.buildMetaModel(modelSName, sEntities, deps);

		// Apply mappings
		SmartMappingEditor mappingEditor = BasicSmartMappingEditor.newInstance() //
				.globalIdPrefix(globalIdPrefix) //
				.smartModel(result.modelS) //
				.delegateModels(asList(result.modelA, result.modelB)) //
				.entityFactory(entityFactory) //
				.build();

		mappingConfigurer.accept(mappingEditor);

		return result;

	}

	private static void addIfNeeded(List<GmMetaModel> deps, GmMetaModel model, List<EntityType<?>> delegateEntities, List<EntityType<?>> sEntities) {
		Set<EntityType<?>> set = new HashSet<>(delegateEntities);

		if (CollectionTools.containsAny(set, sEntities))
			deps.add(model);
	}

	public static <T extends IncrementalAccess> T newAccess(EntityType<T> entityType, String name, GmMetaModel model) {
		T result = entityType.createPlain();
		result.setExternalId(name);
		result.setMetaModel(model);

		return result;
	}

}
