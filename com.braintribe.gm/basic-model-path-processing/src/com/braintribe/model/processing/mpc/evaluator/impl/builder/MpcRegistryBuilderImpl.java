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

import java.util.HashMap;
import java.util.Map;

import com.braintribe.model.mpc.ModelPathCondition;
import com.braintribe.model.mpc.atomic.MpcFalse;
import com.braintribe.model.mpc.atomic.MpcTrue;
import com.braintribe.model.mpc.logic.MpcConjunction;
import com.braintribe.model.mpc.logic.MpcDisjunction;
import com.braintribe.model.mpc.logic.MpcNegation;
import com.braintribe.model.mpc.quantifier.MpcQuantifier;
import com.braintribe.model.mpc.structure.MpcElementType;
import com.braintribe.model.mpc.structure.MpcPropertyName;
import com.braintribe.model.mpc.structure.MpcSequence;
import com.braintribe.model.mpc.value.MpcMatchesType;
import com.braintribe.model.processing.mpc.evaluator.api.MpcEvaluator;
import com.braintribe.model.processing.mpc.evaluator.api.MpcRegistry;
import com.braintribe.model.processing.mpc.evaluator.api.builder.MpcRegistryBuilder;
import com.braintribe.model.processing.mpc.evaluator.impl.MpcRegistryImpl;
import com.braintribe.model.processing.mpc.evaluator.impl.atomic.MpcFalseEvaluator;
import com.braintribe.model.processing.mpc.evaluator.impl.atomic.MpcTrueEvaluator;
import com.braintribe.model.processing.mpc.evaluator.impl.logic.MpcConjunctionEvaluator;
import com.braintribe.model.processing.mpc.evaluator.impl.logic.MpcDisjunctionEvaluator;
import com.braintribe.model.processing.mpc.evaluator.impl.logic.MpcNegationEvaluator;
import com.braintribe.model.processing.mpc.evaluator.impl.quantifier.MpcQuantifierEvaluator;
import com.braintribe.model.processing.mpc.evaluator.impl.structure.MpcElementTypeEvaluator;
import com.braintribe.model.processing.mpc.evaluator.impl.structure.MpcPropertyNameEvaluator;
import com.braintribe.model.processing.mpc.evaluator.impl.structure.MpcSequenceEvaluator;
import com.braintribe.model.processing.mpc.evaluator.impl.value.MpcMatchesTypeEvaluator;

public class MpcRegistryBuilderImpl implements MpcRegistryBuilder {

	private MpcRegistry registry = null;
	MpcRegistryBuilder self = null;
	private final static Map<Class<? extends ModelPathCondition>, MpcEvaluator<?>> DEFAULT_EXPERTS;

	// setting up the list of default experts
	static {

		DEFAULT_EXPERTS = new HashMap<Class<? extends ModelPathCondition>, MpcEvaluator<?>>();

		// logic
		DEFAULT_EXPERTS.put(MpcConjunction.class, new MpcConjunctionEvaluator());
		DEFAULT_EXPERTS.put(MpcDisjunction.class, new MpcDisjunctionEvaluator());
		DEFAULT_EXPERTS.put(MpcNegation.class, new MpcNegationEvaluator());

		// structure
		DEFAULT_EXPERTS.put(MpcPropertyName.class, new MpcPropertyNameEvaluator());
		DEFAULT_EXPERTS.put(MpcElementType.class, new MpcElementTypeEvaluator());
		DEFAULT_EXPERTS.put(MpcSequence.class, new MpcSequenceEvaluator());

		// value
		DEFAULT_EXPERTS.put(MpcMatchesType.class, new MpcMatchesTypeEvaluator());

		// atomic
		DEFAULT_EXPERTS.put(MpcTrue.class, new MpcTrueEvaluator());
		DEFAULT_EXPERTS.put(MpcFalse.class, new MpcFalseEvaluator());

		// quantifier
		DEFAULT_EXPERTS.put(MpcQuantifier.class, new MpcQuantifierEvaluator());

	}

	public MpcRegistryBuilderImpl() {
		this.registry = new MpcRegistryImpl();
		this.self = this;
	}

	@Override
	public MpcRegistry defaultSetup() {
		this.registry.resetRegistry();
		this.registry.setExperts(DEFAULT_EXPERTS);

		return this.registry;
	}

	@Override
	public MpcRegistryBuilder addRegistry(MpcRegistry otherRegistry) {
		this.registry.loadOtherRegistry(otherRegistry);
		return this.self;
	}

	@Override
	public MpcRegistryBuilder loadDefaultSetup() {
		MpcRegistry tempRegistry = new MpcRegistryImpl();
		tempRegistry.setExperts(DEFAULT_EXPERTS);

		this.registry.loadOtherRegistry(tempRegistry);
		return this.self;
	}

	@Override
	public <D extends ModelPathCondition> MpcRegistryBuilder withExpert(Class<D> mpcType, MpcEvaluator<? super D> mpcEvaluator) {
		this.registry.putExpert(mpcType, mpcEvaluator);
		return this.self;
	}

	@Override
	public MpcRegistryBuilder removeExpert(Class<? extends ModelPathCondition> mpcType) {
		this.registry.removeExpert(mpcType);
		return this.self;
	}

	@Override
	public MpcRegistry done() {
		return this.registry;
	}

}
