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
package com.braintribe.model.processing.query.parser.impl.listener;

import static com.braintribe.utils.lcd.StringTools.getLastNCharacters;
import static com.braintribe.utils.lcd.StringTools.removeLastNCharacters;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.antlr.v4.runtime.ParserRuleContext;

import com.braintribe.model.generic.value.Variable;
import com.braintribe.model.processing.query.parser.api.GmqlQueryParserException;
import com.braintribe.model.processing.query.parser.impl.autogenerated.GmqlParser.BooleanValueContext;
import com.braintribe.model.processing.query.parser.impl.autogenerated.GmqlParser.CalendarOffsetValueContext;
import com.braintribe.model.processing.query.parser.impl.autogenerated.GmqlParser.CollectionContext;
import com.braintribe.model.processing.query.parser.impl.autogenerated.GmqlParser.DateOffsetContext;
import com.braintribe.model.processing.query.parser.impl.autogenerated.GmqlParser.DateValueContext;
import com.braintribe.model.processing.query.parser.impl.autogenerated.GmqlParser.DecimalValueContext;
import com.braintribe.model.processing.query.parser.impl.autogenerated.GmqlParser.DoubleValueContext;
import com.braintribe.model.processing.query.parser.impl.autogenerated.GmqlParser.EscBContext;
import com.braintribe.model.processing.query.parser.impl.autogenerated.GmqlParser.EscBSContext;
import com.braintribe.model.processing.query.parser.impl.autogenerated.GmqlParser.EscFContext;
import com.braintribe.model.processing.query.parser.impl.autogenerated.GmqlParser.EscNContext;
import com.braintribe.model.processing.query.parser.impl.autogenerated.GmqlParser.EscRContext;
import com.braintribe.model.processing.query.parser.impl.autogenerated.GmqlParser.EscSQContext;
import com.braintribe.model.processing.query.parser.impl.autogenerated.GmqlParser.EscTContext;
import com.braintribe.model.processing.query.parser.impl.autogenerated.GmqlParser.FloatValueContext;
import com.braintribe.model.processing.query.parser.impl.autogenerated.GmqlParser.IdentifierContext;
import com.braintribe.model.processing.query.parser.impl.autogenerated.GmqlParser.IntegerDecimalRepresenationContext;
import com.braintribe.model.processing.query.parser.impl.autogenerated.GmqlParser.IntegerHexRepresentationContext;
import com.braintribe.model.processing.query.parser.impl.autogenerated.GmqlParser.LiteralValueContext;
import com.braintribe.model.processing.query.parser.impl.autogenerated.GmqlParser.LongDecimalRepresenationContext;
import com.braintribe.model.processing.query.parser.impl.autogenerated.GmqlParser.LongHexRepresentationContext;
import com.braintribe.model.processing.query.parser.impl.autogenerated.GmqlParser.NullValueContext;
import com.braintribe.model.processing.query.parser.impl.autogenerated.GmqlParser.PlainContentContext;
import com.braintribe.model.processing.query.parser.impl.autogenerated.GmqlParser.StringValueContext;
import com.braintribe.model.processing.query.parser.impl.autogenerated.GmqlParser.TimeZoneOffsetContext;
import com.braintribe.model.processing.query.parser.impl.autogenerated.GmqlParser.UnicodeEscapeContext;
import com.braintribe.model.processing.query.parser.impl.autogenerated.GmqlParser.ValueContext;
import com.braintribe.model.processing.query.parser.impl.autogenerated.GmqlParser.VariableContext;
import com.braintribe.model.processing.query.parser.impl.context.CalendarOffsetCustomContext;
import com.braintribe.model.processing.query.parser.impl.context.ObjectCustomContext;
import com.braintribe.model.processing.query.parser.impl.context.StringBuilderCustomContext;
import com.braintribe.model.processing.query.parser.impl.context.ValueCustomContext;
import com.braintribe.model.processing.query.parser.impl.context.VariableCustomContext;
import com.braintribe.model.processing.query.parser.impl.context.basetype.BigDecimalCustomContext;
import com.braintribe.model.processing.query.parser.impl.context.basetype.BooleanCustomContext;
import com.braintribe.model.processing.query.parser.impl.context.basetype.DateCustomContext;
import com.braintribe.model.processing.query.parser.impl.context.basetype.DefaultCustomContext;
import com.braintribe.model.processing.query.parser.impl.context.basetype.DoubleCustomContext;
import com.braintribe.model.processing.query.parser.impl.context.basetype.FloatCustomContext;
import com.braintribe.model.processing.query.parser.impl.context.basetype.IntegerCustomContext;
import com.braintribe.model.processing.query.parser.impl.context.basetype.LongCustomContext;
import com.braintribe.model.time.CalendarOffset;
import com.braintribe.model.time.DateOffsetUnit;
import com.braintribe.utils.format.lcd.FormatTool;

