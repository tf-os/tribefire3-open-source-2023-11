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

import java.util.List;

import com.braintribe.model.folder.Folder;
import com.braintribe.wire.api.space.WireSpace;

public interface WopiWbInitializerContract extends WireSpace {

	String WOPI_FOLDER = "WOPI";

	// WopiSession
	String WOPI_SESSIONS_FOLDER = "WOPI sessions";
	String OPEN_WOPI_SESSIONS = "Active";
	String EXPIRED_WOPI_SESSIONS = "Expired";
	String CLOSED_WOPI_SESSIONS = "Closed";
	String WOPI_SESSIONS_SEARCH = "Search";
	String WOPI_SESSION_STATISTICS = "Statistics";
	String WOPI_SESSION_OPEN = "Open";
	String WOPI_SESSION_CLOSE = "Close";
	String WOPI_SESSION_CLOSE_ALL = "Close All";
	String WOPI_SESSION_REMOVE = "Remove";
	String WOPI_SESSION_REMOVE_ALL = "Remove All";
	String WOPI_HEALTH_CHECK = "Health";

	// Demo
	String WOPI_DEMO_TESTING_FOLDER = "Demo/Testing Docs";
	String WOPI_DEMO_ADD = "Add";
	String WOPI_DEMO_REMOVE = "Remove";
	String WOPI_ENSURE_TES_DOC = "Ensure Test Doc";

	// Action
	String OPEN_WOPI_DOCUMENT = "Open WOPI document";
	String DOWNLOAD_CURRENT_RESOURCE = "Download Current Resource";
	String CLOSE_WOPI_SESSION = "Close WOPI Session";
	String REMOVE_WOPI_SESSION = "Remove WOPI Session";
	String EXPORT_WOPI_SESSION = "Export WOPI Session";

	Folder entryPointFolder();

	List<Folder> actionbarFolders();

	// -----------------------------------------------------------------------
	// ACTIONBAR
	// -----------------------------------------------------------------------

	Folder openWopiDocumentActionBar();
	Folder downloadCurrentResourceActionBar();
	Folder closeWopiSessionActionBar();
	Folder removeWopiSessionActionBar();
	Folder exportWopiSessionActionBar();

	// -----------------------------------------------------------------------
	// ADMINISTRATION
	// -----------------------------------------------------------------------

	Folder openWopiSessions();
	Folder expiredWopiSessions();
	Folder closedWopiSessions();
	Folder wopiSessionSearch();
	Folder wopiSessionStatistics();
	Folder openWopiSession();
	Folder closeAllWopiSessions();
	Folder removeAllWopiSessions();
	Folder wopiHealthCheck();

	// -----------------------------------------------------------------------
	// DEMO/TESTING
	// -----------------------------------------------------------------------

	Folder addDemoDocs();
	Folder removeDemoDocs();
	Folder ensureTestDoc();
}
