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
package com.braintribe.codec.string;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.braintribe.codec.string.GzipBase64Codec;

/**
 * Uses any Object/String codec and additionally zips and Base64-encodes the result (and vice versa).
 * 
 */
public class CompressingEntityCodec implements Codec<Object,String> {

	protected Codec<Object,String> embeddedCodec = null;
	protected GzipBase64Codec compressCodec = new GzipBase64Codec();
	

	@Override
	public String encode(Object value) throws CodecException {
		String encodedValue = this.embeddedCodec.encode(value);
		String compressedValue = this.compressCodec.encode(encodedValue);
		return compressedValue;
	}

	@Override
	public Object decode(String encodedValue) throws CodecException {
		String decompressedValue = this.compressCodec.decode(encodedValue);
		Object decodedValue = this.embeddedCodec.decode(decompressedValue);
		return decodedValue;
	}

	@Override
	public Class<Object> getValueClass() {
		return this.embeddedCodec.getValueClass();
	}

	@Configurable @Required
	public void setEmbeddedCodec(Codec<Object, String> embeddedCodec) {
		this.embeddedCodec = embeddedCodec;
	}
	@Configurable
	public void setCompressCodec(GzipBase64Codec compressCodec) {
		this.compressCodec = compressCodec;
	}

}
