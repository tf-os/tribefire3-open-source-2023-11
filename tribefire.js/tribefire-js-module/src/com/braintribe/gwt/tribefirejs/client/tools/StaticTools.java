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
package com.braintribe.gwt.tribefirejs.client.tools;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.braintribe.gwt.browserfeatures.client.Console;
import com.braintribe.gwt.genericmodel.client.codec.jse.JseCodec;
import com.braintribe.gwt.gmrpc.api.client.user.ResourceSupport;
import com.braintribe.gwt.gmrpc.web.client.GwtGmWebRpcEvaluator;
import com.braintribe.gwt.gmrpc.web.client.StandardDecodingContext;
import com.braintribe.gwt.tribefirejs.client.TfJsNameSpaces;
import com.braintribe.gwt.tribefirejs.client.error.TfJsError;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.GmCoreApiInteropNamespaces;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.proxy.ProxyContext;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.session.GmSession;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.core.expert.api.MutableDenotationMap;
import com.braintribe.model.processing.core.expert.impl.PolymorphicDenotationMap;
import com.braintribe.model.processing.meta.cmd.CmdResolverImpl;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.meta.oracle.BasicModelOracle;
import com.braintribe.model.processing.query.parser.QueryParser;
import com.braintribe.model.processing.query.parser.api.GmqlParsingError;
import com.braintribe.model.processing.query.parser.api.ParsedQuery;
import com.braintribe.model.query.Query;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.processing.async.api.AsyncCallback;
import com.braintribe.processing.async.api.JsPromise;
import com.braintribe.utils.promise.JsPromiseCallback;
import com.google.gwt.core.client.JsDate;
import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.i18n.shared.DateTimeFormat.PredefinedFormat;
import com.google.gwt.xhr.client.XMLHttpRequest;

import jsinterop.annotations.JsMethod;

@SuppressWarnings("unusable-by-js")
public class StaticTools {
	
	//DATE AND TIME
	
	@JsMethod(namespace = TfJsNameSpaces.DATE_TOOLS)
	public static Date now(){
		return new Date();
	}
	
	@JsMethod(namespace = TfJsNameSpaces.DATE_TOOLS)
	public static Date fromJsDate(JsDate jsDate){
		return toDateTime(jsDate.getFullYear(), jsDate.getMonth()+1, jsDate.getDate(), jsDate.getHours(), jsDate.getMinutes(), jsDate.getSeconds());
	}
	
	@JsMethod(name = "parseDate", namespace = TfJsNameSpaces.DATE_TOOLS)
	public static Date toDate(String dateString, String dtf){
		DateTimeFormat dateTimeFormat = getDefaultDateTimeFormat();
		if(dtf != null)
			dateTimeFormat = DateTimeFormat.getFormat(dtf);
		return dateTimeFormat.parse(dateString);
		
	}
	
	@JsMethod(namespace = TfJsNameSpaces.DATE_TOOLS)
	public static String printDate(Date date, String dtf){
		DateTimeFormat dateTimeFormat = getDefaultDateTimeFormat();
		if(dtf != null)
			dateTimeFormat = DateTimeFormat.getFormat(dtf);
		return dateTimeFormat.format(date);
	}
	
	@JsMethod(name = "printJsDate", namespace = TfJsNameSpaces.DATE_TOOLS)
	public static String printDate(JsDate date, String dtf){
		DateTimeFormat dateTimeFormat = getDefaultDateTimeFormat();
		if(dtf != null)
			dateTimeFormat = DateTimeFormat.getFormat(dtf);
		return dateTimeFormat.format(fromJsDate(date));
	}
		
	@JsMethod(name = "date", namespace = TfJsNameSpaces.DATE_TOOLS)
	public static Date toDate(int year, int month, int dayOfMonth){
		DateTimeFormat dtf = DateTimeFormat.getFormat("yyyy-MM-dd");
		return dtf.parse(year + "-" + month + "-" + dayOfMonth);
	}	
	
	@JsMethod(name = "time", namespace = TfJsNameSpaces.DATE_TOOLS)
	public static Date toTime(int hours, int minutes, int seconds){
		DateTimeFormat dtf = DateTimeFormat.getFormat("HH:mm:ss");
		return dtf.parse(hours + ":" + minutes + ":" + seconds);
	}
	
