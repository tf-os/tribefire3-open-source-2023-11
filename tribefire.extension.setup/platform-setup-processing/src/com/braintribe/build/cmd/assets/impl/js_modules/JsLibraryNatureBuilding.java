package com.braintribe.build.cmd.assets.impl.js_modules;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import com.braintribe.build.cmd.assets.api.PlatformAssetBuilderContext;
import com.braintribe.build.cmd.assets.api.PlatformAssetNatureBuilder;
import com.braintribe.cfg.Required;

import tribefire.cortex.asset.resolving.ng.impl.PlatformAssetSolution;
import tribefire.extension.js.model.asset.natures.JsLibrary;
import tribefire.extension.js.model.asset.natures.JsUxModule;

public abstract class JsLibraryNatureBuilding<T extends JsLibrary> implements PlatformAssetNatureBuilder<T> {

	private Supplier<JsUxModuleCollector> jsUxModuleCollectorFactory;

	@Required
	public void setJsUxModuleCollectorFactory(Supplier<JsUxModuleCollector> jsUxModuleCollectorFactory) {
		this.jsUxModuleCollectorFactory = jsUxModuleCollectorFactory;
	}

	@Override
	public void transfer(PlatformAssetBuilderContext<T> context) {
		JsUxModuleCollector collector = context.getCollector(JsUxModuleCollector.class, jsUxModuleCollectorFactory);
		transferToCollector(collector, context.getClassifiedSolution());
	}

	protected abstract void transferToCollector(JsUxModuleCollector collector, PlatformAssetSolution classifiedSolution);

	@Override
	public List<String> relevantParts() {
		return Collections.emptyList();
	}

	// JsLibrary

	public static class JsLibraryNatureBuilder extends JsLibraryNatureBuilding<JsLibrary> {
		@Override
		protected void transferToCollector(JsUxModuleCollector collector, PlatformAssetSolution classifiedSolution) {
			collector.addLibrary(classifiedSolution);
		}
	}

	// JsUxModule

	public static class JsUxModuleNatureBuilder extends JsLibraryNatureBuilding<JsUxModule> {
		@Override
		protected void transferToCollector(JsUxModuleCollector collector, PlatformAssetSolution classifiedSolution) {
			collector.addSolution(classifiedSolution);
		}
	}

}
