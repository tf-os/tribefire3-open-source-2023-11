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
package com.braintribe.model.generic.tools;

import static com.braintribe.utils.lcd.CollectionTools2.asMap;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.braintribe.common.lcd.UnknownEnumException;
import com.braintribe.exception.Exceptions;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.generic.value.EnumReference;

/**
 * @author peter.gazdik
 */
public class GmValueCodec {

	public static final String NULL_STRING = "null";

	private static final Function<ParsingContext, Object> STANDARD_PARSER = GmValueCodec::objectFromGmString;

	public static class ParsingContext {
		/* package */ String element;
		/* package */ EnumParsingMode enumParsingMode;
		/* package */ ElementType elementType;

		// @formatter:off
		public String element() { return element; }
		public EnumParsingMode enumParsingMode() { return enumParsingMode; }
		public ElementType elementType() { return elementType; }
		// @formatter:on
	}

	public static enum EnumParsingMode {
		enumAsValue,
		enumAsReference,
		enumAsStringArray,
	}

	public static enum ElementType {
		listElement,
		setElement,
		mapKey,
		mapValue
	}

	//
	// DO NOT ADD ANY DEPENDENCY ON GM REFLECTION!!!
	//
	// This class is used when parsing the @Initializer annotation, so it could be accessed when initializing the GM
	// reflection. Therefore let's avoid cyclic dependencies. There are also cases where this is reachable and GM
	// reflection is not available at all (e.g. GmCore4Jvm not on the classpath).
	//

	// ###############################################
	// ## . . . . . . . Stringifying . . . . . . . .##
	// ###############################################

	/**
	 * @param value
	 *            cannot be null!
	 */
	public static String objectToGmString(Object value) {
		Object o = value;

		if (o == null)
			return NULL_STRING;

		if (o instanceof Enum<?>)
			return enumToGmString((Enum<?>) o);

		if (o instanceof EnumReference)
			return enumReferenceToGmString((EnumReference) o);

		if (o instanceof String)
			return stringToGmString((String) o);

		if (o instanceof Integer)
			return integerToGmString((Integer) o);

		if (o instanceof Boolean)
			return booleanToGmString((Boolean) o);

		if (o instanceof BigDecimal)
			return decimalToGmString((BigDecimal) o);

		if (o instanceof Long)
			return longToGmString((Long) o);

		if (o instanceof Float)
			return floatToGmString((Float) o);

		if (o instanceof Double)
			return doubleToGmString((Double) o);

		if (o instanceof Date)
			return dateToGmString((Date) o);

		if (o instanceof List)
			return listToGmString((List<?>) o);

		if (o instanceof Set)
			return setToGmString((Set<?>) o);

		if (o instanceof Map)
			return stringifyMap((Map<?, ?>) o);

		throw new IllegalArgumentException("Unsupported initializer type: " + o.getClass() + ". Actual value: " + o);
	}

	public static String booleanToGmString(Boolean value) {
		return value.toString();
	}

	public static String integerToGmString(Integer value) {
		return value.toString();
	}

	public static String longToGmString(Long value) {
		return value.toString() + "L";
	}

	public static String floatToGmString(Float value) {
		return value.toString() + "F";
	}

	public static String doubleToGmString(Double value) {
		return value.toString() + "D";
	}

	public static String decimalToGmString(BigDecimal value) {
		return value.toString() + "B";
	}

	public static String stringToGmString(String value) {
		return "'" + value.toString() + "'";
	}

	public static String dateToGmString(Date value) {
		return "GMT:" + Long.toString(value.getTime());
	}

	public static String enumToGmString(Enum<?> value) {
		return enumToGmString(value.getClass().getName(), value.toString());
	}

	public static String enumReferenceToGmString(EnumReference ref) throws GenericModelException {
		return enumToGmString(ref.getTypeSignature(), ref.getConstant());
	}

	public static String enumToGmString(String enumTypeSignature, String constant) throws GenericModelException {
		return "enum(" + enumTypeSignature + "," + constant + ")";
	}

	public static String listToGmString(List<?> list) {
		return collectionToGmString(list, "[", "]");
	}

	public static String setToGmString(Set<?> set) {
		return collectionToGmString(set, "{", "}");
	}

	public static String stringifyMap(Map<?, ?> map) {
		return streamToGmString(mapToStream(map), "map[", "]");
	}

	private static Stream<Object> mapToStream(Map<?, ?> map) {
		return map.entrySet().stream().flatMap(e -> Stream.of(e.getKey(), e.getValue()));
	}

	private static String collectionToGmString(Collection<?> collection, String prefix, String suffix) {
		return streamToGmString(collection.stream(), prefix, suffix);
	}

	public static String streamToGmString(Stream<?> stream, String prefix, String suffix) {
		return stream //
				.map(GmValueCodec::objectToGmString) //
				.map(GmValueCodec::escapeSeparator) //
				.collect(Collectors.joining(",", prefix, suffix));
	}

