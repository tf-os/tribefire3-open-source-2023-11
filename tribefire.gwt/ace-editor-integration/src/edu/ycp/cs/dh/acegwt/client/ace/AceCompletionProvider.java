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

/**
 * A provider of custom code-completion proposals.
 * 
 * <strong>Warning</strong>: this is an experimental feature of AceGWT.
 * It is possible that the API will change in an incompatible way
 * in future releases.
 */
public interface AceCompletionProvider {
	/**
	 * Call to get code completion proposals, which are delivered
	 * to a callback. Note that it is fine for this method to
	 * invoke the callback at a later time (for example, on completion
	 * of RPC.) 
	 * 
	 * @param editor   the {@link AceEditor}
	 * @param pos      the cursor position
	 * @param prefix   the word prefix
	 * @param callback the {@link AceCompletionCallback} to which the
	 *                 proposals should be delivered
	 */
	public void getProposals(AceEditor editor, AceEditorCursorPosition pos, String prefix, AceCompletionCallback callback);
}