public abstract class GmqlLiteralParserListener extends GmqlBasicParserListener {

	private static final char MILLISECOND = 's';
	private static final char SECOND = 'S';
	private static final char MINUTE = 'm';
	private static final char HOUR = 'H';
	private static final char DAY = 'D';
	private static final char MONTH = 'M';
	private static final char YEAR = 'Y';

	@Override
	public void exitLiteralValue(LiteralValueContext ctx) {
		propagateChildResult(ctx);
	}

	@Override
	public void exitVariable(VariableContext ctx) {
		
		IdentifierContext variableIdentitifier = ctx.identifier(0);
		DefaultCustomContext variableCustomContext = (DefaultCustomContext) takeValue(variableIdentitifier).cast();
		String varName = variableCustomContext.getReturnValue();
		String variableType = null;
		Object defaultValue = null;
		
		IdentifierContext variableTypeCtx = ctx.identifier(1);
		if (variableTypeCtx != null) {
			DefaultCustomContext typSigCustomContext = (DefaultCustomContext) takeValue(variableTypeCtx).cast();
			variableType = typSigCustomContext.getReturnValue();
		}
		ValueContext defaultValueCtx = ctx.value();
		if (defaultValueCtx != null) {
			defaultValue = ((ValueCustomContext<?>) takeValue(defaultValueCtx).cast()).getReturnValue();
		}
		
		Variable variable = getVariable(varName, variableType, defaultValue);
		setValue(ctx, new VariableCustomContext(variable));
		
	}

	@Override
	public void exitCollection(CollectionContext ctx) {
		Set<Object> list = new HashSet<Object>();
		for (ValueContext currentCtx : ctx.value()) {
			Object value = takeValue(currentCtx).getReturnValue();
			list.add(value);
		}
		setValue(ctx, new ObjectCustomContext(list));
	}

	@Override
	public void exitDateValue(DateValueContext ctx) {
		List<CalendarOffset> offsetList = new ArrayList<CalendarOffset>();
		for (ParserRuleContext tempCtx : ctx.calendarOffsetValue()) {
			CalendarOffset currentOffset = ((CalendarOffsetCustomContext) takeValue(tempCtx).cast()).getReturnValue();
			offsetList.add(currentOffset);
		}
		Date date = FormatTool.getExpert().getDateFormat().createDateFromOffsetList(offsetList);
		setValue(ctx, new DateCustomContext(date));
	}

	@Override
	public void exitCalendarOffsetValue(CalendarOffsetValueContext ctx) {
		propagateChildResult(ctx);
	}

	@Override
	public void exitDateOffset(DateOffsetContext ctx) {
		String dateFragment = ctx.DateOffset().getText();
		char fragmentIdentifier = dateFragment.charAt(dateFragment.length() - 1);
		dateFragment = dateFragment.substring(0, dateFragment.length() - 1);

		int dateInteger = Integer.parseInt(dateFragment);
		CalendarOffset offset = null;
		switch (fragmentIdentifier) {

			case YEAR:
				offset = $.dateOffset(dateInteger, DateOffsetUnit.year);
				break;
			case MONTH:
				offset = $.dateOffset(--dateInteger, DateOffsetUnit.month);
				break;
			case DAY:
				offset = $.dateOffset(dateInteger, DateOffsetUnit.day);
				break;
			case HOUR:
				offset = $.dateOffset(dateInteger, DateOffsetUnit.hour);
				break;
			case MINUTE:
				offset = $.dateOffset(dateInteger, DateOffsetUnit.minute);
				break;
			case SECOND:
				offset = $.dateOffset(dateInteger, DateOffsetUnit.second);
				break;
			case MILLISECOND:
				offset = $.dateOffset(dateInteger, DateOffsetUnit.millisecond);
				break;
			default:
				setCustomParsingExcpetion(new GmqlQueryParserException("Unidentified dateOffset identifier used: " + fragmentIdentifier));
				throw new RuntimeException();
		}
		setValue(ctx, new CalendarOffsetCustomContext(offset));
	}

