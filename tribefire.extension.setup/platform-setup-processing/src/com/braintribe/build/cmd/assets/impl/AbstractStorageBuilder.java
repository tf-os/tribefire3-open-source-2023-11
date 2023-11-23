// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.cmd.assets.impl;

import static com.braintribe.console.ConsoleOutputs.configurableSequence;
import static com.braintribe.console.ConsoleOutputs.println;
import static com.braintribe.console.ConsoleOutputs.sequence;
import static com.braintribe.console.ConsoleOutputs.text;
import static com.braintribe.console.ConsoleOutputs.yellow;

import com.braintribe.build.cmd.assets.api.PlatformAssetBuilderContext;
import com.braintribe.build.cmd.assets.api.PlatformAssetNatureBuilder;
import com.braintribe.console.output.ConfigurableConsoleOutputContainer;
import com.braintribe.model.asset.natures.StoragePriming;
import com.braintribe.model.setuppackage.PackagedPlatformAsset;

public abstract class AbstractStorageBuilder<N extends StoragePriming> implements PlatformAssetNatureBuilder<N> {
	
	@Override
	public final void transfer(PlatformAssetBuilderContext<N> context) {
		StoragePriming priming = context.getNature();
		
		if (!context.getAsset().getPlatformProvided()) {
			ConfigurableConsoleOutputContainer idSeq = configurableSequence();
			
			priming.effectiveAccessIds().forEach(a -> {
				if (idSeq.size() > 0)
					idSeq.append(", ");
				
				idSeq.append(yellow(a));
			});
			
			println(sequence(text("  affecting accesses: "), idSeq));
		}
		
		PackagedPlatformSetupBuilder packagedPlatformSetupBuilder = context.getPackagedPlatformSetupBuilder();
		
		PackagedPlatformAsset packagedPlatformAsset = packagedPlatformSetupBuilder.get(context.getAsset());
		packagedPlatformSetupBuilder.getCsaIndexer().register(packagedPlatformAsset);
		
		transfer(context, context.getCollector(StorageConfigurationCollector.class, StorageConfigurationCollector::new));
	}
	
	protected abstract void transfer(PlatformAssetBuilderContext<N> context, StorageConfigurationCollector storageCollector);
}
