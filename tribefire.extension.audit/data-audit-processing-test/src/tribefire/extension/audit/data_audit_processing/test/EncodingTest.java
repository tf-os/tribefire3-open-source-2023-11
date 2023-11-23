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
package tribefire.extension.audit.data_audit_processing.test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.meta.GmTypeKind;
import com.braintribe.model.resource.Resource;

import tribefire.extension.audit.processing.ManipulationRecordValueEncoder;

public class EncodingTest {
	
	@Test
	public void testEncodings() {
		List<Object> list = new ArrayList<Object>();
		list.add(null);
		list.add(1L);
		list.add(23D);
		list.add(17F);
		list.add(5);
		list.add(true);
		list.add(new BigDecimal("3.1415"));
		list.add(new Date(1617115671832L));
		list.add("Hallo Welt!");
		list.add("Hallo 'Welt!");
		list.add("Hallo \\Welt!");
		list.add(GmTypeKind.BASE);
		
		Resource resource2 = Resource.T.create();
		resource2.setId(23L);
		Resource resource3 = Resource.T.create();
		resource3.setId(42L);
		resource3.setPartition("partition");
		
		list.add(resource2);
		list.add(resource3);
		
		Set<Object> set = new LinkedHashSet<>(list);
		
		Map<Object, Object> map = new LinkedHashMap<>();
		
		for (Object v: list) {
			map.put(v, v);
		}
		
		String encodedList = ManipulationRecordValueEncoder.encode(GMF.getTypeReflection().getListType(BaseType.INSTANCE), list);
		String encodedSet = ManipulationRecordValueEncoder.encode(GMF.getTypeReflection().getSetType(BaseType.INSTANCE), set);
		String encodedMap = ManipulationRecordValueEncoder.encode(GMF.getTypeReflection().getMapType(BaseType.INSTANCE, BaseType.INSTANCE), map);
		
		String expectedEncodedList = "[null,1L,23.0,17.0F,5,true,3.1415B,@2021-03-30T14:47:51.832+0000,'Hallo Welt!','Hallo \\'Welt!','Hallo \\\\Welt!',com.braintribe.model.meta.GmTypeKind->BASE,com.braintribe.model.resource.Resource[23L],com.braintribe.model.resource.Resource[42L,'partition']]";
		String expectedEncodedSet = "(null,1L,23.0,17.0F,5,true,3.1415B,@2021-03-30T14:47:51.832+0000,'Hallo Welt!','Hallo \\'Welt!','Hallo \\\\Welt!',com.braintribe.model.meta.GmTypeKind->BASE,com.braintribe.model.resource.Resource[23L],com.braintribe.model.resource.Resource[42L,'partition'])";
		String expectedEncodedMap = "{null:null,1L:1L,23.0:23.0,17.0F:17.0F,5:5,true:true,3.1415B:3.1415B,@2021-03-30T14:47:51.832+0000:@2021-03-30T14:47:51.832+0000,'Hallo Welt!':'Hallo Welt!','Hallo \\'Welt!':'Hallo \\'Welt!','Hallo \\\\Welt!':'Hallo \\\\Welt!',com.braintribe.model.meta.GmTypeKind->BASE:com.braintribe.model.meta.GmTypeKind->BASE,com.braintribe.model.resource.Resource[23L]:com.braintribe.model.resource.Resource[23L],com.braintribe.model.resource.Resource[42L,'partition']:com.braintribe.model.resource.Resource[42L,'partition']}";

		Assertions.assertThat(encodedList).isEqualTo(expectedEncodedList);
		Assertions.assertThat(encodedSet).isEqualTo(expectedEncodedSet);
		Assertions.assertThat(encodedMap).isEqualTo(expectedEncodedMap);
	}
}
