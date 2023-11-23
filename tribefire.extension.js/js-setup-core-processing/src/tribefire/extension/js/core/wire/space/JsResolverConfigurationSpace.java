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
package tribefire.extension.js.core.wire.space;

import java.io.File;
import java.util.Collection;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.model.artifact.PartTuple;
import com.braintribe.ve.api.VirtualEnvironment;

import tribefire.extension.js.core.api.JsResolver;
import tribefire.extension.js.core.wire.contract.JsResolverConfigurationContract;

/**
 * the space that allows configuration of the {@link JsResolver}
 * @author pit
 *
 */
public class JsResolverConfigurationSpace implements JsResolverConfigurationContract {

	private File workingDirectory;
	private File resolutionDirectory;
	private File m2Repository;
	private Collection<PartTuple> relevantPartTuples;
	private VirtualEnvironment virtualEnvironment;
	private boolean supportLocalProjects;
	private boolean preferMinOverPretty;
	private boolean useSymbolicLink;
	
	@Required @Configurable
	public void setWorkingDirectory(File workingDirectory) {
		this.workingDirectory = workingDirectory;
	}
	@Override
	public File workingDirectory() {
		return workingDirectory;
	}
	
	@Configurable
	public void setResolutionDirectory(File resolutionDirectory) {
		this.resolutionDirectory = resolutionDirectory;
	}
	@Override
	public File resolutionDirectory() {
		return resolutionDirectory;
	}
	
	
	@Configurable
	public void setM2Repository(File m2Repository) {
		this.m2Repository = m2Repository;
	}
	@Override
	public File m2Repository() {		
		return m2Repository;
	}
	
	@Configurable
	public void setRelevantPartTuples(Collection<PartTuple> relevantPartTuples) {
		this.relevantPartTuples = relevantPartTuples;
	}
	@Override
	public Collection<PartTuple> relevantPartTuples() {	
		return relevantPartTuples;
	}
	
	@Configurable @Required
	public void setVirtualEnvironment(VirtualEnvironment virtualEnvironment) {
		this.virtualEnvironment = virtualEnvironment;
	}
	@Override
	public VirtualEnvironment virtualEnvironment() {	
		return virtualEnvironment;
	}
	
	@Configurable
	public void setPreferMinOverPretty(boolean preferMinOverPretty) {
		this.preferMinOverPretty = preferMinOverPretty;
	}
	@Override
	public boolean preferMinOverPretty() {	
		return preferMinOverPretty;
	}
	
	@Configurable
	public void setSupportLocalProjects(boolean supportLocalProjects) {
		this.supportLocalProjects = supportLocalProjects;
	}
	@Override
	public boolean supportLocalProjects() {
		return supportLocalProjects;
	}
	
	@Configurable
	public void setUseSymbolicLink(boolean useSymbolicLink) {
		this.useSymbolicLink = useSymbolicLink;
	}
	@Override
	public boolean useSymbolicLink() {
		return useSymbolicLink;
	} 
	
	
	

}
