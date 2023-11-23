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
package com.braintribe.model.processing.leadership.test.config;

import java.io.File;

import com.braintribe.logging.Logger;
import com.braintribe.model.processing.leadership.test.wire.contract.EtcdLeadershipTestContract;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

public class Configurator {

	private static final Logger logger = Logger.getLogger(Configurator.class);

	protected WireContext<EtcdLeadershipTestContract> context;

	public Configurator() throws Exception {
		this.initialize();
	}
	
	public void initialize() throws Exception {
		try {
			
			this.cleanDebug();
			
			logger.info("Initializing context");

			context = Wire
					.context(EtcdLeadershipTestContract.class)
					.bindContracts("com.braintribe.model.processing.leadership.test.wire")
					.build();
			
			logger.info("Done initializing context");
			
		} catch(Throwable t) {
			logger.info("Error while initializing context", t);
			throw new RuntimeException("Error while initializing context", t);
		}
	}
	
	public void close() {
		context.shutdown();
	}

	protected void cleanDebug() {
		File debugOutput = new File("debug");
		if (debugOutput.exists()) {
			File[] files = debugOutput.listFiles();
			if (files != null) {
				for (File f : files) {
					f.delete();
				}
			}
			debugOutput.delete();
		}
	}
	
	public EtcdLeadershipTestContract getConfiguration() {
		return context.contract();
	}
}
