package com.braintribe.build.cmd.assets.api.selector;

import com.braintribe.model.asset.selector.DependencySelector;

public interface DependencySelectorProcessor<S extends DependencySelector> {
	boolean matches(DependencySelectorContext context, S selector);
}