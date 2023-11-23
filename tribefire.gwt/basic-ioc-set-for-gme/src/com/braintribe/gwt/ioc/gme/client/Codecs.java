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
import com.braintribe.gwt.gme.propertypanel.client.field.SimplifiedEntityRendererCodec;
import com.braintribe.gwt.gme.propertypanel.client.field.SimplifiedEntityRendererCodecsProvider;
import com.braintribe.gwt.gmview.codec.client.ColorRendererCodec;
import com.braintribe.gwt.gmview.codec.client.IconRendererCodec;
import com.braintribe.gwt.gmview.codec.client.KeyConfigurationRendererCodec;
import com.braintribe.gwt.gmview.codec.client.LocalizedStringRendererCodec;
import com.braintribe.gwt.gmview.codec.client.MetaDataRelatedDateCodec;
import com.braintribe.gwt.gmview.codec.client.ResourceRendererCodec;
import com.braintribe.gwt.gmview.codec.client.TimeSpanRendererCodec;
import com.braintribe.gwt.gmview.util.client.LocaleUtil;
import com.braintribe.gwt.gxt.gxtresources.text.LocalizedText;
import com.braintribe.model.common.Color;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.resource.AdaptiveIcon;
import com.braintribe.model.resource.Icon;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.SimpleIcon;
import com.braintribe.model.time.TimeSpan;
import com.braintribe.model.workbench.KeyConfiguration;
import com.braintribe.provider.SingletonBeanProvider;
import com.google.gwt.core.client.GWT;

class Codecs {
	
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
	
