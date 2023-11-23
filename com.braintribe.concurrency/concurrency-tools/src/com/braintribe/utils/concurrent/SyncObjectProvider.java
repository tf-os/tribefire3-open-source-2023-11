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
package com.braintribe.utils.concurrent;

/**
 * Provider for object that should be used as lock for synchronization. The java {@code synchronized()} block is 100%
 * exclusive, which in some cases is not enough. We may want some degree of concurrency, which is what classes
 * implementing this interface provide. The method {@link #acquireSyncObject(Object)} decides, for which objects the
 * block may run concurrently. For example one may need a restriction, that only objects which are equal need to be
 * synchronized, but non-equal objects may be processed concurrently.
 * <p>
 * The method {@link #releaseSyncObject(Object)} serves only for memory cleanup purposes.
 * <p>
 * 
 * The recommended usage of this provider is something like this:
 * <p>
 * <code>
 * try {
 * 	 	synchronized(syncObjectProvider.acquireSyncObject(object)) {
 * 			// some code
 * 	 	}
 * } finally {
 * 		syncObjectProvider.releaseSyncObject(object);
 * }
 * </code>
 * 
 * @see ConcurrentSyncObjectProvider
 * @see ExclusiveSyncObjectProvider
 */
public interface SyncObjectProvider<T> {

	/**
	 * This method controls for which object the synchronized code may be running concurrently. As long, as a the code
	 * may be run at once for a given set of objects, this method should always return a different object (which serves
	 * as a lock), which means that synchronizing on the result of this method doesn't make a {@link Thread} wait for
	 * another thread. If, however, processing some two objects A and B concurrently should not be possible, then this
	 * method should return the same object twice for arguments A and B (assuming there was no relevant invocation of
	 * {@link #releaseSyncObject(Object)} in the meantime).
	 */
	Object acquireSyncObject(T arg);

	/**
	 * Notify the provider, that the synchronization part for some object is over, so it may do some internal cleanup.
	 */
	void releaseSyncObject(T arg);

}
