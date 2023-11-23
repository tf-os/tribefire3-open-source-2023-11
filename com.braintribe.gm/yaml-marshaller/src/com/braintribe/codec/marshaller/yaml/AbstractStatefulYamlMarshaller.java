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
package com.braintribe.codec.marshaller.yaml;

import java.io.IOException;
import java.io.Writer;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.function.Consumer;

import com.braintribe.codec.marshaller.api.EntityVisitorOption;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.IdentityManagementMode;
import com.braintribe.codec.marshaller.api.IdentityManagementModeOption;
import com.braintribe.codec.marshaller.api.TypeExplicitness;
import com.braintribe.codec.marshaller.api.TypeExplicitnessOption;
import com.braintribe.codec.marshaller.api.options.attributes.StabilizeOrderOption;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.LinearCollectionType;
import com.braintribe.model.generic.reflection.ListType;
import com.braintribe.model.generic.reflection.MapType;
import com.braintribe.model.generic.reflection.SetType;

public abstract class AbstractStatefulYamlMarshaller {
	protected final GmSerializationOptions options;
	protected final Writer writer;
	protected final Object rootValue;
	protected final Indent indent = new Indent(2);
	protected int anchorSequence;
	protected TypeExplicitness typeExplicitness;
	protected final Consumer<? super GenericEntity> entityVisitor;
	protected final IdentityManagementMode identityManagementMode;
	protected boolean stabilize;

	public AbstractStatefulYamlMarshaller(GmSerializationOptions options, Writer writer, Object rootValue) {
		super();
		this.options = options;
		this.writer = writer;
		this.rootValue = rootValue;
		this.typeExplicitness = options.findOrNull(TypeExplicitnessOption.class);
		this.stabilize = options.findOrDefault(StabilizeOrderOption.class, false);
		if (this.typeExplicitness == null || this.typeExplicitness == TypeExplicitness.auto) {
			this.typeExplicitness = TypeExplicitness.entities;
		}
		this.entityVisitor = options.findOrNull(EntityVisitorOption.class);
		this.identityManagementMode = options.findAttribute(IdentityManagementModeOption.class).orElse(IdentityManagementMode.auto);
	}

	protected void write(GenericModelType inferredType, GenericModelType type, Object value) throws IOException {
		write(inferredType, type, value, false);
	}

	protected void writeSpaceIfRequired(boolean required) throws IOException {
		if (required)
			writer.write(' ');
	}

	/**
	 * 
	 * @param inferredType
	 *            Expected type of value - e.g. property type or inferred root type
	 * @param type
	 *            Actual type of value
	 * @param value
	 *            Value to marshal
	 * @param isComplexPropertyValue
	 *            To prevent unnecessary newlines it is tried to render as little newlines as possible. However when a
	 *            property value of complex type is going to be marshaled for an entity, a newline (that otherwise would not
	 *            have been necessary) has to be enforced via this flag.
	 * @throws IOException
	 */
	protected void write(GenericModelType inferredType, GenericModelType type, Object value, boolean isComplexPropertyValue) throws IOException {
		if (value == null) {
			writeSpaceIfRequired(isComplexPropertyValue);
			writer.write("null");
			return;
		}

		switch (type.getTypeCode()) {
			case objectType:
				write(inferredType, type.getActualType(value), value, isComplexPropertyValue);
				break;

			// Strings
			case stringType:
				writeSpaceIfRequired(isComplexPropertyValue);
				writeString(value);
				break;

			// native literal types
			case booleanType:
			case integerType:
			case doubleType:
				writeSpaceIfRequired(isComplexPropertyValue);
				writer.write(value.toString());
				break;

			// straight custom types
			case longType:
			case floatType:
			case decimalType:
			case enumType:
				writeSpaceIfRequired(isComplexPropertyValue);
				writeSimpleCustomType(inferredType, type, value);
				break;

			case dateType:
				writeSpaceIfRequired(isComplexPropertyValue);
				writeDate((Date) value);
				break;

			case entityType:
				writeEntity(inferredType, (GenericEntity) value, isComplexPropertyValue);
				break;

			// collection types
			case listType:
				writeList((ListType) type, (List<?>) value, isComplexPropertyValue);
				break;
			case setType:
				writeSet((SetType) type, (Set<?>) value, isComplexPropertyValue);
				break;
			case mapType:
				writeMap((MapType) type, (Map<?, ?>) value, isComplexPropertyValue);
				break;

			default:
				break;

		}
	}

