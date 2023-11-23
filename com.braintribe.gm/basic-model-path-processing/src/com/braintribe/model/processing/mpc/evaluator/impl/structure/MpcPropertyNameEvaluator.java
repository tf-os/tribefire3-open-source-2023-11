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
package com.braintribe.model.processing.mpc.evaluator.impl.structure;

import com.braintribe.logging.Logger;
import com.braintribe.model.generic.path.api.IModelPathElement;
import com.braintribe.model.generic.path.api.IPropertyModelPathElement;
import com.braintribe.model.mpc.structure.MpcPropertyName;
import com.braintribe.model.processing.mpc.evaluator.api.MpcEvaluator;
import com.braintribe.model.processing.mpc.evaluator.api.MpcEvaluatorContext;
import com.braintribe.model.processing.mpc.evaluator.api.MpcEvaluatorRuntimeException;
import com.braintribe.model.processing.mpc.evaluator.api.MpcMatch;
import com.braintribe.model.processing.mpc.evaluator.impl.MpcMatchImpl;

/**
 * {@link MpcEvaluator} for {@link MpcPropertyName}
 *
 */
public class MpcPropertyNameEvaluator implements MpcEvaluator<MpcPropertyName> {

	private static Logger logger = Logger.getLogger(MpcPropertyNameEvaluator.class);
	private static boolean debug = logger.isDebugEnabled();
	
	@Override
	public MpcMatch matches(MpcEvaluatorContext context, MpcPropertyName condition, IModelPathElement element)throws MpcEvaluatorRuntimeException {

		// validate that the property name of the condition is the same as that of the path
		if(debug) logger.debug( "validate that the property name of the condition is the same as that of the path");
		if (element instanceof IPropertyModelPathElement && ((IPropertyModelPathElement) element).getProperty().getName().equals(condition.getPropertyName())) {
			
			MpcMatchImpl result = new MpcMatchImpl(element.getPrevious());
			if(debug) logger.debug( "result" + result);
			return result;
		}
		if(debug) logger.debug( "result" + null);
		return null;
	}

	@Override
	public boolean allowsPotentialMatches(MpcPropertyName condition) {
		return false;
	}

	@Override
	public boolean hasNestedConditions(MpcPropertyName condition) {
		return false;
	}

}
