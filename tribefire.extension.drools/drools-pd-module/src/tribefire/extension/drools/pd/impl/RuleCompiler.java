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
package tribefire.extension.drools.pd.impl;

import java.util.Objects;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.resource.Resource;

import tribefire.extension.drools.integration.api.DroolsContainer;
import tribefire.extension.drools.integration.api.DroolsContainerFactory;
import tribefire.extension.drools.model.pd.HasRuleSheet;

public class RuleCompiler {
	private DroolsContainerFactory containerFactory;
	
	@Required
	@Configurable
	public void setContainerFactory(DroolsContainerFactory containerFactory) {
		this.containerFactory = containerFactory;
	}
	
	public <T extends HasRuleSheet & Deployable> DroolsContainer compile(T deployable) {
		Resource resource = Objects.requireNonNull(deployable.getRules(), "HasRuleSheet.resource must not be null");
		return containerFactory.open(resource::openStream, deployable.getExternalId());
	}
}
