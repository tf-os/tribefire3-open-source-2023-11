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
package com.braintribe.devrock.artifactcontainer.control.walk.wired;

import com.braintribe.build.artifacts.mc.wire.classwalk.contract.ClasspathResolverContract;
import com.braintribe.devrock.artifactcontainer.container.ArtifactContainer;
import com.braintribe.devrock.artifactcontainer.control.walk.ArtifactContainerUpdateRequestType;
import com.braintribe.devrock.artifactcontainer.plugin.malaclypse.listener.MalaclypseAnalysisMonitor;

/**
 * an update request for the {@link ThreadedArtifactContainerWalkProcessor}
 * @author pit
 *
 */
public class WiredArtifactContainerUpdateRequest {
	private ArtifactContainer container;
	private ArtifactContainerUpdateRequestType walkMode;
	private ClasspathResolverContract classpathResolverContract;
	
	
	private boolean updateEclipse;
	private String requestId;
	private MalaclypseAnalysisMonitor analysisMonitor;
	
	public WiredArtifactContainerUpdateRequest(String id, ArtifactContainer container, ArtifactContainerUpdateRequestType walkMode, boolean updateEclipse) {
		super();
		this.requestId = id;
		this.container = container;
		this.walkMode = walkMode;
		this.updateEclipse = updateEclipse;
		this.analysisMonitor = new MalaclypseAnalysisMonitor(id);		
	}
	public WiredArtifactContainerUpdateRequest(String id, ArtifactContainer container, ArtifactContainerUpdateRequestType walkMode, boolean updateEclipse, ClasspathResolverContract contract) {
		super();
		this.requestId = id;
		this.container = container;		
		this.walkMode = walkMode;
		this.updateEclipse = updateEclipse;
		this.classpathResolverContract = contract;
		this.analysisMonitor = new MalaclypseAnalysisMonitor( id);		
	}
	
	public WiredArtifactContainerUpdateRequest( WiredArtifactContainerUpdateRequest sibling) {
		super();		
		this.container = sibling.container;
		this.walkMode = sibling.walkMode;
		this.updateEclipse = sibling.updateEclipse;
		this.classpathResolverContract = sibling.classpathResolverContract;
		this.analysisMonitor = new MalaclypseAnalysisMonitor( sibling.getAnalysisMonitor());
		this.requestId = sibling.getRequestId();
	}
	
	public String getRequestId() {
		return requestId;
	}
	
	public ArtifactContainer getContainer() {
		return container;
	}
	public void setContainer(ArtifactContainer container) {
		this.container = container;
	}
	public ArtifactContainerUpdateRequestType getWalkMode() {
		return walkMode;
	}
	public void setWalkMode(ArtifactContainerUpdateRequestType walkMode) {
		this.walkMode = walkMode;
	}		
		
	public boolean getUpdateEclipse() {
		return updateEclipse;
	}
	public void setUpdateEclipse(boolean updateEclipse) {
		this.updateEclipse = updateEclipse;
	}
			
	public MalaclypseAnalysisMonitor getAnalysisMonitor() {
		return analysisMonitor;
	}
	public void setAnalysisMonitor(MalaclypseAnalysisMonitor analysisMonitor) {
		this.analysisMonitor = analysisMonitor;
	}
	public ClasspathResolverContract getClasspathResolverContract() {
		return classpathResolverContract;
	}
	public void setClasspathResolverContract(ClasspathResolverContract classpathResolverContract) {
		this.classpathResolverContract = classpathResolverContract;
	}
}
