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
package com.braintribe.model.processing.platform.setup.wire.space;

import static com.braintribe.wire.api.util.Lists.list;

import com.braintribe.build.cmd.assets.PlatformSetupProcessor;
import com.braintribe.build.cmd.assets.impl.AssetAggregatorNatureBuilder;
import com.braintribe.build.cmd.assets.impl.ContainerProjectionNatureBuilder;
import com.braintribe.build.cmd.assets.impl.DisjointCollector;
import com.braintribe.build.cmd.assets.impl.LicensePrimingNatureBuilder;
import com.braintribe.build.cmd.assets.impl.ManipulationPrimingNatureBuilder;
import com.braintribe.build.cmd.assets.impl.MarkdownDocumentationConfigNatureBuilder;
import com.braintribe.build.cmd.assets.impl.MarkdownDocumentationNatureBuilder;
import com.braintribe.build.cmd.assets.impl.MasterCartridgeNatureBuilder;
import com.braintribe.build.cmd.assets.impl.ModelPrimingNatureBuilder;
import com.braintribe.build.cmd.assets.impl.PluginNatureBuilder;
import com.braintribe.build.cmd.assets.impl.PluginPrimingNatureBuilder;
import com.braintribe.build.cmd.assets.impl.ResourcePrimingNatureBuilder;
import com.braintribe.build.cmd.assets.impl.RuntimePropertiesNatureBuilder;
import com.braintribe.build.cmd.assets.impl.ScriptPrimingNatureBuilder;
import com.braintribe.build.cmd.assets.impl.WebContextNatureBuilder;
import com.braintribe.build.cmd.assets.impl.js_modules.JsLibraryNatureBuilding.JsLibraryNatureBuilder;
import com.braintribe.build.cmd.assets.impl.js_modules.JsLibraryNatureBuilding.JsUxModuleNatureBuilder;
import com.braintribe.build.cmd.assets.impl.js_modules.JsUxModuleCollector;
import com.braintribe.build.cmd.assets.impl.modules.nature.DynamicInitializerInputNatureBuilder;
import com.braintribe.build.cmd.assets.impl.modules.nature.ModuleNatureBuilder;
import com.braintribe.build.cmd.assets.impl.modules.nature.PlatformLibraryNatureBuilder;
import com.braintribe.build.cmd.assets.impl.modules.nature.TribefireWebPlatformNatureBuilder;
import com.braintribe.build.cmd.assets.impl.preprocessing.RedirectStoragePrimingPreprocessor;
import com.braintribe.model.asset.natures.AssetAggregator;
import com.braintribe.model.asset.natures.ContainerProjection;
import com.braintribe.model.asset.natures.DynamicInitializerInput;
import com.braintribe.model.asset.natures.LicensePriming;
import com.braintribe.model.asset.natures.ManipulationPriming;
import com.braintribe.model.asset.natures.MarkdownDocumentation;
import com.braintribe.model.asset.natures.MarkdownDocumentationConfig;
import com.braintribe.model.asset.natures.MasterCartridge;
import com.braintribe.model.asset.natures.ModelPriming;
import com.braintribe.model.asset.natures.PlatformLibrary;
import com.braintribe.model.asset.natures.Plugin;
import com.braintribe.model.asset.natures.PluginPriming;
import com.braintribe.model.asset.natures.ResourcePriming;
import com.braintribe.model.asset.natures.RuntimeProperties;
import com.braintribe.model.asset.natures.ScriptPriming;
import com.braintribe.model.asset.natures.TribefireModule;
import com.braintribe.model.asset.natures.TribefireWebPlatform;
import com.braintribe.model.asset.natures.WebContext;
import com.braintribe.model.asset.preprocessing.RedirectStoragePriming;
import com.braintribe.model.processing.platform.setup.wire.contract.PlatformSetupDependencyEnvironmentContract;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.annotation.Scope;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.extension.js.model.asset.natures.JsLibrary;
import tribefire.extension.js.model.asset.natures.JsUxModule;

@Managed
public class PlatformSetupSpace implements WireSpace {

	@Import
	private PlatformSetupDependencyEnvironmentContract dependencyEnvironment;

	@Managed
	public PlatformSetupProcessor platformSetupProcessor() {
		PlatformSetupProcessor bean = new PlatformSetupProcessor();

		bean.setVirtualEnvironment(dependencyEnvironment.virtualEnvironment());
		bean.registerExpert(ManipulationPriming.T, manipulationPrimingNatureBuilder());
		bean.registerExpert(ResourcePriming.T, resourcePrimingNatureBuilder());
		bean.registerExpert(LicensePriming.T, licensePrimingNatureBuilder());
		bean.registerExpert(ModelPriming.T, modelPrimingNatureBuilder());
		bean.registerExpert(PluginPriming.T, pluginPrimingNatureBuilder());
		bean.registerExpert(Plugin.T, pluginNatureBuilder());
		bean.registerExpert(ScriptPriming.T, scriptPrimingNatureBuilder());
		bean.registerExpert(WebContext.T, webContextNatureBuilder());
		bean.registerExpert(TribefireWebPlatform.T, tribefireWebPlatformNatureBuilder());
		bean.registerExpert(TribefireModule.T, tribefireModuleNatureBuilder());
		bean.registerExpert(DynamicInitializerInput.T, dynamicInitializerInputNatureBuilder());
		bean.registerExpert(PlatformLibrary.T, platformLibraryNatureBuilder());
		bean.registerExpert(MasterCartridge.T, masterCartridgeNatureBuilder());
		bean.registerExpert(AssetAggregator.T, assetAggregatorNatureBuilder());
		bean.registerExpert(ContainerProjection.T, containerProjectionNatureBuilder());
		bean.registerExpert(RuntimeProperties.T, runtimePropertiesNatureBuilder());
		bean.registerExpert(MarkdownDocumentation.T, markdownDocumentationNatureBuilder());
		bean.registerExpert(MarkdownDocumentationConfig.T, markdownDocumentationConfigNatureBuilder());
		bean.registerExpert(JsUxModule.T, jsUxModuleNatureBuilder());
		bean.registerExpert(JsLibrary.T, jsLibraryNatureBuilder());

		// TODO Dirk should check this TWICE!
		// bean.registerInheritanceExpert(ModelPriming.T, modelPrimingNatureBuilder());

		bean.registerAssetPreprocessor(RedirectStoragePriming.T, overlayAccessIdsPreprocessor());

		// Static collectors

		bean.setStaticCollectors(list( //
				this::disjointCollector //
		));

		return bean;
	}

