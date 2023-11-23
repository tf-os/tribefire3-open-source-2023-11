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
package com.braintribe.devrock.artifactcontainer.views.dependency.tabs.capability.clipboard;

import java.util.function.Function;

/**
 * a) a marker interface to declare that the implementer can produce meaningful contents for a transfer to the clip board<br/>
 * b) enables the use of the {@link Function} function provide( {@link ClipboardContentsProviderMode});<br/>
 * c) tells what {@link ClipboardContentsProviderMode} the implementer supports, even if what the actual mode means for the provider is left to the provider, 
 * so that the view can decide whether the action (and icon) should be enabled or not 
 * 
 * @author pit
 *
 */
public interface ClipboardContentsProviderCapable extends Function<ClipboardContentsProviderMode, String>{
	/**
	 * tells whether the implementer supports the specific mode, as defined in {@link ClipboardContentsProviderMode}
	 * @param mode - the {@link ClipboardContentsProviderMode} value to test 
	 * @return - true if it supports it, false otherwise 
	 */
	public boolean supportsMode( ClipboardContentsProviderMode mode);
}
