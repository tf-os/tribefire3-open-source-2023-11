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
package tribefire.cortex.initializer.tools;

import static com.braintribe.utils.lcd.CollectionTools2.newSet;
import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Stream;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Model;

/**
 * @author peter.gazdik
 */
public class CommonModelsResolver {

	public static Set<Model> getModelsOf(Iterable<EntityType<?>> entityTypes) {
		requireNonNull(entityTypes, "Cannot get models of null (should be EntityTypes).");
		
		Set<Model> result = newSet();
		for (EntityType<?> et: entityTypes)
			result.add(getModelOf(et));
			
		return result;
		
	}

	public static Model getModelOf(EntityType<?> entityType) {
		requireNonNull(entityType, "Cannot get model of null (should be EntityType).");

		Model result = entityType.getModel();
		if (result == null)
			throw new IllegalArgumentException("Model not found for type: " + entityType.getTypeSignature()
					+ ". Make sure the type is declared in a model (not standalone), and this model is on the classpath.");
		return result;
	}

	public static Set<Model> findCoveringModels(Model... models) {
		return findCoveringModels(Arrays.asList(models));
	}

	public static Set<Model> findCoveringModels(Stream<Model> models) {
		return findCoveringModels(models::iterator);
	}

	public static Set<Model> findCoveringModels(Iterable<Model> models) {
		CommonModelsResolver instance = new CommonModelsResolver(models);
		return instance.findCoveringModelsImpl();
	}

	private final Iterable<Model> models;

	private final Set<Model> result = newSet();
	private final Set<Model> coverage = newSet();

	private CommonModelsResolver(Iterable<Model> models) {
		this.models = models;
	}

	private Set<Model> findCoveringModelsImpl() {
		for (Model model : models)
			ensureCovered(model);

		return result;
	}

	private void ensureCovered(Model model) {
		if (coverage.contains(model))
			return;

		Set<Model> modelCoverage = computeCoverageOf(model);
		result.removeAll(modelCoverage);
		result.add(model);

		coverage.addAll(modelCoverage);
	}

	private static Set<Model> computeCoverageOf(Model model) {
		Set<Model> result = newSet();
		computeCoverage(model, result);

		return result;
	}

	private static void computeCoverage(Model model, Set<Model> result) {
		if (result.add(model))
			for (Model dependency : model.getDependencies())
				computeCoverage(dependency, result);
	}

}
