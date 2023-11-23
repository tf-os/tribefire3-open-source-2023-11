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
package com.braintribe.model.processing.manipulation.parser.impl;

import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;
import static java.util.Collections.emptySet;

import java.util.Map;
import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.manipulation.parser.api.GmmlManipulatorErrorHandler;
import com.braintribe.model.processing.manipulation.parser.api.MutableGmmlManipulatorParserConfiguration;
import com.braintribe.model.processing.manipulation.parser.api.ProblematicEntitiesRegistry;
import com.braintribe.model.processing.manipulation.parser.impl.listener.error.LenientErrorHandler;
import com.braintribe.model.processing.manipulation.parser.impl.listener.error.StrictErrorHandler;

public class BasicGmmlParserConfiguration implements MutableGmmlManipulatorParserConfiguration {

	private String stageName;
	private boolean parseSingleBlock = true;
	private GmmlManipulatorErrorHandler errorHandler;
	private Map<String, Object> variables;
	private Set<GenericEntity> createdEntities;
	private Set<String> homeopathicVariables;
	private boolean bufferEntireInput;
	private ProblematicEntitiesRegistry registry;

	@Override
	public String stageName() {
		return stageName;
	}

	@Override
	public void setStageName(String stageName) {
		this.stageName = stageName;
	}

	@Override
	public void setParseSingleBlock(boolean parseSingleBlock) {
		this.parseSingleBlock = parseSingleBlock;
	}

	@Override
	public boolean parseSingleBlock() {
		return parseSingleBlock;
	}

	@Override
	@Deprecated
	public void setLenient(boolean lenient) {
		setErrorHandler(lenient ? LenientErrorHandler.INSTANCE : StrictErrorHandler.INSTANCE);
	}

	@Override
	public void setVariables(Map<String, Object> variables) {
		this.variables = variables;
	}

	@Override
	public Map<String, Object> variables() {
		return variables != null ? variables : newMap();
	}

	@Override
	public void setPreviouslyCreatedEntities(Set<GenericEntity> createdEntities) {
		this.createdEntities = createdEntities;
	}

	@Override
	public Set<GenericEntity> previouslyCreatedEntities() {
		return createdEntities != null ? createdEntities : emptySet();
	}

	@Override
	public void setHomeopathicVariables(Set<String> homeopathicVariables) {
		this.homeopathicVariables = homeopathicVariables;
	}

	@Override
	public Set<String> homeopathicVariables() {
		return homeopathicVariables != null ? homeopathicVariables : newSet();
	}

	@Override
	public boolean bufferEntireInput() {
		return bufferEntireInput;
	}

	@Override
	public void setBufferEntireInput(boolean bufferEntireInput) {
		this.bufferEntireInput = bufferEntireInput;
	}

	@Override
	public ProblematicEntitiesRegistry problematicEntitiesRegistry() {
		return registry;
	}

	@Override
	public void setProblematicEntitiesRegistry(ProblematicEntitiesRegistry registry) {
		this.registry = registry;
	}

	@Override
	public GmmlManipulatorErrorHandler errorHandler() {
		return errorHandler;
	}

	@Override
	public void setErrorHandler(GmmlManipulatorErrorHandler errorHandler) {
		this.errorHandler = errorHandler;
	}

}
