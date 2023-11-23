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
package com.braintribe.codec.marshaller.dom.coder;

import com.braintribe.codec.marshaller.dom.coder.collection.ListDomCoder;
import com.braintribe.codec.marshaller.dom.coder.collection.MapDomCoder;
import com.braintribe.codec.marshaller.dom.coder.collection.SetDomCoder;
import com.braintribe.codec.marshaller.dom.coder.entity.EntityReferenceDomCoder;
import com.braintribe.codec.marshaller.dom.coder.scalar.BooleanDomCoder;
import com.braintribe.codec.marshaller.dom.coder.scalar.DateDomCoder;
import com.braintribe.codec.marshaller.dom.coder.scalar.DecimalDomCoder;
import com.braintribe.codec.marshaller.dom.coder.scalar.DoubleDomCoder;
import com.braintribe.codec.marshaller.dom.coder.scalar.EnumDomCoder;
import com.braintribe.codec.marshaller.dom.coder.scalar.FloatDomCoder;
import com.braintribe.codec.marshaller.dom.coder.scalar.IntegerDomCoder;
import com.braintribe.codec.marshaller.dom.coder.scalar.LongDomCoder;
import com.braintribe.codec.marshaller.dom.coder.scalar.NullDomCoder;
import com.braintribe.codec.marshaller.dom.coder.scalar.StringDomCoder;
import com.braintribe.model.generic.GenericEntity;

public class DomCoders {
	// null
	public static final NullDomCoder nullCoder = new NullDomCoder();
	
	// object
	public static final ObjectDomCoder objectCoder = new ObjectDomCoder();
	
	// scalar types
	public static final StringDomCoder stringCoder = new StringDomCoder();
	public static final BooleanDomCoder booleanCoder = new BooleanDomCoder();
	public static final IntegerDomCoder integerCoder = new IntegerDomCoder();
	public static final LongDomCoder longCoder = new LongDomCoder();
	public static final FloatDomCoder floatCoder = new FloatDomCoder();
	public static final DoubleDomCoder doubleCoder = new DoubleDomCoder();
	public static final DecimalDomCoder decimalCoder = new DecimalDomCoder();
	public static final DateDomCoder dateCoder = new DateDomCoder();
	
	// custom types
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static final DomCoder<Object> enumCoder = new EnumDomCoder();
	public static final EntityReferenceDomCoder<GenericEntity> entityReferenceCoder = new EntityReferenceDomCoder<GenericEntity>();
	
	// collections
	public static final ListDomCoder<Object> listCoder = new ListDomCoder<Object>(objectCoder);
	public static final SetDomCoder<Object> setCoder = new SetDomCoder<Object>(objectCoder);
	public static final MapDomCoder<Object, Object> mapCoder = new MapDomCoder<Object, Object>(objectCoder, objectCoder);
}
