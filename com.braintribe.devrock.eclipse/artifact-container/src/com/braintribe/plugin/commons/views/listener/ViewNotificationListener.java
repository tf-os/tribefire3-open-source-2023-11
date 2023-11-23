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
package com.braintribe.plugin.commons.views.listener;

import org.eclipse.core.resources.IProject;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.part.ViewPart;

import com.braintribe.model.malaclypse.WalkMonitoringResult;

public interface ViewNotificationListener {
	/**
	 * the view (or actually rather the part) has been made visible 
	 * @param key - the name of the part {@link IWorkbenchPartReference}'s part name
	 */
	void acknowledgeVisibility( String key);
	
	/**
	 * the view (or the actually rather the {@link ViewPart}) has been made invisible
	 * @param key - the name of the part 
	 */
	void acknowledgeInvisibility( String key);
	
	/**
	 * the current project has changed 
	 * @param project - the {@link IProject} that is current (selected in the package explorer)
	 */
	void acknowledgeProjectChanged( IProject project);
	
	/**
	 * lock the current terminal, i.e. do not react to change notifications
	 * @param lock
	 */
	void acknowledgeLockTerminal( boolean lock);
	
	void acknowledgeExternalMonitorResult( WalkMonitoringResult result);
}
