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
package com.braintribe.model.access.smood.collaboration.lab;

import java.io.File;

import com.braintribe.model.access.smood.collaboration.deployment.CsaBuilder;
import com.braintribe.model.access.smood.collaboration.deployment.CsaDeployedUnit;
import com.braintribe.model.access.smood.collaboration.tools.CsaTestTools;
import com.braintribe.model.cortexapi.access.collaboration.MergeCollaborativeStage;
import com.braintribe.model.csa.CollaborativeSmoodConfiguration;
import com.braintribe.model.generic.GMF;

/**
 * @author peter.gazdik
 */
public class CsaLabMain {

	static final File LAB_FILE = new File("res/Lab");

	private final CsaDeployedUnit csaUnit;

	public static void main(String[] args) throws Exception {
		new CsaLabMain().run();

		System.out.println("CSA Lab done!");
	}

	public CsaLabMain() throws Exception {
		File workingFolder = CsaTestTools.createWorkingFolder(LAB_FILE);

		csaUnit = CsaBuilder.create() //
				.baseFolder(workingFolder) //
				.cortex(true) //
				.configurationSupplier(this::prepareNewConfiguration) //
				.model(GMF.getTypeReflection().getModel("tribefire.cortex:platform-setup-workbench-model").getMetaModel()) //
				.done();
	}

	private void run() throws Exception {
		mergeStageToPredecessor("trunk", "pre-trunk");
	}

	private CollaborativeSmoodConfiguration prepareNewConfiguration() {
		throw new IllegalStateException("config.json not found");
	}

	private void mergeStageToPredecessor(String source, String target) {
		csaUnit.eval(mergeStageToPredecessorRequest(source, target));
	}

	private MergeCollaborativeStage mergeStageToPredecessorRequest(String source, String target) {
		MergeCollaborativeStage result = MergeCollaborativeStage.T.create();
		result.setSource(source);
		result.setTarget(target);

		return result;
	}

}
