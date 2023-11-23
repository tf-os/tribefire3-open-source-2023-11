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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import com.braintribe.cfg.Required;
import com.braintribe.common.lcd.Constants;
import com.braintribe.model.access.collaboration.persistence.tools.CsaPersistenceTools;
import com.braintribe.model.processing.manipulation.parser.api.GmmlManipulatorErrorHandler;
import com.braintribe.model.processing.manipulation.parser.api.GmmlManipulatorParserConfiguration;
import com.braintribe.model.processing.manipulation.parser.api.MutableGmmlManipulatorParserConfiguration;
import com.braintribe.model.processing.manipulation.parser.api.ProblematicEntitiesRegistry;
import com.braintribe.model.processing.manipulation.parser.impl.ManipulatorParser;
import com.braintribe.model.processing.manipulation.parser.impl.listener.error.LenientErrorHandler;
import com.braintribe.model.processing.session.api.collaboration.ManipulationPersistenceException;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializer;
import com.braintribe.model.smoodstorage.stages.PersistenceStage;
import com.braintribe.model.smoodstorage.stages.StaticStage;

/**
 * @author peter.gazdik
 */
public class SimpleGmmlInitializer implements PersistenceInitializer {

	private String stageName;
	private File gmmlFile;

	private GmmlManipulatorErrorHandler errorHandler = LenientErrorHandler.INSTANCE;
	private ProblematicEntitiesRegistry problematicEntitiesRegistry;

	private static final String MODEL_MAN = "model.man";
	private static final String DATA_MAN = "data.man";

	@Required
	public void setStageName(String stageName) {
		this.stageName = stageName;
	}

	@Required
	public void setGmmlFile(File gmmlFile) {
		this.gmmlFile = gmmlFile;
		validateGmmlFile();
	}

	private void validateGmmlFile() {
		String fileName = gmmlFile.getName();
		boolean isValidFileName = fileName.equals(MODEL_MAN) || fileName.equals(DATA_MAN);

		if (!isValidFileName)
			throw new IllegalArgumentException(
					"Invalid GMML file name. Must be either `model.man` or `data.man`. Configured file: " + gmmlFile.getPath());
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
		initialize(context, MODEL_MAN);
	}

	@Override
	public void initializeData(PersistenceInitializationContext context) throws ManipulationPersistenceException {
		initialize(context, DATA_MAN);
	}

	private void initialize(PersistenceInitializationContext context, String phaseName) {
		if (gmmlFile.exists() && gmmlFile.getName().equals(phaseName)) {
			try (InputStream in = new FileInputStream(gmmlFile)) {
				ManipulatorParser.parse(in, Constants.ENCODING_UTF8, context.getSession(), parserConfig());

			} catch (Exception e) {
				throw new ManipulationPersistenceException("Error while parsing file: " + gmmlFile.getAbsolutePath(), e);
			}
		}
	}

	private GmmlManipulatorParserConfiguration parserConfig() {
		MutableGmmlManipulatorParserConfiguration result = CsaPersistenceTools.parserConfig(gmmlFile);
		result.setStageName(stageName);
		result.setParseSingleBlock(true);
		result.setErrorHandler(errorHandler);
		result.setProblematicEntitiesRegistry(problematicEntitiesRegistry);
		return result;
	}
	
	@Override
	public String toString() {
		return "SimpleGmmlInitializer. Gmml File: "+gmmlFile+", Stage Name: "+stageName;
	}
}
