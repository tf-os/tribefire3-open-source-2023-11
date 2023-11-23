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
package com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client;

import java.text.ParseException;
import java.util.Date;

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.google.gwt.i18n.shared.DateTimeFormat;
import com.sencha.gxt.widget.core.client.form.DateTimePropertyEditor;

/**
 * This {@link DateTimePropertyEditor} implementation can be configured with a Codec for handling string/date encoding/decoding.
 * @author michel.docouto
 *
 */
public class CodecDateTimePropertyEditor extends DateTimePropertyEditor {
	
	private Codec<Date, String> codec;
	
	public CodecDateTimePropertyEditor() {
		format = DateTimeFormat.getFormat("dd.MM.yyyy");
	}
	
	/**
	 * Configures the required codec use for validation.
	 */
	public void setCodec(Codec<Date, String> codec) {
		this.codec = codec;
	}
	
	/**
	 * Configures the default pattern, used for returning a default DateTimeFormat.
	 * Defaults to dd.MM.yyyy
	 */
	public void setDefaultPattern(String defaultPattern) {
		format = DateTimeFormat.getFormat(defaultPattern);
	}
	
	@Override
	public DateTimeFormat getFormat() {
		return format;
	}
	
	@Override
	public Date parse(CharSequence text) throws ParseException {
		try {
			return codec.decode(text.toString());
		} catch (CodecException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	@Override
	public String render(Date value) {
		try {
			return codec.encode(value);
		} catch (CodecException e) {
			throw new IllegalArgumentException(e);
		}
	}

}
