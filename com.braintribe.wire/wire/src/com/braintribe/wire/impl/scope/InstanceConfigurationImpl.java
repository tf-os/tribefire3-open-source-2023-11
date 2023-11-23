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
package com.braintribe.wire.impl.scope;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.braintribe.wire.api.scope.InstanceConfiguration;
import com.braintribe.wire.api.scope.InstanceQualification;
import com.braintribe.wire.impl.util.Exceptions;

public class InstanceConfigurationImpl implements InstanceConfiguration {
	private static Logger logger = Logger.getLogger(InstanceConfigurationImpl.class.getName());
	public InstanceConfigurationImpl(InstanceQualification qualification) {
		super();
		this.qualification = qualification;
	}

	private Runnable destroyCallback;
	private InstanceQualification qualification;
	
	@Override
	public void onDestroy(Runnable function) {
		destroyCallback = new ExceptionSafeLinkRunnable(destroyCallback, function);
	}
	
	@Override
	public void closeOnDestroy(AutoCloseable closable) {
		onDestroy((Runnable)() -> closeAutoCloseable(closable));
	}
	
	private static void closeAutoCloseable(AutoCloseable closable) {
		try {
			closable.close();
		}
		catch (Exception e) {
			throw Exceptions.unchecked(e);
		}
	}
	
	public Runnable destroyCallback() {
		return destroyCallback;
	}

	@Override
	public InstanceQualification qualification() {
		return qualification;
	}
	
	private class ExceptionSafeLinkRunnable implements Runnable {
		private Runnable predecessor;
		private Runnable runnable;
		
		public ExceptionSafeLinkRunnable(Runnable predecessor, Runnable runnable) {
			super();
			this.predecessor = predecessor;
			this.runnable = runnable;
		}
		
		@Override
		public void run() {
			if (predecessor != null)
				predecessor.run();
			
			try {
				runnable.run();
			}
			catch (Exception e) {
				logger.log(Level.SEVERE,"Error while calling destroy callback [" + runnable + "] of bean " + qualification.space().getClass() + "/" + qualification.name(), e);
			}
		}
	}
}
