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
package com.braintribe.crypto.mac;

import java.net.InetAddress;
import java.net.NetworkInterface;

import com.braintribe.crypto.CryptoServiceException;
import com.braintribe.crypto.utils.TextUtils;
 
/**
 * gives access to the MAC address of the network adapter 
 * bound the the local host ip.
 * 
 * @author pit
 *
 */
public class MacAddressExtractor {
	
	/*
     * Get NetworkInterface for the current host and then read the 
     * hardware address.
     */
	  public static byte [] getMacAddress()  throws CryptoServiceException{
		  try {
			InetAddress address = InetAddress.getLocalHost();		            
			  NetworkInterface ni = NetworkInterface.getByInetAddress(address);
			  byte[] mac = ni.getHardwareAddress();
			  return mac;
		} catch (Exception e) {
			 throw new CryptoServiceException("cannot retrieve MAC address as " + e, e);		
		}
 
	  }

	  public static String getMacAddressAsString() throws CryptoServiceException{
	        try {  	         
	        	byte [] mac = getMacAddress();
	            /*
	             * Extract each array of mac address and convert it to hex with the 
	             * following format like 08-00-27-DC-4A-9E.
	             */
	            StringBuilder builder = new StringBuilder();
	            for (int i = 0; i < mac.length; i++) {
	             builder.append( String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));                
	            }
	            return builder.toString();
	        } catch (Exception e) {
	            throw new CryptoServiceException("cannot retrieve MAC address as " + e, e); 
	        }
	    }
    
    public static void main(String [] args) {
    	try {
			byte [] mac = getMacAddress();
			System.out.println( TextUtils.convertToHex( mac));
			System.out.println( TextUtils.convertToHex2( mac));
		} catch (CryptoServiceException e) {		
			e.printStackTrace();
		}
    }
}
