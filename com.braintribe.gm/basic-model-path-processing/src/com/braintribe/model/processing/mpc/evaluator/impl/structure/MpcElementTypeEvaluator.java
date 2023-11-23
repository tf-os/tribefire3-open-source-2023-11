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
import com.braintribe.model.generic.path.api.ModelPathElementType;
import com.braintribe.model.mpc.structure.MpcElementType;
import com.braintribe.model.processing.mpc.evaluator.api.MpcEvaluator;
import com.braintribe.model.processing.mpc.evaluator.api.MpcEvaluatorContext;
import com.braintribe.model.processing.mpc.evaluator.api.MpcEvaluatorRuntimeException;
import com.braintribe.model.processing.mpc.evaluator.api.MpcMatch;
import com.braintribe.model.processing.mpc.evaluator.impl.MpcMatchImpl;

/**
 * {@link MpcEvaluator} for {@link MpcElementType}
 *
 */
public class MpcElementTypeEvaluator implements MpcEvaluator<MpcElementType> {

	private static Logger logger = Logger.getLogger(MpcElementTypeEvaluator.class);
	private static boolean debug = logger.isDebugEnabled();
	
	@Override
	public MpcMatch matches(MpcEvaluatorContext context, MpcElementType condition, IModelPathElement element) throws MpcEvaluatorRuntimeException {

		MpcMatchImpl result = new MpcMatchImpl(element.getPrevious());

		if(debug) logger.debug(" compare the Element type of the condition (which is GmModelPathElementType)"+ condition.getElementType() +" against the element type of the path (which is ModelPathElementType)" + element.getElementType() );
		switch (condition.getElementType()) {

			case ListItem:

				return (element.getElementType() == ModelPathElementType.ListItem) ? result : null;

			case MapKey:

				return (element.getElementType() == ModelPathElementType.MapKey) ? result : null;

			case MapValue:

				return (element.getElementType() == ModelPathElementType.MapValue) ? result : null;

			case Property:

				return (element.getElementType() == ModelPathElementType.Property) ? result : null;

			case EntryPoint: //As per call with Dirk on 16.01.2015, treat EntryPoint as Root. 
			case Root:

				return (element.getElementType() == ModelPathElementType.Root) ? result : null;

			case SetItem:

				return (element.getElementType() == ModelPathElementType.SetItem) ? result : null;

			default:

				throw new MpcEvaluatorRuntimeException("Unsupported MpcElementType type :" + condition.getElementType());
		}

	}

	@Override
	public boolean allowsPotentialMatches(MpcElementType condition) {
		return false;
	}

	@Override
	public boolean hasNestedConditions(MpcElementType condition) {
		return false;
	}

}
