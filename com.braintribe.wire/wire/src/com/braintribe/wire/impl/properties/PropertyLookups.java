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
package com.braintribe.wire.impl.properties;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import com.braintribe.cfg.Required;
import com.braintribe.wire.api.annotation.Decrypt;
import com.braintribe.wire.api.annotation.Default;
import com.braintribe.wire.api.annotation.Name;
import com.braintribe.wire.impl.util.Exceptions;

/**
 * PropertyLookups is a proxy factory that creates proxies for interfaces that represent properties from some.
 * A generic lookup function is used to back the properties while the methods in the interface act as type safe accessor
 * with certain defaulting mechanisms.
 * 
 * Each method in the interface has to be of the following signatures:
 * 
 * <ul>
 * 	<li>property_type propertyName()
 * 	<li>property_type propertyName(property_type def)
 * 	<li>property_type propertyName()
 * 	<li>property_type propertyName()
 * </ul>
 * 
 * The property_type can be one of the following types:
 * 
 * <ul>
 * <li>{@link String}
 * <li>{@link Boolean} or boolean
 * <li>{@link Character} or char
 * <li>{@link Byte} or byte
 * <li>{@link Short} or short
 * <li>{@link Integer} or int
 * <li>{@link Long} or long
 * <li>{@link Float} or float
 * <li>{@link Double} or double
 * <li>{@link BigDecimal}
 * <li>{@link Date} patterns: <i>yyyy-MM-dd['T'HH[:mm[:ss[.SSS]]]][Z]</i> or <i>yyyyMMdd['T'HH[mm[ss[SSS]]]][Z]</i>
 * <li>{@link Duration} patterns: <i>P[nD]T[nH][nM][n.nS]</i>
 * <li>{@link File} 
 * <li>{@link Path}
 * <li>{@link Class}
 * <li>{@link Enum} subclasses
 * <li>generic collection types of the other types
 * <ul>
 *   <li>{@link List}
 *   <li>{@link Set}
 *   <li>{@link Map}
 * </ul>
 * </li>
 * </ul>
 * 
 * Additionally the methods can be annotated with:
 * 
 * <ul>
 * <li>{@link Default} to give parameter free accessors a default in the case the property is not given by the lookup
 * <li>{@link Name} to give an explicit property name that is not derived from the method name
 * <li>{@link Decrypt} to decrypt property values and default values which is useful for confidential data
 * <li>{@link Required} to make a non primitive typed property mandatory
 * </ul>
 * 
 * 
 * @author Dirk Scheffler
 *
 */
public class PropertyLookups implements InvocationHandler {

	private static final DateTimeFormatter DATETIME_FORMATTER = 
			new DateTimeFormatterBuilder().optionalStart().appendPattern("yyyy-MM-dd['T'HH[:mm[:ss[.SSS]]]][Z]").optionalEnd()
			.optionalStart().appendPattern("yyyyMMdd['T'HH[mm[ss[SSS]]]][Z]").optionalEnd()
		            .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
		            .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
		            .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
		            .parseDefaulting(ChronoField.NANO_OF_SECOND, 0)
		            .parseDefaulting(ChronoField.OFFSET_SECONDS, 0)
		            .toFormatter();
	
	private Function<String, String> lookup;
	
	private static Map<Type, Function<String, ?>> converters = new ConcurrentHashMap<>();

	private boolean suppressDecryption;
	
	private static <T> void registerConverter(Class<T> type, Function<String, T> converter) {
		converters.put(type, converter);
	}
	
	private static <T> void registerConverter(Class<T> type, Class<T> primitiveType, Function<String, T> converter) {
		converters.put(type, converter);
		converters.put(primitiveType, converter);
	}
	
	static {
		registerConverter(Boolean.class, boolean.class, Boolean::parseBoolean);
		registerConverter(Character.class, char.class, PropertyLookups::parseCharacter);
		registerConverter(Byte.class, byte.class, Byte::parseByte);
		registerConverter(Short.class, short.class, Short::parseShort);
		registerConverter(Integer.class, int.class, Integer::parseInt);
		registerConverter(Long.class, long.class, Long::parseLong);
		registerConverter(Float.class, float.class, Float::parseFloat);
		registerConverter(Double.class, double.class, Double::parseDouble);
		registerConverter(BigDecimal.class, BigDecimal::new);
		registerConverter(String.class, Function.identity());
		registerConverter(Date.class, PropertyLookups::parseDate);
		registerConverter(Duration.class, Duration::parse);
		registerConverter(File.class, File::new);
		registerConverter(Path.class, Paths::get);
		registerConverter(Class.class, PropertyLookups::forName);
	}
	
