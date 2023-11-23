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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.braintribe.model.generic.reflection.EssentialCollectionTypes;

import tribefire.extension.audit.processing.ManipulationRecordValueDecoder;
import tribefire.extension.audit.processing.ManipulationRecordValueEncoder;

public class DecodingTest {
	
	@Test
	public void testScalars() {
		// integers
		Assertions.assertThat((Object)ManipulationRecordValueDecoder.parse("0")).isEqualTo(0);
		Assertions.assertThat((Object)ManipulationRecordValueDecoder.parse("5")).isEqualTo(5);
		Assertions.assertThat((Object)ManipulationRecordValueDecoder.parse("+5")).isEqualTo(5);
		Assertions.assertThat((Object)ManipulationRecordValueDecoder.parse("-5")).isEqualTo(-5);
		Assertions.assertThat((Object)ManipulationRecordValueDecoder.parse(Integer.toString(Integer.MAX_VALUE))).isEqualTo(Integer.MAX_VALUE);
		Assertions.assertThat((Object)ManipulationRecordValueDecoder.parse(Integer.toString(Integer.MIN_VALUE))).isEqualTo(Integer.MIN_VALUE);
		
		// longs
		Assertions.assertThat((Object)ManipulationRecordValueDecoder.parse("0L")).isEqualTo(0L);
		Assertions.assertThat((Object)ManipulationRecordValueDecoder.parse("5L")).isEqualTo(5L);
		Assertions.assertThat((Object)ManipulationRecordValueDecoder.parse("+5L")).isEqualTo(5L);
		Assertions.assertThat((Object)ManipulationRecordValueDecoder.parse("-5L")).isEqualTo(-5L);
		Assertions.assertThat((Object)ManipulationRecordValueDecoder.parse(Long.toString(Long.MAX_VALUE) + "L")).isEqualTo(Long.MAX_VALUE);
		Assertions.assertThat((Object)ManipulationRecordValueDecoder.parse(Long.toString(Long.MIN_VALUE) + "L")).isEqualTo(Long.MIN_VALUE);
		
		// floats
		Assertions.assertThat((Object)ManipulationRecordValueDecoder.parse("5E+9F")).isEqualTo(5E+9F);
		Assertions.assertThat((Object)ManipulationRecordValueDecoder.parse("5E-9F")).isEqualTo(5E-9F);
		Assertions.assertThat((Object)ManipulationRecordValueDecoder.parse("-5E+9F")).isEqualTo(-5E+9F);
		Assertions.assertThat((Object)ManipulationRecordValueDecoder.parse("+5E-9F")).isEqualTo(+5E-9F);
		Assertions.assertThat((Object)ManipulationRecordValueDecoder.parse("-5.3E+9F")).isEqualTo(-5.3E+9F);
		Assertions.assertThat((Object)ManipulationRecordValueDecoder.parse("+5.3E-9F")).isEqualTo(+5.3E-9F);
		
		// decimals
		Assertions.assertThat(decimalEquals(ManipulationRecordValueDecoder.parse("5E+9B"), new BigDecimal("5E+9"))).isTrue();
		Assertions.assertThat(decimalEquals(ManipulationRecordValueDecoder.parse("5E-9B"), new BigDecimal("5E-9"))).isTrue();
		Assertions.assertThat(decimalEquals(ManipulationRecordValueDecoder.parse("-5E+9B"), new BigDecimal("-5E+9"))).isTrue();
		Assertions.assertThat(decimalEquals(ManipulationRecordValueDecoder.parse("+5E-9B"), new BigDecimal("+5E-9"))).isTrue();
		Assertions.assertThat(decimalEquals(ManipulationRecordValueDecoder.parse("-5.3E+9B"), new BigDecimal("-5.3E+9"))).isTrue();
		Assertions.assertThat(decimalEquals(ManipulationRecordValueDecoder.parse("+5.3E-9B"), new BigDecimal("+5.3E-9"))).isTrue();

		// doubles
		Assertions.assertThat((Object)ManipulationRecordValueDecoder.parse("5E+9")).isEqualTo(5E+9D);
		Assertions.assertThat((Object)ManipulationRecordValueDecoder.parse("5E-9")).isEqualTo(5E-9D);
		Assertions.assertThat((Object)ManipulationRecordValueDecoder.parse("-5E+9")).isEqualTo(-5E+9D);
		Assertions.assertThat((Object)ManipulationRecordValueDecoder.parse("+5E-9")).isEqualTo(+5E-9D);
		Assertions.assertThat((Object)ManipulationRecordValueDecoder.parse("-5.3E+9")).isEqualTo(-5.3E+9D);
		Assertions.assertThat((Object)ManipulationRecordValueDecoder.parse("+5.3E-9")).isEqualTo(+5.3E-9D);
		Assertions.assertThat((Object)ManipulationRecordValueDecoder.parse("-5.3E+9D")).isEqualTo(-5.3E+9D);
		Assertions.assertThat((Object)ManipulationRecordValueDecoder.parse("+5.3E-9D")).isEqualTo(+5.3E-9D);
		
		// null
		Assertions.assertThat((Object)ManipulationRecordValueDecoder.parse("null")).isEqualTo(null);
		
		// booleans
		Assertions.assertThat((Object)ManipulationRecordValueDecoder.parse("true")).isEqualTo(true);
		Assertions.assertThat((Object)ManipulationRecordValueDecoder.parse("false")).isEqualTo(false);
	}