	@JsMethod(name = "datetime", namespace = TfJsNameSpaces.DATE_TOOLS)
	public static Date toDateTime(int year, int month, int dayOfMonth, int hours, int minutes, int seconds){
		DateTimeFormat dtf = DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ss");
		return dtf.parse(year + "-" + month + "-" + dayOfMonth + " " + hours + ":" + minutes + ":" + seconds);
	}

	private static DateTimeFormat getDefaultDateTimeFormat() {
		return DateTimeFormat.getFormat(PredefinedFormat.ISO_8601);
	}
	
	//I18N
	
	@JsMethod(namespace = TfJsNameSpaces.I18N_TOOLS)
	public static Object defaultLocale(Object...params) {
		if(params.length == 2) {
			LocalizedString ls = (LocalizedString)params[0];
			String value = (String)params[1];
			
			return putDefaultLocale(ls, value);
		}else if(params.length == 1){
			LocalizedString ls = (LocalizedString)params[0];
			
			return getDefaultLocale(ls);
		}
		return null;
	}
	
	@JsMethod(namespace = TfJsNameSpaces.I18N_TOOLS)
	public static LocalizedString putDefaultLocale(LocalizedString ls, String value){
		return ls.putDefault(value);
	}
	
	@JsMethod(namespace = TfJsNameSpaces.I18N_TOOLS)
	public static String getDefaultLocale(LocalizedString ls){
		return ls.value();
	}	
	
	@JsMethod(namespace = TfJsNameSpaces.I18N_TOOLS)
	public static Object locale(Object...params) {
		if(params.length == 3) {
			LocalizedString ls = (LocalizedString)params[0];
			String locale = (String)params[1];
			String value = (String)params[2];
			
			return putLocale(ls, locale, value);
		}else if(params.length == 2){
			LocalizedString ls = (LocalizedString)params[0];
			String locale = (String)params[1];
			
			return getLocale(ls, locale);
		}else if(params.length == 1){
			GmSession session = (GmSession)params[0];
			return session.create(LocalizedString.T);
		}else
			return LocalizedString.T.create();
		//return null;
	}
	
	private static LocalizedString putLocale(LocalizedString ls, String locale, String value){
		return ls.put(locale, value);
	}
	
	private static String getLocale(LocalizedString ls, String locale){
		return ls.value(locale);
	}	
	
	//QUERY TOOLS
	
	@JsMethod(name = "parse", namespace = TfJsNameSpaces.QUERY_TOOLS)
	public static Query parseQuery(String queryString) throws IllegalArgumentException{
		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		if(parsedQuery.getIsValidQuery())
			return parsedQuery.getQuery();
		else
		{
			StringBuilder sb = new StringBuilder();
			for(GmqlParsingError error : parsedQuery.getErrorList())
				sb.append(error.getMessage());
			throw new IllegalArgumentException(sb.toString());
		}
	}
	
	//ERROR HANDLING
	
	@JsMethod(namespace = TfJsNameSpaces.ERROR_TOOLS)
	public static TfJsError createError(Throwable t){
		return new TfJsError(t);
	}
	
	// COLLECTION TOOLS

	@JsMethod(namespace = TfJsNameSpaces.TF)
	public static List<?> list(String typeSignature) {
		return GMF.getTypeReflection().getListType(typeBySig(typeSignature)).createPlain();
	}

	@JsMethod(namespace = TfJsNameSpaces.TF)
	public static Set<?> set(String typeSignature) {
		return GMF.getTypeReflection().getSetType(typeBySig(typeSignature)).createPlain();
	}

	@JsMethod(namespace = TfJsNameSpaces.TF)
	public static Map<?, ?> map(String keySignature, String valueSignature) {
		return GMF.getTypeReflection().getMapType(typeBySig(keySignature), typeBySig(valueSignature)).createPlain();
	}

	private static GenericModelType typeBySig(String signature) {
		return signature == null ? BaseType.INSTANCE : GMF.getTypeReflection().getType(signature);
	}

	@JsMethod(namespace = GmCoreApiInteropNamespaces.util)
	public static <K extends GenericEntity, V> MutableDenotationMap<K, V> newDenotationMap() {
		return new PolymorphicDenotationMap<>();
	}

	//DDSA
	
