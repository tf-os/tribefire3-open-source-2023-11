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

/**
 * Represents an Ace Range Object.
 */
public final class AceRange extends JavaScriptObject {
	
	protected AceRange() {
	}
	
	public static AceRange create(int startRow, int startColumn, int endRow, int endColumn) {
		return toJsObject(startRow, startColumn, endRow, endColumn).cast();
	}

	/**
	 * Detaches both, start and end from this {@link AceRange}.
	 */
	public void detach() {
		detachStart();
		detachEnd();
	}
	
	/**
	 * Detaches the start anchor from this {@link AceRange}.
	 */
	public native void detachStart() /*-{
		if (typeof this.start != 'undefined' && typeof this.start != 'object') {
			this.start.detach();
		}
	}-*/;

	/**
	 * Detaches the end achor from this {@link AceRange}.
	 */
	public native void detachEnd() /*-{
		if (typeof this.end != 'undefined' && typeof this.end != 'object') {
			this.end.detach();
		}
	}-*/;
	
	/**
	 * @return creates a new Range object.
	 */
	static native JavaScriptObject toJsObject(int startRow, int startColumn, int endRow, int endColumn) /*-{
		var Range = $wnd.require('ace/range').Range;
		var range = new Range(startRow, startColumn, endRow, endColumn);
		return range;
	}-*/;
}
