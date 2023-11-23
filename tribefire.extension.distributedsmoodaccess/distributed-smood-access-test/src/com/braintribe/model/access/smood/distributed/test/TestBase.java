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
package com.braintribe.model.access.smood.distributed.test;

import java.io.File;
import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.experimental.categories.Category;

import com.braintribe.model.access.smood.distributed.test.config.Configurator;
import com.braintribe.model.access.smood.distributed.test.utils.TestUtilities;
import com.braintribe.model.access.smood.distributed.test.wire.contract.DistributedSmoodAccessTestContract;
import com.braintribe.processing.test.db.derby.DerbyServerControl;
import com.braintribe.testing.category.SpecialEnvironment;
import com.braintribe.utils.FileTools;

@Category(SpecialEnvironment.class)
public abstract class TestBase {

	protected static DistributedSmoodAccessTestContract configuration = null;
	protected static Configurator configurator = null;
	protected static TestUtilities testUtilities = null;
	protected static DerbyServerControl derbyServerControl = null;

	@BeforeClass
	public static void initializeDatabase() throws Exception {
		deleteDatabaseFiles();
		
		derbyServerControl = new DerbyServerControl();
		derbyServerControl.start();
		
		configurator = new Configurator();
		configuration =  configurator.getConfiguration();
		testUtilities = configuration.utils();
	}
	@AfterClass
	public static void shutdown() throws Exception {
		
		if (configurator != null) {
			configurator.close();
		}
		
		if (derbyServerControl != null) {
			derbyServerControl.stop();
		}
		deleteDatabaseFiles();
		File logFile = new File("derby.log");
		if (logFile.exists()) {
			logFile.delete();
		}
		
		//Add a sleep interval to allow all threads to terminate in the meantime
		Thread.sleep(2000L);
	}
	
	@Ignore
	protected static void deleteDatabaseFiles() throws IOException {
		File dbFolder = new File("res/db");
		if (dbFolder.exists() && dbFolder.isDirectory()) {
			FileTools.deleteDirectoryRecursively(dbFolder);
		}
	}

}
