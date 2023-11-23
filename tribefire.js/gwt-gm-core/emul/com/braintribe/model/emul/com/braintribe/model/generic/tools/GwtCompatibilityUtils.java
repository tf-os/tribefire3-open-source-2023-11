package com.braintribe.model.generic.tools;

import java.util.Map;

import com.braintribe.exception.Exceptions;

import java.util.HashMap;

public class GwtCompatibilityUtils {

	public static <K, V> Map<K, V> newConcurrentMap() {
		return new HashMap<K, V>();
	}
	
	public static Class<?> getEnumClass(String className) {
		throw new UnsupportedOperationException("getEnumClass(" + className + ") should not be called in a GWT environment");
	}

}
