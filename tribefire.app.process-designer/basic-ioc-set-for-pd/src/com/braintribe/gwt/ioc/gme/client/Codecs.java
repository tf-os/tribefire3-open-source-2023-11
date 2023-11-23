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
package com.braintribe.gwt.ioc.gme.client;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.braintribe.codec.Codec;
import com.braintribe.gwt.codec.registry.client.CodecRegistry;
import com.braintribe.gwt.codec.string.client.BigDecimalCodec;
import com.braintribe.gwt.codec.string.client.BooleanCodec;
import com.braintribe.gwt.codec.string.client.DoubleCodec;
import com.braintribe.gwt.codec.string.client.FloatCodec;
import com.braintribe.gwt.codec.string.client.IntegerCodec;
import com.braintribe.gwt.codec.string.client.LongCodec;
import com.braintribe.gwt.gmview.codec.client.LocalizedStringRendererCodec;
import com.braintribe.gwt.gmview.codec.client.MetaDataRelatedDateCodec;
import com.braintribe.gwt.gmview.util.client.LocaleUtil;
import com.braintribe.gwt.gxt.gxtresources.text.LocalizedText;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.provider.SingletonBeanProvider;
import com.google.gwt.core.client.GWT;

public class Codecs {
	
	private static LocalizedText localizedText = (LocalizedText) GWT.create(LocalizedText.class);
	
	private static Supplier<MetaDataRelatedDateCodec> displayDateTimeCodec = new SingletonBeanProvider<MetaDataRelatedDateCodec>() {
		@Override
		public MetaDataRelatedDateCodec create() throws Exception {
			MetaDataRelatedDateCodec bean = publish(new MetaDataRelatedDateCodec());
			bean.setFormatByString(LocaleUtil.getDateTimeFormat());
			bean.setDefaultPattern(LocaleUtil.getDateTimeFormat());
			return bean;
		}
	};
	
	private static Supplier<IntegerCodec> integerCodec = new SingletonBeanProvider<IntegerCodec>() {
		@Override
		public IntegerCodec create() throws Exception {
			IntegerCodec bean = new IntegerCodec();
			return bean;
		}
	};
	
	private static Supplier<DoubleCodec> doubleCodec = new SingletonBeanProvider<DoubleCodec>() {
		@Override
		public DoubleCodec create() throws Exception {
			DoubleCodec bean = new DoubleCodec();
			bean.setUseCommaAsDecimalSeparator(useCommaAsDecimalSeparator.get());
			return bean;
		}
	};
	
	private static Supplier<LongCodec> longCodec = new SingletonBeanProvider<LongCodec>() {
		@Override
		public LongCodec create() throws Exception {
			LongCodec bean = new LongCodec();
			return bean;
		}
	};
	
	private static Supplier<BigDecimalCodec> bigDecimalCodec = new SingletonBeanProvider<BigDecimalCodec>() {
		@Override
		public BigDecimalCodec create() throws Exception {
			BigDecimalCodec bean = new BigDecimalCodec();
			bean.setUseCommaAsDecimalSeparator(useCommaAsDecimalSeparator.get());
			return bean;
		}
	};
	
	private static Supplier<FloatCodec> floatCodec = new SingletonBeanProvider<FloatCodec>() {
		@Override
		public FloatCodec create() throws Exception {
			FloatCodec bean = new FloatCodec();
			bean.setUseCommaAsDecimalSeparator(useCommaAsDecimalSeparator.get());
			return bean;
		}
	};
	
	private static Supplier<BooleanCodec> displayBooleanCodec = new SingletonBeanProvider<BooleanCodec>() {
		@Override
		public BooleanCodec create() throws Exception {
			BooleanCodec bean = new BooleanCodec(localizedText.trueEntry(), localizedText.falseEntry());
			return bean;
		}
	};
	
	private static Supplier<LocalizedStringRendererCodec> localizedStringRendererCodec = new SingletonBeanProvider<LocalizedStringRendererCodec>() {
		@Override
		public LocalizedStringRendererCodec create() throws Exception {
			LocalizedStringRendererCodec bean = new LocalizedStringRendererCodec();
			return bean;
		}
	};
	
	protected static Supplier<CodecRegistry<String>> renderersCodecRegistry = new SingletonBeanProvider<CodecRegistry<String>>() {
		@Override
		public CodecRegistry<String> create() throws Exception {
			CodecRegistry<String> bean = publish(new CodecRegistry<>());
			bean.setCodecMap(gmRendererCodecsMap.get());
			return bean;
		}
	};
	
	private static Supplier<Boolean> useCommaAsDecimalSeparator = new SingletonBeanProvider<Boolean>() {
		@Override
		public Boolean create() throws Exception {
			return LocaleUtil.getDecimalSeparator().equals(",");
		}
	};
	
	private static Supplier<Map<Class<?>, Supplier<? extends Codec<?, String>>>> gmRendererCodecsMap = new SingletonBeanProvider<Map<Class<?>, Supplier<? extends Codec<?, String>>>>() {
		@Override
		public Map<Class<?>, Supplier<? extends Codec<?, String>>> create() throws Exception {
			Map<Class<?>, Supplier<? extends Codec<?, String>>> bean = publish(new HashMap<>());
			bean.put(Date.class, displayDateTimeCodec);
			bean.put(Integer.class, integerCodec);
			bean.put(Double.class, doubleCodec);
			bean.put(Long.class, longCodec);
			bean.put(BigDecimal.class, bigDecimalCodec);
			bean.put(Float.class, floatCodec);
			bean.put(Boolean.class, displayBooleanCodec);
			bean.put(LocalizedString.class, localizedStringRendererCodec);
			return bean;
		}
	};

}