	private boolean decimalEquals(BigDecimal decimal, BigDecimal decimalParsed) {
		return decimalParsed.compareTo(decimal) == 0;
	}
	
	@Test
	public void testCollections() {
		String encodedList = "[null,1L,23.0,17.0F,5,true,false,3.1415B,@2021-03-30T14:47:51.832+0000,'Hallo Welt!','Hallo \\'Welt!','Hallo \\\\Welt!',com.braintribe.model.meta.GmTypeKind->BASE,com.braintribe.model.resource.Resource[23L],com.braintribe.model.resource.Resource[42L,'partition']]";
		String encodedSet = "(null,1L,23.0,17.0F,5,true,false,3.1415B,@2021-03-30T14:47:51.832+0000,'Hallo Welt!','Hallo \\'Welt!','Hallo \\\\Welt!',com.braintribe.model.meta.GmTypeKind->BASE,com.braintribe.model.resource.Resource[23L],com.braintribe.model.resource.Resource[42L,'partition'],~com.braintribe.model.resource.Resource[42L,'partition'])";
		String encodedMap = "{null:null,1L:1L,23.0:23.0,17.0F:17.0F,5:5,true:true,false:false,3.1415B:3.1415B,@2021-03-30T14:47:51.832+0000:@2021-03-30T14:47:51.832+0000,'Hallo Welt!':'Hallo Welt!','Hallo \\'Welt!':'Hallo \\'Welt!','Hallo \\\\Welt!':'Hallo \\\\Welt!',com.braintribe.model.meta.GmTypeKind->BASE:com.braintribe.model.meta.GmTypeKind->BASE,com.braintribe.model.resource.Resource[23L]:com.braintribe.model.resource.Resource[23L],com.braintribe.model.resource.Resource[42L,'partition']:com.braintribe.model.resource.Resource[42L,'partition']}";

		List<Object> decodedList = ManipulationRecordValueDecoder.parse(encodedList);
		Set<Object> decodedSet = ManipulationRecordValueDecoder.parse(encodedSet);
		Map<Object, Object> decodedMap = ManipulationRecordValueDecoder.parse(encodedMap);
		
		String reencodedList = ManipulationRecordValueEncoder.encode(EssentialCollectionTypes.TYPE_LIST, decodedList);
		String reencodedMap = ManipulationRecordValueEncoder.encode(EssentialCollectionTypes.TYPE_MAP, decodedMap);
		String reencodedSet = ManipulationRecordValueEncoder.encode(EssentialCollectionTypes.TYPE_SET, decodedSet);

		Assertions.assertThat(encodedList).isEqualTo(reencodedList);
		Assertions.assertThat(encodedSet).isEqualTo(reencodedSet);
		Assertions.assertThat(encodedMap).isEqualTo(reencodedMap);
	}
}
