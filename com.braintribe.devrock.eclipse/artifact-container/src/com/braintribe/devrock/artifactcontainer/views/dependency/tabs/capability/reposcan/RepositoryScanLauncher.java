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
package com.braintribe.devrock.artifactcontainer.views.dependency.tabs.capability.reposcan;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TreeItem;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.devrock.greyface.GreyfacePlugin;
import com.braintribe.devrock.greyface.GreyfaceViewLauncher;
import com.braintribe.model.artifact.Dependency;

/**
 * 
 * @author pit
 *
 */
public class RepositoryScanLauncher implements HasRepositoryScanTokens {

	public static void initiateRepositoryScan( Display display, TreeItem ...items) {
		if (items == null || items.length == 0)
			return;
		final GreyfaceViewLauncher launcher = GreyfacePlugin.getInstance();
		for (TreeItem item : items){
			Dependency dependency = (Dependency) item.getData( DATAKEY_DEPENDENCY);
			launcher.addDependency( NameParser.buildName(dependency));			
		}
		display.asyncExec( new Runnable() {
			
			@Override
			public void run() {
				launcher.activateGreyface();				
			}
		});
	}
}
