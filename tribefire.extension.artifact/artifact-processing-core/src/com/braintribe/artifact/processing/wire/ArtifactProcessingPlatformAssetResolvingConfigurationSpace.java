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
package com.braintribe.artifact.processing.wire;

import com.braintribe.build.artifact.representations.artifact.maven.settings.LocalRepositoryLocationProvider;
import com.braintribe.build.artifact.representations.artifact.maven.settings.persistence.MavenSettingsPersistenceExpert;
import com.braintribe.build.artifact.representations.artifact.pom.listener.PomReaderNotificationListener;
import com.braintribe.build.artifacts.mc.wire.buildwalk.contract.MavenSettingsContract;
import com.braintribe.build.artifacts.mc.wire.buildwalk.contract.NotificationContract;
import com.braintribe.cfg.Configurable;
import com.braintribe.ve.api.VirtualEnvironment;

import tribefire.cortex.asset.resolving.wire.contract.PlatformAssetResolvingConfigurationContract;

public class ArtifactProcessingPlatformAssetResolvingConfigurationSpace implements PlatformAssetResolvingConfigurationContract, MavenSettingsContract, NotificationContract {
	
	private MavenSettingsPersistenceExpert persistenceExpert;
	private LocalRepositoryLocationProvider localRepositoryProvider;
	private VirtualEnvironment ve;
	private PomReaderNotificationListener pomNotificationListener;
	
	public ArtifactProcessingPlatformAssetResolvingConfigurationSpace() {		
	}

	public ArtifactProcessingPlatformAssetResolvingConfigurationSpace(MavenSettingsPersistenceExpert persistenceExpert, LocalRepositoryLocationProvider localRepositoryProvider, VirtualEnvironment ve, PomReaderNotificationListener pomNotificationListener) {
		this.persistenceExpert = persistenceExpert;
		this.localRepositoryProvider = localRepositoryProvider;
		this.ve = ve;
		this.pomNotificationListener = pomNotificationListener;
	}

	@Override
	public PomReaderNotificationListener pomReaderNotificationListener() {
		return pomNotificationListener;
	}
	
	@Configurable
	public void setPomNotificationListener(PomReaderNotificationListener pomNotificationListener) {
		this.pomNotificationListener = pomNotificationListener;
	}

	@Override
	public MavenSettingsPersistenceExpert settingsPersistenceExpert() {
		return persistenceExpert;
	}
	
	@Configurable
	public void setSettingsPersistenceExpert(MavenSettingsPersistenceExpert persistenceExpert) {
		this.persistenceExpert = persistenceExpert;
	}	

	@Override
	public LocalRepositoryLocationProvider localRepositoryLocationProvider() {
		return localRepositoryProvider;
	}
	
	@Configurable
	public void setLocalRepositoryProvider(LocalRepositoryLocationProvider localRepositoryProvider) {
		this.localRepositoryProvider = localRepositoryProvider;
	}

	@Override
	public VirtualEnvironment virtualEnvironment() {
		return ve;
	}
	
	@Configurable
	public void setVirtualEnvironment(VirtualEnvironment ve) {
		this.ve = ve;
	}
}
