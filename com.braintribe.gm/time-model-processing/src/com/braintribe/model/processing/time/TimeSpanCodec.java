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

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.braintribe.model.time.TimeSpan;
import com.braintribe.model.time.TimeUnit;

public class TimeSpanCodec implements Codec<TimeSpan, Double> {
	private TimeUnit unit = TimeUnit.milliSecond;
	
	public void setUnit(TimeUnit unit) {
		this.unit = unit;
	}

	@Override
	public Double encode(TimeSpan span) throws CodecException {
		if (span == null)
			return null;
		
		return TimeSpanConversion.fromTimeSpan(span).unit(unit).toValue();
	}

	@Override
	public TimeSpan decode(Double encodedValue) throws CodecException {
		if (encodedValue == null)
			return null;
		
		return TimeSpanConversion.fromValue(encodedValue, unit).toTimeSpan();
	}

	@Override
	public Class<TimeSpan> getValueClass() {
		return TimeSpan.class;
	}
}
