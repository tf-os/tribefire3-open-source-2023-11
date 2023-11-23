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
package tribefire.module.api;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.model.generic.GenericEntity;

/**
 * Represents a result of a {@link DenotationEnricher}, with two components:
 * <ul>
 * <li>{@link #result()} - indicates whether this enricher did something with the instance
 * <li>{@link #callAgain()} - indicates whether the enricher would like to be called again.
 * </ul>
 */
public interface DenotationEnrichmentResult<T extends GenericEntity> {

	/**
	 * The result of the transformation, which might be the same instance given (if source type and target type are the same).
	 * <p>
	 * IMPORTANT: The result must be <tt>null</tt> iff no change was done. This is very important, because the system needs to know whether or not
	 * something was changed, to support the {@link #callAgain()} feature.
	 */
	Maybe<T> result();

	/**
	 * Indicates whether or not the transformer that returned this result should be called again later, in case the processed entity is changed in the
	 * meantime by a different transformer.
	 */
	boolean callAgain();

	/** Optional textual description of the change done by this enricher for logging purposes. */
	String changeDescription();

	// ############################################
	// ## . . . . . . Static Builders. . . . . . ##
	// ############################################

	/**
	 * Returns a result that indicates no change was done and there is no need for this {@link DenotationTransformer transformer} to be
	 * {@link #callAgain() called again}.
	 */
	static <T extends GenericEntity> DenotationEnrichmentResult<T> nothingNowOrEver() {
		return (DenotationEnrichmentResult<T>) TransformationResultImpl.NEVER;
	}

	/**
	 * Returns a result that indicates no change was done, but the {@link DenotationTransformer transformer} should be {@link #callAgain() called
	 * again}.
	 */
	static <T extends GenericEntity> DenotationEnrichmentResult<T> nothingYetButCallMeAgain() {
		return (DenotationEnrichmentResult<T>) TransformationResultImpl.LATER;
	}

	/**
	 * Creates a result that indicates the entity was modified and that this {@link DenotationTransformer transformer} should be {@link #callAgain()
	 * called again}.
	 */
	static <T extends GenericEntity> DenotationEnrichmentResult<T> somethingAndCallMeAgain(T result, String changeDescription) {
		return new TransformationResultImpl<>(Maybe.complete(result), true, changeDescription);
	}

	/**
	 * Creates a result that indicates the entity was modified, but there is no need for this {@link DenotationTransformer transformer} to be
	 * {@link #callAgain() called again}.
	 */
	static <T extends GenericEntity> DenotationEnrichmentResult<T> allDone(T result, String changeDescription) {
		return new TransformationResultImpl<>(Maybe.complete(result), false, changeDescription);
	}

	/**
	 * Creates a result that indicates the {@link DenotationTransformer transformer} should do something but cannot, and thus the whole transformation
	 * should be aborted.
	 */
	static <T extends GenericEntity> DenotationEnrichmentResult<T> error(Reason why) {
		return new TransformationResultImpl<>(Maybe.empty(why), false, null);
	}

}

class TransformationResultImpl<T extends GenericEntity> implements DenotationEnrichmentResult<T> {

	public static final TransformationResultImpl<?> NEVER = new TransformationResultImpl<>(null, false, null);
	public static final TransformationResultImpl<?> LATER = new TransformationResultImpl<>(null, true, null);

	private final Maybe<T> result;
	private final boolean callAgainLater;
	private final String changeDescription;

	public TransformationResultImpl(Maybe<T> result, boolean callAgainLater, String changeDescription) {
		this.result = result;
		this.callAgainLater = callAgainLater;
		this.changeDescription = changeDescription;
	}

	// @formatter:off
	@Override public Maybe<T> result() { return result; }
	@Override public boolean callAgain() { return callAgainLater; }
	@Override public String changeDescription() { return changeDescription; }
	// @formatter:on

}
