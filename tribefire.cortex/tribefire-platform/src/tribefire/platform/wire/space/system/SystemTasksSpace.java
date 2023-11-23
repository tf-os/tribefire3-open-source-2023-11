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
package tribefire.platform.wire.space.system;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import com.braintribe.execution.ExtendedScheduledThreadPoolExecutor;
import com.braintribe.execution.virtual.CountingVirtualThreadFactory;
import com.braintribe.utils.system.SystemTools;
import com.braintribe.utils.system.exec.CommandExecutionImpl;
import com.braintribe.utils.system.exec.ProcessTerminatorImpl;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.module.wire.contract.SystemToolsContract;
import tribefire.platform.wire.space.cortex.services.WorkerSpace;

@Managed
public class SystemTasksSpace implements SystemToolsContract {

	@Import
	private SystemInformationSpace systemInformation;

	@Import
	private WorkerSpace worker;

	public void startTasks() {
		worker.taskScheduler() //
				.scheduleAtFixedRate("Process-Terminator", processTerminator(), 0, 10, TimeUnit.SECONDS) //
				.interruptOnCancel(false) //
				.done();
	}

	@Managed
	public ScheduledExecutorService scheduledExecutor() {
		// @formatter:off
		ExtendedScheduledThreadPoolExecutor bean = 
				new ExtendedScheduledThreadPoolExecutor(
						10, 				// corePoolSize
						threadFactory() 	// threadFactory
				);
		bean.setDescription("Scheduled System Executor");
		return bean;
		// @formatter:on
	}

	@Managed
	private ThreadFactory threadFactory() {
		return new CountingVirtualThreadFactory(executorId() + "-");
	}

	private String executorId() {
		return "tribefire.system.executor";
	}

	@Override
	@Managed
	public SystemTools systemTools() {
		SystemTools bean = new SystemTools();
		bean.setCommandExecution(commandExecution());
		return bean;
	}

	@Override
	@Managed
	public CommandExecutionImpl commandExecution() {
		CommandExecutionImpl bean = new CommandExecutionImpl();
		bean.setProcessTerminator(processTerminator());
		return bean;
	}

	@Override
	@Managed
	public ProcessTerminatorImpl processTerminator() {
		ProcessTerminatorImpl bean = new ProcessTerminatorImpl();
		return bean;
	}

}
