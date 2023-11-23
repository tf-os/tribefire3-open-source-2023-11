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
package com.braintribe.model.processing.meta.oracle;

import static com.braintribe.utils.lcd.CollectionTools2.nullSafe;

import java.util.stream.Stream;

import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmModelElement;
import com.braintribe.model.meta.data.HasMetaData;
import com.braintribe.model.meta.info.GmEntityTypeInfo;
import com.braintribe.model.meta.info.GmEnumTypeInfo;

/**
 * @author peter.gazdik
 */
public interface QualifiedMetaDataTools {

	static <M extends GmModelElement & HasMetaData> Stream<QualifiedMetaData> ownMetaData(M modelElementWithMd) {
		return nullSafe(modelElementWithMd.getMetaData()).stream().map(md -> new BasicQualifiedMetaData(md, modelElementWithMd));
	}

	static Stream<QualifiedMetaData> modelEnumMetaData(GmMetaModel model) {
		return nullSafe(model.getEnumTypeMetaData()).stream().map(md -> new BasicQualifiedMetaData(md, model));
	}

	static Stream<QualifiedMetaData> modelConstantMetaData(GmMetaModel model) {
		return nullSafe(model.getEnumConstantMetaData()).stream().map(md -> new BasicQualifiedMetaData(md, model));
	}

	static Stream<QualifiedMetaData> entityPropertyMetaData(GmEntityTypeInfo eti) {
		return nullSafe(eti.getPropertyMetaData()).stream().map(md -> new BasicQualifiedMetaData(md, eti));
	}

	static Stream<QualifiedMetaData> enumConstantMetaData(GmEnumTypeInfo eti) {
		return nullSafe(eti.getEnumConstantMetaData()).stream().map(md -> new BasicQualifiedMetaData(md, eti));
	}

}
