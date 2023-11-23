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
package com.braintribe.gwt.gmresourceapi.client;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.braintribe.gwt.gmresourceapi.client.resources.GmResourceModuleResources;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.source.StaticSource;
import com.braintribe.model.resource.specification.PageCountSpecification;
import com.braintribe.model.resource.specification.PixelDimensionSpecification;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeUri;

public class GmImageResource implements ImageResource {
	private Resource delegate;
	private String url;
	private static boolean checkForExistence = false;
	
	private List<GmImageResourceListener> gmImageResourceListeners = new ArrayList<>();
	
	/**
	 * Configures whether we should check if the image exists.
	 */
	public static void setCheckForExistence(boolean check) {
		checkForExistence = check;
	}
	
	/**
	 * Creates a new GmImageResource. It checks for the image existence.
	 */
	public GmImageResource(Resource delegate, final Function<? super Resource, String> resourceUrlProvider) {
		super();
		this.delegate = delegate;
		try {
			this.url = resourceUrlProvider.apply(this.delegate);
	 
			RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, url);
			requestBuilder.sendRequest(null, new RequestCallback() {
				
				@Override
				public void onResponseReceived(Request request, Response response) {
					if (response.getStatusCode() == 404)
						onError(null, null);
					else if (response.getStatusCode() == 200) {
						fireOnImageChanged(GmImageResource.this);			
					}
				}
				
				@Override
				public void onError(Request request, Throwable exception) {
					url = provideFallbackUrl(GmImageResource.this.delegate);
					fireOnImageChanged(provideFallbackResource(GmImageResource.this.delegate));					
				}
			});
		} catch (Exception e) {
			throw new IllegalArgumentException("error while using provider to determine resource url", e);
		}
	}
	
	/**
	 * Creates a new GmImageResource. It checks for the image existence only if {@link #checkForExistence} is set to true.
	 */
	public GmImageResource(Resource delegate, final String url) {
		super();
		this.delegate = delegate;
		this.url = url;
		
		if (checkForExistence) {
			try {
				RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, url);
				requestBuilder.sendRequest(null, new RequestCallback() {
					@Override
					public void onResponseReceived(Request request, Response response) {
						if (response.getStatusCode() == 404)
							onError(null, null);
						else if(response.getStatusCode() == 200)
							fireOnImageChanged(GmImageResource.this);
					}
					
					@Override
					public void onError(Request request, Throwable exception) {
						GmImageResource.this.url = provideFallbackUrl(GmImageResource.this.delegate);
						fireOnImageChanged(provideFallbackResource(GmImageResource.this.delegate));					
					}
				});
			} catch (Exception e) {
				throw new IllegalArgumentException("error while using provider to determine resource url", e);
			}
		}
	}

	@Override
	public String getName() {
		return delegate.getName();
	}

	@Override
	public int getHeight() {
		if(delegate.getSpecification() instanceof PixelDimensionSpecification)
			return ((PixelDimensionSpecification) delegate.getSpecification()).getPixelHeight();
		else
			return 0;
	}

	@Override
	public int getLeft() {
		return 0;
	}

	@Override
	public SafeUri getSafeUri() {
		return new SafeUri() {
			
			@Override
			public String asString() {
				return url;
			}
		};
	}

	@Override
	public int getTop() {
		return 0;
	}

	/**
		@deprecated Use {@link #getSafeUri()} instead.
	 */
	@Override
	@Deprecated
	public String getURL() {
		return getSafeUri().asString();
	}

	@Override
	public int getWidth() {
		if(delegate.getSpecification() instanceof PixelDimensionSpecification)
			return ((PixelDimensionSpecification) delegate.getSpecification()).getPixelWidth();
		else
			return 0;
	}

	@Override
	public boolean isAnimated() {
		if(delegate.getSpecification() instanceof PageCountSpecification)
			return ((PageCountSpecification) delegate.getSpecification()).getPageCount() > 0;
		else
			return false;
	}
	
	public void addGmImageResourceListener(GmImageResourceListener gmImageResourceListener){
		gmImageResourceListeners.add(gmImageResourceListener);
	}
	
	public void removeGmImageResourceListener(GmImageResourceListener gmImageResourceListener){
		gmImageResourceListeners.remove(gmImageResourceListener);
	}
	
	public void fireOnImageChanged(ImageResource imageResource){
		for(GmImageResourceListener listener : gmImageResourceListeners){
			listener.onImageChanged(imageResource);
		}
	}
	
	private String provideFallbackUrl(Resource imageResource) {
		PixelDimensionSpecification	specification = null;
		if (imageResource.getSpecification() instanceof PixelDimensionSpecification)
			specification = (PixelDimensionSpecification) imageResource.getSpecification();
		
		if (specification != null) {
			if (specification.getPixelWidth() >= 0 && specification.getPixelWidth() <= 8)
				return GmResourceModuleResources.INSTANCE.defaultActionIconSmall().getSafeUri().asString();
			else if(specification.getPixelWidth() > 9 && specification.getPixelWidth() <= 16)
				return GmResourceModuleResources.INSTANCE.defaultActionIconMedium().getSafeUri().asString();
			else if(specification.getPixelWidth() > 17 && specification.getPixelWidth() <= 32)
				return GmResourceModuleResources.INSTANCE.defaultActionIconLarge().getSafeUri().asString();
		}
		
		if (imageResource.getResourceSource() instanceof StaticSource)
			return ((StaticSource)imageResource.getResourceSource()).getResolverURI();
		else if (imageResource.session() instanceof ManagedGmSession)
			return ((ManagedGmSession) imageResource.session()).resources().url(imageResource).asString();
		
		return null;
	}
	
	private ImageResource provideFallbackResource(Resource imageResource){
		PixelDimensionSpecification	specification = null;
		if(imageResource.getSpecification() instanceof PixelDimensionSpecification)
			specification = (PixelDimensionSpecification) imageResource.getSpecification();
		if(specification != null){
			if (specification.getPixelWidth() >= 0 && specification.getPixelWidth() <= 8)
				return GmResourceModuleResources.INSTANCE.defaultActionIconSmall();
			else if(specification.getPixelWidth() > 8 && specification.getPixelWidth() <= 16)
				return GmResourceModuleResources.INSTANCE.defaultActionIconMedium();
			else if(specification.getPixelWidth() > 16 && specification.getPixelWidth() <= 32)
				return GmResourceModuleResources.INSTANCE.defaultActionIconLarge();
		}
		return null;
	}
}
