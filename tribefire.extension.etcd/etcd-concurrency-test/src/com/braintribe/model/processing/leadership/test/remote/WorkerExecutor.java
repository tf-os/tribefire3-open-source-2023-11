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
package com.braintribe.model.processing.leadership.test.remote;

import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.braintribe.model.processing.leadership.api.LeadershipManager;
import com.braintribe.model.processing.leadership.test.config.Configurator;
import com.braintribe.model.processing.leadership.test.wire.contract.EtcdLeadershipTestContract;
import com.braintribe.model.processing.leadership.test.worker.PortWriter;
import com.braintribe.utils.DateTools;


public class WorkerExecutor implements ThreadCompleteListener{

	protected Configurator configurator = null;
	protected EtcdLeadershipTestContract configuration = null;
	protected Map<String,String> props = new HashMap<String, String>();

	protected String candidateId;
	protected PrintWriter logWriter = null;

	public WorkerExecutor(String[] args) {
		this.parseOpts(args);

		String candidateId = this.props.get("candidateId");
		if (candidateId == null) {
			candidateId = "fallback-"+UUID.randomUUID().toString();
		}
		this.candidateId = candidateId;
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
		String message = DateTools.encode(new Date(), DateTools.LEGACY_DATETIME_WITH_MS_FORMAT)+" [Executor/"+candidateId+"]: "+text;
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

	protected void performTest() throws Exception {

		configurator = new Configurator();
		configuration =  configurator.getConfiguration();

		log("Starting Leadership manager");

		LeadershipManager leadershipManager = configuration.leadershipManager();

		String domainId = this.props.get("domainId");

		int failProbability = Integer.parseInt(this.props.get("failProbability"));
		int iterations = Integer.parseInt(this.props.get("iterations"));
		int listeningPort = Integer.parseInt(this.props.get("listeningPort"));


		log("Starting PortWriter with "+candidateId+" and "+iterations+" iterations");

		PortWriter writer = new PortWriter(configuration, candidateId, iterations, listeningPort);
		writer.setFailProbability(failProbability);
		leadershipManager.addLeadershipListener(domainId, candidateId, writer);
		writer.registerManger(this);
		writer.start();
	}

	@Override
	public void notifyOfThreadComplete(Thread thread) throws Exception {

		log("Shutting down " + candidateId);
		configurator.close();

	}
}
