package com.braintribe.build.cmd.assets.api.selector;

import java.util.Set;

public interface DependencySelectorContext {
	boolean isRuntime();
	boolean isDesigntime();
	String getStage();
	Set<String> getTags();
}