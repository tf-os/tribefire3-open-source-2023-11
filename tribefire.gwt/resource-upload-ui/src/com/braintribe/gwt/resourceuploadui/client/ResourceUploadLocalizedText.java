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
package com.braintribe.gwt.resourceuploadui.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Messages;

public interface ResourceUploadLocalizedText extends Messages {
	
	public static ResourceUploadLocalizedText INSTANCE = GWT.create(ResourceUploadLocalizedText.class);
	
	String abortUpload();
	String cancel();
	String close();
	String dropMessage();
	String dropTarget();
	String maxAmountOfFiles(int filesMax, int filesSelected);
	String noActionAvailableForHandlingFile();
	String noActionAvailableForHandlingMultipleFiles();
	String open();
	String progressText(String test);
	String resourceUploaded(int size);
	String uploadAborted();
	String uploadHeader();
	String uploadingFiles(int size);

}
