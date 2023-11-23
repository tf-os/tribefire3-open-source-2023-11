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
package tribefire.extension.xml.schemed.xsd.mapper;

import java.util.Collection;

import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.processing.itw.analysis.JavaTypeAnalysis;

import tribefire.extension.xml.schemed.xsd.analyzer.registry.schema.QPath;
import tribefire.extension.xml.schemed.xsd.api.mapper.type.TypeMapper;

/**
 * a generator that creates {@link GmType} for some special XSD types, such as NMTOKEN,TOKEN,NcName etc
 * @author pit
 *
 */
public interface SpecialConstraintsTypeResolver {
	static String token_Pattern = "^[^\\s][^\\n\\t]*[^\\s]$";
	static final String nmToken_Pattern = "\\c+";
	static final String language_Pattern = "[a-zA-Z]{1,8}(-[a-zA-Z0-9]{1,8})*";
	static final String ncName_Pattern = "[\\i-[:]][\\c-[:]]*";
	static final String normalizedString_Pattern = "[^\\n\\t]*";
	
	

	default GmType createSpecializedType( TypeMapper typeMapper, String xsdName, GmType baseType, Collection<MetaData> metadata) {
		QPath qpath = new QPath();
		GmEntityType specializedType = typeMapper.generateGmEntityTypeForSimpleType(qpath, null, xsdName, baseType);
		specializedType.setGlobalId( JavaTypeAnalysis.typeGlobalId( specializedType.getTypeSignature()));
		specializedType.getMetaData().addAll(metadata);		
		return specializedType;
	}

	GmType createNmToken();
	GmType createLanguage();
	GmType createToken();
	GmType createNormalizedString();
	GmType createNcName();
	GmType createId();
	GmType createIdRef();
	
	GmType createAnyType(String packageName);
}
