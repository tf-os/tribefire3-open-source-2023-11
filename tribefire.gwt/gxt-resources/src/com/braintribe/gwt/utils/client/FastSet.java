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
package com.braintribe.gwt.utils.client;

import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

@SuppressWarnings("serial")
public class FastSet extends AbstractSet<String> implements Serializable {
  private Map<String, String> map;
  private static final String PRESENT = "";

  public FastSet() {
    map = new FastMap<>();
  }
  
  public FastSet(Collection<String> collection) {
	  this();
	  collection.forEach(s -> map.put(s, PRESENT));
  }

  @Override
  public boolean add(String s) {
    return map.put(s, PRESENT) == null;
  }

  @Override
  public void clear() {
    map.clear();
  }

  @Override
  public boolean contains(Object o) {
    return map.containsKey(o);
  }

  @Override
  public boolean isEmpty() {
    return map.isEmpty();
  }

  @Override
  public Iterator<String> iterator() {
    return map.keySet().iterator();
  }

  @Override
  public boolean remove(Object o) {
    String s = map.remove(o);
    return s != null && s.equals(PRESENT);
  }

  @Override
  public int size() {
    return map.size();
  }

}
