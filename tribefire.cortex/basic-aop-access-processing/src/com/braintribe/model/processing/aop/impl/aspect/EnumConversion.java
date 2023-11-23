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
package com.braintribe.model.processing.aop.impl.aspect;

import com.braintribe.model.extensiondeployment.meta.AccessJoinPoint;
import com.braintribe.model.extensiondeployment.meta.Advice;

public class EnumConversion {
	/**
	 * helper function to convert modeled join point enum values to api one 
	 * @param joinPoint - the {@link AccessJoinPoint} as modeled
	 * @return - the corresponding {@link com.braintribe.model.processing.aop.api.aspect.AccessJoinPoint} 
	 */
	public static AccessJoinPoint convert( com.braintribe.model.processing.aop.api.aspect.AccessJoinPoint joinPoint) {
		if (joinPoint == null)
			return null;
		
		switch ( joinPoint) {
		case applyManipulation:
			return AccessJoinPoint.applyManipulation;
		case getMetaModel:
			return AccessJoinPoint.getMetaModel;
		case getReferences:
			return AccessJoinPoint.getReferences;
		case query:
			return AccessJoinPoint.query;			
		case queryEntities:
			return AccessJoinPoint.queryEntities;
		case queryProperties:
			return AccessJoinPoint.queryProperties;
		default:
			return null;			
		}
	}

	/**
	 * helper function to convert modeled advice enum values to api enum
	 * @param advice - the {@link Advice} as modeled 
	 * @return - the corresponding {@link com.braintribe.model.processing.aop.api.aspect.Advice}
	 */
	public static Advice convert( com.braintribe.model.processing.aop.api.aspect.Advice advice) {
		switch( advice) {
		case after:
			return Advice.after;
		case around:			
			return Advice.around;
		case before:
			return Advice.before;
		default:
			return null;
			
		}
	}
	
	/**
	 * helper function to convert modeled join point enum values to api one 
	 * @param joinPoint - the {@link AccessJoinPoint} as modeled
	 * @return - the corresponding {@link com.braintribe.model.processing.aop.api.aspect.AccessJoinPoint} 
	 */
	public static com.braintribe.model.processing.aop.api.aspect.AccessJoinPoint convert( AccessJoinPoint joinPoint) {
		switch ( joinPoint) {
		case applyManipulation:
				return com.braintribe.model.processing.aop.api.aspect.AccessJoinPoint.applyManipulation;
		case getMetaModel:
			return com.braintribe.model.processing.aop.api.aspect.AccessJoinPoint.getMetaModel;
		case getReferences:
			return com.braintribe.model.processing.aop.api.aspect.AccessJoinPoint.getReferences;
		case query:
			return com.braintribe.model.processing.aop.api.aspect.AccessJoinPoint.query;			
		case queryEntities:
			return com.braintribe.model.processing.aop.api.aspect.AccessJoinPoint.queryEntities;
		case queryProperties:
			return com.braintribe.model.processing.aop.api.aspect.AccessJoinPoint.queryProperties;
		default:
			return null;			
		}
	}

	/**
	 * helper function to convert modeled advice enum values to api enum
	 * @param advice - the {@link Advice} as modeled 
	 * @return - the corresponding {@link com.braintribe.model.processing.aop.api.aspect.Advice}
	 */
	public static com.braintribe.model.processing.aop.api.aspect.Advice convert( Advice advice) {
		switch( advice) {
		case after:
			return com.braintribe.model.processing.aop.api.aspect.Advice.after;
		case around:			
			return com.braintribe.model.processing.aop.api.aspect.Advice.around;
		case before:
			return com.braintribe.model.processing.aop.api.aspect.Advice.before;
		default:
			return null;
			
		}
	}
}
