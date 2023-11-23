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
package com.braintribe.gwt.gm.storage.impl.wb.form.setting;

import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.gme.workbench.client.Workbench;
import com.braintribe.model.folder.Folder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

public class WbSettingQueryDialogConfig {
	private Folder queryFolder = null;
	private Workbench workbench = null;
	private PersistenceGmSession workbenchSession = null;
	private Future<WbSettingQueryDialogResult> dialogResult = null;

	public Folder getQueryFolder() {
		return this.queryFolder;
	}

	public void setQueryFolder(final Folder queryFolder) {
		this.queryFolder = queryFolder;
	}

	public PersistenceGmSession getWorkbenchSession() {
		return this.workbenchSession;
	}

	public void setWorkbenchSession(final PersistenceGmSession workbenchSession) {
		this.workbenchSession = workbenchSession;
	}

	public Workbench getWorkbench() {
		return this.workbench;
	}

	public void setWorkbench(final Workbench workbench) {
		this.workbench = workbench;
	}

	public Future<WbSettingQueryDialogResult> getDialogResult() {
		return this.dialogResult;
	}

	public void setDialogResult(final Future<WbSettingQueryDialogResult> dialogResult) {
		this.dialogResult = dialogResult;
	}
}
