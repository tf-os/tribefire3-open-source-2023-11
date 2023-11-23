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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.tribefire.jinni.cmdline.api.EntityFactory;
import com.braintribe.tribefire.jinni.cmdline.api.ParsedCommandLine;

public class ParsedCommandLineImpl implements ParsedCommandLine {

	private final EntityFactory entityFactory;
	private final List<GenericEntity> options = new ArrayList<>();
	private final Map<EntityType<?>, GenericEntity> optionsByType = new ConcurrentHashMap<>();

	public ParsedCommandLineImpl(EntityFactory entityFactory) {
		super();
		this.entityFactory = entityFactory;
	}

	@Override
	public void addEntity(GenericEntity entity) {
		options.add(entity);
		optionsByType.put(entity.entityType(), entity);
	}

	@Override
	public <O extends GenericEntity> O acquireInstance(EntityType<O> optionsType) {
		return (O) optionsByType.computeIfAbsent(optionsType, t -> entityFactory.create(t.getTypeSignature()).get());
	}

	@Override
	public <O extends GenericEntity> Optional<O> findInstance(EntityType<O> optionsType) {
		return (Optional<O>) options.stream().filter(optionsType::isInstance).findFirst();
	}

	@Override
	public <O extends GenericEntity> List<O> listInstances(EntityType<O> optionsType) {
		return (List<O>) options.stream().filter(optionsType::isInstance).collect(Collectors.toList());
	}
}
