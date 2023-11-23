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
package com.braintribe.model.processing.mpc.evaluator.impl.logic;

import com.braintribe.logging.Logger;
import com.braintribe.model.generic.path.api.IModelPathElement;
import com.braintribe.model.mpc.logic.MpcJunctionCapture;
import com.braintribe.model.processing.mpc.evaluator.api.MpcMatch;
import com.braintribe.model.processing.mpc.evaluator.api.logic.MpcJunctionCaptureResult;
import com.braintribe.model.processing.mpc.evaluator.impl.MpcMatchImpl;

/**
 * A helper class used by {@link MpcConjunctionEvaluator} and
 * {@link MpcDisjunctionEvaluator} to assist with evaluation of
 * {@link MpcJunctionCapture} condition
 * 
 */
public class MpcJunctionCaptureResultImpl implements MpcJunctionCaptureResult {

	private static Logger logger = Logger.getLogger(MpcJunctionCaptureResultImpl.class);
	private static boolean trace = logger.isTraceEnabled();

	private MpcMatchImpl matchResult;
	private int length;

	public MpcJunctionCaptureResultImpl() {
		matchResult = new MpcMatchImpl(null);
	}

	@Override
	public void setPathLength(int length) {
		this.length = length;
	}

	@Override
	public int getPathLength() {
		return length;
	}

	@Override
	public void setReturnPath(IModelPathElement path) {
		this.matchResult.setPath(path);
		this.length = path.getDepth();
	}

	@Override
	public void setReturnPath(MpcMatch match) {
		if (trace)
			logger.trace("set return path with " + match);
		if (match == null) {
			matchResult = null;
		} else {
			this.matchResult.setPath(match.getPath());
			if (trace)
				logger.trace("set length where path " + match.getPath());
			this.length = match.getPath() == null ? -1 : match.getPath().getDepth();
			if (trace)
				logger.trace("length" + this.length);
		}
	}

	@Override
	public IModelPathElement getReturnPath() {
		return (getMatchResult() != null) ? getMatchResult().getPath() : null;
	}

	@Override
	public MpcMatch getMatchResult() {
		return matchResult;
	}
}
