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
package com.braintribe.devrock.zarathud.wirings.core.space;

import com.braintribe.devrock.zarathud.wirings.core.context.CoreContext;
import com.braintribe.devrock.zarathud.wirings.core.contract.ZedCoreContract;
import com.braintribe.devrock.zed.analyze.BasicZedArtifactAnalyzer;
import com.braintribe.devrock.zed.api.ZedCore;
import com.braintribe.devrock.zed.api.core.ResourceScanner;
import com.braintribe.devrock.zed.api.core.ZedArtifactAnalyzer;
import com.braintribe.devrock.zed.core.BasicZedCore;
import com.braintribe.devrock.zed.scan.BasicResourceScanner;
import com.braintribe.wire.api.annotation.Managed;
@Managed
public class ZedCoreSpace implements ZedCoreContract {

	@Override
	@Managed
	public ZedCore core(CoreContext context) {
		BasicZedCore bean = new BasicZedCore();
		bean.setDeclared( context.getDependencies());
		bean.setClasspath( context.getClasspath());
		bean.setClassesDirectories( context.getClassesDirectories());
		bean.setArtifactsRepresentedByClasses( context.getFoldersForNonPackagedClasses());
		
		bean.setSession( context.getSession());
		
		bean.setResourceScanner(resourceScanner());
		bean.setArtifactScanner( analyzer());
		
		return bean;
	}
	
	@Managed
	protected ResourceScanner resourceScanner() {
		BasicResourceScanner bean = new BasicResourceScanner();
		return bean;
	}
	
	@Managed
	protected ZedArtifactAnalyzer analyzer() {
		BasicZedArtifactAnalyzer bean = new BasicZedArtifactAnalyzer();
		return bean;
	}
	

}
