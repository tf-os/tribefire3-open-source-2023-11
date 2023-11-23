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
package com.braintribe.utils.system.exec;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ProcessTerminatorImplTest {

	protected ProcessTerminatorImpl impl = null;
	protected ScheduledExecutorService service = Executors.newScheduledThreadPool(2);
	
	@Before
	public void initialize() throws Exception {
		this.impl = new ProcessTerminatorImpl();
		this.service.scheduleWithFixedDelay(this.impl, 0L, 1000L, TimeUnit.MILLISECONDS);
	}
	@After
	public void destroy() throws Exception {
		if (this.service != null) {
			this.service.shutdown();
		}
	}
	
	@Test
	public void testProcessTerminatorImpl() throws Exception {
		
		FakeProcess successProcess = new FakeProcess(true);
		this.impl.addProcess("command", successProcess, 2000L);
		Thread.sleep(4000L);
		Assert.assertEquals(true, successProcess.isExitCalled());
		Assert.assertEquals(false, successProcess.isDestroyCalled());
		
		FakeProcess failureProcess = new FakeProcess(false);
		this.impl.addProcess("command", failureProcess, 2000L);
		Thread.sleep(4000L);
		Assert.assertEquals(true, failureProcess.isExitCalled());
		Assert.assertEquals(true, failureProcess.isDestroyCalled());
		
	}
	
	class FakeProcess extends Process {
		protected boolean exitCalled = false;
		protected boolean destroyCalled = false;
		protected boolean successful = true;
		
		public FakeProcess(boolean successful) {
			this.successful = successful;
		}
		@Override
		public OutputStream getOutputStream() {
			return null;
		}
		@Override
		public InputStream getInputStream() {
			return null;
		}
		@Override
		public InputStream getErrorStream() {
			return null;
		}
		@Override
		public int waitFor() throws InterruptedException {
			return 0;
		}
		@Override
		public int exitValue() {
			this.exitCalled = true;
			if (this.successful) {
				return 0;
			} else {
				throw new IllegalThreadStateException();
			}
		}
		@Override
		public void destroy() {
			this.destroyCalled = true;
		}
		public boolean isExitCalled() {
			return exitCalled;
		}
		public boolean isDestroyCalled() {
			return destroyCalled;
		}		
	}
	
}
