// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.model.build;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.braintribe.model.build.AbstractCreateModelDeclaration.AbstractCommandLineParameters;

public class CreateModelDeclarationFromMaven {

	public static void main(String[] args) {

		Set<String> mandatoryProperties = Stream
				.of(AbstractCommandLineParameters.ARTIFACT, AbstractCommandLineParameters.LOCAL_REPOSITORY,
						AbstractCommandLineParameters.DEPENDENCIES, AbstractCommandLineParameters.CLASSES_FOLDER)
				.collect(Collectors.toCollection(HashSet::new));

		Map<String, String> parameters = AbstractCommandLineParameters.parseParameterValues(Arrays.asList(args), mandatoryProperties);
		ModelDeclrationContext context = new ModelDeclrationContext();
		context.classesFolder = parameters.get(AbstractCommandLineParameters.CLASSES_FOLDER);
		context.artifact = parameters.get(AbstractCommandLineParameters.ARTIFACT);
		context.dependencies = parameters.get(AbstractCommandLineParameters.DEPENDENCIES);
		context.modelRevision = parameters.get(AbstractCommandLineParameters.MODEL_REVISION);
		context.localRepository = parameters.get(AbstractCommandLineParameters.LOCAL_REPOSITORY);

		context.dependencies = convertDependenciesFormat(context.dependencies, context.localRepository);
		CreateModelDeclaration.createModelDeclaration(context);
	}

	private static String convertDependenciesFormat(String dependencies, String localRepository) {
		List<String> deps = Arrays.asList(dependencies.split(System.getProperty("path.separator")));
		String result = "";
		for (String dep : deps) {
			String finalDep = dep.substring(localRepository.length() + 1);
			finalDep = finalDep.substring(0, finalDep.lastIndexOf(File.separator));
			finalDep = replaceLast(finalDep, File.separator, ":");
			finalDep = replaceLast(finalDep, File.separator, ":");
			finalDep = finalDep.replaceAll("\\" + File.separator, ".");
			result += finalDep + ",";
		}

		return result.substring(0, result.length() - 1);
	}

	private static String replaceLast(String string, String substring, String replacement) {
		int index = string.lastIndexOf(substring);
		if (index == -1) {
			return string;
		}
		return string.substring(0, index) + replacement + string.substring(index + substring.length());
	}
}
