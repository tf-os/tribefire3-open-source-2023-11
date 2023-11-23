// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2020 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

/*
 * Copyright 2007 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package java.lang;

import static javaemul.internal.InternalPreconditions.checkNotNull;

import java.io.Serializable;

import java.JsAnnotationsPackageNames;

import javaemul.internal.JsUtils;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;

/**
 * Wraps native <code>boolean</code> as an object.
 */
@SuppressWarnings("unusable-by-js")
public final class Boolean implements Comparable<Boolean>, Serializable {

  public static final Boolean FALSE = false;
  public static final Boolean TRUE = true;

  public static final Class<Boolean> TYPE = boolean.class;

  public static int compare(boolean x, boolean y) {
    return (x == y) ? 0 : (x ? 1 : -1);
  }

  public static int hashCode(boolean value) {
    // The Java API doc defines these magic numbers.
    return value ? 1231 : 1237;
  }

  public static boolean logicalAnd(boolean a, boolean b) {
    return a && b;
  }

  public static boolean logicalOr(boolean a, boolean b) {
    return a || b;
  }

  public static boolean logicalXor(boolean a, boolean b) {
    return a ^ b;
  }

  public static boolean parseBoolean(String s) {
    return "true".equalsIgnoreCase(s);
  }

  public static String toString(boolean x) {
    return String.valueOf(x);
  }

  @JsMethod(name="valueOfBoolean")
  public static Boolean valueOf(boolean b) {
    return b ? $create(true) : $create(false);
  }

  public static Boolean valueOf(String s) {
    return valueOf(parseBoolean(s));
  }

  public Boolean(boolean value) {
    /*
     * Call to $create(value) must be here so that the method is referenced and not pruned
     * before new Boolean(value) is replaced by $create(value) by
     * RewriteConstructorCallsForUnboxedTypes.
     */
    $create(value);
  }

  @JsIgnore
  public Boolean(String s) {
    this(parseBoolean(s));
  }

  public boolean booleanValue() {
    return JsUtils.unsafeCastToBoolean(checkNotNull(this));
  }

  @Override
  public int compareTo(Boolean b) {
    return compare(booleanValue(), b.booleanValue());
  }

  @Override
  public boolean equals(Object o) {
    return checkNotNull(this) == o;
  }

  @Override
  public int hashCode() {
    return hashCode(booleanValue());
  }

  @Override
  public String toString() {
    return toString(booleanValue());
  }

  // CHECKSTYLE_OFF: Utility Methods for unboxed Boolean.
  protected static Boolean $create(boolean x) {
    return createNative(x);
  }

  protected static Boolean $create(String x) {
    return createNative(Boolean.parseBoolean(x));
  }

  private static native Boolean createNative(boolean x) /*-{
    return x;
  }-*/;

  @JsMethod
  protected static boolean $isInstance(Object instance) {
    return "boolean".equals(JsUtils.typeOf(instance));
  }
  //CHECKSTYLE_ON: End utility methods
}
