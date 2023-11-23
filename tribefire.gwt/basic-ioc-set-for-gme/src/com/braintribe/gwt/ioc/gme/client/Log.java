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
package com.braintribe.gwt.ioc.gme.client;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.braintribe.gwt.logging.client.LogConfig;
import com.braintribe.gwt.logging.client.LogConsole;
import com.braintribe.gwt.logging.client.LogEventBuffer;
import com.braintribe.gwt.logging.client.LogListener;
import com.braintribe.gwt.logging.ui.gxt.client.LogWindow;
import com.braintribe.provider.SingletonBeanProvider;

/**
 * This is the IoC configuration for the Logging.
 * @author Dirk
 *
 */
class Log {
	
	private static Supplier<LogEventBuffer> logEventBuffer = new SingletonBeanProvider<LogEventBuffer>() {
		@Override
		public LogEventBuffer create() throws Exception {
			LogEventBuffer bean = new LogEventBuffer();
			return bean;
		}
	};
	
	protected static Supplier<LogWindow> logWindow = new SingletonBeanProvider<LogWindow>() {
		@Override
		public LogWindow create() throws Exception {
			LogWindow bean = new LogWindow();
			bean.setLogEventBuffer(logEventBuffer.get());
			return bean;
		}
	};

	private static Supplier<LogConsole> logConsole = new SingletonBeanProvider<LogConsole>() {
		@Override
		public LogConsole create() throws Exception {
			LogConsole bean = new LogConsole();
			return bean;
		}
	};

	private static Supplier<List<LogListener>> logListeners = new SingletonBeanProvider<List<LogListener>>() {
		@Override
		public List<LogListener> create() throws Exception {
			List<LogListener> bean = new ArrayList<LogListener>();
			bean.add(logEventBuffer.get());
			//bean.add(logWindow.get());
			bean.add(logConsole.get());
			return bean;
		}
	};
	
	protected static Supplier<LogConfig> logConfig = new SingletonBeanProvider<LogConfig>() {
		@Override
		public LogConfig create() throws Exception {
			LogConfig bean = new LogConfig();
			bean.setLogListeners(logListeners.get());
			bean.setErrorDialogLogEventBuffer(logEventBuffer.get());
			bean.setProfilingEnabled(Runtime.profilingEnablement.get());
			bean.setLogLevel(Runtime.logLevel.get());
			//ErrorDialog.addExceptionFilterAction(sessionTimeoutExceptionFilter.get(), sessionTimeoutAction.get());
			return bean;
		}
	};
	
	/*private static Supplier<SessionTimeoutExceptionFilter> sessionTimeoutExceptionFilter = new SingletonBeanProvider<SessionTimeoutExceptionFilter>() {
		@Override
		public SessionTimeoutExceptionFilter create() throws Exception {
			SessionTimeoutExceptionFilter bean = publish(new SessionTimeoutExceptionFilter());
			return bean;
		}
	};
	
	private static Supplier<SessionTimeoutAction> sessionTimeoutAction = new SingletonBeanProvider<SessionTimeoutAction>() {
		@Override
		public SessionTimeoutAction create() throws Exception {
			SessionTimeoutAction bean = publish(new SessionTimeoutAction());
			return bean;
		}
	};*/
	
}
