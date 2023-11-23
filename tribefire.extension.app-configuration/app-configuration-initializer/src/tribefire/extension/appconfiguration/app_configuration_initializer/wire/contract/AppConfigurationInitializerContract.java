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
package tribefire.extension.appconfiguration.app_configuration_initializer.wire.contract;

import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.extensiondeployment.meta.ProcessWith;
import com.braintribe.model.meta.data.prompt.Hidden;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.extension.appconfiguration.model.deployment.AppConfigurationProcessor;
import tribefire.extension.appconfiguration.model.deployment.ExportLocalizationsToSpreadsheetProcessor;
import tribefire.extension.appconfiguration.model.deployment.ImportLocalizationsFromSpreadsheetProcessor;
import tribefire.extension.js.model.deployment.ViewWithJsUxComponent;

public interface AppConfigurationInitializerContract extends WireSpace {

	IncrementalAccess appConfigurationAccess();

	AppConfigurationProcessor appConfigurationProcessor();

	ProcessWith processWithAppConfigurationProcessor();

	ImportLocalizationsFromSpreadsheetProcessor importLocalizationsFromSpreadsheetProcessor();

	ProcessWith processWithImportLocalizationsFromSpreadsheetProcessor();

	ExportLocalizationsToSpreadsheetProcessor exportLocalizationsToSpreadsheetProcessor();

	ProcessWith processWithExportLocalizationsToSpreadsheetProcessor();

	Hidden hiddenForNonAdminAndApi();

	Hidden hiddenForNonAdminAndGme();

	ViewWithJsUxComponent viewWithJsUxComponent();
}
