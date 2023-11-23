package com.braintribe.build.cmd.assets.impl;

import com.braintribe.build.cmd.assets.api.selector.DependencySelectorContext;
import com.braintribe.build.cmd.assets.api.selector.DependencySelectorProcessor;
import com.braintribe.model.asset.selector.ConjunctionDependencySelector;
import com.braintribe.model.asset.selector.DependencySelector;
import com.braintribe.model.asset.selector.DisjunctionDependencySelector;
import com.braintribe.model.asset.selector.IsDesigntime;
import com.braintribe.model.asset.selector.IsRuntime;
import com.braintribe.model.asset.selector.IsStage;
import com.braintribe.model.asset.selector.IsTagged;
import com.braintribe.model.asset.selector.NegationDependencySelector;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.core.expert.api.MutableDenotationMap;
import com.braintribe.model.processing.core.expert.impl.PolymorphicDenotationMap;

public class GenericDependencySelectorProcessor implements DependencySelectorProcessor<DependencySelector> {
	public static GenericDependencySelectorProcessor INSTANCE = new GenericDependencySelectorProcessor();
	
	private final MutableDenotationMap<DependencySelector, DependencySelectorProcessor<?>> experts = new PolymorphicDenotationMap<>();
	
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
