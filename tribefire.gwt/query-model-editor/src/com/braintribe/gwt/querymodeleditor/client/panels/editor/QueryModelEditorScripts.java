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
package com.braintribe.gwt.querymodeleditor.client.panels.editor;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.user.client.Event;

public class QueryModelEditorScripts {

	/************************* Generic Scripts *************************/

	// @formatter:off
	public static native Node getCurrentElement() /*-{
	    if ($wnd.document.activeElement !== undefined) {
	        return $wnd.document.activeElement;
	    } else {
		    return $wnd.document.querySelector(':focus');
		}
	}-*/;

	public static native String getPasteData(Event event) /*-{
		if (event !== undefined) {
		    // Get and check clipboard data object (FireBox & Chrome)
		    var clipboardData = (event.originalEvent || event).clipboardData;
		    if (clipboardData !== undefined) {
		        // Get data as text from clipboard
		        return clipboardData.getData('text/plain');
		    } else {
		        // Get data as text from clipboard (IE & Fallback)
		        return $wnd.clipboardData.getData('text');
		    }
	    }

	    return null;
	}-*/;

	public static native String getStyle(Element element, String styleProperty) /*-{
	    var defaultView = (element.ownerDocument || document).defaultView;
	    if (defaultView && defaultView.getComputedStyle) {
	        // sanitize property name to css notation (hypen separated words eg. font-Size)
	    	styleProperty = styleProperty.replace(/([A-Z])/g, '-$1').toLowerCase();
	        return defaultView.getComputedStyle(element, null).getPropertyValue(styleProperty);
	    } else if (element.currentStyle) { // IE
	        // sanitize property name to camelCase
	    	styleProperty = styleProperty.replace(/\-(\w)/g, function (str, letter) {
	            return letter.toUpperCase();
	        });

	        var value = element.currentStyle[styleProp];
	        // convert other units to pixels on IE
	        if (/^\d+(em|pt|%|ex)?$/i.test(value)) {
	            return (function (value) {
	                var oldLeft = element.style.left;
	                var oldRsLeft = element.runtimeStyle.left;

	                element.runtimeStyle.left = el.currentStyle.left;
	                element.style.left = value || 0;
	                value = element.style.pixelLeft + 'px';

	                element.style.left = oldLeft;
	                element.runtimeStyle.left = oldRsLeft;
	                return value;
	            })(value);
	        }
	        return value;
	    }
	}-*/;
	// @formatter:on

	/*********************** Handle Input Element **********************/

	// @formatter:off
	public static native int getInputCursorPosition(Element input) /*-{
	    if ((input !== undefined && input !== null) && input.selectionStart !== undefined) {
	        return input.selectionStart;
	    }

	    return -1;
	}-*/;

	public static native void setInputCursorPosition(Element input, int position) /*-{
	    if ((input !== undefined && input !== null) && input.setSelectionRange !== undefined) {
	        input.focus();
	        input.setSelectionRange(position, position);
	    }
	}-*/;
	// @formatter:on

	/****************** Handle SubElements of Element ******************/

	// @formatter:off
	public static native int getCursorPositionOfNode(Element root, Node searchNode) /*-{
	    var result = -1;

	    if ((root !== undefined && root !== null) && (searchNode !== undefined && searchNode !== null)) {
	        // Create range and set current child
	        var searchNodeRange = $wnd.document.createRange();
	        searchNodeRange.selectNode(searchNode);

	        // Function of the tree-walker filter
	        var treeWalkerFilter = function (checkNode) {
	            // Create range and set current child
	            var checkNodeRange = $wnd.document.createRange();
	            checkNodeRange.selectNode(checkNode);

	            // Return ACCEPT if cursor is before current child node by comparing ranges, otherwise return REJECT
	            return checkNodeRange.compareBoundaryPoints(Range.END_TO_START, searchNodeRange) < 0 ? NodeFilter.FILTER_ACCEPT : NodeFilter.FILTER_REJECT;
	        };

	        // Work through all child nodes of current element using the tree-walker filter
	        var treeWalker = $wnd.document.createTreeWalker(root, NodeFilter.SHOW_ALL, treeWalkerFilter, false);
	        while (treeWalker.nextNode()) {
	            // Check for start container or node
	            var currentNode = treeWalker.currentNode;
	            if (currentNode === searchNode) {
	                result = (result > -1 ? result : 0);
	            } else if (!isNaN(currentNode.length)) {
	                result = (result > -1 ? result : 0) + currentNode.length;
	            } else {
	                result += 1;
	            }
	        }
	    }

	    return result;
	}-*/;

