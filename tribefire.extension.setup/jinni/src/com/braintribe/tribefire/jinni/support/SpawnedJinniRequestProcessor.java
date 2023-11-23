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
package com.braintribe.tribefire.jinni.support;

import static com.braintribe.model.service.api.result.Neutral.NEUTRAL;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.LinearCollectionType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.jinni.api.SpawnedJinniRequest;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.service.api.result.Neutral;

public class SpawnedJinniRequestProcessor implements ServiceProcessor<SpawnedJinniRequest, Neutral> {

	@Override
	public Neutral process(ServiceRequestContext requestContext, SpawnedJinniRequest request) {
		List<String> commandAndArgs = createJinniExecutionArgs(request.getInstallationDir());

		marshallToArgs(request, commandAndArgs);

		ProcessBuilder processBuilder = new ProcessBuilder(commandAndArgs);
		try {
			processBuilder.start();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}

		return NEUTRAL;
	}

	private static ArrayList<String> createJinniExecutionArgs(String installationDir) {
		Path installationPath = Paths.get(installationDir);

		ArrayList<String> args = new ArrayList<>();

		args.add("java");
		args.add("-Djinni.installationDir=" + installationPath);
		args.add("-jar");
		args.add(installationPath.resolve("lib").toString());

		return args;
	}

	private static void marshallToArgs(GenericEntity entity, List<String> args) {
		EntityType<GenericEntity> entityType = entity.entityType();

		args.add(entityType.getTypeSignature());

		for (Property property : entityType.getProperties()) {
			GenericModelType propertyType = property.getType();
			Object value = property.get(entity);
			marshallValue(propertyType, value, args, property.getName());
		}
	}
	private static void marshallValue(GenericModelType type, Object value, List<String> args, String valueName) {
		switch (type.getTypeCode()) {
			case dateType:
				break;

			case booleanType:
			case decimalType:
			case doubleType:
			case floatType:
			case integerType:
			case enumType:
			case longType:
			case stringType:
				if (value != null) {
					if (valueName != null)
						args.add("--" + valueName);

					args.add(value.toString());
				}

				break;

			case listType:
			case setType:
				LinearCollectionType collectionType = (LinearCollectionType) type;
				GenericModelType elementType = collectionType.getCollectionElementType();

				if (elementType.isScalar()) {
					if (valueName != null)
						args.add("--" + valueName);

					for (Object element : (Collection<?>) value) {
						marshallValue(elementType, element, args, null);
					}
				}
				break;

			default:
				break;

		}

	}
}
