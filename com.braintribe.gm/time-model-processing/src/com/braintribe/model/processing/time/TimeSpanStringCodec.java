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
package com.braintribe.model.processing.time;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.time.TimeSpan;
import com.braintribe.model.time.TimeUnit;

/**
 * <p>
 * Converts strings (like {@code 30m}, {@code 1.5d}, {@code 100s}) to {@link TimeSpan} instances and vice-versa.
 * 
 */
public class TimeSpanStringCodec implements Codec<TimeSpan, String> {

	private Map<String, TimeUnit> stringToUnitMap;

	private static final Pattern pattern = Pattern.compile("^(\\d*\\.?\\d+)(\\w+)$");

	public TimeSpanStringCodec() {
		setupStringToUnitMap();
	}

	public void setStringToUnitMap(Map<String, TimeUnit> stringToUnitMap) {
		if (stringToUnitMap != null) {
			this.stringToUnitMap = stringToUnitMap;
		} else {
			setupStringToUnitMap();
		}
	}

	@Override
	public TimeSpan decode(String stringValue) throws CodecException {

		if (stringValue == null) {
			throw new IllegalArgumentException("encode argument cannot be null");
		}

		Matcher matcher = pattern.matcher(stringValue);

		if (!matcher.find()) {
			throw new CodecException("Given value [ " + stringValue + " ] doesn't match expected format");
		}

		Double value = Double.valueOf(matcher.group(1));
		String unit = matcher.group(2);
		TimeUnit timeUnit = stringToUnitMap.get(unit);
		if (timeUnit == null) {
			throw new CodecException("Given value [ " + stringValue + " ] contains an unsupported unit: " + unit);
		}

		// TimeSpan timeSpan = TimeSpan.T.create(); // Fails with "Cannot instantiate abstract type: com.braintribe.model.generic.StandardIdentifiable"
		TimeSpan timeSpan = EntityTypes.T(TimeSpan.class).createRaw();
		timeSpan.setValue(value);
		timeSpan.setUnit(timeUnit);
		return timeSpan;

	}

	@Override
	public String encode(TimeSpan encodedValue) throws CodecException {

		if (encodedValue == null) {
			throw new IllegalArgumentException("decode argument cannot be null");
		}

		TimeUnit timeUnit = encodedValue.getUnit();

		if (timeUnit == null) {
			throw new CodecException("Given value [ " + encodedValue + " ] has no unit set");
		}

		String unit = null;
		for (Map.Entry<String, TimeUnit> entry : stringToUnitMap.entrySet()) {
			if (entry.getValue().equals(timeUnit)) {
				unit = entry.getKey();
				break;
			}
		}

		if (unit == null) {
			throw new CodecException("Given value [ " + encodedValue + " ] contains an unsupported unit: " + timeUnit);
		}

		Double value = encodedValue.getValue();

		if (value % 1 == 0) {
			return value.longValue() + unit;
		} else {
			return value + unit;
		}

	}

	@Override
	public Class<TimeSpan> getValueClass() {
		return TimeSpan.class;
	}

	protected void setupStringToUnitMap() {
		stringToUnitMap = new HashMap<>(7);
		stringToUnitMap.put("ns", TimeUnit.nanoSecond);
		stringToUnitMap.put("\u00b5s", TimeUnit.microSecond);
		stringToUnitMap.put("ms", TimeUnit.milliSecond);
		stringToUnitMap.put("s", TimeUnit.second);
		stringToUnitMap.put("m", TimeUnit.minute);
		stringToUnitMap.put("h", TimeUnit.hour);
		stringToUnitMap.put("d", TimeUnit.day);
	}

}
