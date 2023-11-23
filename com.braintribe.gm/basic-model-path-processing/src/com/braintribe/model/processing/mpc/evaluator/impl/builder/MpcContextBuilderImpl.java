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
package com.braintribe.model.processing.mpc.evaluator.impl.builder;

import com.braintribe.model.generic.path.api.IModelPathElement;
import com.braintribe.model.processing.mpc.evaluator.api.MpcEvaluatorContext;
import com.braintribe.model.processing.mpc.evaluator.api.MpcEvaluatorRuntimeException;
import com.braintribe.model.processing.mpc.evaluator.api.MpcMatch;
import com.braintribe.model.processing.mpc.evaluator.api.MpcRegistry;
import com.braintribe.model.processing.mpc.evaluator.api.builder.MpcContextBuilder;
import com.braintribe.model.processing.mpc.evaluator.impl.MpcEvaluatorContextImpl;

public class MpcContextBuilderImpl implements MpcContextBuilder {

	protected MpcEvaluatorContext context = new MpcEvaluatorContextImpl();

	@Override
	public MpcContextBuilder withRegistry(MpcRegistry registry) {
		this.context.setMpcRegistry(registry);
		return this;
	}

	@Override
	public MpcMatch mpcMatches(Object condition, IModelPathElement element) throws MpcEvaluatorRuntimeException {
		return this.context.matches(condition, element);
	}

}
