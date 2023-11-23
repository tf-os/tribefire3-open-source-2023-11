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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.braintribe.model.processing.leadership.test.worker.PortWriter;

public class JvmExecutor {

	public static List<RemoteProcess> executeWorkers(
			int workerCount, 
			String domainId, 
			int failProbability,
			int iterations,
			List<Integer> remoteWriterPorts,
			int udpOffset) throws Exception {

		String separator = System.getProperty("file.separator");
		String classpath = System.getProperty("java.class.path");
		String path = System.getProperty("java.home") + separator + "bin" + separator + "java";
		
		List<RemoteProcess> remoteProcesses = new ArrayList<RemoteProcess>();

		for (int i=0; i<workerCount; ++i) {
			String candidateId = "Writer-"+i+"-"+UUID.randomUUID().toString();
			int listeningPort = PortWriter.UDP_BASEPORT + udpOffset + i;
			remoteWriterPorts.add(listeningPort);
			
			ProcessBuilder processBuilder = new ProcessBuilder(path, "-cp", classpath, "-DtestMode=remote", WorkerExecutor.class.getName(), 
					"failProbability="+failProbability, 
					"domainId="+domainId, 
					"candidateId="+candidateId,
					"id="+i,
					"listeningPort="+listeningPort,
					"iterations="+iterations);
			processBuilder.inheritIO();
			Process process = processBuilder.start();
			RemoteProcess remoteProcess = new RemoteProcess(process, candidateId);
			remoteProcesses.add(remoteProcess);
			
			InputStream inStream = process.getInputStream();
			InputStreamWriter ishStdout = new InputStreamWriter(inStream);
			ishStdout.start();
		}
		
		return remoteProcesses;
		
	}

}
