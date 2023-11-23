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
package edu.ycp.cs.dh.acegwt.client.ace;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

/**
 * Implementation of {@link AceCompletionCallback}
 * that delegates to a native JavaScript Ace code completion
 * callback.
 */
class AceCompletionCallbackImpl implements AceCompletionCallback {
	private JavaScriptObject jsCallback;
	
	public AceCompletionCallbackImpl(JavaScriptObject jsCallback) {
		this.jsCallback = jsCallback;
	}
	
	@Override
	public void invokeWithCompletions(AceCompletion[] proposals) {
		JsArray<JavaScriptObject> jsProposals = JavaScriptObject.createArray().cast();
		for (AceCompletion proposal : proposals) {
			jsProposals.push(proposal.toJsObject());
		}
		doInvokeWithCompletions(jsProposals);
	}
	
	private native void doInvokeWithCompletions(JsArray<JavaScriptObject> jsProposals) /*-{
		var callback = this.@edu.ycp.cs.dh.acegwt.client.ace.AceCompletionCallbackImpl::jsCallback;
		callback(null, jsProposals);
	}-*/;
}
