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
package com.braintribe.model.processing.locking.db.test.remote;

import java.io.File;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.braintribe.common.db.DbVendor;
import com.braintribe.common.db.wire.DbTestConnectionsWireModule;
import com.braintribe.common.db.wire.contract.DbTestDataSourcesContract;
import com.braintribe.model.processing.locking.db.impl.DbLocking;
import com.braintribe.model.processing.locking.db.test.wire.contract.DbLockingTestContract;
import com.braintribe.utils.DateTools;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

public class WorkerExecutor implements ThreadCompleteListener {

	protected DbLockingTestContract configuration = null;
	protected Map<String,String> props = new HashMap<String, String>();

	protected String workerId;
	protected PrintWriter logWriter = null;

	public WorkerExecutor(String[] args) {
		this.parseOpts(args);

		String workerId = this.props.get("workerId");
		if (workerId == null) {
			workerId = "fallback-"+UUID.randomUUID().toString();
		}
		this.workerId = workerId;
	}

	public static void main(String[] args) {
		try {
			WorkerExecutor app = new WorkerExecutor(args);
			app.performTest();
		} catch(Exception e) {
			e.printStackTrace(System.out);
			System.out.flush();
		}
	} 

	private void log(String text) {
		String message = DateTools.encode(new Date(), DateTools.LEGACY_DATETIME_WITH_MS_FORMAT)+" [Executor/"+workerId+"]: "+text;
		if (logWriter != null) {
			logWriter.println(message);
			logWriter.flush();
		}
		System.out.println(message);
		System.out.flush();
	}

	protected void parseOpts(String[] args) {
		if (args != null) {
			for (String arg : args) {
				int idx = arg.indexOf("=");
				String key = arg.substring(0,  idx).trim();
				String value = arg.substring(idx+1).trim();
				this.props.put(key, value);
			}
		}
	}

	private WireContext<DbTestDataSourcesContract> dbResourcesContext;
	private WireContext<DbLockingTestContract> lockingWireContext;
	
	protected void performTest() throws Exception {

		dbResourcesContext = Wire.context(DbTestConnectionsWireModule.INSTANCE);

		lockingWireContext = Wire.context(DbLockingTestContract.class) //
				.bindContracts(DbLockingTestContract.class) //
				.bindContract(DbTestDataSourcesContract.class, dbResourcesContext.contract()) //
				.build();

		DbLocking locking = lockingWireContext.contract().locking(DbVendor.derby);
		
		int failProbability = Integer.parseInt(this.props.get("failProbability"));
		int iterations = Integer.parseInt(this.props.get("iterations"));
		long maxWait = Long.parseLong(this.props.get("maxWait"));
		String filePath = this.props.get("file");
		File file = new File(filePath);
		if (!file.exists()) {
			notifyOfThreadComplete();
			throw new Exception("Could not find file: "+file.getAbsolutePath());
		}

		log("Starting "+workerId+" with "+iterations+" iterations");

		LockingWorker writer = new LockingWorker(locking, workerId, iterations, file, maxWait);
		writer.setFailProbability(failProbability);
		writer.registerManger(this);
		
		new Thread(writer).start();
	}

	@Override
	public void notifyOfThreadComplete() {
		log("Shutting down " + workerId);
		lockingWireContext.shutdown();
		dbResourcesContext.shutdown();
	}
}
