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
package com.braintribe.commons.plugin.commands;

import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IWorkingSet;

import com.braintribe.commons.plugin.selection.SelectionTuple;
import com.braintribe.commons.plugin.selection.TargetProvider;
import com.braintribe.commons.plugin.selection.TargetProviderImpl;

public abstract class AbstractDropdownCommandHandler extends AbstractHandler implements TargetProvider {
	
	protected String PARM_MSG = "com.braintribe.artifactcontainer.common.commands.command.param";
	private TargetProviderImpl targetProvider = new TargetProviderImpl();
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		refresh();
		String parameter = event.getParameter(PARM_MSG);
		process( parameter);
		return null;	    
	}
  
	public abstract void process( String parameter);  
	public void executeSingle(IProject project){}

	public void executeSingle( IProject project, IProgressMonitor monitor){}

	
	// target provider delegation 
	@Override
	public SelectionTuple getSelectionTuple() {	
		return targetProvider.getSelectionTuple();
	}
	@Override
	public IWorkingSet getTargetWorkingSet() {		
		return targetProvider.getTargetWorkingSet();
	}	
	@Override
	public IProject getTargetProject() {
		return targetProvider.getTargetProject();
	}
	@Override
	public Set<IProject> getTargetProjects() {
		return targetProvider.getTargetProjects();
	}
	@Override
	public void refresh() {
		targetProvider.refresh();	
	}
		
	
}
