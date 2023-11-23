package com.braintribe.build.cmd.assets.impl;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.braintribe.build.cmd.assets.PlatformAssetStorageRecorder;
import com.braintribe.model.platform.setup.api.SetupDependencyConfig;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.ve.api.VirtualEnvironment;

import tribefire.cortex.asset.resolving.ng.api.PlatformAssetResolvingContext;


public class SimplePlatformAssetResolutionContext implements PlatformAssetResolvingContext {
	private SetupDependencyConfig config;
	private Map<Class<?>, Object> customData = new IdentityHashMap<>();
	private PlatformAssetStorageRecorder recorder = new PlatformAssetStorageRecorder();
	private VirtualEnvironment virtualEnvironment;

	public SimplePlatformAssetResolutionContext(VirtualEnvironment virtualEnvironment, SetupDependencyConfig config) {
		this.virtualEnvironment = virtualEnvironment;
		this.config = config;
	}

	@Override
	public boolean isRuntime() {
		return config.getRuntime();
	}

	@Override
	public boolean isDesigntime() {
		return !config.getRuntime();
	}

	@Override
	public String getStage() {
		return config.getStage();
	}

	@Override
	public Set<String> getTags() {
		return config.getTags();
	}

	@Override
	public <C> C getSharedInfo(Class<C> key, Supplier<C> supplier) {
		return (C) customData.computeIfAbsent(key, k -> supplier.get());
	}

	@Override
	public <C> C findSharedInfo(Class<C> key) {
		return (C) customData.get(key);
	}

	@Override
	public ManagedGmSession session() {
		return recorder.session();
	}
	
	@Override
	public VirtualEnvironment getVirtualEnvironment() {
		return virtualEnvironment;
	}
}
