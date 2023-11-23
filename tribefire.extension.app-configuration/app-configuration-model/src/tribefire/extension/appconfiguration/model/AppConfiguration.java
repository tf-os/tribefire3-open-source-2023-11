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
package tribefire.extension.appconfiguration.model;

import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.annotation.meta.Unique;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

@SelectiveInformation("App Configuration ${name}")
public interface AppConfiguration extends GenericEntity {

	EntityType<AppConfiguration> T = EntityTypes.T(AppConfiguration.class);

	String name = "name";
	String localizations = "localizations";
	String themes = "themes";
	String descriptors = "descriptors";
	String defaults = "defaults";

	@Unique
	@Name("Name")
	String getName();
	void setName(String name);

	@Name("Localizations")
	List<AppLocalization> getLocalizations();
	void setLocalizations(List<AppLocalization> localizations);

	@Name("Themes")
	List<AppTheme> getThemes();
	void setThemes(List<AppTheme> themes);

	@Name("Descriptors")
	List<AppDescriptor> getDescriptors();
	void setDescriptors(List<AppDescriptor> descriptor);

	@Name("Defaults")
	List<AppDefault> getDefaults();
	void setDefaults(List<AppDefault> defaults);

}
