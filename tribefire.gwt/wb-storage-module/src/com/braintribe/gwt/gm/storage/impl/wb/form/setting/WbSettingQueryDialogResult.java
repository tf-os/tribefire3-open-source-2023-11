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

import com.braintribe.model.folder.Folder;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.resource.Icon;

public class WbSettingQueryDialogResult {
	private Folder parentFolder = null;
	private LocalizedString folderName = null;
	private Icon icon = null;
	private TraversingCriterion context = null;
	private Boolean multiSelection = null;
	private boolean forceForm = false;
	private Integer autoPagingSize;
	private String defaultView;

	public Folder getParentFolder() {
		return this.parentFolder;
	}

	public void setParentFolder(final Folder parentFolder) {
		this.parentFolder = parentFolder;
	}

	public LocalizedString getFolderName() {
		return this.folderName;
	}

	public void setFolderName(final LocalizedString folderName) {
		this.folderName = folderName;
	}

	public Icon getIcon() {
		return this.icon;
	}

	public void setIcon(final Icon icon) {
		this.icon = icon;
	}

	public TraversingCriterion getContext() {
		return this.context;
	}

	public void setContext(final TraversingCriterion context) {
		this.context = context;
	}

	public boolean getMultiSelection() {
		return this.multiSelection;
	}

	public void setMultiSelection(final boolean multiSelection) {
		this.multiSelection = multiSelection;
	}
	
	public boolean getForceForm() {
		return forceForm;
	}
	
	public void setForceForm(boolean forceForm) {
		this.forceForm = forceForm;
	}
	
	public Integer getAutoPagingSize() {
		return autoPagingSize;
	}
	
	public void setAutoPagingSize(Integer autoPagingSize) {
		this.autoPagingSize = autoPagingSize;
	}

	public String getDefaultView() {
		return this.defaultView;
	}

	public void setDefaultView(final String defaultView) {
		this.defaultView = defaultView;
	}
}
