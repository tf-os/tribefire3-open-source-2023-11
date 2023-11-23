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

import static com.braintribe.utils.lcd.CollectionTools2.newLinkedSet;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

import com.braintribe.model.generic.reflection.Model;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;

/**
 * @author peter.gazdik
 */
public class ModelInitializingTools {

	public static void extendModelToCoverModels(ManagedGmSession session, GmMetaModel modelToExtend, Set<Model> extensionModels) {
		extendModelToCoverModels(modelToExtend, extensionModels, m -> (GmMetaModel) session.getEntityByGlobalId(m.globalId()));
	}

	public static void extendModelToCoverModels(GmMetaModel modelToExtend, Set<Model> extensionModels, Function<Model, GmMetaModel> modelLookup) {
		Set<Model> coveringModels = CommonModelsResolver.findCoveringModels(extensionModels);

		List<GmMetaModel> deps = modelToExtend.getDependencies();

		coveringModels.stream() //
				.map(modelLookup) //
				.filter(model -> !deps.contains(model)) //
				.forEach(deps::add);
	}

	public static void extendModelToCoverModels(GmMetaModel modelToExtend, Set<Model> extensionModels, Set<String> extensionModelNames,
			Function<Model, GmMetaModel> modelLookup, Function<String, GmMetaModel> modelByNameLookup) {

		Set<GmMetaModel> gmModels = newLinkedSet();
		for (Model model : extensionModels)
			gmModels.add(modelLookup.apply(model));
		for (String modelName : extensionModelNames)
			gmModels.add(modelByNameLookup.apply(modelName));

		Set<GmMetaModel> coveringModels = CommonGmModelsResolver.findCoveringModels(gmModels);

		List<GmMetaModel> deps = modelToExtend.getDependencies();

		coveringModels.stream() //
				.filter(model -> !deps.contains(model)) //
				.forEach(deps::add);

	}

}
