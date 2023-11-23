// ============================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2015 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================


package java.text;

import java.util.Locale;

public class Collator implements java.util.Comparator<Object>, Cloneable{
	
	public static Collator instance = new Collator();
	
	public static final int PRIMARY = 0;
	public static final int SECONDARY  = 0;
	public static final int TERTIARY  = 0;
	public static final int IDENTICAL  = 0;
	public static final int NO_DECOMPOSITION  = 0;
	public static final int CANONICAL_DECOMPOSITION = 1;
	public static final int FULL_DECOMPOSITION = 2;
	
	int strength;
	int decmp;
	
    protected Collator ()
    {
      strength = TERTIARY;
      decmp = CANONICAL_DECOMPOSITION;
    }

	public int compare(Object arg0, Object arg1) {
		return compare(arg0.toString(), arg1.toString());
	}
	
	public native int compare( String source, String target )/*-{
    	return source.localeCompare( target );
  	}-*/;
	
	public static synchronized Collator getInstance() {
	    return getInstance(Locale.getDefault());
	}
	
	public synchronized void setStrength (int strength)
	{
			if (strength != PRIMARY && strength != SECONDARY && strength != TERTIARY && strength != IDENTICAL)
				throw new IllegalArgumentException ();
			this.strength = strength;
	}
	
	public static synchronized Collator getInstance(Locale desiredLocale)
	{
		return instance;
	}

}