	public static native Node getNodeOfCursorPosition(Element root, int position) /*-{
	    var result = null;

	    if ((root !== undefined && root !== null) && position !== undefined) {
	        var checkPosition = 0;

	        // Function of the tree-walker filter
	        var treeWalkerFilter = function (node) {
	            // Check the position
	            if (checkPosition <= position) {
	                // Check the node length
	                if (!isNaN(node.length)) {
	                    checkPosition += node.length;
	                } else {
	                    checkPosition += 1;
	                }

	                // Recheck the position
	                if (checkPosition >= position) {
	                    // Node found!
	                    return NodeFilter.FILTER_ACCEPT;
	                } else {
	                    // Node not yet found!
	                    return NodeFilter.FILTER_REJECT;
	                }
	            } else {
	                // CheckPosition is to high!
	                return NodeFilter.FILTER_REJECT;
	            }
	        };

	        // Work through all child nodes of current element using the tree-walker filter
	        var treeWalker = document.createTreeWalker(root, NodeFilter.SHOW_ALL, treeWalkerFilter, false);
	        while (treeWalker.nextNode() !== null) {
	            result = treeWalker.currentNode;
	        }
	    }

	    return result;
	}-*/;
	// @formatter:on

	/************* Set/Get Cursor of SubElement of Element *************/

	// @formatter:off
	public static native int getElementCursorPosition(Element element) /*-{
	    var result = -1;

	    if (element !== undefined && element !== null) {
	        // Get selection and range from window
	        var selection = $wnd.getSelection();
	        if (selection.rangeCount > 0) {
	            var range = selection.getRangeAt(0);

	            // Create range and set current child
	            var startNodeRange = $wnd.document.createRange();
	            startNodeRange.selectNode(range.startContainer);

	            // Function of the tree-walker filter
	            var treeWalkerFilter = function (checkNode) {
	                // Create range and set current child
	                var checkNodeRange = $wnd.document.createRange();
	                checkNodeRange.selectNode(checkNode);

	                // Return ACCEPT if cursor is before current child node by comparing ranges, otherwise return REJECT
	                return checkNodeRange.compareBoundaryPoints(Range.END_TO_START, startNodeRange) < 0 ? NodeFilter.FILTER_ACCEPT : NodeFilter.FILTER_REJECT;
	            };

	            // Work through all child nodes of current element using the tree-walker filter
	            var treeWalker = $wnd.document.createTreeWalker(element, NodeFilter.SHOW_ALL, treeWalkerFilter, false);
	            while (treeWalker.nextNode()) {
	                // Check for start container or node
	                var currentNode = treeWalker.currentNode;
	                if (currentNode === range.startContainer) {
	                    result = (result > -1 ? result : 0) + range.startOffset;
	                } else if (!isNaN(currentNode.length)) {
	                    result = (result > -1 ? result : 0) + currentNode.length;
	                } else {
	                    result += 1;
	                }
	            }
	        }
	    }

	    return result;
	}-*/;

	public static void setElementCursorPosition(final Element element, final int position) {
		// Get and check sub node of cursor position
		final Node cursorNode = getNodeOfCursorPosition(element, position);
		if (cursorNode != null) {
			// Get start position of sub node
			final int cursorNodePosition = getCursorPositionOfNode(element, cursorNode);
			if (cursorNodePosition >= 0 && cursorNodePosition <= position) {
				// Remove start position from position
				setElementCursorPositionNative(cursorNode, position - cursorNodePosition);
			}
		}
	}

	private static native void setElementCursorPositionNative(Node node, int position) /*-{
	    if ((node !== undefined && node !== null && !isNaN(node.length)) && (position >= 0 && position <= node.length)) {
	        // Create new range and set values
	        var range = $wnd.document.createRange();
	        range.setEnd(node, position);
	        range.setStart(node, position);

	        // Get selection and set new range
	        var selection = $wnd.getSelection();
	        selection.removeAllRanges();
	        selection.addRange(range);
	    }
	}-*/;
	// @formatter:on
}
