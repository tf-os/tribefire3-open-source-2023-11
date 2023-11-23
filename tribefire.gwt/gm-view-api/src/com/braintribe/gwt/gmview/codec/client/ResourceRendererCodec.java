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
package com.braintribe.gwt.gmview.codec.client;

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.braintribe.gwt.gmresourceapi.client.GmImageResource;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.resource.Resource;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

/**
 * {@link Codec} responsible for encoding (rendering) a {@link Resource}.
 * @author michel.docouto
 *
 */
public class ResourceRendererCodec implements Codec<Resource, String>, HtmlRenderer {
	
	private int maxWidth = 40;
	private int maxHeight = 40;
	private String accessId;
	
	static {
		exportErrorHandlingMethod();
	}
	
	/**
	 * Configures the max height to be used by the icon. Defaults to 40.
	 */
	@Configurable
	public void setMaxHeight(int maxHeight) {
		this.maxHeight = maxHeight;
	}
	
	/**
	 * Configures the max width to be used by the icon. Defaults to 40.
	 */
	@Configurable
	public void setMaxWidth(int maxWidth) {
		this.maxWidth = maxWidth;
	}
	
	/**
	 * Configures the optional access where the streaming will be performed.
	 * If no acessId is provided, then the original access within the session will be used.
	 */
	@Configurable
	public void setAccessId(String accessId) {
		this.accessId = accessId;
	}
	
	@Override
	public String encode(Resource resource) throws CodecException {
		if (resource == null || !(resource.session() instanceof ManagedGmSession))
			return null;
		
		String url = accessId == null ? ((ManagedGmSession) resource.session()).resources().url(resource).asString()
				: ((ManagedGmSession) resource.session()).resources().url(resource).accessId(accessId).asString();
		GmImageResource imageResource = new GmImageResource(resource, url);
		
		if (imageResource.getHeight() > maxHeight || imageResource.getWidth() > maxWidth || imageResource.getWidth() == 0 || imageResource.getHeight() == 0) {
			StringBuilder imageHtml = new StringBuilder();
			imageHtml.append("<img onError='this.onError=null; handleTFResourceImageError(this);' src='").append(url).append("' width='32px' height='32px'/>");
			return imageHtml.toString();
		} else
			return AbstractImagePrototype.create(imageResource).getHTML();
	}

	@Override
	public Resource decode(String encodedValue) throws CodecException {
		throw new UnsupportedOperationException("This codec should be used as an encoder (renderer) only.");
	}

	@Override
	public Class<Resource> getValueClass() {
		return Resource.class;
	}
	
	private static void handleBrokenImage(JavaScriptObject object) {
		ImageElement image = object.cast();
		image.setSrc(CodecResources.INSTANCE.empty().getSafeUri().asString());
	}
	
	private static native void exportErrorHandlingMethod() /*-{
		$wnd.handleTFResourceImageError = function(element) {
			@com.braintribe.gwt.gmview.codec.client.ResourceRendererCodec::handleBrokenImage(Lcom/google/gwt/core/client/JavaScriptObject;)(element);
		};
	}-*/;

}
