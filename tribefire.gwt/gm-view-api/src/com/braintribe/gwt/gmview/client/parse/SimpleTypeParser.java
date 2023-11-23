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
package com.braintribe.gwt.gmview.client.parse;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import com.braintribe.gwt.gmview.action.client.ParserResult;
import com.braintribe.gwt.gmview.client.parse.expert.DecimalParser;
import com.braintribe.gwt.gmview.client.parse.expert.DoubleParser;
import com.braintribe.gwt.gmview.client.parse.expert.FloatParser;
import com.braintribe.gwt.gmview.client.parse.expert.IntegerParser;
import com.braintribe.gwt.gmview.client.parse.expert.LocalizedBooleanParser;
import com.braintribe.gwt.gmview.client.parse.expert.LocalizedDateParser;
import com.braintribe.gwt.gmview.client.parse.expert.LongParser;
import com.braintribe.gwt.gmview.client.parse.expert.StringParser;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.reflection.SimpleType;
import com.braintribe.model.generic.typecondition.TypeCondition;


/**
 * Prioritized registry of parsers for all the {@link SimpleType}s.
 */
public class SimpleTypeParser implements Function<ParserArgument, List<ParserResult>> {

	public static final String DEFAULT_LOCALE = "en";
	private Supplier<String> localeProvider = () -> DEFAULT_LOCALE;
	protected Map<SimpleType, Function<String, ?>> valueParsers;

	@Configurable
	public void setLocaleProvider(Supplier<String> localeProvider) {
		this.localeProvider = localeProvider;
	}

	public SimpleTypeParser() {
		initializeValueParsers();
	}

	private void initializeValueParsers() {
		valueParsers = new LinkedHashMap<SimpleType, Function<String, ?>>();

		// #############################################################################################
		// ## . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . ##
		// ## . . THE ORDER IS IMPORTANT - TYPES ARE SORTED FROM MOST PROBABLE TO LEAST PROBABLE . . .##
		// ## . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . ##
		// #############################################################################################

		registerSimpleTypeParser(GenericModelTypeReflection.TYPE_INTEGER, new IntegerParser());
		registerSimpleTypeParser(GenericModelTypeReflection.TYPE_LONG, new LongParser());

		registerSimpleTypeParser(GenericModelTypeReflection.TYPE_FLOAT, new FloatParser());
		registerSimpleTypeParser(GenericModelTypeReflection.TYPE_DOUBLE, new DoubleParser());
		registerSimpleTypeParser(GenericModelTypeReflection.TYPE_DECIMAL, new DecimalParser());

		registerSimpleTypeParser(GenericModelTypeReflection.TYPE_DATE, new LocalizedDateParser(this));

		registerSimpleTypeParser(GenericModelTypeReflection.TYPE_BOOLEAN, new LocalizedBooleanParser(this));
		registerSimpleTypeParser(GenericModelTypeReflection.TYPE_STRING, new StringParser());
	}

	private void registerSimpleTypeParser(SimpleType type, Function<String, ?> parser) {
		valueParsers.put(type, parser);
	}
	
	/**
	 * For given value provides a map containing all possible {@link SimpleType}s this value may be converted to. The
	 * entries in the resulting map are sorted by priority - according to what is the most probable type the value may
	 * represent. This order corresponds to the order of iteration through the entries, with more probable types being
	 * first.
	 */
	@Override
	public List<ParserResult> apply(ParserArgument pa) {
		List<ParserResult> result = new ArrayList<>();

		if (pa.hasValue()) {
			addResultForNonEmptyValue(result, pa);
		} else {
			addResultForEmptyValue(result, pa);
		}
		
		return result;
	}
	
	protected void addResultForEmptyValue(List<ParserResult> result, ParserArgument parserArgument) {
		for (Map.Entry<SimpleType, Function<String, ?>> parserEntry : valueParsers.entrySet()) {
			SimpleType type = parserEntry.getKey();
			
			if (matchingType(parserArgument, type)) {
				String typeSignature = type.getTypeSignature();
				result.add(new ParserResult(typeSignature, typeSignature, emptyValueFor(parserArgument.getValue(), type)));
			}
		}
	}

	private Object emptyValueFor(String value, SimpleType type) {
		// for String return the original value, for everything else return null
		return GenericModelTypeReflection.TYPE_STRING == type ? value : null;
	}

	protected void addResultForNonEmptyValue(List<ParserResult> result, ParserArgument parserArgument) {
		for (Map.Entry<SimpleType, Function<String, ?>> parserEntry : valueParsers.entrySet()) {
			SimpleType type = parserEntry.getKey();

			if (matchingType(parserArgument, type)) {
				Function<String, ?> parser = parserEntry.getValue();

				Object parsedValue = safeParse(parser, parserArgument.getValue().trim());

				if (parsedValue != null) {
					String typeSignature = type.getTypeSignature();
					result.add(new ParserResult(typeSignature, typeSignature, parsedValue));
				}
			}
		}
	}

	protected boolean matchingType(ParserArgument parserArgument, SimpleType type) {
		TypeCondition tc = parserArgument.getTypeCondition();
		return tc == null || tc.matches(type);
	}

	protected Object safeParse(Function<String, ?> parser, String value) {
		try {
			return parser.apply(value);

		} catch (Exception e) {
			return null;
		}
	}

	public String locale() {
		return localeProvider.get();
	}

}