	@Override
	public void exitTimeZoneOffset(TimeZoneOffsetContext ctx) {
		String dateFragment = ctx.TimeZoneOffset().getText();
		dateFragment = dateFragment.substring(0, dateFragment.length() - 1);
		CalendarOffset offset = $.timeZoneOffset(parseTimeZone(dateFragment));
		setValue(ctx, new CalendarOffsetCustomContext(offset));
	}

	// same logic from SimpleDateFormatter
	private static int parseTimeZone(String zoneFormat) {
		int result = 0;
		// parse the sign (+ -)
		byte sign = 1;
		byte index = 0;
		char c = zoneFormat.charAt(0);
		if (c == '+') {
			sign = 1;
			index = 1;
		} else if (c == '-') {
			sign = -1;
			index = 1;
		}
		// Parse hh
		parse: {
			c = zoneFormat.charAt(index++);
			int hours = c - '0';
			c = zoneFormat.charAt(index++);
			hours = hours * 10 + (c - '0');

			if (hours > 23) {
				break parse;
			}

			// Proceed with parsing mm
			c = zoneFormat.charAt(index++);

			int minutes = c - '0';
			c = zoneFormat.charAt(index++);
			minutes = minutes * 10 + (c - '0');
			if (minutes > 59) {
				break parse;
			}

			minutes += hours * 60;
			result = minutes * sign;
		}
		if (result == 0 && !(zoneFormat.equals("0000") || zoneFormat.equals("-0000") || zoneFormat.equals("+0000"))) {
			setCustomParsingExcpetion(new GmqlQueryParserException("TimeZone could not be parsed"));
			throw new RuntimeException();
		}
		return result;
	}

	@Override
	public void exitBooleanValue(BooleanValueContext ctx) {
		setValue(ctx, new BooleanCustomContext(Boolean.parseBoolean(ctx.Boolean().getText())));
	}

	@Override
	public void enterStringValue(StringValueContext ctx) {
		setValue(ctx, new StringBuilderCustomContext(new StringBuilder()));
	}

	@Override
	public void exitStringValue(StringValueContext ctx) {
		// loop to remove all the contexts
		removeContextList(ctx.plainContent());
		removeContextList(ctx.escape());

		StringBuilderCustomContext currentContext = getValue(ctx).cast();

		setValue(ctx, new DefaultCustomContext(currentContext.getReturnValue().toString()));

	}

	@Override
	public void exitPlainContent(PlainContentContext ctx) {

		StringBuilderCustomContext parentContext = getValue(ctx.getParent()).cast();
		parentContext.getReturnValue().append(ctx.getText());

		setValue(ctx, new DefaultCustomContext("")); // reserve space so that
														// default rule does not
														// fire
	}

	@Override
	public void exitEscB(EscBContext ctx) {

		StringBuilderCustomContext parentContext = getValue(ctx.getParent()).cast();
		parentContext.getReturnValue().append("\\b");

		setValue(ctx, new DefaultCustomContext("")); // reserve space so that
														// default rule does not
														// fire
	}

	@Override
	public void exitEscBS(EscBSContext ctx) {

		StringBuilderCustomContext parentContext = getValue(ctx.getParent()).cast();
		parentContext.getReturnValue().append("\\\\");

		setValue(ctx, new DefaultCustomContext("")); // reserve space so that
														// default rule does not
														// fire
	}

	@Override
	public void exitEscF(EscFContext ctx) {

		StringBuilderCustomContext parentContext = getValue(ctx.getParent()).cast();
		parentContext.getReturnValue().append("\\f");

		setValue(ctx, new DefaultCustomContext("")); // reserve space so that
														// default rule does not
														// fire
	}

