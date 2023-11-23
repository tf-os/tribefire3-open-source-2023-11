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
package com.braintribe.model.processing.manipulation.marshaller;

import static com.braintribe.utils.lcd.CollectionTools2.nullSafe;

import java.io.IOException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.AcquireManipulation;
import com.braintribe.model.generic.manipulation.AddManipulation;
import com.braintribe.model.generic.manipulation.AtomicManipulation;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.ClearCollectionManipulation;
import com.braintribe.model.generic.manipulation.CompoundManipulation;
import com.braintribe.model.generic.manipulation.DeleteManipulation;
import com.braintribe.model.generic.manipulation.InstantiationManipulation;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.ManipulationType;
import com.braintribe.model.generic.manipulation.PropertyManipulation;
import com.braintribe.model.generic.manipulation.RemoveManipulation;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.EssentialTypes;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.LinearCollectionType;
import com.braintribe.model.generic.reflection.ListType;
import com.braintribe.model.generic.reflection.MapType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.SetType;
import com.braintribe.model.generic.reflection.SimpleTypes;
import com.braintribe.utils.format.api.CustomDateFormat;
import com.braintribe.utils.format.lcd.FormatTool;

public abstract class ManipulationStringifier {

	private static final char[] HEX_CHARS = "0123456789ABCDEF".toCharArray();
	private static final String[] ESCAPES = new String[128];

	static {
		ESCAPES['\''] = "\\\'";
		ESCAPES['\\'] = "\\\\";
		ESCAPES['\t'] = "\\t";
		ESCAPES['\f'] = "\\f";
		ESCAPES['\n'] = "\\n";
		ESCAPES['\r'] = "\\r";

		for (int i = 0; i < 32; i++) {
			if (ESCAPES[i] == null)
				ESCAPES[i] = "\\u00" + HEX_CHARS[i >> 4] + HEX_CHARS[i & 0xF];
		}
	}

	public static String stringify(Manipulation m, boolean isRemote) {
		ManipulationStringifier stringirier = isRemote ? new RemoteManipulationStringifier() : new LocalManipulationStringifier();
		try {
			StringBuilder sb = new StringBuilder();
			stringirier.stringify(sb, m);
			return sb.toString();

		} catch (IOException e) {
			return "Error while stringifying manipulation: " + e.getMessage();
		}
	}

	protected GenericEntity lastEntity;
	private final CustomDateFormat dateFormat = FormatTool.getExpert().getDateFormat();

	protected Map<String, NameSequence> varSequences = new HashMap<>();

	private boolean singleBlock;

	protected static class NameSequence {
		public int lowestUnusedNumber = 0;
	}

	protected void recognizeVarNames(Collection<String> varNames) {
		for (String varName : varNames) {
			int i = varName.length();
			while (--i >= 0 && Character.isDigit(varName.charAt(i))) {
				// noop
			}
			// i is now the position before the last digit, so we move it to the position of the last digit. 
			// if there was no digit, ++i will be equal to varName.length()
			boolean hasNumber = (++i < varName.length());

			String prefix  = varName.substring(0, i);
			int number = hasNumber ? Integer.parseInt(varName.substring(i)) : 0;
			
			NameSequence nameSequece = acquireVarSequence(prefix);
			nameSequece.lowestUnusedNumber = Math.max(nameSequece.lowestUnusedNumber, number + 1);
		}
	}

	public void stringify(Appendable writer, Manipulation manipulation) throws IOException {
		stringify(writer, Collections.singletonList(manipulation));
	}

	public void setSingleBlock(boolean singleBlock) {
		this.singleBlock = singleBlock;
	}

	public void stringify(Appendable writer, List<Manipulation> manipulations) throws IOException {
		if (!singleBlock)
			writer.append("{");

		for (Manipulation manipulation : manipulations)
			write(writer, manipulation);

		if (!singleBlock)
			writer.append("\n}");
	}

