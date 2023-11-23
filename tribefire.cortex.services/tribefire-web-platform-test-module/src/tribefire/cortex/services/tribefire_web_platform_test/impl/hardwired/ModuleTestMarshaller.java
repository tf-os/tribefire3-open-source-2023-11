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
package tribefire.cortex.services.tribefire_web_platform_test.impl.hardwired;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.tools.GmValueCodec;
import com.braintribe.utils.IOTools;

/**
 * @author peter.gazdik
 */
public class ModuleTestMarshaller implements Marshaller {

	@Override
	public Object unmarshall(InputStream in, GmDeserializationOptions options) throws MarshallException {
		return unmarshall(in);
	}

	@Override
	public Object unmarshall(InputStream in) throws MarshallException {
		try {
			String s = IOTools.slurp(in, StandardCharsets.UTF_8.name());
			return GmValueCodec.objectFromGmString(s);

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void marshall(OutputStream out, Object value, GmSerializationOptions options) throws MarshallException {
		marshall(out, value);
	}

	@Override
	public void marshall(OutputStream out, Object value) throws MarshallException {
		GenericModelType type = GMF.getTypeReflection().getType(value);
		if (!type.isSimple())
			throw new UnsupportedOperationException("Test error. Only simple values should be used, not: " + value);

		try {
			out.write(GmValueCodec.objectToGmString(value).getBytes());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
