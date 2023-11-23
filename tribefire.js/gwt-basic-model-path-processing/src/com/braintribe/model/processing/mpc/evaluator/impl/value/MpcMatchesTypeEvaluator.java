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
package com.braintribe.model.processing.mpc.evaluator.impl.value;

import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.path.api.IModelPathElement;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.typecondition.TypeCondition;
import com.braintribe.model.mpc.value.MpcElementAxis;
import com.braintribe.model.mpc.value.MpcMatchesType;
import com.braintribe.model.processing.mpc.evaluator.api.MpcEvaluator;
import com.braintribe.model.processing.mpc.evaluator.api.MpcEvaluatorContext;
import com.braintribe.model.processing.mpc.evaluator.api.MpcEvaluatorRuntimeException;
import com.braintribe.model.processing.mpc.evaluator.api.MpcMatch;
import com.braintribe.model.processing.mpc.evaluator.impl.MpcMatchImpl;
import com.braintribe.model.processing.mpc.evaluator.utils.MpcEvaluationTools;

/**
 * {@link MpcEvaluator} for {@link MpcMatchesType}
 * 
 */
public class MpcMatchesTypeEvaluator implements MpcEvaluator<MpcMatchesType> {

	private static Logger logger = Logger.getLogger(MpcMatchesTypeEvaluator.class);
	private static boolean debug = logger.isDebugEnabled();
	private static boolean trace = logger.isTraceEnabled();
	
	@Override
	public MpcMatch matches(MpcEvaluatorContext context, MpcMatchesType condition, IModelPathElement element) throws MpcEvaluatorRuntimeException {

		MpcMatchImpl result = new MpcMatchImpl(element.getPrevious());
		
		if(debug) logger.debug("resolve the path according to the axis of the condition");
		Object evaluationResult = MpcEvaluationTools.resolve(condition.getElementValue(), element);
		
		if(debug) logger.debug("get the type of the resolved value");
		GenericModelType type = GMF.getTypeReflection().getType(evaluationResult);

		if(trace) logger.trace("the value was null, so the GenericModelType was retrieved incorrectly based on null alone. "+ evaluationResult);
		if(evaluationResult == null && condition.getElementValue() == MpcElementAxis.value){
			type = element.getType();
		}
		
		TypeCondition typeCondition = condition.getTypeCondition();
		boolean matchResult = typeCondition.matches(type);

		if(debug) logger.debug(" check if the type condition matches" + matchResult);
		
		return matchResult ? result : null;
	}

	@Override
	public boolean allowsPotentialMatches(MpcMatchesType condition) {
		return false;
	}

	@Override
	public boolean hasNestedConditions(MpcMatchesType condition) {
		return false;
	}

}