	@Override
	public void exitEscN(EscNContext ctx) {

		StringBuilderCustomContext parentContext = getValue(ctx.getParent()).cast();
		parentContext.getReturnValue().append("\\n");

		setValue(ctx, new DefaultCustomContext("")); // reserve space so that
														// default rule does not
														// fire
	}

	@Override
	public void exitEscR(EscRContext ctx) {

		StringBuilderCustomContext parentContext = getValue(ctx.getParent()).cast();
		parentContext.getReturnValue().append("\\r");

		setValue(ctx, new DefaultCustomContext("")); // reserve space so that
														// default rule does not
														// fire
	}

	@Override
	public void exitEscSQ(EscSQContext ctx) {

		StringBuilderCustomContext parentContext = getValue(ctx.getParent()).cast();
		parentContext.getReturnValue().append("\\'");

		setValue(ctx, new DefaultCustomContext("")); // reserve space so that
														// default rule does not
														// fire

	}

	@Override
	public void exitEscT(EscTContext ctx) {

		StringBuilderCustomContext parentContext = getValue(ctx.getParent()).cast();
		parentContext.getReturnValue().append("\\t");

		setValue(ctx, new DefaultCustomContext("")); // reserve space so that
														// default rule does not
														// fire
	}

	@Override
	public void exitUnicodeEscape(UnicodeEscapeContext ctx) {

		StringBuilderCustomContext parentContext = getValue(ctx.getParent()).cast();

		// TODO check with Dirk
		parentContext.getReturnValue().append(ctx.UnicodeEscape());

		setValue(ctx, new DefaultCustomContext("")); // reserve space so that
														// default rule does not
														// fire
	}

	@Override
	public void exitDecimalValue(DecimalValueContext ctx) {

		String value = ctx.DecimalLiteral().getText();
		value = value.substring(0, value.length() - 1);

		BigDecimal bigDecimalValue = new BigDecimal(value);

		setValue(ctx, new BigDecimalCustomContext(bigDecimalValue));
	}

	@Override
	public void exitFloatValue(FloatValueContext ctx) {
		String value = ctx.FloatLiteral().getText();
		value = cutLastChar(value);

		Float floatValue = Float.parseFloat(value);

		setValue(ctx, new FloatCustomContext(floatValue));
	}

	@Override
	public void exitDoubleValue(DoubleValueContext ctx) {
		String value = ctx.DoubleLiteral().getText();
		if (getLastNCharacters(value, 1).toUpperCase().equals("D"))
			value = cutLastChar(value);

		Double doubleValue = Double.parseDouble(value);

		setValue(ctx, new DoubleCustomContext(doubleValue));
	}

	private String cutLastChar(String value) {
		return removeLastNCharacters(value, 1);
	}

	@Override
	public void exitLongDecimalRepresenation(LongDecimalRepresenationContext ctx) {

		String value = ctx.LongBase10Literal().getText();
		value = value.substring(0, value.length() - 1);

		Long longValue = Long.parseLong(value);

		setValue(ctx, new LongCustomContext(longValue));
	}

	@Override
	public void exitLongHexRepresentation(LongHexRepresentationContext ctx) {

		String value = ctx.LongBase16Literal().getText();
		if (value.charAt(0) != '0') {
			value = value.charAt(0) + value.substring(3, value.length() - 1);
		} else {
			value = value.substring(2, value.length() - 1);
		}
		Long longValue = Long.parseLong(value, 16);

		setValue(ctx, new LongCustomContext(longValue));
	}

	@Override
	public void exitIntegerDecimalRepresenation(IntegerDecimalRepresenationContext ctx) {

		String value = ctx.IntegerBase10Literal().getText();
		Integer integerValue = Integer.parseInt(value);

		setValue(ctx, new IntegerCustomContext(integerValue));
	}

	@Override
	public void exitIntegerHexRepresentation(IntegerHexRepresentationContext ctx) {

		String value = ctx.IntegerBase16Literal().getText();
		if (value.charAt(0) != '0') {
			value = value.charAt(0) + value.substring(3);
		} else {
			value = value.substring(2);
		}

		Integer integerValue = Integer.parseInt(value, 16);

		setValue(ctx, new IntegerCustomContext(integerValue));
	}

	@Override
	public void exitNullValue(NullValueContext ctx) {
		setValue(ctx, new ObjectCustomContext(null));
	}

}
