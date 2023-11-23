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
package com.braintribe.model.processing.mpc.evaluator.impl;

import java.util.HashMap;
import java.util.Map;

import com.braintribe.model.mpc.ModelPathCondition;
import com.braintribe.model.processing.mpc.evaluator.api.MpcEvaluator;
import com.braintribe.model.processing.mpc.evaluator.api.MpcRegistry;

public class MpcRegistryImpl implements MpcRegistry {

	private Map<Class<? extends ModelPathCondition>, MpcEvaluator<?>> experts = new HashMap<Class<? extends ModelPathCondition>, MpcEvaluator<?>>();

	@Override
	public Map<Class<? extends ModelPathCondition>, MpcEvaluator<?>> getExperts() {
		return this.experts;
	}

	@Override
	public void setExperts(Map<Class<? extends ModelPathCondition>, MpcEvaluator<?>> experts) {
		this.experts = experts;
	}

	@Override
	public <D extends ModelPathCondition> void putExpert(Class<D> mpcType, MpcEvaluator<? super D> mpcEvaluator) {
		this.experts.put(mpcType, mpcEvaluator);
	}

	@Override
	public void removeExpert(Class<? extends ModelPathCondition> mpcType) {
		this.experts.remove(mpcType);
	}

	@Override
	public void resetRegistry() {
		this.experts = new HashMap<Class<? extends ModelPathCondition>, MpcEvaluator<?>>();
	}

	@Override
	public void loadOtherRegistry(MpcRegistry otherRegistry) {
		this.experts.putAll(otherRegistry.getExperts());
	}

}
