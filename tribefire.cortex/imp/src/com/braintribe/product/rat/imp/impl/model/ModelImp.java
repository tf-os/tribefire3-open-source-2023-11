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
package com.braintribe.product.rat.imp.impl.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.processing.meta.editor.BasicModelMetaDataEditor;
import com.braintribe.model.processing.meta.editor.ModelMetaDataEditor;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.product.rat.imp.AbstractImp;
import com.braintribe.product.rat.imp.ImpException;
import com.braintribe.utils.lcd.Arguments;

/**
 * A {@link AbstractImp} specialized in {@link GmMetaModel}
 */
public class ModelImp extends AbstractImp<GmMetaModel> {

	public ModelImp(PersistenceGmSession session, GmMetaModel model) {
		super(session, model);
	}

	/**
	 * erases the model managed by this imp:<br>
	 * * deletes the model entity<br>
	 * * erases all types of the model (deletes them and eventual properties and constants as well) <br>
	 */
	public void deleteRecursively() {
		for (GmType type : new HashSet<>(instance.getTypes())) {
			SimpleTypeImp<GmType> typeImp = new SimpleTypeImp<GmType>(session(), type);
			typeImp.deleteRecursively();
		}

		super.delete();
	}

	/**
	 * Sets the version of the contained {@link GmMetaModel}
	 */
	public ModelImp version(String version) {
		instance.setVersion(version);
		return this;
	}

	/**
	 * @param dependencyModels
	 *            metamodels to be added as dependency to the metamodel managed by this imp
	 */
	public ModelImp addDependencies(Collection<GmMetaModel> dependencyModels) {
		Arguments.notNullWithName("superModels", dependencyModels);
		for (GmMetaModel newDependency : dependencyModels) {
			instance.getDependencies().add(newDependency);
		}

		return this;
	}

	/**
	 * @param dependencyModels
	 *            metamodels to be added as dependency to the metamodel managed by this imp
	 */
	public ModelImp addDependencies(GmMetaModel... dependencyModels) {
		Arguments.notNullWithName("superModels", dependencyModels);

		return addDependencies(Arrays.asList(dependencyModels));
	}

	/**
	 * @param dependencyModelNames
	 *            names of the metamodels to be added as dependency to the metamodel managed by this imp
	 * @throws ImpException
	 *             if no model could be found for at least one of the provided names
	 */
	public ModelImp addDependencies(String... dependencyModelNames) {
		Arguments.notNullWithName("superModels", dependencyModelNames);

		Collection<GmMetaModel> foundModels = queryHelper.entitiesWithPropertyIn(GmMetaModel.T, "name", (Object[]) dependencyModelNames);

		if (foundModels.size() != dependencyModelNames.length) {
			throw new ImpException("Did not find (all) the models you were looking for. Found " + foundModels);
		}

		return addDependencies(foundModels);
	}

	/**
	 * @return {@link ModelMetaDataEditor} for the model managed by this imp
	 */
	public ModelMetaDataEditor metaDataEditor() {
		return new BasicModelMetaDataEditor(instance);
	}

	/**
	 * @return the groupId part of this imp's model's full name
	 */
	public String groupId() {
		int colonIndex = instance.getName().indexOf(":");

		return instance.getName().substring(0, colonIndex);
	}

	/**
	 * @return the simple name part of this imp's model's full name
	 */
	public String nameWithoutGroupId() {
		int colonIndex = instance.getName().indexOf(":");

		return instance.getName().substring(colonIndex + 1);
	}

	/**
	 * @return a collection of this imp's model's types plus all the types of its dependency models
	 */
	public Collection<GmType> typesIncludingDependencies() {
		Collection<GmMetaModel> dependencies = getModelDependenciesRecursively(instance);

		//@formatter:off
		return dependencies.stream()
				.flatMap(m -> m.getTypes().stream())
				.collect(Collectors.toSet());
		//@formatter:on

	}

	/**
	 * @return MultiModelImp managing all direct dependency models of this imp's model
	 */
	public MultiModelImp dependencies() {
		return new ModelImpCave(session()).with(instance.getDependencies());
	}

	/**
	 * @return MultiModelImp managing all dependency models of this imp's model plus their dependencies (recursively)
	 */
	public MultiModelImp dependenciesRecursively() {
		return new ModelImpCave(session()).with(getModelDependenciesRecursively());
	}

	Collection<GmMetaModel> getModelDependenciesRecursively() {
		return getModelDependenciesRecursively(instance);
	}

	private Collection<GmMetaModel> getModelDependenciesRecursively(GmMetaModel model) {
		Collection<GmMetaModel> dependencies = new ArrayList<>(model.getDependencies());

		for (GmMetaModel dependencyModel : model.getDependencies()) {
			dependencies.addAll(getModelDependenciesRecursively(dependencyModel));
		}

		dependencies.add(model);

		return dependencies;
	}
}
