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
package com.braintribe.model.jvm.reflection;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.braintribe.logging.Logger;
import com.braintribe.utils.FileTools;

/**
 * For models that are on classpath as projects there is an issue sometimes that "model-declaration.xml" is missing. This is checking that all such
 * projects do have the xml files, otherwise an exception is thrown.
 * 
 * @author peter.gazdik
 */
class ModelDeclarationXmlChecker4Debug {

	private static final Logger log = Logger.getLogger(ModelDeclarationXmlChecker4Debug.class);

	public static void run() {
		new ModelDeclarationXmlChecker4Debug().checkModelProjectsHaveModDecXml();
	}

	private void checkModelProjectsHaveModDecXml() {
		ClassLoader cl = ModelDeclarationXmlChecker4Debug.class.getClassLoader();
		if (!(cl instanceof URLClassLoader)) {
			log.warn("Cannot check models on classpath, ClassLoader is not an instance of URLClassLoadeer, but: " + cl.getClass().getName());
			return;
		}

		URL[] urls = ((URLClassLoader) cl).getURLs();

		Map<File, List<File>> projectToOutputs = Stream.of(urls) //
				.map(url -> new File(url.getPath())) //
				.filter(File::isDirectory) //
				.collect(Collectors.groupingBy(File::getParentFile));

		for (Entry<File, List<File>> e : projectToOutputs.entrySet())
			if (isModelArtifactDir(e.getKey()))
				checkContainsModDecXml(e.getKey(), e.getValue());
	}

	private boolean isModelArtifactDir(File folder) {
		File pomXml = new File(folder, "pom.xml");
		return pomXml.exists() && FileTools.read(pomXml).asString().contains("<archetype>model</archetype>");
	}

	private void checkContainsModDecXml(File projectDir, List<File> projectOutputDirs) {
		for (File dir : projectOutputDirs)
			if (new File(dir, "model-declaration.xml").exists())
				return;

		throw new IllegalStateException("CANNOT START TRIBEFIRE SERVER!!!\n\t"//
				+ "[model-declaration.xml] not found for model project: " + projectDir.getName() + ".\n\t" + //
				"This xml should be auto-generated in Project's build output folder by the DevRock Model Builder plugin. " //
				+ "Please try to clean this project.\n\t" + //
				"Checked output dirs:\n\t\t" + projectOutputDirs.stream().map(File::getName).collect(Collectors.joining("\n\t\t")));
	}

}
