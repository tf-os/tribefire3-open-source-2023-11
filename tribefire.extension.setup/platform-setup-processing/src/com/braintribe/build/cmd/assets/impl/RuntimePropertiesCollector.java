// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.cmd.assets.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.braintribe.build.cmd.assets.PlatformAssetDistributionConstants;
import com.braintribe.build.cmd.assets.api.PlatformAssetBuilderContext;
import com.braintribe.build.cmd.assets.api.PlatformAssetCollector;
import com.braintribe.build.cmd.assets.api.PlatformAssetDistributionContext;
import com.braintribe.exception.Exceptions;
import com.braintribe.setup.tools.TfSetupTools;
import com.braintribe.utils.IOTools;

public class RuntimePropertiesCollector implements PlatformAssetCollector, PlatformAssetDistributionConstants {
	
	private static final Pattern PATTERN_TRIBEFIRE_PROPERTIES = Pattern.compile("tribefire-(\\d+)\\.properties");
	private int sequence = 0;

	public void appendPropertySection(PlatformAssetBuilderContext<?> context, String title, Map<String, String> propertyMap) {
		String propertiesFileName = String.format("tribefire-%d.properties", sequence++);
		File file = context.registerPackageFile(context.projectionBaseFolder(true).push(FOLDER_ENVIRONMENT).push(propertiesFileName));
		
		file.getParentFile().mkdirs();
		
		writeRuntimePropertiesToFile(title, propertyMap, file);
	}

	private void writeRuntimePropertiesToFile(String title, Map<String, String> propertyMap, File file) {
		try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8")) {
			writer.write("# ");
			writer.write(title);
			writer.write('\n');
			for (Map.Entry<String, String> entry: propertyMap.entrySet()) {
				writer.write(entry.getKey());
				writer.write('=');
				writer.write(TfSetupTools.escapePropertyValue(entry.getValue()));
				writer.write('\n');
			}
		}
		catch (Exception e) {
			throw Exceptions.unchecked(e, "Error while writing runtime properties to " + file.getAbsolutePath());
		}
	}
	
	@Override
	public void transfer(PlatformAssetDistributionContext context) {
		for (File file: context.getPackageBaseDir().listFiles()) {
			if (file.isDirectory()) {
				File environmentFolder = new File(file, FOLDER_ENVIRONMENT);
				if (!environmentFolder.exists())
					continue;

				File propertiesTargetFile = new File(environmentFolder, FILE_TRIBEFIRE_PROPERTIES);
				
				SortedMap<Integer, File> sortedFiles = new TreeMap<>();
				
				for (File propertiesCandidateFile: environmentFolder.listFiles()) {
					Matcher matcher = PATTERN_TRIBEFIRE_PROPERTIES.matcher(propertiesCandidateFile.getName());
					if (propertiesCandidateFile.isFile() && matcher.matches()) {
						sortedFiles.put(Integer.valueOf(matcher.group(1)), propertiesCandidateFile);
					}
				}

				try (OutputStream out = new FileOutputStream(propertiesTargetFile)) {
					for (File propertiesFile: sortedFiles.values()) {
						try (InputStream in = new FileInputStream(propertiesFile)) {
							IOTools.transferBytes(in, out, IOTools.BUFFER_SUPPLIER_8K);
						}
						propertiesFile.delete();
					}
				}
				catch (Exception e) {
					throw Exceptions.unchecked(e, "Error while merging properties files " + 
							sortedFiles.values() + " into: " + propertiesTargetFile.getAbsolutePath());
				}
			}
		}
	}
	
}
