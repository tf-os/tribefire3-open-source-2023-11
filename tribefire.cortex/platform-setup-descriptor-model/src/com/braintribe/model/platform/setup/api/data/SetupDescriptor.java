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
package com.braintribe.model.platform.setup.api.data;

import java.util.Date;
import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.platform.setup.api.ProjectDescriptor;

@Description("This descriptor hosts setup information and is persisted in <code>installationPath/setup-info/setup-descriptor.yml</code>.")
public interface SetupDescriptor extends GenericEntity {

	EntityType<SetupDescriptor> T = EntityTypes.T(SetupDescriptor.class);

	@Description("The setup execution timestamp as date.")
	Date getSetupDate();
	void setSetupDate(Date setupDate);

	@Description("The user who executed the setup call.")
	String getSetupBy();
	void setSetupBy(String setupBy);

	@Description("The hostname on which the setup has run.")
	String getSetupHost();
	void setSetupHost(String setupHost);

	@Description("The project descriptor describing information like the (display) name of a setup.")
	ProjectDescriptor getProjectDescriptor();
	void setProjectDescriptor(ProjectDescriptor projectDescriptor);

	@Description("The full qualified version of the setup taken from resolved primarySetupAsset.")
	String getVersion();
	void setVersion(String version);

	@Description("The primary setup dependency is the one that is handed over via <code>--setupDependency</code>. "
			+ "The version is taken as is, so it may be full qualified or ranged.")
	String getPrimarySetupDependency();
	void setPrimarySetupDependency(String primarySetupDependency);

	@Description("Represents the resolved <code>primarySetupDependency</code> with a full qualified version.")
	String getPrimarySetupAsset();
	void setPrimarySetupAsset(String primarySetupAsset);

	@Description("Further setup dependencies are listed here, including <code>primarySetupDependency</code>. "
			+ "The versions are taken as is, so they may be full qualified or ranged.")
	List<String> getSetupDependencies();
	void setSetupDependencies(List<String> setupDependencies);

	@Description("Represents the resolved <code>setupDependencies</code> with a full qualified version.")
	List<String> getSetupAssets();
	void setSetupAssets(List<String> setupAssets);

	@Description("The list of all resolved asses within the setup")
	List<String> getAssets();
	void setAssets(List<String> assets);
	
	@Description("The major.minor version of the license asset used in the setup.")
	String getLicenseAssetMajorMinorVersion();
	void setLicenseAssetMajorMinorVersion(String licenseAssetMajorMinorVersion);

}
