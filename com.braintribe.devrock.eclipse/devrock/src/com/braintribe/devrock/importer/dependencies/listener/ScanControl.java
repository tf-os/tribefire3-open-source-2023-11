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
package com.braintribe.devrock.importer.dependencies.listener;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.braintribe.devrock.eclipse.model.identification.RemoteCompiledDependencyIdentification;

public interface ScanControl {
	
	/**
	 * @return - true if a scan is currently running, false otherwise
	 */
	boolean isScanActive();

	/**
	 * starts a scan 
	 * @param monitor - the {@link IProgressMonitor}, may be null
	 */
	void rescan(IProgressMonitor monitor);
	
	/**
	 * stops a currently running scan - not implemented yet 
	 */
	void stop();
	
	
	/**
	 * @param listener - {@link RemoteRepositoryScanListener} to add 
	 */
	void addListener( RemoteRepositoryScanListener listener);
	
	/**
	 * @param listener - {@link RemoteRepositoryScanListener} to remove
	 */
	void removeListener( RemoteRepositoryScanListener listener);
		
	/**
	 * run a CamelCase-style query 
	 * @param txt - the expression
	 * @return - a {@link List} of matching {@link RemoteCompiledDependencyIdentification}
	 */
	List<RemoteCompiledDependencyIdentification> runQuery(String txt);
	
	/**
	 * run a 'contains'-query
	 * @param txt - the expression
	 * @return - a {@link List} of matching {@link RemoteCompiledDependencyIdentification}
	 */
	List<RemoteCompiledDependencyIdentification> runContainsQuery( String txt);
	
	default void scheduleRescan() {
		Job.create("Scanning source repositories", (IProgressMonitor monitor) -> {
			rescan( monitor);
			return Status.OK_STATUS;
		}).schedule();
	}
}
