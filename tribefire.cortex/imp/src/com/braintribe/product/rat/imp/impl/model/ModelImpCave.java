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
import java.util.Collection;
import java.util.stream.Collectors;

import com.braintribe.gm._RootModel_;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.product.rat.imp.AbstractImpCave;
import com.braintribe.product.rat.imp.ImpException;
import com.braintribe.utils.CollectionTools;
import com.braintribe.utils.CommonTools;

/**
 * A {@link AbstractImpCave} specialized in {@link GmMetaModel}
 */
public class ModelImpCave extends AbstractImpCave<GmMetaModel, ModelImp> {

	public static final String ROOT_MODEL_NAME = _RootModel_.reflection.name();

	public ModelImpCave(PersistenceGmSession session) {
		super(session, "name", GmMetaModel.T);
	}

	/**
	 * creates a new matamodel with provided full name and dependencies<br>
	 * currently the version is initially hardcoded to "1.0"
	 *
	 * @param modelFullName
	 *            full name of model including groupId (<code>com.braintribe.gm:folder-model</code>)
	 * @throws ImpException
	 *             when a model with given name already exist, a dependency was not found or is null or the name was
	 *             provided in wrong format
	 * @return an Imp managing the newly created model
	 */
	public ModelImp create(String modelFullName, GmMetaModel... dependencies) {
		return create(modelFullName, CommonTools.getList(dependencies));
	}

	/**
	 * @see #create(String, String...)
	 */
	public ModelImp create(String modelFullName) {
		return create(modelFullName, new ArrayList<GmMetaModel>());
	}

	/**
	 * @see #create(String, String...)
	 */
	public ModelImp create(String modelFullName, String... dependencyModelNames) {
		return create(modelFullName, with(dependencyModelNames).get());
	}

	/**
	 * @see #create(String, String...)
	 */
	public ModelImp create(String modelFullName, Collection<GmMetaModel> dependencies) {

		if (find(modelFullName).isPresent()) {
			throw new ImpException("Model with this name already exists: " + modelFullName + ". Consider using the withName() method");
		}

		if (CollectionTools.isAnyNull(dependencies)) {
			throw new ImpException("Cannot create a model with a 'null' dependency");
		}

		if (!modelFullName.contains(":") || !modelFullName.contains(".")) {
			throw new ImpException(
					"Please specify a full name including groupId (including at least one '.' and ':'). You supplied " + modelFullName);
		}

		GmMetaModel model = session().create(GmMetaModel.T);
		model.setName(modelFullName);
		model.setVersion("1.0");

		String errorMsg = "Fatal Error: No GmMetaModel has been found with the name [" + ROOT_MODEL_NAME
				+ "]. Please make sure this imp's session runs on a fully set up cortex access";

		GmMetaModel rootModel = find(ROOT_MODEL_NAME).orElseThrow(() -> new IllegalStateException(errorMsg));

		if (dependencies.size() != 0) {
			model.setDependencies(new ArrayList<GmMetaModel>(dependencies));
		} else {
			model.setDependencies(CommonTools.getList(rootModel));
		}

		return new ModelImp(session(), model);
	}

	/**
	 * returns a MultiModelImp managing all metamodels that match the passed pattern (like you would use in a query with
	 * the 'like' operator)
	 */
	@Override
	public MultiModelImp allLike(String modelFullNamePatterns) {
		Collection<ModelImp> impsToUse = findAll(modelFullNamePatterns).stream().map(this::buildImp).collect(Collectors.toSet());

		return new MultiModelImp(session(), impsToUse);
	}

	/**
	 * returns a MultiModelImp managing all passed metamodels
	 */
	@Override
	public <E extends GmMetaModel> MultiModelImp with(Collection<E> models) {
		return new MultiModelImp(session(), impify(models));
	}

	/**
	 * returns a MultiModelImp managing all passed metamodels
	 */
	@Override
	public MultiModelImp with(GmMetaModel... instances) {
		return with(CommonTools.toCollection(instances));
	}

	@Override
	protected ModelImp buildImp(GmMetaModel instance) {
		return new ModelImp(session(), instance);
	}

	/**
	 * go to a deeper level of the EntityTypeImp API with lots of utility methods for retrieving, creating and editing
	 * GmEntityTypes
	 */
	public EntityTypeImpCave entityType() {
		return new EntityTypeImpCave(session());
	}

	/**
	 * go to a deeper level of the EnumTypeImp API with lots of utility methods for retrieving, creating and editing
	 * GmEnumTypes
	 */
	public EnumTypeImpCave enumType() {
		return new EnumTypeImpCave(session());
	}

	/**
	 * go to a deeper level of the TypeImp API with lots of utility methods for retrieving, creating and editing GmTypes
	 */
	public SimpleTypeImpCave type() {
		return new SimpleTypeImpCave(session());
	}

	/**
	 * @param type
	 *            entity type that should be managed by the newly created imp
	 */
	public EntityTypeImp entityType(EntityType<?> type) {
		return new EntityTypeImpCave(session()).with(type);
	}

	/**
	 * searches for a model to {@link ModelImp#deleteRecursively()} it. Does nothing if it isn't found
	 *
	 * @param modelFullName
	 *            full name of model to be erased
	 */
	public void deleteRecursivelyIfPresent(String modelFullName) {
		find(modelFullName).ifPresent(model -> with(model).deleteRecursively());
	}
}
