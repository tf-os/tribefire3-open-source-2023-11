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
package com.braintribe.model.configurator;

import com.braintribe.config.configurator.Configurator;
import com.braintribe.config.configurator.ConfiguratorException;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.pr.criteria.stringifier.TraversingCriteriaStringifier;
import com.braintribe.model.generic.typecondition.TypeCondition;
import com.braintribe.model.generic.typecondition.stringifier.TypeConditionStringifier;

/**
 * @author peter.gazdik
 */
public class TcStringifierConfigurator implements Configurator {

	@Override
	public void configure() throws ConfiguratorException {
		GMF.platform().registerStringifier(TypeCondition.T, TypeConditionStringifier::stringify);
		GMF.platform().registerStringifier(TraversingCriterion.T, TraversingCriteriaStringifier::stringify);
	}

	@Override
	public String toString() {
		return "TcStringifierConfigurator (registering stringifier for TypeCondition and TraversingCriterion)";
	}
}
