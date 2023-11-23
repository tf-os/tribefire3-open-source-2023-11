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
package tribefire.extension.appconfiguration.model.api;

import com.braintribe.model.accessapi.AccessRequest;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.service.api.ServiceRequest;

/**
 * Imports {@link tribefire.extension.appconfiguration.model.AppConfiguration#getLocalizations() localizations} from a spreadsheet.
 */
public interface ImportLocalizationsFromSpreadsheet extends AccessRequest {

	EntityType<ImportLocalizationsFromSpreadsheet> T = EntityTypes.T(ImportLocalizationsFromSpreadsheet.class);

	/** The {@link tribefire.extension.appconfiguration.model.AppConfiguration#getName() name} of the configuration to export. */
	String getAppConfigurationName();
	void setAppConfigurationName(String appConfigurationName);

	/** The spreasheet resource from which to import. */
	Resource getSpreadsheet();
	void setSpreadsheet(Resource spreadsheet);

	/**
	 * The import mode, i.e. whether to {@link LocalizationsImportMode#update update} existing localizations or completely
	 * {@link LocalizationsImportMode#reset reset} them.
	 */
	@Initializer("update")
	LocalizationsImportMode getMode();
	void setMode(LocalizationsImportMode mode);

	@Override
	EvalContext<? extends LocalizationsImportReport> eval(Evaluator<ServiceRequest> evaluator);
}