	@JsMethod(namespace = TfJsNameSpaces.REMOTE)
	public static JsPromise<Object> evaluate(String servicesUrl, ServiceRequest sr){
		JsPromiseCallback<Object> callback = new JsPromiseCallback<Object>();
		
		GwtGmWebRpcEvaluator evaluator = new GwtGmWebRpcEvaluator();
		evaluator.setServerUrl(servicesUrl + "/rpc");
		
		sr.eval(evaluator).with(ResourceSupport.class, true).get(callback);
		
		return callback.asPromise();
	}
	
	@JsMethod(namespace = GmCoreApiInteropNamespaces.util)
	public static <T> List<T> streamAsList(Stream<T> stream) {
		return stream.collect(Collectors.toList());		
	}
	
	@JsMethod(namespace = GmCoreApiInteropNamespaces.util)
	public static <T> Set<T> streamAsSet(Stream<T> stream) {
		return stream.collect(Collectors.toSet());		
	}
	
	@JsMethod(namespace = GmCoreApiInteropNamespaces.util)
	public static <T> Object[] streamAsArray(Stream<T> stream) {
		return stream.toArray();
	}
	
	@JsMethod(namespace = GmCoreApiInteropNamespaces.util)
	public static <T> Predicate<T> predicate(Object test) {
		return new JsPredicate<T>(test);
	}
	
	@JsMethod(namespace = GmCoreApiInteropNamespaces.util)
	public static <T> Consumer<T> consumer(Object accept) {
		return new JsConsumer<T>(accept);
	}

	@JsMethod(namespace = GmCoreApiInteropNamespaces.util)
	public static String newUuid() {
		return GMF.platform().newUuid();
	}

	@JsMethod(namespace = GmCoreApiInteropNamespaces.util)
	public static JsPromise<GmMetaModel> loadModelFile(String url) {
		JsPromiseCallback<GmMetaModel> callback = new JsPromiseCallback<GmMetaModel>();
		XMLHttpRequest request = XMLHttpRequest.create();
		request.open("get", url);
		request.setRequestHeader("Accept", "gm/jse");		
		request.setOnReadyStateChange(xhr -> {
			if (xhr.getStatus() == 404) {
				Console.error("No model resource found for url '" + url + "'");
				//callback.onFailure(new NotFoundException("No model resource found for url '" + url + "'"));
				callback.onSuccess(null);
			}
			if (xhr.getReadyState() == XMLHttpRequest.DONE && xhr.getStatus() == 200) {		
				try {
					String resp = xhr.getResponseText();
					StandardDecodingContext context = new StandardDecodingContext(null);
					context.setProxyContext(new ProxyContext());
					context.setLenientDecode(true);
					GmMetaModel model = new JseCodec().decode(resp, context);	
					GMF.getTypeReflection().deploy(model, new AsyncCallback<Void>() {
						@Override
						public void onSuccess(Void future) {
							callback.onSuccess(model);
						}	
						@Override
						public void onFailure(Throwable t) {
							callback.onFailure(t);							
						}
					});					
				} catch (Exception e) {
					callback.onFailure(e);
				}
			}
		});		
		request.send();
		return callback.asPromise();
	}
	
	// MATH TOOLS
	
	@JsMethod(namespace = TfJsNameSpaces.MATH_TOOLS)
	public static BigDecimal bigDecimal(double val) {
		return new BigDecimal(val);
	}
	
	@JsMethod(name="bigDecimalFromString", namespace = TfJsNameSpaces.MATH_TOOLS)
	public static BigDecimal bigDecimal(String val) {
		return new BigDecimal(val);
	}

	@JsMethod(namespace = GmCoreApiInteropNamespaces.reflection)
	public static GenericModelTypeReflection typeReflection() {
		return GMF.getTypeReflection();
	}

	@JsMethod(namespace = TfJsNameSpaces.I18N_TOOLS)
	public static String getLocale() {
		return GMF.getLocale();
	}
	
	// META
	
	@JsMethod(namespace = TfJsNameSpaces.META_TOOLS)
	public static ModelMdResolver modelResolver(GmMetaModel model) {
		BasicModelOracle modelOracle = new BasicModelOracle(model);
		return new CmdResolverImpl(modelOracle).getMetaData();
	}

}