	private void write(Appendable writer, Manipulation manipulation) throws IOException {
		if (manipulation.manipulationType() != ManipulationType.COMPOUND)
			writer.append('\n');

		switch (manipulation.manipulationType()) {
			case INSTANTIATION:
				writeInstantiationManipulation(writer, (InstantiationManipulation) manipulation);
				break;
			case ACQUIRE:
				writeAcquireManipulation(writer, (AcquireManipulation) manipulation);
				break;
			case DELETE:
				writeDeleteManipulation(writer, (DeleteManipulation) manipulation);
				break;

			case CHANGE_VALUE:
				writeChangeValueManipulation(writer, (ChangeValueManipulation) manipulation);
				break;

			case ADD:
				writeAddManipulation(writer, (AddManipulation) manipulation);
				break;
			case REMOVE:
				writeRemoveManipulation(writer, (RemoveManipulation) manipulation);
				break;
			case CLEAR_COLLECTION:
				writeClearCollectionManipulation(writer, (ClearCollectionManipulation) manipulation);
				break;

			case COMPOUND:
				writeCompoundManipulation(writer, (CompoundManipulation) manipulation);
				break;

			case ABSENTING:
			case MANIFESTATION:
				// noop
				break;
			default:
				writeCustomManipulation(writer, (AtomicManipulation) manipulation);
		}
	}

	protected abstract void writeInstantiationManipulation(Appendable writer, InstantiationManipulation manipulation) throws IOException;
	protected abstract void writeAcquireManipulation(Appendable writer, AcquireManipulation manipulation) throws IOException;
	protected abstract void writeDeleteManipulation(Appendable writer, DeleteManipulation manipulation) throws IOException;

	protected boolean isLastReference(GenericEntity reference) {
		return lastEntity != reference;
	}

	private void writeCompoundManipulation(Appendable writer, CompoundManipulation manipulation) throws IOException {
		for (Manipulation singleManipulation : nullSafe(manipulation.getCompoundManipulationList())) 
				write(writer, singleManipulation);
	}

	private void writeCustomManipulation(Appendable writer, AtomicManipulation manipulation) throws IOException {
		writeCustomInstance(writer, manipulation);
	}

	private void writeCustomInstance(Appendable writer, GenericEntity entity) throws IOException {
		EntityType<GenericEntity> entityType = entity.entityType();
		writer.append(entityType.getTypeSignature());
		writer.append('{');

		boolean first = true;
		for (Property property : entityType.getProperties()) {
			Object value = property.get(entity);

			if (property.isEmptyValue(value))
				continue;

			if (first)
				first = false;
			else
				writer.append(',');

			writer.append("\n ");
			writer.append(property.getName());
			writer.append(':');
			writeValue(writer, property.getType(), value);
		}
		if (!first)
			writer.append('\n');
		writer.append('}');
	}

	protected void writeValue(Appendable writer, GenericModelType type, Object value) throws IOException {
		if (value == null) {
			writer.append("null");
			return;
		}

		switch (type.getTypeCode()) {
			case objectType:
				writeValue(writer, resolveActualType(value), value);
				return;

			case booleanType:
			case integerType:
				writer.append(value.toString());
				return;

			case stringType:
				writeString(writer, value.toString());
				return;

			case doubleType:
				if (isNaNOrPositiveInfinity((Double) value))
					writer.append('+');
				writer.append(value.toString());
				writer.append('D');
				return;

			case floatType:
				if (isNaNOrPositiveInfinity((Float) value))
					writer.append('+');
				writer.append(value.toString());
				writer.append('F');
				return;

			case longType:
				writer.append(value.toString());
				writer.append('L');
				return;

			case decimalType:
				writer.append(value.toString());
				writer.append('B');
				return;

			case dateType:
				writeDate(writer, (Date) value);
				return;

			case entityType:
				writeEntity(writer, (GenericEntity) value);
				return;

			case enumType:
				writeEnum(writer, value);
				return;

			case listType:
				writeList(writer, (ListType) type, (List<?>) value);
				return;
			case setType:
				writeSet(writer, (SetType) type, (Set<?>) value);
				return;
			case mapType:
				writeMap(writer, (MapType) type, (Map<?, ?>) value);
				return;

			default:
				return;
		}
	}

