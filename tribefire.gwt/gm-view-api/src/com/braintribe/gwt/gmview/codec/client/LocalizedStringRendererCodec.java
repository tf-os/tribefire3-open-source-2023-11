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
package com.braintribe.gwt.gmview.codec.client;

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.utils.i18n.I18nTools;

/**
 * This codec is responsible for transforming a LocalizedString into String,
 * for visualization only purpose.
 * @author michel.docouto
 * 
 */
public class LocalizedStringRendererCodec implements Codec<LocalizedString, String> {

	@Override
	public LocalizedString decode(String encodedValue) throws CodecException {
		throw new CodecException("Decode is not supported");
	}

	@Override
	public String encode(LocalizedString localizedString) throws CodecException {
		return I18nTools.getDefault(localizedString, "");
	}

	@Override
	public Class<LocalizedString> getValueClass() {
		return LocalizedString.class;
	}

}
