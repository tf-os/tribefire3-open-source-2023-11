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
package com.braintribe.gwt.gxt.gxtresources.text;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Messages;

public interface LocalizedText extends Messages {
	
	public static LocalizedText INSTANCE = GWT.create(LocalizedText.class);

	String addMetaData();
	String apply();
	String applyDescription();
	String assign();
	String assignProperty(String propertyName);
	String authenticationFailed();
	String changeColumn();
	String cancel();
	String clear();	
	String close();
	String columns();
	String comment();
	String commentEmptyText();	
	String constants();
	String currentSessionContext();		
	String dataWillBeLost();
	String details();
	String dateTimeFormat();
	String deleteComment();
	String deleteCommentConfirm();
	String deleteCommentThreadConfirm();
	String dependencies();		
	String editComment();
	String enterTextToFilter();
	String errorAddingEntries();
	String errorRollingBack();
	String errorRuntime();
	String errorSigningIn();
	String errorSigningOut();
	String declaredAt();
	String declaredInModel();
	String declaredPropertyOverview();
	String declaredOverview();
	String declaredTypesOnly();
	String declaredPropertiesOnly();
	String designer();
	String displayAccess();
	String displayRole();
	String displayUseCase();
	String effectiveOverview();
	String falseEntry();
	String hide();
	String hideAllColumns();	
	String informationOverview();
	String inherited();
	String invalidDate(String pattern);
	String invalidTime();
	String invalidTimeParam(String pattern);
	String isInherited();
	String loading();
	String labelDeclaredEntity();
	String labelDeclaredModel();
	String labelName();
	String labelValue();
	String labelSelector();
	String labelPriority();
	String language();
	String listView();
	String loadingUser();
	String log();		
	String metadataView();
	String multiLineEditor();
	String next();
	String noItemsToDisplay();
	String noPackagingInfo();
	String noProperty();	
	String ok();
	String ownerType();
	String others();
	String packagingInfo();
	String packagingInfoDescription();
	String packagingInfoFor(String artifact);	
	String password();
	String platformAssetManagement();
	String previous();
	String profile();
	String properties();
	String reader();
	String redoTransient();
	String refreshing();
	String refreshMetaData();
	String removeMetaData();
	String reload();
	String reloadCurrentSession();
	String resolvedAsDefault();
	String resolutionView();
	String search();
	String searching();
	String select(String type);
	String selectFilterType();
	String selectView();
	String sessionLocked();
	String sessionLockedMessage();
	String sessionNotCreated();
	String sessionTimeout();
	String sessionTimeoutMessage();
	String showAllColumns();
	String showConfiguredColumns();
	String showColumns();
	String showLog();
	String signIn();
	String signInDescription();
	String signingIn();
	String signingOut();
	String signOut();
	String signoutConfirmation();
	String source();
	String thumbnailView();
	String time();
	String trueEntry();
	String types();
	String typeToShowValues();
	String undoTransient();
	String upload();
	String userHasNoUIAccess(String userName);
	String userName();
	String userNotAvailable();
	String youAreCurrentlyEditing();	
}

