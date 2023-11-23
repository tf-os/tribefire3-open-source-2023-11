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

import java.util.Set;

import com.braintribe.model.meta.GmMetaModel;

/**
 * @author peter.gazdik
 */
public class CommonGmModelsResolver {

	public static Set<GmMetaModel> findCoveringModels(Iterable<GmMetaModel> models) {
		CommonGmModelsResolver instance = new CommonGmModelsResolver(models);
		return instance.findCoveringModelsImpl();
	}

	private final Iterable<GmMetaModel> models;

	private final Set<GmMetaModel> result = newSet();
	private final Set<GmMetaModel> coverage = newSet();

	private CommonGmModelsResolver(Iterable<GmMetaModel> models) {
		this.models = models;
	}

	private Set<GmMetaModel> findCoveringModelsImpl() {
		for (GmMetaModel model : models)
			ensureCovered(model);

		return result;
	}

	private void ensureCovered(GmMetaModel model) {
		if (coverage.contains(model))
			return;

		Set<GmMetaModel> modelCoverage = computeCoverageOf(model);
		result.removeAll(modelCoverage);
		result.add(model);

		coverage.addAll(modelCoverage);
	}

	private static Set<GmMetaModel> computeCoverageOf(GmMetaModel model) {
		Set<GmMetaModel> result = newSet();
		computeCoverage(model, result);

		return result;
	}

	private static void computeCoverage(GmMetaModel model, Set<GmMetaModel> result) {
		if (result.add(model))
			for (GmMetaModel dependency : model.getDependencies())
				computeCoverage(dependency, result);
	}

}
