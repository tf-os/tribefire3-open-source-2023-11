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
package com.braintribe.gwt.async.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;


import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class RuntimeConfiguration {
	private static RuntimeConfiguration instance;
	
	public static RuntimeConfiguration getInstance() {
		if (instance == null) {
			instance = new RuntimeConfiguration();
		}

		return instance;
	}
	
	private Map<String, String> properties = new HashMap<String, String>();
	
	public Future<Boolean> loadConfiguration() {
		return loadConfiguration(null);
	}
	
	public Future<Boolean> loadConfiguration(String individualConfigContext) {
		
		final Future<Boolean> future = new Future<Boolean>();
		
		// extract context name for the module
		String baseUrl = GWT.getHostPageBaseURL();
		if (baseUrl.endsWith("/"))
			baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
		
		int index = baseUrl.lastIndexOf('/');
		String moduleContextName = baseUrl.substring(index + 1);
		if (individualConfigContext == null)
			individualConfigContext = moduleContextName + "-config";
		
		LoaderChainImpl
				.begin(AsyncUtils.loadStringResource("../../" + individualConfigContext + "/runtimeConfiguration.xml"))
				.decode(new RuntimeConfigurationPropertiesCodec())
				.load(new AsyncCallback<Map<String,String>>() {
					@Override
					public void onSuccess(Map<String, String> result) {
						properties = result;
						future.onSuccess(true);
					}
					
					@Override
					public void onFailure(Throwable caught) {
						if (caught instanceof StatusCodeException) {
							StatusCodeException scex = (StatusCodeException)caught;
							if (scex.getStatusCode() == 404) {
								properties = new HashMap<String, String>();
								future.onSuccess(true);
							} else
								future.onFailure(caught);
						} else 
							future.onFailure(caught);
					}
				});
		
		return future;
	}
	
	public String getProperty(String name, String def) {
		return properties.containsKey(name) ? properties.get(name) : def;
	}
	
	public String getProperty(String name) {
		return getProperty(name, null);
	}
	
	public Integer getIntegerProperty(String name, Integer def) {
		return properties.containsKey(name) ? new Integer(properties.get(name)) : def;
	}
	
	public Double getDoubleProperty(String name, Double def) {
		return properties.containsKey(name) ? new Double(properties.get(name)) : def;
	}
	
	public Boolean getBooleanProperty(String name, Boolean def) {
		return properties.containsKey(name) ? Boolean.valueOf(properties.get(name)) : def;
	}
	
	protected static Function<String, ?> getDecoder(Class<?> valueClass) {
		if (valueClass == Integer.class) {
			return new Function<String, Integer>() {
				@Override
				public Integer apply(String index) throws RuntimeException {
					return new Integer(index);
				}
			};
		}
		else if (valueClass == Boolean.class) {
			return new Function<String, Boolean>() {
				@Override
				public Boolean apply(String index) throws RuntimeException {
					return Boolean.valueOf(index);
				}
			};
		}
		else if (valueClass == Double.class) {
			return new Function<String, Double>() {
				@Override
				public Double apply(String index) throws RuntimeException {
					return new Double(index);
				}
			};
		}
		else if (valueClass == String.class) {
			return new Function<String, String>() {
				@Override
				public String apply(String index) throws RuntimeException {
					return index;
				}
			};
		}
		else throw new RuntimeException("RuntimeConfiguration has no decoder for value class " + valueClass);
	}
	
	public <T> List<T> getPropertyList(Class<T> valueClass, String name, List<T> def) {
		if (!properties.containsKey(name)) return def; 
		
		Function<String, T> decoder = (Function<String, T>)getDecoder(valueClass);

		String encodedList = properties.get(name);
		String encodedValues[] = encodedList.split(",");
		List<T> values = new ArrayList<T>(encodedValues.length);
		
		for (String encodedValue: encodedValues) {
			encodedValue = URL.decode(encodedValue);
			try {
				T value = decoder.apply(encodedValue);
				values.add(value);
			} catch (RuntimeException e) {
				throw new RuntimeException("could not decode the value " + encodedValue + " to an instance of " + valueClass);
			}
		}
		
		return values;
	}
	
	public List<String> getPropertyList(String name, List<String> def) {
		return getPropertyList(String.class, name, def);
	}
	
	public List<Integer> getIntegerPropertyList(String name, List<Integer> def) {
		return getPropertyList(Integer.class, name, def);
	}
	
	public List<Double> getDoublePropertyList(String name, List<Double> def) {
		return getPropertyList(Double.class, name, def);
	}
	
	public List<Boolean> getBooleanPropertyList(String name, List<Boolean> def) {
		return getPropertyList(Boolean.class, name, def);
	}
	
	public <K, V> Map<K, V> getPropertyMap(Class<K> keyClass, Class<V> valueClass, String name, Map<K, V> def) {
		if (!properties.containsKey(name)) return def; 
		
		Function<String, K> keyDecoder = (Function<String, K>)getDecoder(keyClass);
		Function<String, V> valueDecoder = (Function<String, V>)getDecoder(valueClass);

		String encodedList = properties.get(name);
		String encodedValues[] = encodedList.split(",");

		Map<K, V> values = new HashMap<K, V>(encodedValues.length);
		
		for (String encodedPair: encodedValues) {
			int index = encodedPair.indexOf(":");
			if (index == -1) throw new RuntimeException("invalid runtimeConfiguration syntax for map pair: " + encodedPair);
			
			String encodedKey = URL.decode(encodedPair.substring(0, index));
			String encodedValue = URL.decode(encodedPair.substring(index + 1));
			try {
				K key = keyDecoder.apply(encodedKey);
				V value = valueDecoder.apply(encodedValue);
				values.put(key, value);
			} catch (RuntimeException e) {
				throw new RuntimeException("could not decode the pair key = " + encodedKey + " value = " + encodedValue, e);
			}
		}
		
		return values;
	}
	
	public Map<String, String> getPropertyMap(String name, Map<String, String> def) {
		return getPropertyMap(String.class, String.class, name, def);
	}
	
	public Map<String, Integer> getIntegerPropertyMap(String name, Map<String, Integer> def) {
		return getPropertyMap(String.class, Integer.class, name, def);
	}
	
	public Map<String, Boolean> getBooleanPropertyMap(String name, Map<String, Boolean> def) {
		return getPropertyMap(String.class, Boolean.class, name, def);
	}
	
	public Map<String, Double> getDoublePropertyMap(String name, Map<String, Double> def) {
		return getPropertyMap(String.class, Double.class, name, def);
	}

	public void put(String key, String value) {
		properties.put(key, value);
	}
	
	public void putAll(Map<String, String> values) {
		properties.putAll(values);
	}
}
