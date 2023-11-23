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
package tribefire.cortex.asset.resolving.impl;

import com.braintribe.model.asset.selector.ConjunctionDependencySelector;
import com.braintribe.model.asset.selector.DependencySelector;
import com.braintribe.model.asset.selector.DisjunctionDependencySelector;
import com.braintribe.model.asset.selector.IsDesigntime;
import com.braintribe.model.asset.selector.IsRuntime;
import com.braintribe.model.asset.selector.IsStage;
import com.braintribe.model.asset.selector.IsTagged;
import com.braintribe.model.asset.selector.NegationDependencySelector;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.core.expert.impl.PolymorphicDenotationMap;

import tribefire.cortex.asset.resolving.api.DependencySelectorContext;
import tribefire.cortex.asset.resolving.api.DependencySelectorProcessor;

public class GenericDependencySelectorProcessor implements DependencySelectorProcessor<DependencySelector> {
	public static GenericDependencySelectorProcessor INSTANCE = new GenericDependencySelectorProcessor();
	
	private final PolymorphicDenotationMap<DependencySelector, DependencySelectorProcessor<?>> experts = new PolymorphicDenotationMap<>();
	
	private GenericDependencySelectorProcessor() {
		register(ConjunctionDependencySelector.T, this::conjunction);
		register(DisjunctionDependencySelector.T, this::disjunction);
		register(NegationDependencySelector.T, this::negation);
		register(IsStage.T, this::isStage);
		register(IsTagged.T, this::isTagged);
		register(IsRuntime.T, this::isRuntime);
		register(IsDesigntime.T, this::isDesigntime);
	}
	
	private <S extends DependencySelector, E extends DependencySelectorProcessor<? super S>> void register(EntityType<S> denotationType, E expert) {
		experts.put(denotationType, expert);
	}
	
	@Override
	public boolean matches(DependencySelectorContext context, DependencySelector selector) {
		DependencySelectorProcessor<DependencySelector> platformAssetDependencySelector = experts.get(selector);
		return platformAssetDependencySelector.matches(context, selector);
	}
	
	public boolean conjunction(DependencySelectorContext context, ConjunctionDependencySelector selector) {
		for (DependencySelector operand: selector.getOperands()) {
			if (!matches(context, operand))
				return false;
		}
		
		return true;
	}
	
	public boolean disjunction(DependencySelectorContext context, DisjunctionDependencySelector selector) {
		for (DependencySelector operand: selector.getOperands()) {
			if (matches(context, operand))
				return true;
		}
		
		return false;
	}
	
	public boolean negation(DependencySelectorContext context, NegationDependencySelector selector) {
		return !matches(context, selector.getOperand());
	}
	
	public boolean isStage(DependencySelectorContext context, IsStage selector) {
		return context.getStage().equals(selector.getStage());
	}

	public boolean isTagged(DependencySelectorContext context, IsTagged selector) {
		return context.getTags().contains(selector.getTag());
	}
		
	public boolean isRuntime(DependencySelectorContext context, @SuppressWarnings("unused") IsRuntime selector) {
		return context.isRuntime();
	}
	
	public boolean isDesigntime(DependencySelectorContext context, @SuppressWarnings("unused") IsDesigntime selector) {
		return context.isDesigntime();
	}
}
