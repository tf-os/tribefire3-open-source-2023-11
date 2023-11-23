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
package com.braintribe.cc.lcd;

import com.braintribe.codec.Codec;

/**
 * an abstract codec that automatically wraps the entity passed for the {@link CodingMap} and {@link CodingSet} respectively <br/>
 * <br>
 *
 * set the flag in the {@link #HashSupportWrapperCodec(boolean)} to true if you have immutable entities, so that the hashCode is only generated once
 * and only returned upon access. False or not overridden gets the hashcode generated when accessed. <br/>
 * <br/>
 * {@link #hashCode()} - override this to implement identity behavior<br/>
 * {@link #equals(Object)} - override to implement equality behavior<br>
 *
 * @param <E>
 *            - the entity to be wrapped
 *
 * @author pit
 */
public abstract class HashSupportWrapperCodec<E> implements Codec<E, HashSupportWrapperCodec<E>.HashSupportWrapper> {

	protected boolean entitiesAreImmutable;

	protected HashSupportWrapperCodec(final boolean entitiesAreImmutable) {
		this.entitiesAreImmutable = entitiesAreImmutable;
	}

	protected HashSupportWrapperCodec() {

	}

	/**
	 * an internal class the wraps the passed entity {@link #hashCode} and {@link #equals} are delegated back to the codec.
	 *
	 * @author pit
	 *
	 */
	public class HashSupportWrapper {
		private final E wrappedEntity;

		public HashSupportWrapper(final E wrappedEntity) {
			super();
			this.wrappedEntity = wrappedEntity;
		}

		public E getWrappedEntity() {
			return this.wrappedEntity;
		}

		/* (non-Javadoc)
		 *
		 * @see java.lang.Object#hashCode() */
		@Override
		public int hashCode() {
			return entityHashCode(this.wrappedEntity);
		}

		/* (non-Javadoc)
		 *
		 * @see java.lang.Object#equals(java.lang.Object) */
		@SuppressWarnings("unchecked")
		@Override
		public boolean equals(final Object obj) {
			return entityEquals(this.wrappedEntity, ((HashSupportWrapper) obj).wrappedEntity);
		}

	}

	public class ImmutableHashSupportWrapper extends HashSupportWrapper {
		private final int hashCode;

		public ImmutableHashSupportWrapper(final E wrappedEntity) {
			super(wrappedEntity);
			this.hashCode = entityHashCode(wrappedEntity);
		}

		/* (non-Javadoc)
		 *
		 * @see java.lang.Object#hashCode() */
		@Override
		public int hashCode() {
			return this.hashCode;
		}
	}

	/* (non-Javadoc)
	 *
	 * @see com.braintribe.codec.Codec#encode(java.lang.Object) */
	@Override
	public HashSupportWrapper encode(final E value) {
		if (value == null) {
			return null;
		}

		final HashSupportWrapper wrapper = this.entitiesAreImmutable ? new ImmutableHashSupportWrapper(value) : new HashSupportWrapper(value);
		return wrapper;
	}

	/* (non-Javadoc)
	 *
	 * @see com.braintribe.codec.Codec#decode(java.lang.Object) */
	@Override
	public E decode(final HashSupportWrapper encodedValue) {
		if (encodedValue == null) {
			return null;
		}
		return encodedValue.getWrappedEntity();
	}

	/* (non-Javadoc)
	 *
	 * @see com.braintribe.codec.Codec#getValueClass() */
	@Override
	public Class<E> getValueClass() {
		throw new UnsupportedOperationException("not supported in this codec");
	}

	/**
	 * overload this function to implement the behavior for the hashCode call will be called from the wrapper
	 *
	 * @param e
	 *            - the wrapped entity
	 * @return - the hashcode generated
	 */
	protected abstract int entityHashCode(E e);

	/**
	 * override this function to implement the behavior for the entityEquals call will be called from the wrapper
	 *
	 * @param e1
	 *            - the first wrapped entity
	 * @param e2
	 *            - the second wrapped entity
	 *
	 * @return - true if they're deemed equal, false otherwise
	 */
	protected abstract boolean entityEquals(E e1, E e2);

}
