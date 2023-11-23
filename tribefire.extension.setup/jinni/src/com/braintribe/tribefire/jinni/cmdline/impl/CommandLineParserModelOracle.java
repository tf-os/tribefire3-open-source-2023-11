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
package com.braintribe.tribefire.jinni.cmdline.impl;

import static java.util.Collections.emptyList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.meta.data.mapping.Alias;
import com.braintribe.model.meta.data.mapping.PositionalArguments;
import com.braintribe.model.processing.session.api.managed.ModelAccessory;

public class CommandLineParserModelOracle {
	private final ModelAccessory modelAccessory;
	private final Map<EntityType<?>, Map<String, Property>> propertyNameIndex = new HashMap<>();

	public CommandLineParserModelOracle(ModelAccessory modelAccessory) {
		this.modelAccessory = modelAccessory;
	}

	Property findProperty(EntityType<?> entityType, String identifier) {
		return propertyNameIndex.computeIfAbsent(entityType, this::generateIndex).get(identifier);
	}

	List<Property> getPositionalArguments(EntityType<?> entityType) {
		PositionalArguments posArgs = modelAccessory.getCmdResolver().getMetaData().entityType(entityType).meta(PositionalArguments.T).exclusive();

		if (posArgs == null)
			return emptyList();

		return posArgs.getProperties().stream() //
				.map(entityType::getProperty) //
				.collect(Collectors.toList());
	}

	private Map<String, Property> generateIndex(EntityType<?> entityType) {
		Map<String, Property> properties = new HashMap<>();

		for (Property property : entityType.getProperties()) {
			String name = property.getName();
			properties.put(name, property);

			List<Alias> aliases = modelAccessory.getCmdResolver().getMetaData().useCase("command-line").property(property).meta(Alias.T).list();
			for (Alias alias : aliases) {
				properties.put(alias.getName(), property);
			}
		}

		return properties;
	}
}
