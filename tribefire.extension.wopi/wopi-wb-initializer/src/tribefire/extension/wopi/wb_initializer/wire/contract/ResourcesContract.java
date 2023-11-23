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

import com.braintribe.model.resource.Resource;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.cortex.initializer.support.impl.lookup.GlobalId;
import tribefire.cortex.initializer.support.impl.lookup.InstanceLookup;

@InstanceLookup(lookupOnly = true, globalIdPrefix = ResourcesContract.GLOBAL_ID_PREFIX)
public interface ResourcesContract extends WireSpace {

	String RESOURCE_ASSET_NAME = "tribefire.extension.wopi:wopi-wb-resources";
	String GLOBAL_ID_PREFIX = "asset-resource://" + RESOURCE_ASSET_NAME + "/";

	@GlobalId("explorer-style-static-extension.wopi.css")
	Resource extensionWopiExplorerCss();

	@GlobalId("logo.png")
	Resource logoPng();

	@GlobalId("favicon-blue.ico")
	Resource favIconBlue();
	@GlobalId("favicon-orange.ico")
	Resource favIconOrange();

	@GlobalId("tribefire-16.png")
	Resource tribefire16Png();

	@GlobalId("wopi-16.png")
	Resource wopi16Png();
	@GlobalId("wopi-32.png")
	Resource wopi32Png();
	@GlobalId("wopi-64.png")
	Resource wopi64Png();

	@GlobalId("magnifier-16.png")
	Resource magnifier16Png();
	@GlobalId("magnifier-32.png")
	Resource magnifier32Png();
	@GlobalId("magnifier-64.png")
	Resource magnifier64Png();

	@GlobalId("connection-16.png")
	Resource connection16Png();
	@GlobalId("connection-32.png")
	Resource connection32Png();
	@GlobalId("connection-64.png")
	Resource connection64Png();

	@GlobalId("office-16.png")
	Resource office16Png();
	@GlobalId("office-32.png")
	Resource office32Png();
	@GlobalId("office-64.png")
	Resource office64Png();

	@GlobalId("adx-16.png")
	Resource adx16Png();
	@GlobalId("adx-32.png")
	Resource adx32Png();
	@GlobalId("adx-64.png")
	Resource adx64Png();

	@GlobalId("session-expired-16.png")
	Resource sessionExpired16Png();
	@GlobalId("session-expired-32.png")
	Resource sessionExpired32Png();
	@GlobalId("session-expired-64.png")
	Resource sessionExpired64Png();

	@GlobalId("add-demo-docs-16.png")
	Resource addDemoDocs16Png();
	@GlobalId("add-demo-docs-32.png")
	Resource addDemoDocs32Png();
	@GlobalId("add-demo-docs-64.png")
	Resource addDemoDocs64Png();

	@GlobalId("word-16.png")
	Resource word16Png();

	@GlobalId("excel-16.png")
	Resource excel16Png();

	@GlobalId("powerpoint-16.png")
	Resource powerpoint16Png();

	// -----------------------------------------------------------------------
	// STANDARD ACTIONS ICONS
	// -----------------------------------------------------------------------

	@GlobalId("view-16.png")
	Resource view16Png();
	@GlobalId("view-32.png")
	Resource view32Png();
	@GlobalId("view-64.png")
	Resource view64Png();

	@GlobalId("open-16.png")
	Resource open16Png();
	@GlobalId("open-32.png")
	Resource open32Png();
	@GlobalId("open-64.png")
	Resource open64Png();

	@GlobalId("edit-16.png")
	Resource edit16Png();
	@GlobalId("edit-32.png")
	Resource edit32Png();
	@GlobalId("edit-64.png")
	Resource edit64Png();

	@GlobalId("delete-16.png")
	Resource delete16Png();
	@GlobalId("delete-32.png")
	Resource delete32Png();
	@GlobalId("delete-64.png")
	Resource delete64Png();

	@GlobalId("assign-16.png")
	Resource assign16Png();
	@GlobalId("assign-32.png")
	Resource assign32Png();
	@GlobalId("assign-64.png")
	Resource assign64Png();

	@GlobalId("add-16.png")
	Resource add16Png();
	@GlobalId("add-32.png")
	Resource add32Png();
	@GlobalId("add-64.png")
	Resource add64Png();

	@GlobalId("insert-16.png")
	Resource insert16Png();
	@GlobalId("insert-32.png")
	Resource insert32Png();
	@GlobalId("insert-64.png")
	Resource insert64Png();

	@GlobalId("remove-16.png")
	Resource remove16Png();
	@GlobalId("remove-32.png")
	Resource remove32Png();
	@GlobalId("remove-64.png")
	Resource remove64Png();