	// ###############################################
	// ## . . . . . . . . Parsing . . . . . . . . . ##
	// ###############################################

	public static Object objectFromGmString(String s) {
		return objectFromGmString(s, EnumParsingMode.enumAsValue, STANDARD_PARSER);
	}

	public static Object objectFromGmString(String s, EnumParsingMode enumParsingMode) {
		return objectFromGmString(s, enumParsingMode, STANDARD_PARSER);
	}

	private static Object objectFromGmString(ParsingContext ctx) {
		return objectFromGmString(ctx.element, ctx.enumParsingMode, STANDARD_PARSER);
	}

	public static Object objectFromGmString(String s, EnumParsingMode enumParsingMode, BiFunction<String, EnumParsingMode, Object> elementParser) {
		return objectFromGmString(s, enumParsingMode, convertElementParser(elementParser));
	}

	public static Object objectFromGmString(String s, EnumParsingMode enumParsingMode, Function<ParsingContext, Object> elementParser) {
		if (s.substring(0, 1).matches("[0-9+-]"))
			return numberFromString(s);

		if (s.startsWith("'"))
			return stringFromGmString(s);

		if (s.equals(NULL_STRING))
			return null;

		if (s.startsWith("enum("))
			return enumFromGmString(s, enumParsingMode);

		if (s.endsWith("e") || s.endsWith("E")) // truE or falsE
			return booleanFromGmString(s);

		if (s.startsWith("GMT:"))
			return dateFromGmString(s);

		if (s.startsWith("["))
			return listFromString(s, enumParsingMode, elementParser);

		if (s.startsWith("map["))
			return mapFromString(s, enumParsingMode, elementParser);

		if (s.startsWith("{"))
			return setFromString(s, enumParsingMode, elementParser);

		throw new IllegalArgumentException("Cannot parse initializer string: " + s);
	}

	private static Object numberFromString(String s) {
		s = s.toLowerCase();

		if (s.endsWith("l"))
			return longFromGmString(s);

		if (s.endsWith("f"))
			return floatFromGmString(s);

		if (s.endsWith("d"))
			return doubleFromGmString(s);

		if (s.endsWith("b"))
			return decimalFromGmString(s);

		return integerFromGmString(s);
	}

	public static Boolean booleanFromGmString(String encodedValue) {
		if ("true".equalsIgnoreCase(encodedValue))
			return true;

		if ("false".equalsIgnoreCase(encodedValue))
			return false;

		throw new IllegalArgumentException("Cannot parse initializer string: " + encodedValue);
	}

	public static Integer integerFromGmString(String encodedValue) {
		return Integer.parseInt(encodedValue);
	}

	public static Long longFromGmString(String encodedValue) {
		return Long.parseLong(encodedValue.substring(0, encodedValue.length() - 1));
	}

	public static Float floatFromGmString(String encodedValue) {
		return Float.parseFloat(encodedValue);
	}

	public static Double doubleFromGmString(String encodedValue) {
		return Double.parseDouble(encodedValue);
	}

	public static BigDecimal decimalFromGmString(String encodedValue) {
		return new BigDecimal(encodedValue.substring(0, encodedValue.length() - 1));
	}

	public static String stringFromGmString(String encodedValue) {
		String s = encodedValue;
		return s.substring(1, s.length() - 1);
	}

