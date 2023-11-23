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
import com.braintribe.model.style.Color;

/**
 * This codec is responsible for transforming a {@link Color} into String,
 * for visualization only purpose.
 * @author michel.docouto
 * 
 */
public class ColorRendererCodec implements Codec<Color, String> {
	private static final String EMPTY_STRING = "";

	@Override
	public Color decode(String encodedValue) throws CodecException {
		throw new CodecException("Decode is not supported");
	}

	@Override
	public String encode(Color color) throws CodecException {
		if (color == null)
			return EMPTY_STRING;
		
		StringBuilder builder = new StringBuilder();
		builder.append("(").append(color.getRed()).append(",").append(color.getGreen()).append(",").append(color.getBlue()).append(")");
		return builder.toString();
		/*String hRed = "00";
		String hGreen = "00";
		String hBlue = "00";
		if (color.getRed() != null)
			hRed = Integer.toHexString(color.getRed());
        if (color.getGreen() != null)
        	hGreen = Integer.toHexString(color.getGreen());
        if (color.getBlue() != null)
        	hBlue = Integer.toHexString(color.getBlue());

        if (hRed.length() == 0)
        	hRed = "00";
        else if (hRed.length() == 1)
        	hRed = "0" + hRed;
        
        if (hGreen.length() == 0) 
        	hGreen = "00";
        else if (hGreen.length() == 1) 
        	hGreen = "0" + hGreen;
        
        if (hBlue.length() == 0) 
        	hBlue = "00";
        else if (hBlue.length() == 1) 
        	hBlue = "0" + hBlue;

        return hRed + hGreen + hBlue;*/
	}

	@Override
	public Class<Color> getValueClass() {
		return Color.class;
	}

}