	private boolean isNaNOrPositiveInfinity(Double value) {
		return value.isNaN() || ( value.isInfinite() && value > 0);
	}

	private boolean isNaNOrPositiveInfinity(Float value) {
		return value.isNaN() || (value.isInfinite() && value > 0);
	}
	
	protected abstract GenericModelType resolveActualType(Object value);

	protected abstract void writeEnum(Appendable writer, Object enumValue) throws IOException;

	private void writeString(Appendable writer, String string) throws IOException {
		writer.append('\'');
		writeEscaped(writer, string);
		writer.append('\'');
	}

	public static void writeEscaped(Appendable writer, String string) throws IOException {
		int len = string.length();
		int s = 0;
		int i = 0;
		String esc = null;
		for (; i < len; i++) {
			char c = string.charAt(i);

			if (c < 128) {
				esc = ESCAPES[c];
				if (esc != null) {
					writer.append(string, s, i);
					writer.append(esc);
					s = i + 1;
				}
			}
		}
		if (i > s) {
			if (s == 0)
				writer.append(string);
			else
				writer.append(string, s, i);
		}
	}

	protected abstract void writeEntity(Appendable writer, GenericEntity entity) throws IOException;

	protected void writeMap(Appendable writer, MapType type, Map<?, ?> map) throws IOException {
		writer.append('{');
		boolean first = true;
		GenericModelType keyType = type.getKeyType();
		GenericModelType valueType = type.getValueType();
		for (Map.Entry<?, ?> entry : map.entrySet()) {
			if (first)
				first = false;
			else
				writer.append(',');

			Object key = entry.getKey();
			Object value = entry.getValue();

			writeValue(writer, keyType, key);
			writer.append(':');
			writeValue(writer, valueType, value);
		}
		writer.append('}');
	}

	protected void writeSet(Appendable writer, SetType type, Set<?> value) throws IOException {
		writer.append('(');
		writeLinearCollection(writer, type, value);
		writer.append(')');
	}

	private void writeList(Appendable writer, ListType type, List<?> value) throws IOException {
		writer.append('[');
		writeLinearCollection(writer, type, value);
		writer.append(']');
	}

	private void writeLinearCollection(Appendable writer, LinearCollectionType type, Collection<?> collection) throws IOException {
		GenericModelType elementType = type.getCollectionElementType();
		boolean first = true;
		for (Object value : collection) {
			if (first)
				first = false;
			else
				writer.append(',');

			writeValue(writer, elementType, value);
		}

	}

	private void writeDate(Appendable writer, Date date) throws IOException {
		// Format string to the parser date-string
		int year = dateFormat.getYear(date);
		int month = dateFormat.getMonth(date);
		int day = dateFormat.getDay(date);
		int hour = dateFormat.getHour(date);
		int minute = dateFormat.getMinute(date);
		int second = dateFormat.getSecond(date);
		int ms = dateFormat.getMilliSecond(date);
		int tz = Calendar.getInstance().getTimeZone().getOffset(date.getTime()) / (1000 * 60);

		writer.append("date(");
		writer.append(Integer.toString(year));
		writer.append("Y,");
		writer.append(Integer.toString(month));
		writer.append("M,");
		writer.append(Integer.toString(day));
		writer.append("D,");
		writer.append(Integer.toString(hour));
		writer.append("H,");
		writer.append(Integer.toString(minute));
		writer.append("m,");
		writer.append(Integer.toString(second));
		writer.append("S,");
		writer.append(Integer.toString(ms));
		writer.append("s,");

		boolean negativeTz = tz < 0;

		if (negativeTz) {
			tz *= -1;
		}

		int tzh = tz / 60;
		int tzm = tz % 60;

		if (negativeTz) {
			writer.append('-');
		} else {
			writer.append('+');
		}

		if (tzh < 10)
			writer.append('0');

		writer.append(Integer.toString(tzh));

		if (tzm < 10)
			writer.append('0');

		writer.append(Integer.toString(tzm));

		writer.append("Z)");
	}

