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
package com.braintribe.devrock.mungojerry.dialog.tab;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import com.braintribe.build.gwt.Monitor;

public class GwtModuleScannerMonitor implements Monitor {
	
	private SubMonitor monitor;	
	
	public void setMonitor(IProgressMonitor monitor) {
		this.monitor = SubMonitor.convert(monitor);
	}

	@Override
	public void acknowledgeModule(String arg0) {
		monitor.setTaskName( arg0.toString());		
	}

	@Override
	public boolean isCancelled() {
		return monitor.isCanceled();
	}

	@Override
	public void acknowledgeTask(String msg, int expectedSteps) {	
		monitor.setTaskName( msg);
		monitor.setWorkRemaining(expectedSteps);		
		
	}

	@Override
	public void acknowledgeStep(String msg, int step) {
		monitor.subTask( msg);	
		monitor.split( 1);		
	}


	@Override
	public void acknowledgeSubStep(String msg) {
		monitor.subTask( msg);		
		monitor.split( 1);
	}

	@Override
	public void done() {
		monitor.done();
		
	}
	
	

}
