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
package tribefire.extension.wopi.wb_initializer.wire.contract;

import com.braintribe.model.resource.Icon;
import com.braintribe.wire.api.space.WireSpace;

/**
 * <p>
 * For structural reasons icons get their own contract.
 * 
 */
public interface IconContract extends WireSpace {

	Icon logoIcon();

	Icon magnifierIcon();

	Icon tribefireIcon();

	Icon officeIcon();

	Icon adxIcon();

	Icon sessionExpiredIcon();

	Icon addDemoAndTestingDocsIcon();

	Icon wordIcon();
	Icon excelIcon();
	Icon powerpointIcon();

	// -----------------------------------------------------------------------
	// STANDARD ACTION ICONS
	// -----------------------------------------------------------------------

	Icon viewIcon();
	Icon openIcon();
	Icon editIcon();
	Icon deleteIcon();
	Icon assignIcon();
	Icon addIcon();
	Icon removeIcon();
	Icon refreshIcon();
	Icon downloadIcon();
	Icon runIcon();
	Icon homeIcon();
	Icon mailIcon();
	Icon copyIcon();
	Icon changesIcon();
	Icon uploadIcon();
	Icon backIcon();
	Icon nextIcon();
	Icon commitIcon();
	Icon expandIcon();
	Icon fileIcon();
	Icon eventIcon();
	Icon settingsIcon();
	Icon infoIcon();
	Icon pictureIcon();
	Icon codeIcon();
	Icon healthIcon();
	Icon activeIcon();
	Icon inactiveIcon();
	Icon statisticsIcon();
	Icon sessionOpenIcon();
	Icon sessionClosedIcon();
	Icon repositoryTemplateIcon();
	Icon documentationIcon();
	Icon academyIcon();
	Icon publicKeyIcon();
	Icon migrationToolIcon();

	Icon quickAccessIcon();

}
