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
package tribefire.extension.drools.pd.wire.space;

import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.processing.deployment.api.ExpertContext;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.extension.drools.integration.api.DroolsContainer;
import tribefire.extension.drools.integration.impl.BasicDroolsContainerFactory;
import tribefire.extension.drools.model.pd.DroolsCondition;
import tribefire.extension.drools.model.pd.HasRuleSheet;
import tribefire.extension.drools.pd.impl.RuleCompiler;
import tribefire.extension.drools.pe.DroolsConditionProcessor;
import tribefire.extension.drools.pe.DroolsTransitionProcessor;
import tribefire.module.wire.contract.ResourceProcessingContract;

@Managed
public class DeployablesSpace implements WireSpace {
	
	@Import
	private ResourceProcessingContract resourceProcessing;
	
	@Managed
	public DroolsTransitionProcessor transitionProcessor(ExpertContext<tribefire.extension.drools.model.pd.DroolsTransitionProcessor> context) {
		DroolsTransitionProcessor bean = new DroolsTransitionProcessor();
		bean.setDroolsContainer(droolsContainer(context));
		return bean;
	}
	
	@Managed 
	public DroolsConditionProcessor conditionProcessor(ExpertContext<DroolsCondition> context) {
		DroolsConditionProcessor bean = new DroolsConditionProcessor();
		bean.setDroolsContainer(droolsContainer(context));
		return bean;
	}
	
	@Managed
	private <T extends HasRuleSheet & Deployable> DroolsContainer droolsContainer(ExpertContext<T> context) {
		return ruleCompiler().compile(context.getDeployable());
	}
	
	@Managed
	private RuleCompiler ruleCompiler() {
		RuleCompiler bean = new RuleCompiler();
		bean.setContainerFactory(containerFactory());
		return bean;
	}
	
	@Managed
	private BasicDroolsContainerFactory containerFactory() {
		BasicDroolsContainerFactory bean = new BasicDroolsContainerFactory();
		return bean;
	}
	
}
