// ============================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2015 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.model.processing.meta.cmd.tools;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;

// ###################################
// ## . . . . . EMMULATION . . . . .##
// ###################################

public class CmdGwtUtils {

	private static final GenericModelTypeReflection typeReflection = GMF.getTypeReflection();
	
	public static <K, V> Map<K, V> newCacheMap() {
		return new HashMap<K, V>();
	}

	public static <K, V> Map<K, V> newWeakCacheMap(int maxSize) {
		return new HashMap<K, V>();
	}

	public static boolean isInstanceOf(Class<?> clazz, Object o) {
		Class<?> objClass = o.getClass();

		do {
			if (objClass.equals(clazz)) {
				return true;
			}
			objClass = objClass.getSuperclass();

		} while (objClass != null);

		return false;
	}

	public static <T> T cast(Class<T> clazz, Object o) {
		return (T) o;
	}

	public static <T> Class<? extends T> asSubclass(Class<?> c, Class<T> clazz) {
		return (Class<? extends T>) c;
	}

}
