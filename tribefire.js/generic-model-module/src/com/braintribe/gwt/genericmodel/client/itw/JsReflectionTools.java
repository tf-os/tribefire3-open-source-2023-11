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
package com.braintribe.gwt.genericmodel.client.itw;

import com.google.gwt.core.client.GwtScriptOnly;
import com.google.gwt.core.client.JavaScriptObject;

/**
 * @author peter.gazdik
 */
@GwtScriptOnly
public class JsReflectionTools {

	private static final String RETURN_NEW = "return new";
	private static final String THIS_DOT = "this.";

	/** Expects source to be like "function() { return new ${package}.${SuperclassName};}" */
	public static Object extractConstructor(JavaScriptObject constructorFunction) {
		String constructor = trimmedSubstring(constructorFunction, RETURN_NEW, "}");
		constructor = ensureJsExpressionWithoutSemicolon(constructor);

		return ScriptOnlyItwTools.eval(constructor);
	}

	/** Expects source to be like "function() {this.${jsProperty}}" */
	public static String extractJsPropertyName(JavaScriptObject propertyWrappedInFunction) {
		String jsPropertyName = trimmedSubstring(propertyWrappedInFunction, THIS_DOT, "}");
		return ensureJsExpressionWithoutSemicolon(jsPropertyName);
	}

	/** GWT likes to play around, sometimes the ';' is there, sometimes it is not (e.g. for code like: <tt>function() {return foo.bar;}</tt>) */
	private static String ensureJsExpressionWithoutSemicolon(String expressionWithPossibleSemicolon) {
		if (expressionWithPossibleSemicolon.endsWith(";"))
			return expressionWithPossibleSemicolon.substring(0, expressionWithPossibleSemicolon.length() - 1).trim();
		else
			return expressionWithPossibleSemicolon;
	}

	private static String trimmedSubstring(JavaScriptObject jso, String left, String right) {
		String source = jso.toString();

		int s = source.indexOf(left);
		int e = source.lastIndexOf(right);

		return source.substring(s + left.length(), e).trim();
	}

	/** This is here to avoid inline implementation, because this will be obfuscated and thus shorter. This one doesn't seem to work though. */
	public static native JavaScriptObject prototypeOf(JavaScriptObject jso) /*-{
	 	return jso.prototype;
	}-*/;

	/** This is here to avoid inline implementation, because this will be obfuscated and thus shorter */
	public static native void defineProperty(JavaScriptObject prototype, JavaScriptObject name, JavaScriptObject getterSetter) /*-{
		Object.defineProperty(prototype, name, getterSetter);
	}-*/;

}
