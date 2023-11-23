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
package com.braintribe.product.rat.imp;

import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.product.rat.imp.impl.deployable.AccessImp;
import com.braintribe.product.rat.imp.impl.deployable.BasicDeployableImp;
import com.braintribe.product.rat.imp.impl.deployable.DeployableImpCave;
import com.braintribe.product.rat.imp.impl.model.ModelImp;
import com.braintribe.product.rat.imp.impl.model.ModelImpCave;
import com.braintribe.product.rat.imp.impl.module.ModuleImp;
import com.braintribe.product.rat.imp.impl.module.ModuleImpCave;
import com.braintribe.product.rat.imp.impl.service.CortexServiceHelperCave;
import com.braintribe.product.rat.imp.impl.service.ServiceHelperCave;

/**
 * This is the entrance point to the ImpAPI. All methods here are to be understood like "chapters", leading you to more
 * specialized imps, helping you by your specific tasks. For example {@code deployable().access()} is the entry point
 * for imps that provides convenient methods for common configuration tasks related to accesses.
 * <p>
 * ImpApi uses a cortex session. This means that everything happens on the cortex access. All access-independent methods
 * can be found in {@link ReducedImpApi}
 * <p>
 * <strong>Imp principles<strong>
 * <ol>
 * <li>There are several variations of imps depending on the topic they are experts in.
 * <li>Each imp holds a {@code GenericEntity} and works with it. For example a ModelImp has a {@code GmMetaModel} and
 * can perform typical operations on it.
 * <li>A ImpCave is a factory for imps and it can also be seen as an entry point for a particular section of the API.
 * <li>One can use an ImpCave to search and find an instance of its related entity type. For example, the
 * {@code deployable().access().find("myAccessId")} returns the respective access.
 * <li>One can initiate an {@link AccessImp} for an existing {@code SmoodAccess} by passing the external id of the
 * access in the respective cave. For example, the {@code deployable().access("myAccessId")} will return an
 * {@link AccessImp} that holds the access with the external id 'myAccessId'.
 * </ol>
 * <p>
 *
 */
public class ImpApi extends ReducedImpApi {
	private final PersistenceGmSessionFactory sessionFactory;

	ImpApi(ImpApiFactory impApiFactory, PersistenceGmSessionFactory sessionFactory, PersistenceGmSession cortexSession) {
		super(impApiFactory, cortexSession);
		this.sessionFactory = sessionFactory;
	}

	/**
	 * Returns an imp that manages the passed model
	 */
	public ModelImp model(GmMetaModel model) {
		return new ModelImpCave(session()).with(model);
	}

	/**
	 * Returns an imp that manages the model with passed model name
	 *
	 * @param modelFullName
	 *            full name of <b>an already existing model</b> that should be managed by the imp
	 * @throws ImpException
	 *             if no model could be found with provided name
	 */
	public ModelImp model(String modelFullName) {
		return new ModelImpCave(session()).with(modelFullName);
	}

	/**
	 * Goes to a {@link ModelImpCave deeper level} of the ModelImp API with lots of utility methods for retrieving,
	 * creating and editing MetaModels
	 */
	public ModelImpCave model() {
		return new ModelImpCave(session());
	}

	/**
	 * Goes to a {@link ModuleImpCave deeper level} of the ModuleImp API with lots of utility methods for
	 * modules
	 */
	public ModuleImpCave module() {
		return new ModuleImpCave(session());
	}
	
	/**
	 * Returns an imp that manages the module with passed globalId
	 *
	 * @param globalId
	 *            of <b>an already existing module</b> that should be managed by the imp
	 * @throws ImpException
	 *             if no module could be found with provided {@code globalId}
	 */
	public ModuleImp module(String globalId) {
		return new ModuleImpCave(session()).with(globalId);
	}

	/**
	 * Goes to a {@link DeployableImpCave deeper level} of the DeployablelImp API with lots of utility methods for
	 * retrieving, creating and editing various types of {@link Deployable deployable}s
	 */
	public DeployableImpCave deployable() {
		return new DeployableImpCave(session());
	}

	/**
	 * Returns an imp that manages the {@link Deployable deployable} of a specified {@code EntityType} and external id
	 *
	 * @param type
	 *            EntityType of the {@link Deployable deployable} that should be managed by the imp
	 * @param externalId
	 *            full externalId of <b>an already existing {@link Deployable deployable}</b> that should be managed by
	 *            the imp
	 */
	public <D extends Deployable> BasicDeployableImp<D> deployable(EntityType<D> type, String externalId) {
		String errorMessage = "Could not find suitable deployable with entity type " + type.getTypeSignature() + " and 'externalId' set to '"
				+ externalId + "'. Either it does not exist or it was not committed yet";

		DeployableImpCave deployableImpCave = new DeployableImpCave(session());
		D foundDeployable = deployableImpCave.find(type, externalId).orElseThrow(() -> new ImpException(errorMessage));

		return (BasicDeployableImp<D>) deployableImpCave.with(foundDeployable);
	}

	/**
	 * Returns an imp that manages the passed {@link Deployable deployable}
	 */
	public <D extends Deployable> BasicDeployableImp<D> deployable(D deployable) {
		return (BasicDeployableImp<D>) new DeployableImpCave(session()).with(deployable);
	}

	/**
	 * Goes to a {@link ServiceHelperCave deeper level} of the ServiceImp API with lots of utility methods for calling
	 * common or custom services
	 */
	@Override
	public CortexServiceHelperCave service() {
		return new CortexServiceHelperCave(session());
	}

	/**
	 * Creates a {@link ReducedImpApi} for the access with given externalId
	 */
	public ReducedImpApi switchToAccess(String accessExternalId) {
		return new ReducedImpApi(impApiFactory, sessionFactory.newSession(accessExternalId));
	}

}