	private void writeChangeValueManipulation(Appendable writer, ChangeValueManipulation manipulation) throws IOException {
		GenericModelType propertyType = writePropertyManipulationStartAndReturnPropertyType(writer, manipulation);

		Object value = manipulation.getNewValue();

		writer.append('=');
		writeValue(writer, propertyType, value);
	}

	private void writeAddManipulation(Appendable writer, AddManipulation manipulation) throws IOException {
		writeCollectionManipulation(writer, manipulation, manipulation.getItemsToAdd(), '+');
	}

	private void writeRemoveManipulation(Appendable writer, RemoveManipulation manipulation) throws IOException {
		writeCollectionManipulation(writer, manipulation, manipulation.getItemsToRemove(), '-');
	}

	private void writeCollectionManipulation(Appendable writer, PropertyManipulation manipulation, Map<Object, Object> items, char operand)
			throws IOException {
		GenericModelType propertyType = writePropertyManipulationStartAndReturnPropertyType(writer, manipulation);

		writer.append(operand);
		writeCollectionManipulationValue(writer, propertyType, items);
	}

	private void writeClearCollectionManipulation(Appendable writer, ClearCollectionManipulation manipulation) throws IOException {
		writePropertyManipulationStart(writer, manipulation);
		writer.append("--");
	}

	/** Writes the beginning of a {@link PropertyManipulation}	 */
	abstract protected void writePropertyManipulationStart(Appendable writer, PropertyManipulation manipulation) throws IOException;
	
	/**
	 * Similar to {@link #writePropertyManipulationStart}, but also returns a type of the property, in order to
	 * facilitate the value stringification. Note that the returned type doesn't have to match exactly for custom types,
	 * because in case of remotified manipulations the corresponding reflection type might not even exist. So for
	 * {@link EntityType} / {@link EnumType} the only information to be taken for granted is that it is an entity or an
	 * enum. But that is enough for our purposes.
	 */
	abstract protected GenericModelType writePropertyManipulationStartAndReturnPropertyType(Appendable writer, PropertyManipulation manipulation) throws IOException;

	private void writeCollectionManipulationValue(Appendable writer, GenericModelType propertyType, Map<Object, Object> items) throws IOException {
		if (propertyType == BaseType.INSTANCE)
			propertyType = EssentialTypes.TYPE_MAP;

		switch (propertyType.getTypeCode()) {
			case listType: {
				GenericModelType elementType = ((ListType) propertyType).getCollectionElementType();
				MapType mapType = GMF.getTypeReflection().getMapType(SimpleTypes.TYPE_INTEGER, elementType);
				writeMap(writer, mapType, items);
				break;
			}
			case mapType: {
				MapType mapType = (MapType) propertyType;
				writeMap(writer, mapType, items);
				break;
			}
			case setType: {
				SetType setType = (SetType) propertyType;
				if (items.size() == 1) {
					writeValue(writer, setType.getCollectionElementType(), items.keySet().iterator().next());
				} else {
					writeSet(writer, setType, items.keySet());
				}
				break;
			}
			default:
				throw new IllegalArgumentException("Unexpected property type [" + propertyType + "]. Should have bean list, set or map type");
		}
	}

	protected String newReferenceVarName() {
		NameSequence nameSequence = acquireVarSequence("$");
		return "$" + nameSequence.lowestUnusedNumber++;
	}

	protected String newTypeVarName(String shortName) {
		NameSequence nameSequence = acquireVarSequence(shortName);

		int num = nameSequence.lowestUnusedNumber++;

		if (num > 0)
			return shortName + num;
		else
			return shortName;
	}

	protected NameSequence acquireVarSequence(String nameSpace) {
		return varSequences.computeIfAbsent(nameSpace, (key) -> new NameSequence());
	}

}