	@Managed
	private RuntimePropertiesNatureBuilder runtimePropertiesNatureBuilder() {
		RuntimePropertiesNatureBuilder bean = new RuntimePropertiesNatureBuilder();
		return bean;
	}

	@Managed
	private ModelPrimingNatureBuilder modelPrimingNatureBuilder() {
		ModelPrimingNatureBuilder bean = new ModelPrimingNatureBuilder();
		return bean;
	}

	@Managed
	private WebContextNatureBuilder webContextNatureBuilder() {
		WebContextNatureBuilder bean = new WebContextNatureBuilder();
		return bean;
	}

	@Managed
	private TribefireWebPlatformNatureBuilder tribefireWebPlatformNatureBuilder() {
		return new TribefireWebPlatformNatureBuilder();
	}

	@Managed
	private ModuleNatureBuilder tribefireModuleNatureBuilder() {
		return new ModuleNatureBuilder();
	}

	@Managed
	private DynamicInitializerInputNatureBuilder dynamicInitializerInputNatureBuilder() {
		return new DynamicInitializerInputNatureBuilder();
	}
	
	@Managed
	private PlatformLibraryNatureBuilder platformLibraryNatureBuilder() {
		return new PlatformLibraryNatureBuilder();
	}
	
	@Managed
	private MasterCartridgeNatureBuilder masterCartridgeNatureBuilder() {
		MasterCartridgeNatureBuilder bean = new MasterCartridgeNatureBuilder();
		return bean;
	}

	@Managed
	private ManipulationPrimingNatureBuilder manipulationPrimingNatureBuilder() {
		ManipulationPrimingNatureBuilder bean = new ManipulationPrimingNatureBuilder();
		return bean;
	}

	@Managed
	private ResourcePrimingNatureBuilder resourcePrimingNatureBuilder() {
		ResourcePrimingNatureBuilder bean = new ResourcePrimingNatureBuilder();
		return bean;
	}
	
	@Managed
	private LicensePrimingNatureBuilder licensePrimingNatureBuilder() {
		LicensePrimingNatureBuilder bean = new LicensePrimingNatureBuilder();
		return bean;
	}

	@Managed
	private AssetAggregatorNatureBuilder assetAggregatorNatureBuilder() {
		AssetAggregatorNatureBuilder bean = new AssetAggregatorNatureBuilder();
		return bean;
	}

	@Managed
	private PluginPrimingNatureBuilder pluginPrimingNatureBuilder() {
		PluginPrimingNatureBuilder bean = new PluginPrimingNatureBuilder();
		return bean;
	}

	@Managed
	private PluginNatureBuilder pluginNatureBuilder() {
		PluginNatureBuilder bean = new PluginNatureBuilder();
		return bean;
	}

	@Managed
	private ScriptPrimingNatureBuilder scriptPrimingNatureBuilder() {
		ScriptPrimingNatureBuilder bean = new ScriptPrimingNatureBuilder();
		return bean;
	}

	@Managed
	private ContainerProjectionNatureBuilder containerProjectionNatureBuilder() {
		ContainerProjectionNatureBuilder bean = new ContainerProjectionNatureBuilder();
		return bean;
	}

	@Managed
	private MarkdownDocumentationNatureBuilder markdownDocumentationNatureBuilder() {
		MarkdownDocumentationNatureBuilder bean = new MarkdownDocumentationNatureBuilder();
		return bean;
	}

	@Managed
	private MarkdownDocumentationConfigNatureBuilder markdownDocumentationConfigNatureBuilder() {
		MarkdownDocumentationConfigNatureBuilder bean = new MarkdownDocumentationConfigNatureBuilder();
		return bean;
	}
	
	@Managed
	private JsUxModuleNatureBuilder jsUxModuleNatureBuilder() {
		JsUxModuleNatureBuilder bean = new JsUxModuleNatureBuilder();
		bean.setJsUxModuleCollectorFactory(this::jsUxModuleCollector);
		
		return bean;
	}
	
	@Managed
	private JsLibraryNatureBuilder jsLibraryNatureBuilder() {
		JsLibraryNatureBuilder bean = new JsLibraryNatureBuilder();
		bean.setJsUxModuleCollectorFactory(this::jsUxModuleCollector);
		
		return bean;
	}
	
	@Managed(Scope.prototype) 
	private JsUxModuleCollector jsUxModuleCollector() {
		JsUxModuleCollector bean = new JsUxModuleCollector();
		
		return bean;
	}

	// Static collectors

	@Managed(Scope.prototype)
	private DisjointCollector disjointCollector() {
		DisjointCollector bean = new DisjointCollector();
		return bean;
	}

	@Managed
	private RedirectStoragePrimingPreprocessor overlayAccessIdsPreprocessor() {
		RedirectStoragePrimingPreprocessor bean = new RedirectStoragePrimingPreprocessor();
		return bean;
	}

}