	protected void writeMap(MapType type, Map<?, ?> map, boolean isComplexPropertyValue) throws IOException {
		if (map.isEmpty()) {
			writeSpaceIfRequired(isComplexPropertyValue);
			writer.write("{}");
			return;
		}

		boolean forceNewline = isComplexPropertyValue;

		GenericModelType keyType = type.getKeyType();
		GenericModelType valueType = type.getValueType();

		for (Map.Entry<?, ?> entry : map.entrySet()) {
			Object key = entry.getKey();
			Object value = entry.getValue();

			if (forceNewline) { // ensures that a newline is written when the collection has at least one entry and is the property of an entity
				writer.write('\n');
				indent.write(writer);
			}
			forceNewline = true;

			GenericModelType actualKeyType = keyType.isBase() ? keyType.getActualType(key) : keyType;

			if (actualKeyType.isScalar()) {
				write(keyType, keyType, key);
				writer.write(':');
				indent.pushIndent();
				write(valueType, valueType, value, true);
				indent.popIndent();
			} else {
				writer.write("? ");
				indent.pushIndent();
				write(keyType, actualKeyType, key);
				indent.popIndent();
				writer.write("\n");
				indent.write(writer);
				writer.write(':');
				indent.pushIndent();
				write(valueType, valueType, value, true);
				indent.popIndent();
			}

		}
	}

	protected void writeList(ListType type, List<?> list, boolean isComplexPropertyValue) throws IOException {
		writeLinearCollection(type, list, '-', isComplexPropertyValue, false);
	}

	protected void writeSet(SetType type, Set<?> set, boolean isComplexPropertyValue) throws IOException {
		writeSpaceIfRequired(isComplexPropertyValue);
		writer.write("!!set");
		writeLinearCollection(type, set, '?', true, true);
	}

	protected void writeLinearCollection(LinearCollectionType type, Collection<?> collection, char bullet, boolean isComplexPropertyValue,
			boolean introductionWritten) throws IOException {
		if (collection.isEmpty()) {
			writeSpaceIfRequired(isComplexPropertyValue);
			writer.write("[]");
			return;
		}

		boolean forceNewline = introductionWritten || isComplexPropertyValue;

		GenericModelType elementType = type.getCollectionElementType();
		for (Object element : collection) {
			if (forceNewline) {
				writer.write('\n');
				indent.write(writer);
			}
			forceNewline = true;
			writer.write(bullet);
			writer.write(' ');
			indent.pushIndent();
			write(elementType, elementType, element);
			indent.popIndent();
		}
	}

