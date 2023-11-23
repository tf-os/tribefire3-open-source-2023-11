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
package com.braintribe.gwt.logging.client;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.braintribe.gwt.packaging.client.CompilationInfo;
import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.xhr.client.ReadyStateChangeHandler;
import com.google.gwt.xhr.client.XMLHttpRequest;

public class StackTraceRenderer {
	
	public static void renderExceptionTranslated(final Throwable throwable, final AsyncCallback<String> callback) {
		if (!GWT.isScript()) {
			String renderedTrace = render(throwable, Collections.<StackTraceElement, StackTraceElement>emptyMap());
			callback.onSuccess(renderedTrace);
			return;
		}
		
		Set<StackTraceElement> elements = new HashSet<>();
		Throwable currentThrowable = throwable;
		while (currentThrowable != null) {
			for (StackTraceElement element: currentThrowable.getStackTrace()) {
				elements.add(element);
			}
			currentThrowable = currentThrowable.getCause();
		}
		
		translateStackTraceElements(elements, new AsyncCallback<Map<StackTraceElement,StackTraceElement>>() {
			@Override
			public void onSuccess(Map<StackTraceElement, StackTraceElement> result) {
				StringBuilder builder = new StringBuilder();
				builder.append("------------------------------------------------------------------------------------------------\n");
				builder.append("deobfuscated stacktrace\n\n");
				builder.append("! line numbers always point to the method header\n");
				builder.append("! original method names are prefixed with a $ when they were made static by the gwt compiler\n");
				builder.append("! method names can still have javascript names if the are not correlated to a java method\n");
				builder.append("! expected frames may be missing as they are potentially inlined by the gwt compiler\n");
				builder.append("------------------------------------------------------------------------------------------------\n\n");
				render(throwable, result, builder);
				builder.append("\n\n");
				builder.append("------------------------------------------------------------------------------------------------\n");
				builder.append("original stacktrace\n\n");
				builder.append("! contains only original javascript names\n");
				builder.append("------------------------------------------------------------------------------------------------\n\n");
				render(throwable, Collections.<StackTraceElement, StackTraceElement>emptyMap(), builder);
				callback.onSuccess(builder.toString());
			}
			
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}
	
	public static void render(Throwable throwable, Map<StackTraceElement, StackTraceElement> elementMap, StringBuilder builder) {
		boolean lastCause = throwable != null ? throwable.getCause() == null : true;
		while (throwable != null) {
			//builder.append(throwable.getClass().getName() + ": " +throwable.getMessage());
			builder.append(throwable.getMessage());
			
			if (lastCause) {
				StackTraceElement[] stackTrace = throwable.getStackTrace();
				
				for (StackTraceElement element: stackTrace) {
					StackTraceElement translatedElement = elementMap.get(element);
					element = translatedElement != null? translatedElement: element;
					builder.append('\n');
					builder.append("    at " + element.getClassName()+ "." + element.getMethodName() + "("+ element.getFileName() + ":" + element.getLineNumber() + ")");
				}
			} else
				builder.append('\n');
			
			throwable = throwable.getCause();
			
			if (!lastCause) {
				if (throwable != null)
					lastCause = throwable.getCause() == null;
				
				if (lastCause)
					builder.append("\nCaused by: ");
			}
		}
	}
	
	public static String render(Throwable throwable, Map<StackTraceElement, StackTraceElement> elementMap) {
		StringBuilder builder = new StringBuilder();
		render(throwable, elementMap, builder);
		return builder.toString();
	}
	
	public static void translateStackTraceElements(final Set<StackTraceElement> elements, final AsyncCallback<Map<StackTraceElement, StackTraceElement>> callback) {
		Set<String> names = new HashSet<String>();
		for (StackTraceElement element: elements) {
			names.add(element.getMethodName());
		}
		translateJavascriptMethodNames(names, new AsyncCallback<Map<String,StackTraceElement>>() {
			@Override
			public void onSuccess(Map<String, StackTraceElement> result) {
				// translate StackTraceElements
				Map<StackTraceElement, StackTraceElement> translation = new HashMap<>();
				for (StackTraceElement element: elements) {
					StackTraceElement translatedElement = result.get(element.getMethodName());
					translation.put(element, translatedElement);
				}
				callback.onSuccess(translation);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}
	
	public static void translateJavascriptMethodNames(Set<String> names, final AsyncCallback<Map<String,StackTraceElement>> callback) {
		XMLHttpRequest xmlHttpRequest = XMLHttpRequest.create();
		
		String compilationId = CompilationInfo.get().getCompilationId();
		
		xmlHttpRequest.open("POST", GWT.getModuleBaseURL() + "../symbolTranslation?compilationId="+ compilationId);
		
		JSONArray array = new JSONArray();
		int i = 0;
		for (String name: names) {
			array.set(i++, new JSONString(name));
		}
		
		String payload = array.toString();
		xmlHttpRequest.setOnReadyStateChange(new ReadyStateChangeHandler() {
			
			@Override
			public void onReadyStateChange(XMLHttpRequest xhr) {
				if (xhr.getReadyState() == XMLHttpRequest.DONE) {
					
					if (xhr.getStatus() == 200) {
						String jsonEncodedMap = xhr.getResponseText();
						JSONValue jsonValue = JSONParser.parseStrict(jsonEncodedMap);
						JSONObject jsonMap = null;
						Map<String, StackTraceElement> result = new HashMap<String, StackTraceElement>();
						boolean invalidResponse = false;
						if ((jsonMap = jsonValue.isObject()) != null) {
							Set<String> keys = jsonMap.keySet();
							
							for (String key: keys) {
								JSONValue value = jsonMap.get(key);
								JSONObject elementValue = null;
								if ((elementValue = value.isObject()) != null) {

									try {
										JSONString classNameJson = (JSONString)elementValue.get("className");
										JSONString memberNameJson = (JSONString)elementValue.get("memberName");
										JSONNumber lineNumberJson = (JSONNumber)elementValue.get("lineNumber");
										String className = classNameJson.stringValue();
										int s = className.lastIndexOf('.');
										int e = className.indexOf('$');
										if (e == -1)
											e = className.length();
										String fileName = className.substring(s + 1, e) + ".java";
										StackTraceElement element = new StackTraceElement(className, memberNameJson.stringValue(), fileName, (int)lineNumberJson.doubleValue());
										result.put(key, element);
									}
									catch (ClassCastException e) {
										invalidResponse = true;
										break;
									}
								}
								else {
									invalidResponse = true;
									break;
								}
							}
						}
						
						if (invalidResponse) {
							String trimmedJson = jsonEncodedMap.substring(0, Math.max(100, jsonEncodedMap.length()));
							callback.onFailure(
									new StackTraceTranslateException(
											"invalid json response from xml http request: " + trimmedJson));
						}
						else {
							callback.onSuccess(result);
						}
						
					}
					else {
						callback.onFailure(new StackTraceTranslateException("error while sending RPC request: " + xhr.getStatusText() + " - statusCode: " + xhr.getStatus()));
					}
				}
			}
		});
		xmlHttpRequest.send(payload);
	}
	
}
