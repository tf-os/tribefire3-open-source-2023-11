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
package com.braintribe.gwt.genericmodel.client.codec.api;

import java.util.function.Function;

import com.braintribe.codec.CodecException;
import com.braintribe.gwt.async.client.Future;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.proxy.ProxyContext;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.processing.async.api.AsyncCallback;
import com.google.gwt.core.client.Scheduler;

public abstract class AsyncCodingTools {
	
	public static <T> void completeAssembly(final GmDecodingContext gmDecodingContext, final T assembly, final Future<T> future) {
		final Function<Object, GmMetaModel> intrinsicModelExtractor = gmDecodingContext.getIntrinsicModelExtractor();
		
		com.braintribe.processing.async.api.AsyncCallback<Void> finalStep = AsyncCallback.of( //
				value -> {
					ProxyContext proxyContext = gmDecodingContext.getProxyContext();
					if (proxyContext != null && intrinsicModelExtractor != null)
						proxyContext.resolveProxiesAndApply(AsyncCallback.of(v -> scheduleSuccessNotification(assembly, future), future::onFailure));
					else {
						// TODO: when the proxyContext is null, nothing was done. The client was waiting forever. Please
						// investigate if there is no further things to be done here.
						scheduleSuccessNotification(assembly, future);
					}
				}, future::onFailure);
		
		if (intrinsicModelExtractor == null)
			finalStep.onSuccess(null);
		else {
			GmMetaModel model;
			try {
				model = intrinsicModelExtractor.apply(assembly);
			} catch (RuntimeException e) {
				future.onFailure(new CodecException("Error while extracting intrinsic model for deployment", e));
				return;
			}
			GMF.getTypeReflection().deploy(model, finalStep);
		}
	}
	
	private static <X> void scheduleSuccessNotification(final X assembly, final Future<X> future) {
		Scheduler.get().scheduleDeferred(() -> future.onSuccess(assembly));
	}
	
	public static <T> T completeAssembly(final GmDecodingContext gmDecodingContext, final T assembly) throws CodecException {
		Function<Object, GmMetaModel> intrinsicModelExtractor = gmDecodingContext.getIntrinsicModelExtractor();
		
		if (intrinsicModelExtractor == null)
			return assembly;
		
		try {
			GmMetaModel model = intrinsicModelExtractor.apply(assembly);
			GMF.getTypeReflection().deploy(model);
		} catch (Exception e) {
			throw new CodecException("Error while trying to deploy assembly intrinsic model", e);
		}

		ProxyContext proxyContext = gmDecodingContext.getProxyContext();
		if (proxyContext != null)
			proxyContext.resolveProxiesAndApply();
		
		return assembly;
	}
}
