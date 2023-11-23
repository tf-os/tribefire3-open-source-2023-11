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
package com.braintribe.model.access.collaboration.persistence;

import static java.util.Objects.requireNonNull;

import java.util.function.Supplier;

import com.braintribe.cfg.Required;
import com.braintribe.model.processing.manipulation.parser.api.GmmlManipulatorErrorHandler;
import com.braintribe.model.processing.manipulation.parser.api.MutableGmmlManipulatorParserConfiguration;
import com.braintribe.model.processing.manipulation.parser.api.ProblematicEntitiesRegistry;
import com.braintribe.model.processing.manipulation.parser.impl.Gmml;
import com.braintribe.model.processing.manipulation.parser.impl.ManipulatorParser;
import com.braintribe.model.processing.manipulation.parser.impl.listener.error.LenientErrorHandler;
import com.braintribe.model.processing.session.api.collaboration.ManipulationPersistenceException;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializer;
import com.braintribe.model.smoodstorage.stages.PersistenceStage;
import com.braintribe.model.smoodstorage.stages.StaticStage;
import com.braintribe.utils.lcd.StringTools;

/**
 * GMML based {@link PersistenceInitializer} which gets the GMML directly as String, as opposed to parsing a GMML file.
 * 
 * @author peter.gazdik
 */
public class DirectGmmlInitializer implements PersistenceInitializer {

	private String stageName;

	private Supplier<String> modelManSupplier;
	private Supplier<String> dataManSupplier;

	private GmmlManipulatorErrorHandler errorHandler = LenientErrorHandler.INSTANCE;
	private ProblematicEntitiesRegistry problematicEntitiesRegistry;

	@Required
	public void setStageName(String stageName) {
		this.stageName = stageName;
	}

	public void setModelManSupplier(Supplier<String> modelManSupplier) {
		this.modelManSupplier = modelManSupplier;
	}

	public void setDataManSupplier(Supplier<String> dataManSupplier) {
		this.dataManSupplier = dataManSupplier;
	}

	public void setGmmlErrorHandler(GmmlManipulatorErrorHandler errorHandler) {
		this.errorHandler = requireNonNull(errorHandler);
	}

	public void setProblematicEntitiesRegistry(ProblematicEntitiesRegistry problematicEntitiesRegistry) {
		this.problematicEntitiesRegistry = problematicEntitiesRegistry;
	}

	@Override
	public PersistenceStage getPersistenceStage() {
		StaticStage stage = StaticStage.T.create();
		stage.setName(stageName);
		return stage;
	}

	@Override
	public void initializeModels(PersistenceInitializationContext context) throws ManipulationPersistenceException {
		initialize(context, modelManSupplier);
	}

	@Override
	public void initializeData(PersistenceInitializationContext context) throws ManipulationPersistenceException {
		initialize(context, dataManSupplier);
	}

	private void initialize(PersistenceInitializationContext context, Supplier<String> manSupplier) {
		if (manSupplier == null)
			return;

		String gmml = manSupplier.get();
		if (StringTools.isEmpty(gmml))
			return;

		ManipulatorParser.parse(gmml, context.getSession(), parserConfig());
	}

	private MutableGmmlManipulatorParserConfiguration parserConfig() {
		MutableGmmlManipulatorParserConfiguration result = Gmml.manipulatorConfiguration();
		result.setStageName(stageName);
		result.setBufferEntireInput(true);
		result.setParseSingleBlock(true);
		result.setErrorHandler(errorHandler);
		result.setProblematicEntitiesRegistry(problematicEntitiesRegistry);
		return result;
	}

	@Override
	public String toString() {
		return "DirectGmmlInitializer. Stage name: "+stageName;
	}
}