	// Formatter to be used for later replacement of the proprietary date formatting copied over from snakeyaml
	private static final DateTimeFormatter DATETIME_FORMATTER = new DateTimeFormatterBuilder().optionalStart()
			.appendPattern("yyyy-MM-dd['T'HH[:mm[:ss[.SSS]]]][Z]").optionalEnd().optionalStart().appendPattern("yyyyMMdd['T'HH[mm[ss[SSS]]]][Z]")
			.optionalEnd().parseDefaulting(ChronoField.HOUR_OF_DAY, 0).parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
			.parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0).parseDefaulting(ChronoField.NANO_OF_SECOND, 0)
			.parseDefaulting(ChronoField.OFFSET_SECONDS, 0).toFormatter();

	protected void writeDate(Date value) throws IOException {
		// TODO: finde the correct pattern for jdk date handling to use it instead of manual date formatting copied over from
		// snakeyamls date represent
		// DATETIME_FORMATTER.formatTo(value.toInstant(), writer);

		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		calendar.setTime(value);
		int years = calendar.get(Calendar.YEAR);
		int months = calendar.get(Calendar.MONTH) + 1; // 0..12
		int days = calendar.get(Calendar.DAY_OF_MONTH); // 1..31
		int hour24 = calendar.get(Calendar.HOUR_OF_DAY); // 0..24
		int minutes = calendar.get(Calendar.MINUTE); // 0..59
		int seconds = calendar.get(Calendar.SECOND); // 0..59
		int millis = calendar.get(Calendar.MILLISECOND);

		String yearsStr = String.valueOf(years);
		int pad = Math.max(4 - yearsStr.length(), 0);
		for (int p = 0; p < pad; p++) {
			writer.write('0');
		}
		writer.write(yearsStr);
		writer.write('-');
		if (months < 10) {
			writer.write('0');
		}
		writer.write(String.valueOf(months));
		writer.write('-');
		if (days < 10) {
			writer.write('0');
		}
		writer.write(String.valueOf(days));
		writer.write('T');
		if (hour24 < 10) {
			writer.write('0');
		}
		writer.write(String.valueOf(hour24));
		writer.write(':');
		if (minutes < 10) {
			writer.write('0');
		}
		writer.write(String.valueOf(minutes));
		writer.write(':');
		if (seconds < 10) {
			writer.write('0');
		}
		writer.write(String.valueOf(seconds));
		if (millis > 0) {
			if (millis < 10) {
				writer.write(".00");
			} else if (millis < 100) {
				writer.write(".0");
			} else {
				writer.write('.');
			}
			writer.write(String.valueOf(millis));
		}

		// Get the offset from GMT taking DST into account
		int gmtOffset = calendar.getTimeZone().getOffset(calendar.get(Calendar.ERA), calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
				calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.DAY_OF_WEEK), calendar.get(Calendar.MILLISECOND));
		if (gmtOffset == 0) {
			writer.write('Z');
		} else {
			if (gmtOffset < 0) {
				writer.write('-');
				gmtOffset *= -1;
			} else {
				writer.write('+');
			}
			int minutesOffset = gmtOffset / (60 * 1000);
			int hoursOffset = minutesOffset / 60;
			int partOfHour = minutesOffset % 60;

			if (hoursOffset < 10) {
				writer.write('0');
			}
			writer.write(String.valueOf(hoursOffset));
			writer.write(':');
			if (partOfHour < 10) {
				writer.write('0');
			}
			writer.write(String.valueOf(partOfHour));
		}
	}

	protected void writeString(Object s) throws IOException {
		writer.write('"');
		writeEscaped(writer, s.toString());
		writer.write('"');
	}

	protected void writeSimpleCustomType(GenericModelType inferredType, GenericModelType type, Object value) throws IOException {
		if (typeExplicitness == TypeExplicitness.always || (typeExplicitness != TypeExplicitness.never && inferredType != type)) {
			writer.write('!');
			writer.write(type.getTypeSignature());
			writer.write(' ');
		}

		writer.write(value.toString());
	}

	protected static void writeSpacer(int count, Writer writer) throws IOException {
		if (count > 0)
			writer.append(' ');
	}

	protected abstract void writeEntity(GenericModelType inferredType, GenericEntity entity, boolean isComplexPropertyValue) throws IOException;

	private final static char[] HEX_CHARS = "0123456789ABCDEF".toCharArray();
	private static final char[][] ESCAPES = new char[128][];

	static {
		ESCAPES['"'] = "\\\"".toCharArray();
		ESCAPES['\\'] = "\\\\".toCharArray();
		ESCAPES['\t'] = "\\t".toCharArray();
		ESCAPES['\f'] = "\\f".toCharArray();
		ESCAPES['\n'] = "\\n".toCharArray();
		ESCAPES['\r'] = "\\r".toCharArray();

		for (int i = 0; i < 32; i++) {
			if (ESCAPES[i] == null)
				ESCAPES[i] = ("\\u00" + HEX_CHARS[i >> 4] + HEX_CHARS[i & 0xF]).toCharArray();
		}
	}

	public static void writeEscaped(Writer writer, String string) throws IOException {
		int len = string.length();
		int s = 0;
		int i = 0;
		char esc[] = null;
		for (; i < len; i++) {
			char c = string.charAt(i);

			if (c < 128) {
				esc = ESCAPES[c];
				if (esc != null) {
					writer.write(string, s, i - s);
					writer.write(esc);
					s = i + 1;
				}
			}
		}
		if (i > s) {
			if (s == 0)
				writer.write(string);
			else
				writer.write(string, s, i - s);
		}
	}

	static class Indent {
		private int depth = 0;
		private final int indentAmount;
		private static final String[] indents = { " ", "  ", "    ", "        ", "                ", "                                ",
				"                                                                ", };

		public Indent(int indentAmount) {
			this.indentAmount = indentAmount;
		}

		public int getDepth() {
			return depth;
		}

		public void pushIndent() {
			depth++;
		}

		public void popIndent() {
			depth--;
		}

		public void write(Writer writer) throws IOException {
			int num = depth * indentAmount;

			int len = indents.length;

			for (int i = 0; num != 0 && i < len; i++, num >>= 1) {
				if ((num & 1) != 0) {
					writer.write(indents[i]);
				}
			}

			// write remains
			if (num > 0) {
				String s = indents[len - 1];
				for (int i = 0; i < num * 2; i++) {
					writer.write(s);
				}
			}
		}
	}

}