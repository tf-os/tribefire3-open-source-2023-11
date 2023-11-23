// ============================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2015 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================


package java.lang;

/**
 * gwt emulation for ThreadLocal which just has to store the value once because
 * there is only one thread
 * 
 * @author dirk.scheffler
 * 
 * @param <T>
 */
public class ThreadLocal<T> {
	private T value;

	/**
	 * Creates a thread local variable.
	 */
	public ThreadLocal() {
		value = initialValue();
	}

	protected T initialValue() {
		return null;
	}

	public T get() {
		return value;
	}

	public void set(T value) {
		this.value = value;
	}

	public void remove() {
		this.value = null;
	}

}
