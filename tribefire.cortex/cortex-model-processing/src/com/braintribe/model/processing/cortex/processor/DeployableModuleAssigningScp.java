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
package com.braintribe.model.processing.cortex.processor;

import static com.braintribe.utils.lcd.CollectionTools2.first;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import com.braintribe.cfg.Required;
import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.deployment.Module;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.sp.api.AfterStateChangeContext;
import com.braintribe.model.processing.sp.api.StateChangeProcessor;
import com.braintribe.model.processing.sp.api.StateChangeProcessorException;
import com.braintribe.model.processing.sp.api.StateChangeProcessorMatch;
import com.braintribe.model.processing.sp.api.StateChangeProcessorRule;
import com.braintribe.model.processing.sp.api.StateChangeProcessorSelectorContext;
import com.braintribe.model.processing.sp.api.StateChangeProcessors;
import com.braintribe.model.stateprocessing.api.StateChangeProcessorCapabilities;

/**
 * @author peter.gazdik
 */
public class DeployableModuleAssigningScp
		implements StateChangeProcessor<GenericEntity, GenericEntity>, StateChangeProcessorRule, StateChangeProcessorMatch {

	private Function<Deployable, Set<String>> bindingModulesResolver;

	/**
	 * Expects a function which returns a set of all the modules which bind (provide an expert for) given deployable. These modules are represented by
	 * they globalId in cortex.
	 */
	@Required
	public void setBindingModulesResolver(Function<Deployable, Set<String>> bindingModulesResolver) {
		this.bindingModulesResolver = bindingModulesResolver;
	}

	// ###################################
	// ## . . . . . . Rule . . . . . . .##
	// ###################################

	/** Matches every {@link Deployable} instantiation. */
	@Override
	public List<StateChangeProcessorMatch> matches(StateChangeProcessorSelectorContext context) {
		if (context.isForInstantiation() && Deployable.T.isAssignableFrom(context.getEntityType()))
			return Collections.singletonList(this);
		else
			return Collections.emptyList();
	}

	@Override
	public String getRuleId() {
		return getProcessorId();
	}

	@Override
	public StateChangeProcessor<? extends GenericEntity, ?> getStateChangeProcessor(String processorId) {
		return this;
	}

	// ###################################
	// ## . . . . . . Match . . . . . . ##
	// ###################################

	@Override
	public String getProcessorId() {
		return getClass().getName();
	}

	@Override
	public StateChangeProcessor<? extends GenericEntity, ?> getStateChangeProcessor() {
		return this;
	}

	// ###################################
	// ## . . . . . Processor . . . . . ##
	// ###################################

	@Override
	public StateChangeProcessorCapabilities getCapabilities() {
		return StateChangeProcessors.afterOnlyCapabilities();
	}

	/**
	 * If the relevant deployable doesn't have it's {@link Deployable#getModule() module} assigned and there is exactly one module which binds this
	 * deployable, we assign that module here.
	 */
	@Override
	public void onAfterStateChange(AfterStateChangeContext<GenericEntity> afterContext, GenericEntity cc) throws StateChangeProcessorException {
		Deployable deployable = (Deployable) afterContext.getProcessEntity();
		if (deployable.getModule() != null)
			return;

		Set<String> bindingModuleGlobalIds = bindingModulesResolver.apply(deployable);
		if (bindingModuleGlobalIds.size() != 1)
			return;

		PersistenceGmSession session = afterContext.getSystemSession();

		// This simply must work, the module must exist, otherwise there is something wrong with the system and we might as well throw an exception
		Module module = session.getEntityByGlobalId(first(bindingModuleGlobalIds));
		deployable.setModule(module);

		session.commit();
	}

}
