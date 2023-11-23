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
package com.braintribe.devrock.zarathud.wirings.forensic.space;

import java.util.Map;

import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.devrock.zarathud.wirings.forensic.contract.ZedForensicsContract;
import com.braintribe.devrock.zed.api.context.ZedForensicsContext;
import com.braintribe.devrock.zed.api.forensics.ClasspathForensics;
import com.braintribe.devrock.zed.api.forensics.DependencyForensics;
import com.braintribe.devrock.zed.api.forensics.ModelForensics;
import com.braintribe.devrock.zed.forensics.BasicClasspathForensics;
import com.braintribe.devrock.zed.forensics.BasicDependencyForensics;
import com.braintribe.devrock.zed.forensics.BasicModelForensics;
import com.braintribe.devrock.zed.forensics.fingerprint.register.FingerPrintRegistry;
import com.braintribe.devrock.zed.forensics.fingerprint.register.RatingRegistry;
import com.braintribe.model.resource.Resource;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.annotation.Scope;
import com.braintribe.zarathud.model.forensics.FingerPrint;
import com.braintribe.zarathud.model.forensics.FingerPrintOrigin;
import com.braintribe.zarathud.model.forensics.ForensicsRating;

@Managed
public class ZedForensicsSpace implements ZedForensicsContract {

	@Override
	@Managed(Scope.prototype)
	public DependencyForensics dependencyForensics(ZedForensicsContext context) {
		BasicDependencyForensics bean = new BasicDependencyForensics(context);
		return bean;
	}

	@Override
	@Managed(Scope.prototype)
	public ClasspathForensics classpathForensics(ZedForensicsContext context) {
		BasicClasspathForensics bean = new BasicClasspathForensics(context);
		return bean;
	}

	@Override
	@Managed(Scope.prototype)
	public ModelForensics modelForensics(ZedForensicsContext context) {
		BasicModelForensics bean = new BasicModelForensics(context);
		return bean;
	}

	@Override
	@Managed(Scope.prototype)
	public FingerPrintRegistry fingerPrintRegistry() {	
		return new FingerPrintRegistry();
	}
	
	@Managed
	private Marshaller marshaller() {
		YamlMarshaller bean = new YamlMarshaller();		
		return bean;
	}

	@SuppressWarnings("unchecked")
	@Override
	@Managed(Scope.prototype)
	public RatingRegistry ratingRegistry(Map<FingerPrint, ForensicsRating> codeRatings, Resource customRatings, Resource pullRequestRatings) {
		RatingRegistry bean = new RatingRegistry();
		
		bean.addRatingOverrides(codeRatings, FingerPrintOrigin.ANNOTATED);
		
		if (customRatings != null) {
			try {
				Map<FingerPrint, ForensicsRating> ovrs = (Map<FingerPrint, ForensicsRating>) marshaller().unmarshall( customRatings.openStream());
				bean.addRatingOverrides(ovrs, FingerPrintOrigin.CUSTOM);
			} catch (Exception e) {
				System.err.println("cannot load external custom fingerprint ratings");
				e.printStackTrace();
			} 
		}
		if (pullRequestRatings != null) {
			Map<FingerPrint, ForensicsRating> ovrs;
			try {
				ovrs = (Map<FingerPrint, ForensicsRating>) marshaller().unmarshall( pullRequestRatings.openStream());
				bean.addRatingOverrides(ovrs, FingerPrintOrigin.ARTIFACT);
			} catch (Exception e) {
				System.err.println("cannot load external PR fingerprint ratings");
				e.printStackTrace();
			}
		}
				
		return bean;
	}

	
	
}