	@GlobalId("refresh-16.png")
	Resource refresh16Png();
	@GlobalId("refresh-32.png")
	Resource refresh32Png();
	@GlobalId("refresh-64.png")
	Resource refresh64Png();

	@GlobalId("download-16.png")
	Resource download16Png();
	@GlobalId("download-32.png")
	Resource download32Png();
	@GlobalId("download-64.png")
	Resource download64Png();

	@GlobalId("run-16.png")
	Resource run16Png();
	@GlobalId("run-32.png")
	Resource run32Png();
	@GlobalId("run-64.png")
	Resource run64Png();

	@GlobalId("home-16.png")
	Resource home16Png();
	@GlobalId("home-32.png")
	Resource home32Png();
	@GlobalId("home-64.png")
	Resource home64Png();

	@GlobalId("mail-16.png")
	Resource mail16Png();
	@GlobalId("mail-32.png")
	Resource mail32Png();
	@GlobalId("mail-64.png")
	Resource mail64Png();

	@GlobalId("copy-16.png")
	Resource copy16Png();
	@GlobalId("copy-32.png")
	Resource copy32Png();
	@GlobalId("copy-64.png")
	Resource copy64Png();

	@GlobalId("changes-16.png")
	Resource changes16Png();
	@GlobalId("changes-32.png")
	Resource changes32Png();
	@GlobalId("changes-64.png")
	Resource changes64Png();

	@GlobalId("upload-16.png")
	Resource upload16Png();
	@GlobalId("upload-32.png")
	Resource upload32Png();
	@GlobalId("upload-64.png")
	Resource upload64Png();

	@GlobalId("back-16.png")
	Resource back16Png();
	@GlobalId("back-32.png")
	Resource back32Png();
	@GlobalId("back-64.png")
	Resource back64Png();

	@GlobalId("next-16.png")
	Resource next16Png();
	@GlobalId("next-32.png")
	Resource next32Png();
	@GlobalId("next-64.png")
	Resource next64Png();

	@GlobalId("commit-16.png")
	Resource commit16Png();
	@GlobalId("commit-32.png")
	Resource commit32Png();
	@GlobalId("commit-64.png")
	Resource commit64Png();

	@GlobalId("explore-16.png")
	Resource explore16Png();
	@GlobalId("explore-32.png")
	Resource explore32Png();
	@GlobalId("explore-64.png")
	Resource explore64Png();

	@GlobalId("more-16.png")
	Resource more16Png();
	@GlobalId("more-32.png")
	Resource more32Png();
	@GlobalId("more-64.png")
	Resource more64Png();

	@GlobalId("expand-16.png")
	Resource expand16Png();
	@GlobalId("expand-32.png")
	Resource expand32Png();
	@GlobalId("expand-64.png")
	Resource expand64Png();

	@GlobalId("file-16.png")
	Resource file16Png();
	@GlobalId("file-32.png")
	Resource file32Png();
	@GlobalId("file-64.png")
	Resource file64Png();

	@GlobalId("event-16.png")
	Resource event16Png();
	@GlobalId("event-32.png")
	Resource event32Png();
	@GlobalId("event-64.png")
	Resource event64Png();

	@GlobalId("settings-16.png")
	Resource settings16Png();
	@GlobalId("settings-32.png")
	Resource settings32Png();
	@GlobalId("settings-64.png")
	Resource settings64Png();

	@GlobalId("info-16.png")
	Resource info16Png();
	@GlobalId("info-32.png")
	Resource info32Png();
	@GlobalId("info-64.png")
	Resource info64Png();

	@GlobalId("picture-16.png")
	Resource picture16Png();
	@GlobalId("picture-32.png")
	Resource picture32Png();
	@GlobalId("picture-64.png")
	Resource picture64Png();

	@GlobalId("code-16.png")
	Resource code16Png();
	@GlobalId("code-32.png")
	Resource code32Png();
	@GlobalId("code-64.png")
	Resource code64Png();

	@GlobalId("health-16.png")
	Resource health16Png();
	@GlobalId("health-32.png")
	Resource health32Png();
	@GlobalId("health-64.png")
	Resource health64Png();

	@GlobalId("active-16.png")
	Resource active16Png();
	@GlobalId("active-32.png")
	Resource active32Png();
	@GlobalId("active-64.png")
	Resource active64Png();

	@GlobalId("inactive-16.png")
	Resource inactive16Png();
	@GlobalId("inactive-32.png")
	Resource inactive32Png();
	@GlobalId("inactive-64.png")
	Resource inactive64Png();

	@GlobalId("statistics-16.png")
	Resource statistics16Png();
	@GlobalId("statistics-32.png")
	Resource statistics32Png();
	@GlobalId("statistics-64.png")
	Resource statistics64Png();

