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
package com.braintribe.gwt.genericmodel.client.codec.jse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.braintribe.codec.CodecException;
import com.braintribe.gwt.async.client.AsyncCallbacks;
import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.genericmodel.client.codec.api.AsyncCodingTools;
import com.braintribe.gwt.genericmodel.client.codec.api.GmAsyncCodec;
import com.braintribe.gwt.genericmodel.client.codec.api.GmDecodingContext;
import com.braintribe.gwt.genericmodel.client.codec.api.GmEncodingContext;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayInteger;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;


public class JseCodec implements GmAsyncCodec<Object, String> {
	private boolean enhanced = true;
	
	public void setEnhanced(boolean enhanced) {
		this.enhanced = enhanced;
	}
	private static final String startMarker = "//BEGIN_TYPES"; 
	private static final String endMarker = "//END_TYPES"; 
	
	private static Set<String> extractTypes(String jseSource) {
		Set<String> typeNames = new HashSet<String>();
		
		int stIndex = jseSource.indexOf(';');
		
		if (stIndex == -1)
			return typeNames;
		
		stIndex = jseSource.indexOf(';', stIndex + 1);
		
		if (stIndex == -1)
			return typeNames;
		
		int s = jseSource.substring(0, stIndex).indexOf(startMarker);
		
		if (s == -1)
			return typeNames;
		
		int e = jseSource.indexOf(endMarker);
		
		String typeNamesSection = jseSource.substring(s + startMarker.length(), e);
		String typeNamesStatements[] = typeNamesSection.split(";");
		
		for (String typeNameFragment: typeNamesStatements) {
			int startQuoteIndex = typeNameFragment.indexOf('"');
			int endQuoteIndex = typeNameFragment.lastIndexOf('"');
			
			if (startQuoteIndex != -1 && endQuoteIndex != -1) {
				String typeName = typeNameFragment.substring(startQuoteIndex + 1, endQuoteIndex);
				typeNames.add(typeName);
			}
		}
		
		return typeNames;
	}

	
	@Override
	public <T> Future<T> decodeAsync(final String encodedValue, final GmDecodingContext context) {
		final Future<T> future = new Future<T>();
		Set<String> typeSignatures = extractTypes(encodedValue);
		
		context.ensureTypes(typeSignatures,
				AsyncCallbacks.of(v -> JseCodec.this.<T> decodeBody(encodedValue, context).get(future), future::onFailure));
		
		return future;
	}
	
	@Override
	public Future<String> encodeAsync(Object value, GmEncodingContext context) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public String encode(Object value, GmEncodingContext context) {
		throw new UnsupportedOperationException();
	}
	
	private <T> Future<T> decodeBody(final String encodedValue, final GmDecodingContext gmDecodingContext) {
		AsyncScriptRunner asyncScriptRunner = new AsyncScriptRunner(gmDecodingContext, encodedValue);

		return asyncScriptRunner.start();
	}
	
	protected class AsyncScriptRunner implements RepeatingCommand {
		private final Future<Object> future = new Future<Object>();
		private final Iterator<String> it;
		private final JavaScriptObject args;
		private Object lastResult;
		private final GmDecodingContext gmDecodingContext;

		public AsyncScriptRunner(GmDecodingContext gmDecodingContext, String script) {
			this.gmDecodingContext = gmDecodingContext;
			JavaScriptObject context = gmDecodingContext.isLenientDecode()?
				JseScriptFunctions.create(gmDecodingContext):
				JseScriptFunctions.create();
			this.args = buildArgs(context);
			this.it = readScript(script).iterator();
		}
		
		public <T extends Object> Future<T> start() {
			Scheduler.get().scheduleIncremental(this);
			return (Future<T>)future;
		}
		
		@Override
		public boolean execute() {
			long s = System.currentTimeMillis();
			while (it.hasNext()) {
				
				try {
					String fragment = it.next();
					try {
						JseScriptFunctions.proxyContext = gmDecodingContext.getProxyContext();
						lastResult = evaluate(fragment, args);
					}
					finally {
						JseScriptFunctions.proxyContext = null;
					}
				} catch (Exception e) {
					future.onFailure(new CodecException("error while decoding js fragment", e));
					return false;
				}
				
				long d = System.currentTimeMillis() - s;
				
				if (d > 100 && it.hasNext())
					return true;
			}
			
			AsyncCodingTools.completeAssembly(gmDecodingContext, lastResult, future);
			return false;
		}
	}

	private List<String> readScript(String script) {
		int splitPointsStart = script.lastIndexOf('[');
		String splitPointsSource = script.substring(splitPointsStart);
		JsArrayInteger splitPoints = decodeSplitPoints(splitPointsSource);
		List<String> fragments = new ArrayList<String>();
		int s = 0;
		
		for (int i = 0; i < splitPoints.length(); i++) {
			int e = splitPoints.get(i);
			String fragment = script.substring(s, e);
			fragments.add(fragment);
			s = e;
		}
		
		return fragments;
	}
	
	private static native JavaScriptObject buildArgs(JavaScriptObject context) /*-{
	    return [context, {}, null];
	}-*/;
	
	private static native JsArrayInteger decodeSplitPoints(String source) /*-{
		return eval(source);
	}-*/;
	
	private static native void log(String msg) /*-{
		$wnd.console.log(msg);
	}-*/;

	@Override
	public <T extends Object> T decode(String encodedValue, GmDecodingContext gmDecodingContext) throws CodecException {
		JseScriptFunctions.proxyContext = gmDecodingContext.getProxyContext();
		
		Set<String> typeSignatures = extractTypes(encodedValue);
		gmDecodingContext.ensureTypes(typeSignatures);

		JavaScriptObject context = gmDecodingContext.isLenientDecode()?
				JseScriptFunctions.create(gmDecodingContext):
				JseScriptFunctions.create();
		
		List<String> fragments = readScript(encodedValue);
		JavaScriptObject args = buildArgs(context);
		
		Object lastResult = null;
		
		for (String fragment: fragments) {
			lastResult = evaluate(fragment, args);
		}
		
		T castedResult = (T) lastResult;
		
		return AsyncCodingTools.completeAssembly(gmDecodingContext, castedResult);
	}

	private static native Object evaluate(String fragment, JavaScriptObject args) /*-{
		return new Function("$", "P", "_", fragment)(args[0], args[1], args[2]);
	}-*/;

	public static <T extends Object> T decodeFunction(JavaScriptObject jseFunction) throws CodecException {
		JavaScriptObject args = buildArgs(JseScriptFunctions.create());

		return (T) evaluate(jseFunction, args);
	}

	private static native Object evaluate(JavaScriptObject jseFunction, JavaScriptObject args) /*-{
		return jseFunction(args[0], args[1], args[2]);
	}-*/;

}