	public static Date dateFromGmString(String encodedValue) {
		int index = "GMT:".length();
		String s = encodedValue.substring(index);

		try {
			return new Date(Long.parseLong(s));
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "error while creating date from string " + encodedValue);
		}
	}

	public static Object enumFromGmString(String encodedValue, EnumParsingMode enumParstingMode) {
		switch (enumParstingMode) {
			case enumAsReference:
				return enumReferenceFromGmString(encodedValue);
			case enumAsValue:
				return enumFromGmString(encodedValue);
			case enumAsStringArray:
				return parseEnumConstantIdentifier(encodedValue);
			default:
				throw new UnknownEnumException(enumParstingMode);

		}
	}

	public static <E extends Enum<E>> E enumFromGmString(String encodedValue) {
		String[] parsedEnum = parseEnumConstantIdentifier(encodedValue);
		return Enum.valueOf((Class<E>) getEnumClass(parsedEnum[0]), parsedEnum[1]);
	}

	public static Object enumReferenceFromGmString(String encodedValue) {
		String[] parsedEnum = parseEnumConstantIdentifier(encodedValue);

		EnumReference result = EnumReference.T.createPlain();
		result.setTypeSignature(parsedEnum[0]);
		result.setConstant(parsedEnum[1]);

		return result;
	}

	private static Class<?> getEnumClass(String className) {
		return GwtCompatibilityUtils.getEnumClass(className);
	}

	/**
	 * Assuming that <tt>enumConstantIdentifier</tt> is a valid enum identifier (e.g. enum(com.bt.model.Color,green)), this returns an array of size
	 * two, with first element being enum signature and second being enum constant name.
	 */
	public static String[] parseEnumConstantIdentifier(String enumConstantIdentifier) {
		return enumConstantIdentifier.substring(5 /* "enum(" */, enumConstantIdentifier.length() - 1).split(",");
	}

	public static List<?> listFromString(String s) {
		return listFromString(s, EnumParsingMode.enumAsValue, STANDARD_PARSER);
	}

	public static List<?> listFromString(String s, EnumParsingMode enumParsingMode, BiFunction<String, EnumParsingMode, Object> elementParser) {
		return listFromString(s, enumParsingMode, convertElementParser(elementParser));
	}

	public static List<?> listFromString(String s, EnumParsingMode enumParsingMode, Function<ParsingContext, Object> elementParser) {
		return linearCollectionFromString(s, "[", "]", enumParsingMode, ElementType.listElement, elementParser).collect(Collectors.toList());
	}

	public static Set<?> setFromString(String s) {
		return setFromString(s, EnumParsingMode.enumAsValue, STANDARD_PARSER);
	}

	public static Set<?> setFromString(String s, EnumParsingMode enumParsingMode, BiFunction<String, EnumParsingMode, Object> elementParser) {
		return setFromString(s, enumParsingMode, convertElementParser(elementParser));
	}

	public static Set<?> setFromString(String s, EnumParsingMode enumParsingMode, Function<ParsingContext, Object> elementParser) {
		return linearCollectionFromString(s, "{", "}", enumParsingMode, ElementType.setElement, elementParser).collect(Collectors.toSet());
	}

	public static Stream<?> linearCollectionFromString(String s, String startingChar, String endingChar) {
		ElementType elementType = "[".equals(startingChar) ? ElementType.listElement : ElementType.setElement;
		return linearCollectionFromString(s, startingChar, endingChar, EnumParsingMode.enumAsValue, elementType, STANDARD_PARSER);
	}

	public static Stream<?> linearCollectionFromString(String s, String startingChar, String endingChar, EnumParsingMode enumParsingMode,
			ElementType elementType, Function<ParsingContext, Object> elementParser) {

		ParsingContext ctx = new ParsingContext();
		ctx.elementType = elementType;
		ctx.enumParsingMode = enumParsingMode;

		return splitIntoParts(s, startingChar, endingChar) //
				.map(_s -> elementParser.apply(withElement(ctx, _s)));
	}

	private static ParsingContext withElement(ParsingContext ctx, String element) {
		ctx.element = element;
		return ctx;
	}

	public static Map<?, ?> mapFromString(String s) {
		return mapFromString(s, EnumParsingMode.enumAsValue, STANDARD_PARSER);
	}

	public static Map<?, ?> mapFromString(String s, EnumParsingMode enumParsingMode, BiFunction<String, EnumParsingMode, Object> elementParser) {
		Object[] parsedElements = splitIntoParts(s, "map[", "]") //
				.map(_s -> elementParser.apply(_s, enumParsingMode)) //
				.toArray();

		return asMap(parsedElements);
	}

	public static Map<?, ?> mapFromString(String s, EnumParsingMode enumParsingMode, Function<ParsingContext, Object> elementParser) {
		ParsingContext ctx = new ParsingContext();
		ctx.elementType = ElementType.mapValue;
		ctx.enumParsingMode = enumParsingMode;

		Object[] parsedElements = splitIntoParts(s, "map[", "]") //
				.map(_s -> elementParser.apply(withKeyOrValue(ctx, _s))) //
				.toArray();

		return asMap(parsedElements);
	}

	private static ParsingContext withKeyOrValue(ParsingContext ctx, String keyOrValue) {
		ctx.element = keyOrValue;
		ctx.elementType = ctx.elementType == ElementType.mapValue ? ElementType.mapKey : ElementType.mapValue;
		return ctx;
	}

	private static Stream<String> splitIntoParts(String s, String startingChar, String endingChar) {
		if (!s.endsWith(endingChar))
			throw new IllegalArgumentException(
					"The initializer value " + s + " starts with a '" + startingChar + "' but does not end with '" + endingChar + "'");

		s = s.substring(startingChar.length(), s.length() - endingChar.length());
		final String splitRegex = "(?<!,),(?!,)";

		return Stream.of(s.split(splitRegex)) //
				.map(GmValueCodec::unescapeSeparator);
	}

	// #################################################
	// ## . . . . . . . . . Helpers . . . . . . . . . ##
	// #################################################

	private static Function<ParsingContext, Object> convertElementParser(BiFunction<String, EnumParsingMode, Object> elementParser) {
		return ctx -> elementParser.apply(ctx.element, ctx.enumParsingMode);
	}

	private static String escapeSeparator(String s) {
		return s.replaceAll(",", ",,");
	}

	private static String unescapeSeparator(String s) {
		return s.replaceAll(",,", ",");
	}

}
