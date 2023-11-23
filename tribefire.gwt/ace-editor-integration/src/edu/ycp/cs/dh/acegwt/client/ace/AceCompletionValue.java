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
 * A completion proposed by an {@link AceCompletionProvider}. 
 * 
 * <strong>Warning</strong>: this is an experimental feature of AceGWT.
 * It is possible that the API will change in an incompatible way
 * in future releases.
 */
public class AceCompletionValue extends AceCompletion {
	
	/**
	 * The caption of the completion (this is the left aligned autocompletion name on the left side of items in the dropdown box. If only a single completion is available in a context, then the caption will not be seen.
	 */
	private final String caption;
	
	/**
	 * The text value of the completion. This does not need to be escaped.
	 */
	private final String value;
	
	
	/**
	 * "meta" means the category of the substitution (this appears right aligned on the dropdown list). This is freeform description and can contain anything but typically a very short category description (9 chars or less) such as "function" or "param" or "template".
	 */
	private final String meta;

	/**
	 * The score is the value assigned to the autocompletion option. Scores with a higher value will appear closer to the top. Items with an identical score are sorted alphbetically by caption in the drop down.
	 */
	private final int score;
	
	/**
	 * The score is the value assigned to the autocompletion option. Scores with a higher value will appear closer to the top. Items with an identical score are sorted alphbetically by caption in the drop down.
	 */
	private final String tooltip;
	
	/**
	 * Constructor. 
	 * 
	 * @param name The caption of the completion (this is the left aligned autocompletion name on the left side of items in the dropdown box. If only a single completion is available in a context, then the caption will not be seen.
	 * @param value  The text value of the completion. This does not need to be escaped.
	 * @param meta "meta" means the category of the substitution (this appears right aligned on the dropdown list). This is freeform description and can contain anything but typically a very short category description (9 chars or less) such as "function" or "param" or "template".
	 * @param score  The score is the value assigned to the autocompletion option. Scores with a higher value will appear closer to the top. Items with an identical score are sorted alphbetically by caption in the drop down.
	 */
	public AceCompletionValue(String name, String value, String meta, int score) {
		this.caption = name;
		this.value = value;
		this.meta = meta;
		this.tooltip = null;
		this.score = score;
	}
	
	/**
	 * Constructor. 
	 * 
	 * @param name The caption of the completion (this is the left aligned autocompletion name on the left side of items in the dropdown box. If only a single completion is available in a context, then the caption will not be seen.
	 * @param value  The text value of the completion. This does not need to be escaped.
	 * @param meta "meta" means the category of the substitution (this appears right aligned on the dropdown list). This is freeform description and can contain anything but typically a very short category description (9 chars or less) such as "function" or "param" or "template".
	 * @param tooltip "tooltip" is an escaped html tooltip to be displayed when the completion option is displayed, this can be null. 
	 * @param score  The score is the value assigned to the autocompletion option. Scores with a higher value will appear closer to the top. Items with an identical score are sorted alphbetically by caption in the drop down.
	 */
	public AceCompletionValue(String name, String value, String meta, String tooltip, int score) {
		this.caption = name;
		this.value = value;
		this.score = score;
		this.tooltip = tooltip;
		this.meta = meta;
	}
	
	/**
	 * Convert to a native JS object in the format expected
	 * by the Ace code completion callback.
	 * 
	 * @return native JS object
	 */
	@Override
	native JavaScriptObject toJsObject() /*-{
		
		
			return {
				caption: this.@edu.ycp.cs.dh.acegwt.client.ace.AceCompletionValue::caption,
				value: this.@edu.ycp.cs.dh.acegwt.client.ace.AceCompletionValue::value,
				score: this.@edu.ycp.cs.dh.acegwt.client.ace.AceCompletionValue::score,
				meta: this.@edu.ycp.cs.dh.acegwt.client.ace.AceCompletionValue::meta,
			    aceGwtHtmlTooltip: this.@edu.ycp.cs.dh.acegwt.client.ace.AceCompletionValue::tooltip
			};

	}-*/;
}
