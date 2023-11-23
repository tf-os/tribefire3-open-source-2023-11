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
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;

@SuppressWarnings("serial")
public class FastMap<V> extends AbstractMap<String, V> implements Serializable {
  private static class FastMapEntry<V> implements Map.Entry<String, V> {

    private String key;
    private V value;

    FastMapEntry(String key, V value) {
      this.key = key;
      this.value = value;
    }

    @Override
    public boolean equals(Object a) {
      if (a instanceof Map.Entry<?, ?>) {
        Map.Entry<?, ?> s = (Map.Entry<?, ?>) a;
        if (equalsWithNullCheck(key, s.getKey()) && equalsWithNullCheck(value, s.getValue())) {
          return true;
        }
      }
      return false;
    }

    @Override
	public String getKey() {
      return key;
    }

    @Override
	public V getValue() {
      return value;
    }

    @Override
    public int hashCode() {
      int keyHash = 0;
      int valueHash = 0;
      if (key != null) {
        keyHash = key.hashCode();
      }
      if (value != null) {
        valueHash = value.hashCode();
      }
      return keyHash ^ valueHash;
    }

    @Override
	public V setValue(V object) {
      V old = value;
      value = object;
      return old;
    }

    private boolean equalsWithNullCheck(Object obj1, Object obj2) {
      return equalWithNull(obj1, obj2);
    }
  }
  
  private static boolean equalWithNull(Object obj1, Object obj2) {
    if (obj1 == obj2) {
      return true;
    } else if (obj1 == null) {
      return false;
    } else {
      return obj1.equals(obj2);
    }
  }

  private static class JsMap<V> extends JavaScriptObject {

    public static FastMap.JsMap<?> create() {
      return JavaScriptObject.createObject().cast();
    }

    protected JsMap() {
    }

    public final native boolean containsKey(String key)/*-{
      return this.hasOwnProperty(key);
    }-*/;

    public final native V get(String key) /*-{
      return this[key];
    }-*/;

    public final native List<String> keySet() /*-{
      var s = @java.util.ArrayList::new()();
      for(var key in this) {
      if (!this.hasOwnProperty(key)) continue;
      s.@java.util.ArrayList::add(Ljava/lang/Object;)(key);
      }
      return s;
    }-*/;

    public final native V put(String key, V value) /*-{
      var previous = this[key];
      this[key] = value;
      return previous;
    }-*/;

    public final native V remove(String key) /*-{
      var previous = this[key];
      delete this[key];
      return previous;
    }-*/;

    public final native int size() /*-{
      var count = 0;
      for(var key in this) {
      if (this.hasOwnProperty(key)) ++count;
      }
      return count;
    }-*/;

    public final native List<V> values() /*-{
      var s = @java.util.ArrayList::new()();
      for(var key in this) {
      if (!this.hasOwnProperty(key)) continue;
      s.@java.util.ArrayList::add(Ljava/lang/Object;)(this[key]);
      }
      return s;
    }-*/;
  }

  private transient HashMap<String, V> javaMap;
  private transient FastMap.JsMap<V> map;

  public FastMap() {
    if (GWT.isScript()) {
      map = JsMap.create().cast();
    } else {
      javaMap = new HashMap<String, V>();
    }
  }

  @Override
  public void clear() {
    if (GWT.isScript()) {
      map = JsMap.create().cast();
    } else {
      javaMap.clear();
    }
  }

  @Override
  public boolean containsKey(Object key) {
    if (GWT.isScript()) {
      return map.containsKey(String.valueOf(key));
    } else {
      return javaMap.containsKey(key);
    }
  }

  @Override
  public boolean containsValue(Object value) {
    return values().contains(value);
  }

  @Override
  public Set<java.util.Map.Entry<String, V>> entrySet() {
    if (GWT.isScript()) {
      return new AbstractSet<Map.Entry<String, V>>() {

        @Override
        public boolean contains(Object key) {
          Map.Entry<?, ?> s = (Map.Entry<?, ?>) key;
          Object value = get(s.getKey());
          if (value == null) {
            return value == s.getValue();
          } else {
            return value.equals(s.getValue());
          }
        }

        @Override
        public Iterator<Map.Entry<String, V>> iterator() {

          Iterator<Map.Entry<String, V>> custom = new Iterator<Map.Entry<String, V>>() {
            Iterator<String> keys = keySet().iterator();

            @Override
			public boolean hasNext() {
              return keys.hasNext();
            }

            @Override
			public Map.Entry<String, V> next() {
              String key = keys.next();
              return new FastMapEntry<V>(key, get(key));
            }

            @Override
			public void remove() {
              keys.remove();
            }
          };
          return custom;
        }

        @Override
        public int size() {
          return FastMap.this.size();
        }

      };
    } else {
      return javaMap.entrySet();
    }
  }

  @Override
  public V get(Object key) {
    if (GWT.isScript()) {
      return map.get(String.valueOf(key));
    } else {
      return javaMap.get(key);
    }
  }

  @Override
  public boolean isEmpty() {
    return size() == 0;
  }

  @Override
  public Set<String> keySet() {
    if (GWT.isScript()) {
      return new AbstractSet<String>() {
        @Override
        public boolean contains(Object key) {
          return FastMap.this.containsKey(key);
        }

        @Override
        public Iterator<String> iterator() {
          return map.keySet().iterator();
        }

        @Override
        public int size() {
          return FastMap.this.size();
        }
      };
    } else {
      return javaMap.keySet();
    }
  }

  @Override
  public V put(String key, V value) {
    if (GWT.isScript()) {
      return map.put(key, value);
    } else {
      return javaMap.put(key, value);
    }
  }

  @Override
  public void putAll(Map<? extends String, ? extends V> m) {
    if (GWT.isScript()) {
      for (String s : m.keySet()) {
        map.put(s, m.get(s));
      }
    } else {
      javaMap.putAll(m);
    }
  }

  @Override
  public V remove(Object key) {
    if (GWT.isScript()) {
      return map.remove((String) key);
    } else {
      return javaMap.remove(key);
    }
  }

  @Override
  public int size() {
    if (GWT.isScript()) {
      return map.size();
    } else {
      return javaMap.size();
    }
  }

  @Override
  public Collection<V> values() {
    if (GWT.isScript()) {
      return map.values();
    } else {
      return javaMap.values();
    }
  }
}