	@GlobalId("session-open-16.png")
	Resource sessionOpen16Png();
	@GlobalId("session-open-32.png")
	Resource sessionOpen32Png();
	@GlobalId("session-open-64.png")
	Resource sessionOpen64Png();

	@GlobalId("session-closed-16.png")
	Resource sessionClosed16Png();
	@GlobalId("session-closed-32.png")
	Resource sessionClosed32Png();
	@GlobalId("session-closed-64.png")
	Resource sessionClosed64Png();

	@GlobalId("documentation-16.png")
	Resource documentation16Png();
	@GlobalId("documentation-32.png")
	Resource documentation32Png();
	@GlobalId("documentation-64.png")
	Resource documentation64Png();

	@GlobalId("academy-16.png")
	Resource academy16Png();
	@GlobalId("academy-32.png")
	Resource academy32Png();
	@GlobalId("academy-64.png")
	Resource academy64Png();

	@GlobalId("migration-tool-16.png")
	Resource migrationTool16Png();
	@GlobalId("migration-tool-32.png")
	Resource migrationTool32Png();
	@GlobalId("migration-tool-64.png")
	Resource migrationTool64Png();

	@GlobalId("public-key-16.png")
	Resource publicKey16Png();
	@GlobalId("public-key-32.png")
	Resource publicKey32Png();
	@GlobalId("public-key-64.png")
	Resource publicKey64Png();

	@GlobalId("repository-template-16.png")
	Resource repositoryTemplate16Png();
	@GlobalId("repository-template-32.png")
	Resource repositoryTemplate32Png();
	@GlobalId("repository-template-64.png")
	Resource repositoryTemplate64Png();

	@GlobalId("cmis-16.png")
	Resource cmis16Png();
	@GlobalId("cmis-32.png")
	Resource cmis32Png();
	@GlobalId("cmis-64.png")
	Resource cmis64Png();

	@GlobalId("dctm-16.png")
	Resource dctm16Png();
	@GlobalId("dctm-32.png")
	Resource dctm32Png();
	@GlobalId("dctm-64.png")
	Resource dctm64Png();

	@GlobalId("oracle-16.png")
	Resource oracle16Png();
	@GlobalId("oracle-32.png")
	Resource oracle32Png();
	@GlobalId("oracle-64.png")
	Resource oracle64Png();

	@GlobalId("mssql-16.png")
	Resource mssql16Png();
	@GlobalId("mssql-32.png")
	Resource mssql32Png();
	@GlobalId("mssql-64.png")
	Resource mssql64Png();

	@GlobalId("postgresql-16.png")
	Resource postgresql16Png();
	@GlobalId("postgresql-32.png")
	Resource postgresql32Png();
	@GlobalId("postgresql-64.png")
	Resource postgresql64Png();

	@GlobalId("mysql-16.png")
	Resource mysql16Png();
	@GlobalId("mysql-32.png")
	Resource mysql32Png();
	@GlobalId("mysql-64.png")
	Resource mysql64Png();

	@GlobalId("db2-16.png")
	Resource db216Png();
	@GlobalId("db2-32.png")
	Resource db232Png();
	@GlobalId("db2-64.png")
	Resource db264Png();

	@GlobalId("fs-16.png")
	Resource fs16Png();
	@GlobalId("fs-32.png")
	Resource fs32Png();
	@GlobalId("fs-64.png")
	Resource fs64Png();

	@GlobalId("db-16.png")
	Resource db16Png();
	@GlobalId("db-32.png")
	Resource db32Png();
	@GlobalId("db-64.png")
	Resource db64Png();

	@GlobalId("elasticsearch-16.png")
	Resource elasticsearch16Png();
	@GlobalId("elasticsearch-32.png")
	Resource elasticsearch32Png();
	@GlobalId("elasticsearch-64.png")
	Resource elasticsearch64Png();

	@GlobalId("policy-16.png")
	Resource policy16Png();
	@GlobalId("policy-32.png")
	Resource policy32Png();
	@GlobalId("policy-64.png")
	Resource policy64Png();

	@GlobalId("schedule-16.png")
	Resource schedule16Png();
	@GlobalId("schedule-32.png")
	Resource schedule32Png();
	@GlobalId("schedule-64.png")
	Resource schedule64Png();

	@GlobalId("exclamation-mark-16.png")
	Resource exclamationMark16Png();
	@GlobalId("exclamation-mark-32.png")
	Resource exclamationMark32Png();
	@GlobalId("exclamation-mark-64.png")
	Resource exclamationMark64Png();

	@GlobalId("quickAccess-16.png")
	Resource quickAccess16Png();

}