	private static Supplier<MetaDataRelatedDateCodec> displayDateCodec = new SingletonBeanProvider<MetaDataRelatedDateCodec>() {
		@Override
		public MetaDataRelatedDateCodec create() throws Exception {
			MetaDataRelatedDateCodec bean = publish(new MetaDataRelatedDateCodec());
			bean.setFormatByString(LocaleUtil.getDateFormat());
			bean.setDefaultPattern(LocaleUtil.getDateFormat());
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
	
	private static Supplier<Boolean> useCommaAsDecimalSeparator = new SingletonBeanProvider<Boolean>() {
		@Override
		public Boolean create() throws Exception {
			return LocaleUtil.getDecimalSeparator().equals(",");
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
	
	private static Supplier<LocalizedStringRendererCodec> localizedStringRendererCodec = new SingletonBeanProvider<LocalizedStringRendererCodec>() {
		@Override
		public LocalizedStringRendererCodec create() throws Exception {
			LocalizedStringRendererCodec bean = new LocalizedStringRendererCodec();
			return bean;
		}
	};
	
	private static Supplier<ColorRendererCodec> colorRendererCodec = new SingletonBeanProvider<ColorRendererCodec>() {
		@Override
		public ColorRendererCodec create() throws Exception {
			ColorRendererCodec bean = new ColorRendererCodec();
			return bean;
		}
	};

	private static Supplier<TimeSpanRendererCodec> timeSpanRendererCodec = new SingletonBeanProvider<TimeSpanRendererCodec>() {
		@Override
		public TimeSpanRendererCodec create() throws Exception {
			TimeSpanRendererCodec bean = new TimeSpanRendererCodec();
			return bean;
		}
	};
	
	protected static Supplier<SimplifiedEntityRendererCodec> simplifiedEntityRendererCodec = new SingletonBeanProvider<SimplifiedEntityRendererCodec>() {
		@Override
		public SimplifiedEntityRendererCodec create() throws Exception {
			SimplifiedEntityRendererCodec bean = new SimplifiedEntityRendererCodec();
			return bean;
		}
	};
	
	private static Supplier<SimplifiedEntityRendererCodec> shortTypeRendererCodec = new SingletonBeanProvider<SimplifiedEntityRendererCodec>() {
		@Override
		public SimplifiedEntityRendererCodec create() throws Exception {
			SimplifiedEntityRendererCodec bean = new SimplifiedEntityRendererCodec();
			bean.setUseShortType(true);
			return bean;
		}
	};
	
	private static Supplier<ResourceRendererCodec> resourceRendererCodec = new SingletonBeanProvider<ResourceRendererCodec>() {
		@Override
		public ResourceRendererCodec create() throws Exception {
			ResourceRendererCodec bean = new ResourceRendererCodec();
			return bean;
		}
	};
	
	private static Supplier<IconRendererCodec> iconRendererCodec = new SingletonBeanProvider<IconRendererCodec>() {
		@Override
		public IconRendererCodec create() throws Exception {
			IconRendererCodec bean = new IconRendererCodec();
			return bean;
		}
	};
	
	private static Supplier<ResourceRendererCodec> userResourceRendererCodec = new SingletonBeanProvider<ResourceRendererCodec>() {
		@Override
		public ResourceRendererCodec create() throws Exception {
			ResourceRendererCodec bean = new ResourceRendererCodec();
			bean.setAccessId(Providers.userAccessId.get());
			return bean;
		}
	};
	
	private static Supplier<IconRendererCodec> userIconRendererCodec = new SingletonBeanProvider<IconRendererCodec>() {
		@Override
		public IconRendererCodec create() throws Exception {
			IconRendererCodec bean = new IconRendererCodec();
			bean.setAccessId(Providers.userAccessId.get());
			return bean;
		}
	};
	
	private static Supplier<KeyConfigurationRendererCodec> keyConfigurationRendererCodec = new SingletonBeanProvider<KeyConfigurationRendererCodec>() {
		@Override
		public KeyConfigurationRendererCodec create() throws Exception {
			KeyConfigurationRendererCodec bean = new KeyConfigurationRendererCodec();
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
	
	protected static Supplier<SimplifiedEntityRendererCodecsProvider> gmErrorRendererCodecsProvider =
			new SingletonBeanProvider<SimplifiedEntityRendererCodecsProvider>() {
		@Override
		public SimplifiedEntityRendererCodecsProvider create() throws Exception {
			SimplifiedEntityRendererCodecsProvider bean = publish(new SimplifiedEntityRendererCodecsProvider());
			bean.setCodecRegistry(renderersCodecRegistry.get());
			bean.setSimplifiedEntityFieldRendererCodec(shortTypeRendererCodec);
			return bean;
		}
	};
	
	protected static Supplier<CodecRegistry<String>> queryRenderersCodecRegistry = new SingletonBeanProvider<CodecRegistry<String>>() {
		@Override
		public CodecRegistry<String> create() throws Exception {
			CodecRegistry<String> bean = publish(new CodecRegistry<>());
			bean.setCodecMap(queryGmRendererCodecsMap.get());
			return bean;
		}
	};
	
	protected static Supplier<CodecRegistry<String>> specialFlowCodecRegistry = new SingletonBeanProvider<CodecRegistry<String>>() {
		@Override
		public CodecRegistry<String> create() throws Exception {
			CodecRegistry<String> bean = publish(new CodecRegistry<>());
			bean.setCodecMap(specialFlowRenderers.get());
			return bean;
		}
	};
	
	protected static Supplier<CodecRegistry<String>> userSpecialFlowCodecRegistry = new SingletonBeanProvider<CodecRegistry<String>>() {
		@Override
		public CodecRegistry<String> create() throws Exception {
			CodecRegistry<String> bean = publish(new CodecRegistry<>());
			bean.setCodecMap(userSpecialFlowRenderers.get());
			return bean;
		}
	};
	
	private static Supplier<Map<Class<?>, Supplier<? extends Codec<?, String>>>> gmRendererCodecsMap = new SingletonBeanProvider<Map<Class<?>, Supplier<? extends Codec<?, String>>>>() {
		@Override
		public Map<Class<?>, Supplier<? extends Codec<?, String>>> create() throws Exception {
			Map<Class<?>, Supplier<? extends Codec<?, String>>> bean = publish(new HashMap<>());
			bean.put(Date.class, Codecs.displayDateTimeCodec);
			bean.put(Integer.class, Codecs.integerCodec);
			bean.put(Double.class, Codecs.doubleCodec);
			bean.put(Long.class, Codecs.longCodec);
			bean.put(BigDecimal.class, Codecs.bigDecimalCodec);
			bean.put(Float.class, Codecs.floatCodec);
			bean.put(Boolean.class, Codecs.displayBooleanCodec);
			bean.put(LocalizedString.class, Codecs.localizedStringRendererCodec);
			bean.put(Color.class, Codecs.colorRendererCodec);
			bean.put(TimeSpan.class, Codecs.timeSpanRendererCodec);
			bean.put(KeyConfiguration.class, Codecs.keyConfigurationRendererCodec);
			return bean;
		}
	};
	
	private static Supplier<Map<Class<?>, Supplier<? extends Codec<?, String>>>> queryGmRendererCodecsMap = new SingletonBeanProvider<Map<Class<?>, Supplier<? extends Codec<?, String>>>>() {
		@Override
		public Map<Class<?>, Supplier<? extends Codec<?, String>>> create() throws Exception {
			Map<Class<?>, Supplier<? extends Codec<?, String>>> bean = publish(new HashMap<>());
			bean.put(Date.class, Codecs.displayDateCodec);
			bean.put(Integer.class, Codecs.integerCodec);
			bean.put(Double.class, Codecs.doubleCodec);
			bean.put(Long.class, Codecs.longCodec);
			bean.put(BigDecimal.class, Codecs.bigDecimalCodec);
			bean.put(Float.class, Codecs.floatCodec);
			bean.put(Boolean.class, Codecs.displayBooleanCodec);
			bean.put(LocalizedString.class, Codecs.localizedStringRendererCodec);
			bean.put(Color.class, Codecs.colorRendererCodec);
			bean.put(TimeSpan.class, Codecs.timeSpanRendererCodec);
			bean.put(KeyConfiguration.class, Codecs.keyConfigurationRendererCodec);
			return bean;
		}
	};
	
	private static Supplier<Map<Class<?>, Supplier<? extends Codec<?, String>>>> specialFlowRenderers = new SingletonBeanProvider<Map<Class<?>, Supplier<? extends Codec<?, String>>>>() {
		@Override
		public Map<Class<?>, Supplier<? extends Codec<?, String>>> create() throws Exception {
			Map<Class<?>, Supplier<? extends Codec<?, String>>> bean = new HashMap<>();
			bean.put(Resource.class, Codecs.resourceRendererCodec);
			bean.put(Icon.class, Codecs.iconRendererCodec);
			bean.put(SimpleIcon.class, Codecs.iconRendererCodec);
			bean.put(AdaptiveIcon.class, Codecs.iconRendererCodec);
			return bean;
		}
	};
	
	private static Supplier<Map<Class<?>, Supplier<? extends Codec<?, String>>>> userSpecialFlowRenderers = new SingletonBeanProvider<Map<Class<?>, Supplier<? extends Codec<?, String>>>>() {
		@Override
		public Map<Class<?>, Supplier<? extends Codec<?, String>>> create() throws Exception {
			Map<Class<?>, Supplier<? extends Codec<?, String>>> bean = new HashMap<>();
			bean.put(Resource.class, Codecs.userResourceRendererCodec);
			bean.put(Icon.class, Codecs.userIconRendererCodec);
			bean.put(SimpleIcon.class, Codecs.userIconRendererCodec);
			bean.put(AdaptiveIcon.class, Codecs.userIconRendererCodec);
			return bean;
		}
	};
}
