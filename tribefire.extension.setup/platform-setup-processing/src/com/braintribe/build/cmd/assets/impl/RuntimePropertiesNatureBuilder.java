// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.cmd.assets.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import com.braintribe.build.cmd.assets.api.PlatformAssetBuilderContext;
import com.braintribe.build.cmd.assets.api.PlatformAssetNatureBuilder;
import com.braintribe.exception.Exceptions;
import com.braintribe.model.asset.natures.RuntimeProperties;

public class RuntimePropertiesNatureBuilder implements PlatformAssetNatureBuilder<RuntimeProperties> {

	private static final String PART_TYPE = "runtime:properties";

	@Override
	public void transfer(PlatformAssetBuilderContext<RuntimeProperties> context) {
		// transfer part property information to nature
		Optional<File> partFileOptional = context.findPartFile(PART_TYPE);

		RuntimeProperties runtimeProperties = context.getNature();
		Map<String, String> targetProperties = runtimeProperties.getProperties();
		
		if (partFileOptional.isPresent()) {
			File partFile = partFileOptional.get();
			
			try (Reader reader = new InputStreamReader(new FileInputStream(partFile), "UTF-8")) {
				Properties properties = new Properties();
				properties.load(reader);
				properties.forEach((k, v) -> targetProperties.put((String)k, (String)v));
			}
			catch (Exception e) {
				throw Exceptions.unchecked(e, "Error while loading properties from part file: " + partFile.getAbsolutePath());
			}
		}
		
		RuntimePropertiesCollector collector = context.getCollector(RuntimePropertiesCollector.class, RuntimePropertiesCollector::new);
		
		String title = "Properties transferred from asset: " + context.getAsset().qualifiedAssetName();
		
		collector.appendPropertySection(context, title, targetProperties);

	}
	
	@Override
	public List<String> relevantParts() {
		return Collections.singletonList(PART_TYPE);
	}
}
