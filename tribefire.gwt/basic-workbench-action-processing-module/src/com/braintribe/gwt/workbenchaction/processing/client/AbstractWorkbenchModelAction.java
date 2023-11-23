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
package com.braintribe.gwt.workbenchaction.processing.client;

import java.util.List;
import java.util.function.Supplier;

import com.braintribe.gwt.gmview.client.ModelAction;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.pr.criteria.matching.StandardMatcher;
import com.braintribe.model.generic.value.Variable;
import com.braintribe.model.processing.vde.evaluator.VDE;
import com.braintribe.model.processing.vde.evaluator.api.VdeEvaluationMode;
import com.braintribe.model.processing.vde.evaluator.api.aspects.UserNameAspect;
import com.braintribe.model.processing.vde.evaluator.api.aspects.VariableProviderAspect;
import com.braintribe.model.processing.workbench.action.api.WorkbenchModelAction;

public abstract class AbstractWorkbenchModelAction extends ModelAction implements WorkbenchModelAction {
	
	private StandardMatcher matcher;
	private Supplier<String> userNameProvider = ()-> null;
	
	public void setUserNameProvider(Supplier<String> userNameProvider) {
		this.userNameProvider = userNameProvider;
	}
	
	private StandardMatcher getMatcher() {
		if (matcher == null) {
			matcher = new StandardMatcher();
			matcher.setCheckOnlyProperties(false);
			matcher.setCriterion(getInplaceContextCriterion());
			matcher.setPropertyValueComparisonResolver(this::evaluate);
		}
		
		return matcher;
	}
	
	@Override
	protected void updateVisibility() {
		if (getInplaceContextCriterion() == null) {
			setHidden(false);
			return;
		}
		
		if (modelPaths == null || (modelPaths.size() != 1 && !getMultiSelectionSupport())) {
			setHidden(true);
			return;
		}
		
		for (List<ModelPath> selection : modelPaths) {
			boolean selectionMatches = false;
			for (ModelPath modelPath : selection) {
				if (getMatcher().matches(modelPath.asTraversingContext())) {
					selectionMatches = true;
					break;
				}
			}
			
			if (!selectionMatches) {
				setHidden(true);
				return;
			}
		}
		
		setHidden(false);
	}
	
	private Object evaluate(Object object) {
		//@formatter:off
		return VDE.evaluate()
				.withEvaluationMode(VdeEvaluationMode.Preliminary)
				.with(UserNameAspect.class, userNameProvider)
				.with(VariableProviderAspect.class, Variable::getDefaultValue)
				.forValue(object);
		//@formatter:on
	}
	
}
