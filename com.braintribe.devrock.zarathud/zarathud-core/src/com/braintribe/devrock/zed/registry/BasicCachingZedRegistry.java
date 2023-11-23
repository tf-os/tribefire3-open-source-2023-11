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
package com.braintribe.devrock.zed.registry;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.braintribe.cc.lcd.EqProxy;
import com.braintribe.devrock.zed.analyze.dependency.ArtifactMatcher;
import com.braintribe.devrock.zed.api.context.ZedAnalyzerContext;
import com.braintribe.devrock.zed.api.core.CachingZedRegistry;
import com.braintribe.devrock.zed.commons.Commons;
import com.braintribe.devrock.zed.commons.ConversionHelper;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.processing.async.impl.HubPromise;
import com.braintribe.provider.Holder;
import com.braintribe.zarathud.model.data.Artifact;
import com.braintribe.zarathud.model.data.ZedEntity;

public class BasicCachingZedRegistry implements CachingZedRegistry{
	private ZedHashComparator zedHashComparator = new ZedHashComparator();
	private Map<EqProxy<String>, HubPromise<ZedEntity>> cache = new ConcurrentHashMap<>();
	private int index;
	private List<Reason> collectedAnalysisErrorReasons = new ArrayList<>();

	private boolean primed;
	
	private void output( ZedAnalyzerContext context, String msg) {
		switch (context.verbosity()) {
		case all:		
		case errors:		
		case standard:
		case warnings:
			System.out.println( msg);
			break;
		default:
			break;
		
		}
	}
	
	
	
	@Override
	public List<Reason> collectedAsmAnalysisErrorReasons() {	
		return collectedAnalysisErrorReasons;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends ZedEntity> T acquire(ZedAnalyzerContext context, String desc, EntityType<T> entityType) {		
		prime( context);
		
		if (desc.contains( ".")) {
			desc = desc.replace( ".", "/");
		}
			
		Holder<Boolean> freshPromiseHolder = new Holder<>( false);
		String descToUseAsKey = desc;
		//
		// parameter entities are not stored under their desc, as there are unlimited numbers of combinations..
		// yes, I know: references to the same type are NOT detected, and will always lead to fresh zeds
		//		
		if (!desc.startsWith( "[") && !desc.startsWith("L") && !simpleTypeDescs.containsKey(desc)) {	
			descToUseAsKey = desc + "@" + index++;
		} else {
			if (descToUseAsKey.startsWith( "L") && !descToUseAsKey.endsWith( ";")) {
				descToUseAsKey = descToUseAsKey +";";
			}
		}
		
		
		// grab an entry 
		HubPromise<ZedEntity> hp = cache.computeIfAbsent( zedHashComparator.eqProxy(descToUseAsKey), k -> {
			freshPromiseHolder.accept(true);
			return new HubPromise<>();		
		});
		// if we just added it, we need to produce a value
		if (freshPromiseHolder.get()) {				
			output( context, "acquiring : " + descToUseAsKey);
			String className = ConversionHelper.toClassName(desc);
			Maybe<ZedEntity> entMaybe = context.resolver().acquireClassResource(context, className);
			// if none's found,
			ZedEntity ent = null;
			if (entMaybe.isSatisfied()) {
				ent = entMaybe.get();
			}
			else {
				// add the failure reason to the registry 
				context.registry().collectedAsmAnalysisErrorReasons().add( entMaybe.whyUnsatisfied());
			}
			if (ent == null) {
				ent = Commons.create(context, entityType);
				ent.setName(className);
				ent.setDesc(descToUseAsKey);
				ent.setScannedFlag(true);
				// identify real artifact, via the resource of the context.. 
				URL currentlyScannedResource = context.currentlyScannedResource();
				if (currentlyScannedResource != null) {
					String stringRepresentationOfCurrentlyScannedResource = currentlyScannedResource.toString();
					if (stringRepresentationOfCurrentlyScannedResource.startsWith("jrt:")) {
						ent.getArtifacts().add(context.artifacts().runtimeArtifact(context));
					}
					else {
						// extract the URL part that points to the jar, and then get the artifact from the map
						Artifact artifact = Commons.getOwningArtifact(context.urlToArtifactMap(), currentlyScannedResource); 
						if (artifact != null) {
							ent.getArtifacts().add( artifact);
						}
						else {
							ent.getArtifacts().add( context.artifacts().artifact(context, className));
						}
					}
				} else { // if not specified, will probably get a unknown/java-rt
					ent.getArtifacts().add( context.artifacts().artifact(context, className));
				}
				
				if (ArtifactMatcher.matchArtifactTo(context.artifacts().runtimeArtifact(context),ent.getArtifacts())) {
					ent.setIsRuntime(true);
				}
				else {
					ent.setIsUnknown(true);
				}				
			}
			else {
				ent.setDesc(desc);
				ent.setName(className);		
				if (ArtifactMatcher.matchArtifactTo(context.artifacts().runtimeArtifact(context),ent.getArtifacts())) {
					ent.setIsRuntime(true);
					ent.setScannedFlag( true);
				}
				if (descToUseAsKey.contains( "@")) {
					ent.setScannedFlag( true);
				}
			}
			hp.accept( ent);			
		}
		else {
			//output( context, "reaccessing : " + descToUseAsKey);
		}
		return (T) hp.get();
	}
	
	@Override
	public Collection<ZedEntity> population() {
		synchronized (cache) {
			return cache.values().stream().map( v -> v.get()).collect(Collectors.toList());
		}
	}
	
	/**
	 * inject a type into the cache - simple types mainly 
	 * @param context - the {@link ZedAnalyzerContext} to use
	 * @param desc - the desc of the type 
	 * @param name - the name of the type
	 */
	private void inject( ZedAnalyzerContext context, String desc, String name) {
		ZedEntity ent = Commons.create(context, ZedEntity.T);
		ent.setDesc(desc);
		ent.setName(name);
		ent.setScannedFlag(true);
		ent.getArtifacts().add( context.artifacts().runtimeArtifact(context));
		ent.setIsRuntime(true);
		HubPromise<ZedEntity> hb = new HubPromise<>(ent);
		cache.put( zedHashComparator.eqProxy(desc),  hb);		
	}

	private Object primingMonitor = new Object();
	
	/**
	 * prime the registry 
	 * @param context - the {@link ZedAnalyzerContext} to sue 
	 */
	private void prime(ZedAnalyzerContext context) {
		if (primed) 
			return;
		
		synchronized (primingMonitor) {
			if (primed) 
				return;
					
			for (Entry<String,String> entry : simpleTypeDescs().entrySet()) {
				inject( context, entry.getKey(), entry.getValue());
			}
			
			primed = true;
		}
	}
	

	
	@Override
	public Map<String, String> simpleTypeDescs() {		 
		return simpleTypeDescs;
	}

	@Override
	public boolean isParameterCode(String value) {
		int index = value.indexOf( '@');
		String test;
		if (index < 0) 
			test = value;
		else
			test = value.substring(0, index);
						
		if (test.length() > 1)
			return false;
		return !simpleTypeDescs().containsKey( test);
				 					
	}

	
	
	
}
