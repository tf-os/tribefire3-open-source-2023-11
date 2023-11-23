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
package com.braintribe.gwt.genericmodel.client.itw;

import com.braintribe.codec.CodecException;
import com.braintribe.gwt.genericmodel.client.codec.jse.JseCodec;
import com.braintribe.gwt.genericmodel.client.codec.jse.JseScriptFunctions;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GmCoreApiInteropNamespaces;
import com.braintribe.model.meta.GmMetaModel;
import com.google.gwt.core.client.JavaScriptObject;

import jsinterop.annotations.JsMethod;

/**
 * @author peter.gazdik
 */
public class TfJsItwTools {

	/**
	 * Expects a function compatible with the {@link JseCodec}, i.e. one that takes ("$", "P", "_") where:
	 * <ul>
	 * <li>$ comes from {@link JseScriptFunctions#create()}
	 * <li>P will be an empty instance - it contains types and properties and was introduced to support fragmentation - processing first fragment
	 * would put the values into P, next fragments can read from it.
	 * <li>_ - something with proxy
	 * </ul>
	 */
	@JsMethod(namespace = GmCoreApiInteropNamespaces.internal)
	public static void ensureModel(JavaScriptObject modelAssembler) throws CodecException {
		GmMetaModel model = JseCodec.decodeFunction(modelAssembler);

		GMF.getTypeReflection().deploy(model);
	}

}
