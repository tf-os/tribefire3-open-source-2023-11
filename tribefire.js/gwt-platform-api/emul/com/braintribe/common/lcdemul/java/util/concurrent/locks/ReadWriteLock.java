package java.util.concurrent.locks;

import java.util.concurrent.locks.Lock;

/** Copied from JDK. */
public interface ReadWriteLock {

	Lock readLock();

	Lock writeLock();

}
