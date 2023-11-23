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
package com.braintribe.gwt.customization.client.cmd;

import static com.braintribe.utils.lcd.CollectionTools2.asList;

import com.braintribe.gwt.customization.client.StartupEntryPoint;
import com.braintribe.gwt.customization.client.cmd.model.Moron;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.data.prompt.Hidden;
import com.braintribe.model.meta.data.prompt.Visible;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.meta.cmd.CmdResolverImpl;
import com.braintribe.model.processing.meta.oracle.BasicModelOracle;
import com.braintribe.model.util.meta.NewMetaModelGeneration;

/**
 * @author peter.gazdik
 */
public class CmdTest {

	public static void run() {
		new CmdTest(false).runTest();
	}

	public static void runJvm() {
		new CmdTest(true).runTest();
	}

	private final boolean jvm;
	private GmMetaModel metaModel;
	private CmdResolver cmdResolver;

	public CmdTest(boolean jvm) {
		this.jvm = jvm;

	}

	private void runTest() {
		prepareModel();
		prepareResolver();

		runResolver();
	}

	private void prepareModel() {
		log("generating meta model");

		metaModel = new NewMetaModelGeneration().buildMetaModel("test.gwt27.CmdModel", asList(Moron.T));

		log("adding metadata");
		MetaModelEditor editor = new MetaModelEditor(metaModel);

		editor.loadEntityType(Moron.T);
		editor.addEntityMetaData(Hidden.T.create());
	}

	private void prepareResolver() {
		log("preparing CmdResolver");
		cmdResolver = new CmdResolverImpl(new BasicModelOracle(metaModel));
	}

	private void runResolver() {
		log("Resolving entity visibility");
		Visible visibile = cmdResolver.getMetaData().entityClass(Moron.class).meta(Visible.T).exclusive();
		log("Resolved visibile: " + visibile);
	}

	private void log(String msg) {
		if (jvm) {
			System.out.println(msg);
		} else {
			StartupEntryPoint.log(msg);
		}
	}

}
