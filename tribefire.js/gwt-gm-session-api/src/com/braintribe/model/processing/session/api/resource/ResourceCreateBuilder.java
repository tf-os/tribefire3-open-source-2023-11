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
package com.braintribe.model.processing.session.api.resource;

import java.util.List;

import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.fileapi.client.Blob;
import com.braintribe.gwt.fileapi.client.File;
import com.braintribe.gwt.fileapi.client.FileList;
import com.braintribe.model.generic.GmCoreApiInteropNamespaces;
import com.braintribe.model.resource.Resource;
import com.braintribe.processing.async.api.JsPromise;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;

/**
 * Builder for creating {@link Resource}(s) and storing their binary data.
 * 
 * @author dirk.scheffler
 */
@JsType(namespace = GmCoreApiInteropNamespaces.resources)
@SuppressWarnings("unusable-by-js")
public interface ResourceCreateBuilder extends AbstractResourceBuilder<ResourceCreateBuilder> {

	@JsMethod(name = "storeFileList")
	JsPromise<List<Resource>> store(FileList fileList);

	@JsMethod(name = "storeFiles")
	JsPromise<List<Resource>> store(File[] files);

	@JsMethod(name = "storeFile")
	JsPromise<List<Resource>> store(File file);

	@JsMethod(name = "storeBlob")
	JsPromise<List<Resource>> store(Blob blob);

	@JsMethod(name = "storeText")
	JsPromise<List<Resource>> store(String text);

	@JsIgnore
	Future<List<Resource>> store(List<File> files);

}
