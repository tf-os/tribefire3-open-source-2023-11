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
package com.braintribe.doc;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.braintribe.console.ConsoleConfiguration;
import com.braintribe.console.PlainSysoutConsole;
import com.braintribe.model.asset.PlatformAsset;
import com.braintribe.testing.test.AbstractTest;
import com.braintribe.utils.CollectionTools;
import com.braintribe.utils.FileTools;

public class MarkdownLab extends AbstractTest{
	public static void main(String[] args) {
		ConsoleConfiguration.install(PlainSysoutConsole.INSTANCE);
		complex();
	}
	
	public static void simple() {
		try {
			File baseFolder = new File("markdown");
			File outputFolder = new File("output");
			
			if (outputFolder.exists())
				FileTools.deleteDirectoryRecursively(outputFolder);
			
			outputFolder.mkdirs();

			PlatformAsset testPA = getAsset("tribefire.group", "asset-name");
			
			Map<String, Object> dataModel = new HashMap<>();
			
			MarkdownCompiler.compile(baseFolder, outputFolder, CollectionTools.getSet(testPA), true, dataModel);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("\nDone.");
	}
	
	public static void complex() {
		try {
			File baseFolder = testDir(MarkdownLab.class);
			File outputFolder = new File("output");
			
			if (outputFolder.exists())
				FileTools.deleteDirectoryRecursively(outputFolder);
			
			outputFolder.mkdirs();
			
			Set<PlatformAsset> assets = Stream.of(baseFolder.listFiles())
				.filter(group -> !group.getName().equals(".mdoc"))
				.flatMap(group -> Stream.of(group.listFiles()))
				.map(assetFolder -> getAsset(assetFolder.getParentFile().getName(), assetFolder.getName()))
				.collect(Collectors.toSet());
			
			Map<String, Object> dataModel = new HashMap<>();
			dataModel.put("jinniVersion", "9.77.complex-pc");
			
			MarkdownCompiler.compile(baseFolder, outputFolder, assets, true, dataModel);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("\nDone.");
	}
	
	private static PlatformAsset getAsset(String groupId, String name) {
		PlatformAsset asset = PlatformAsset.T.create();
		asset.setGroupId(groupId);
		asset.setName(name);
		
		return asset;
	}
}
