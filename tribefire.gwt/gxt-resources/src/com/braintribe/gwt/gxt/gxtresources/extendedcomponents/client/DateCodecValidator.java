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

import java.util.Date;
import java.util.List;

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.braintribe.gwt.gxt.gxtresources.text.LocalizedText;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.EditorError;
import com.sencha.gxt.widget.core.client.form.error.DefaultEditorError;
import com.sencha.gxt.widget.core.client.form.validator.AbstractValidator;

/**
 * This Validator is used for validating Date Fields by using a given Codec.
 * @author michel.docouto
 *
 */
public class DateCodecValidator extends AbstractValidator<String> {
	
	private Codec<Date, String> codec;
	private String patternForErrorMessage;
	
	/**
	 * Configures the required codec use for validation.
	 */
	public void setCodec(Codec<Date, String> codec) {
		this.codec = codec;
	}
	
	/**
	 * Configures the required pattern for displaying in the error message, in case a given string
	 * is not a valid date.
	 */
	public void setPatternForErrorMessage(String patternForErrorMessage) {
		this.patternForErrorMessage = patternForErrorMessage;
	}
	
	@Override
	public List<EditorError> validate(Editor<String> editor, String value) {
		try {
			codec.decode(value);
		} catch (CodecException e) {
			return createError(new DefaultEditorError(editor, LocalizedText.INSTANCE.invalidDate(patternForErrorMessage), value));
		}
		return null;
	}

}