	private static Class<?> forName(String className) {
		try {
			return Class.forName(className);
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	private static Date parseDate(String date) {
		ZonedDateTime result = ZonedDateTime.parse(date, DATETIME_FORMATTER);
		return Date.from(result.toInstant());
	}
	
	private static Character parseCharacter(String s) {
		if (s.length() == 1)
			return s.charAt(0);
		else
			throw new IllegalArgumentException("'" + s + "' cannot be parsed to a Character"); 
	}
	
	private PropertyLookups(Function<String, String> lookup) {
		this(lookup, false);
	}
	
	private PropertyLookups(Function<String, String> lookup, boolean suppressDecryption) {
		super();
		this.lookup = lookup;
		this.suppressDecryption = suppressDecryption;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		String propertyName = getPropertyName(method);
		try {
			if (method.getDeclaringClass() == Object.class) {
				return method.invoke(this, args);
			}
			
			Type returnType = method.getGenericReturnType();
			String stringValue = lookup.apply(propertyName);
			Object value = null;
			
			Default defaultValue = getDefaultAndCheckValidity(method);
			
			// default fallback?
			if (stringValue == null && defaultValue != null) {
				stringValue = defaultValue.value();
			}
			
			if (stringValue != null) {
				stringValue = decryptIfRequired(method, stringValue);
				
				value = convert(method, propertyName, returnType, stringValue);
			}
			// parameter default value
			else if (args != null && args.length == 1) {
				value = args[0];
				
				if (value instanceof String) {
					value = decryptIfRequired(method, (String)value);
				}
			}
			
			if (value == null) {
				if (method.isAnnotationPresent(Required.class)) {
					throw new IllegalStateException("Mandatory property " + propertyName + " must be configured " + returnType);
				}
				else if (isPrimitive(returnType)) {
					Class<?> primitiveClass = (Class<?>)returnType;
					switch (primitiveClass.getName()) {
						case "boolean": value = Boolean.FALSE; break;
						case "char": value = (char)0; break;
						case "byte": value = (byte)0; break;
						case "short": value = (short)0; break;
						case "int": value = 0; break;
						case "long": value = 0L; break;
						case "float": value = 0.0F; break;
						case "double": value = 0.0D; break;
					}
				}
			}
			
			return value;
		}
		catch (Exception e) {
			throw Exceptions.contextualize(e, "Error while resolving property [" + propertyName + "] via " + method.getDeclaringClass().getName() + "." + method.getName());
		}
	}

	private Default getDefaultAndCheckValidity(Method method) {
		Default defaultValue = method.getAnnotation(Default.class);
		Class<?>[] parameterTypes = method.getParameterTypes();
		
		switch (parameterTypes.length) {
			case 0:
				break;
			case 1:
				if (defaultValue != null)
					throw new IllegalStateException("The property method " + method + " cannot have a @Default annotation and a default parameter");
				
				Class<?> argType = parameterTypes[0];
				Class<?> returnType = method.getReturnType();
				
				if (argType != returnType)
					throw new IllegalStateException("The property method " + method + " has a missmatch between default parameter type [" +  argType + "] and return type [" + returnType + "]");
				
				break;
			default:
				throw new IllegalStateException("The property method " + method + " cannot have more than one arguments");
		}
		
		return defaultValue;
	}

	private String decryptIfRequired(Method method, String stringValue) throws Exception {
		if (suppressDecryption)
			return stringValue;
		
		Decrypt decrypt = method.getAnnotation(Decrypt.class);
		
		if (decrypt != null) {
			String secret = decrypt.secret();
			String algorithm = decrypt.algorithm();
			String keyFactoryAlgorithm = decrypt.keyFactoryAlgorithm();
			int keyLength = decrypt.keyLength();
			
			switch (decrypt.secretResolution()) {
				case REFERENCE:
					secret = lookup.apply(secret);
					if (secret == null || secret.isEmpty()) {
						throw new IllegalArgumentException("missing secret resolution property: "  + decrypt.secret());
					}
					break;
				case VALUE:
					break;
				default:
					throw new IllegalArgumentException("unsupported secret resolution: " + decrypt.secretResolution());
			}
			
			stringValue = decrypt(secret, algorithm, keyFactoryAlgorithm, stringValue, keyLength, method);
		}
		return stringValue;
	}


	private boolean isPrimitive(Type returnType) {
		return returnType instanceof Class<?>?
				((Class<?>)returnType).isPrimitive():
				false;
	}

	private String getPropertyName(Method method) {
		Name name = method.getAnnotation(Name.class);
		return name != null? name.value(): method.getName();
	}
	
	private static Function<String, ?> buildConverter(Method method, Type type) {
		if (type instanceof Class<?>) {
			Class<?> enumCandidateClass = (Class<?>)type;
			if (enumCandidateClass.isEnum()) {
				@SuppressWarnings("rawtypes")
				Class<Enum> enumClass = (Class<Enum>) enumCandidateClass;
				return s -> Enum.valueOf(enumClass, s);
			}
		}
		
		try {
			ParameterizedType parameterizedType = (ParameterizedType)type;
			
			Class<?> rawType = (Class<?>)parameterizedType.getRawType();
			
			if (rawType == List.class) {
				return buildCollectionConverter(method, parameterizedType, ArrayList::new);
			}
			else if (rawType == Set.class) {
				return buildCollectionConverter(method, parameterizedType, LinkedHashSet::new);
			}
			else if (rawType == Map.class) {
				return buildMapConverter(method, parameterizedType);
			}
			else
				throw new IllegalStateException("The following method has an unsupported return type: " + method);
		}
		catch (ClassCastException e) {
			throw new IllegalStateException("The following method has an unsupported return type: " + method);
		}
	}

	private static CollectionConverter buildCollectionConverter(Method method, ParameterizedType parameterizedType, Supplier<Collection<Object>> factory) {
		Type[] types = parameterizedType.getActualTypeArguments();
		
		Class<?> elementType = (Class<?>) types[0];
		
		Function<String, ?> elementConverter = converters.get(elementType);
		
		if (elementConverter == null)
			throw new IllegalStateException("The following method has an unsupported return type: " + method);
		
		return new CollectionConverter(factory, elementConverter);
	}
	
	private static MapConverter buildMapConverter(Method method, ParameterizedType parameterizedType) {
		Type[] types = parameterizedType.getActualTypeArguments();
		
		Class<?> keyType = (Class<?>) types[0];
		Class<?> valueType = (Class<?>) types[1];
		
		Function<String, ?> keyConverter = converters.get(keyType);
		Function<String, ?> valueConverter = converters.get(valueType);
		
		if (keyConverter == null || valueConverter == null)
			throw new IllegalStateException("The following method has an unsupported return type: " + method);
		
		return new MapConverter(keyConverter, valueConverter);
	}
	
	private static class CollectionConverter implements Function<String, Collection<?>> {
		private Supplier<Collection<Object>> factory;
		private Function<String, ?> elementConverter;
		
		public CollectionConverter(Supplier<Collection<Object>> factory, Function<String, ?> elementConverter) {
			super();
			this.factory = factory;
			this.elementConverter = elementConverter;
		}

		@Override
		public Collection<?> apply(String t) {
			StringTokenizer tokenizer = new StringTokenizer(t, ",");
			
			Collection<Object> collection = factory.get();
			
			while (tokenizer.hasMoreTokens()) {
				String token = tokenizer.nextToken();
				
				String value;
				try {
					value = URLDecoder.decode(token, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					throw new IllegalStateException(e);
				}
				
				collection.add(elementConverter.apply(value));
			}
			
			return collection;
		}
	}
	
	private static class MapConverter implements Function<String, Map<?, ?>> {
		private Function<String, ?> keyConverter;
		private Function<String, ?> valueConverter;
		
		public MapConverter(Function<String, ?> keyConverter, Function<String, ?> valueConverter) {
			super();
			this.keyConverter = keyConverter;
			this.valueConverter = valueConverter;
		}
		
		@Override
		public Map<?, ?> apply(String t) {
			StringTokenizer tokenizer = new StringTokenizer(t, ",");
			
			Map<Object, Object> map = new LinkedHashMap<>();
			
			while (tokenizer.hasMoreTokens()) {
				String token = tokenizer.nextToken();
				
				int index = token.indexOf("=");
				
				if (index == -1)
					throw new IllegalArgumentException("Map entry is missing '=': " + token);
				
				try {
					String key = URLDecoder.decode(token.substring(0, index), "UTF-8");
					String value = URLDecoder.decode(token.substring(index + 1), "UTF-8");
					map.put(keyConverter.apply(key), valueConverter.apply(value));
				} catch (UnsupportedEncodingException e) {
					throw new IllegalStateException(e);
				}
			}
			
			return map;
		}
	}

	private Object convert(Method method, String propertyName, Type returnType, String value) {
		Function<String, ?> converter = converters.computeIfAbsent(returnType, t -> buildConverter(method, t));
		
		if (converter == null)
			throw new IllegalStateException("The following method has an unsupported return type: " + method);
		
		try {
			return converter.apply(value);
		}
		catch (Exception e) {
			throw new IllegalArgumentException("Could not convert [" + value + "] of property " + propertyName + " to " + returnType, e);
		}
	}
	
	public static <T> T create(Class<T> iface, Function<String, String> lookup) {
		return create(iface, lookup, false);
	}
	
	public static <T> T create(Class<T> iface, Function<String, String> lookup, boolean supressDecryption) {
		PropertyLookups lookups = new PropertyLookups(lookup, supressDecryption);
		return (T)Proxy.newProxyInstance(iface.getClassLoader(), new Class<?>[] {iface}, lookups);
	}
	
	public String decrypt(String secret, String algorithm, String keyFactoryAlgorithm, String contentEncryptedAndEncoded, int keyLength, Method method) throws Exception {
		
		final Cipher cipher;
		try {
			cipher = Cipher.getInstance(algorithm);
		}
		catch (Exception e) {
			throw new IllegalArgumentException("Unsupported cipher algorithm: " + algorithm, e);
		}

		final ByteBuffer buffer;
		
		try {
			buffer = ByteBuffer.wrap(Base64.getDecoder().decode(contentEncryptedAndEncoded));
		}
		catch (Exception e) {
			throw new IllegalArgumentException("Invalid Base64 content", e);
		}

		byte[] saltBytes = new byte[20];
		buffer.get(saltBytes, 0, saltBytes.length);
		byte[] ivBytes1 = new byte[cipher.getBlockSize()];
		buffer.get(ivBytes1, 0, ivBytes1.length);
		byte[] encryptedTextBytes = new byte[buffer.capacity() - saltBytes.length - ivBytes1.length];

		buffer.get(encryptedTextBytes);

		final SecretKeyFactory factory;
		
		try {
			// Deriving the key
			factory = SecretKeyFactory.getInstance(keyFactoryAlgorithm);
		}
		catch (Exception e) {
			throw new IllegalArgumentException("Unsupported secret key factory algorithm: " + keyFactoryAlgorithm);
		}
		
		
		final SecretKey secretKey;
		
		try {
			final PBEKeySpec spec = new PBEKeySpec(secret.toCharArray(), saltBytes, 65556, keyLength);
			secretKey = factory.generateSecret(spec);
		}
		catch (Exception e) {
			throw new IllegalStateException("Error while generating secret key for algorithm [" + algorithm + "] with secret key algorithm [" + keyFactoryAlgorithm + "] and key length [" + keyLength + "]", e);
		}

		try {
			SecretKeySpec secretSpec = new SecretKeySpec(secretKey.getEncoded(), "AES");
	
			cipher.init(Cipher.DECRYPT_MODE, secretSpec, new IvParameterSpec(ivBytes1));
	
			byte[] decryptedTextBytes = cipher.doFinal(encryptedTextBytes);
	
			String text = new String(decryptedTextBytes, "UTF-8");
	
			return text;
		}
		catch (Exception e) {
			throw new IllegalStateException("Error while encrypting value with algorithm [" + algorithm + "], secret key algorithm [" + keyFactoryAlgorithm + "] and key length [" + keyLength + "]", e);
		}
	}
}
