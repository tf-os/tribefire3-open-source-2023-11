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
package com.braintribe.codec.registry;

import java.io.File;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;

import com.braintribe.codec.string.BigDecimalCodec;
import com.braintribe.codec.string.BooleanCodec;
import com.braintribe.codec.string.ClassCodec;
import com.braintribe.codec.string.CommaSeparatedStringsCodec;
import com.braintribe.codec.string.DateCodec;
import com.braintribe.codec.string.DoubleCodec;
import com.braintribe.codec.string.FloatCodec;
import com.braintribe.codec.string.IntegerCodec;
import com.braintribe.codec.string.LongCodec;
import com.braintribe.codec.string.NullCodec;
import com.braintribe.codec.string.StringCodec;
import com.braintribe.codec.string.UniformDoubleCodec;
import com.braintribe.codec.string.UrlCodec;
import com.braintribe.codec.string.UrlEncodedLongArrayCodec;
import com.braintribe.codec.string.UrlEncodedStringArrayCodec;
import com.braintribe.logging.Logger;

public class BasicCodecs {

	protected static final Logger logger = Logger.getLogger(BasicCodecs.class);
	
	public static final BooleanCodec booleanCodec = new BooleanCodec();
	public static final ClassCodec classCodec = new ClassCodec();
	//public static final ConstantCodec constantCodec = new ConstantCodec();
	public static final DateCodec dateCodec = new DateCodec();
	public static final BigDecimalCodec bigDecimalCodec = new BigDecimalCodec();
	public static final FloatCodec floatCodec = new FloatCodec();
	public static final DoubleCodec doubleCodec = new DoubleCodec();
	public static final UniformDoubleCodec uniformDoubleCodec = new UniformDoubleCodec();
	public static final IntegerCodec integerCodec = new IntegerCodec();
	public static final LongCodec longCodec = new LongCodec();
	public static final NullCodec nullCodec = new NullCodec();
	public static final StringCodec stringCodec = new StringCodec();
	public static final UrlEncodedStringArrayCodec stringListCodec = new UrlEncodedStringArrayCodec();
	public static final UrlEncodedStringArrayCodec urlEncodedStringArrayCodec = new UrlEncodedStringArrayCodec();
	public static final UrlEncodedLongArrayCodec urlEncodedLongArrayCodec = new UrlEncodedLongArrayCodec();
	public static final CommaSeparatedStringsCodec commaSeparatedStringsCodec = new CommaSeparatedStringsCodec();
	
	public static void registerCodecs(CodecRegistry registry) {
		registerCodecs(registry, true);
	}
  
	public static void registerCodecs(CodecRegistry registry, boolean override) {
		registry.registerCodec("null", nullCodec, override);
		registry.registerCodec("string", String.class, stringCodec, override);
		registry.registerCodec("int", Integer.class, integerCodec, override);
		registry.registerCodec("long", Long.class, longCodec, override);
		registry.registerCodec("double", Double.class, doubleCodec, override);
		registry.registerCodec("uniformDouble", Double.class, uniformDoubleCodec, override);
		registry.registerCodec("float", Float.class, floatCodec, override);
		registry.registerCodec("boolean", Boolean.class, booleanCodec, override); //boolean
		registry.registerCodec("timestamp", Date.class, dateCodec, override); //date from long
		registry.registerCodec("bigDecimal", BigDecimal.class, bigDecimalCodec, override);
		registry.registerCodec("class", Class.class, classCodec, override); //class for name
		registry.registerCodec("string[]", commaSeparatedStringsCodec, override); 
		registry.registerCodec("ustring[]", urlEncodedStringArrayCodec, override);
		
		try {
			registry.registerCodec("url", URL.class, new UrlCodec( new File(".").toURI().toURL() ), override);
		} catch (MalformedURLException e) {
			throw new RuntimeException("unexpected exception while creating URL for current working dir", e);
		} catch (SecurityException e) {
			//NOTE: silently ignore. The URL parser will not be available
			//      unless registered explicitely by the application.
			//      This is the usual case for (unsigned) applets.
		}
		
	}
	
	public static void main(String[] args) {
        Double d = Double.valueOf(.1);
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        DecimalFormat format = new DecimalFormat("", symbols);
        format.setGroupingUsed(false);
        format.setMinimumFractionDigits(1);
        format.setMinimumIntegerDigits(1);
        System.out.println(format.format(d));        
    }
}
