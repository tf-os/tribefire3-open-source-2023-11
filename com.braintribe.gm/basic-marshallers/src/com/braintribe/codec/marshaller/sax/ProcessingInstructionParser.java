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
package com.braintribe.codec.marshaller.sax;

import java.util.HashMap;
import java.util.Map;

public class ProcessingInstructionParser {

    /**
     * This will parse and load the instructions for the PI.
     * This is separated to allow it to occur once and then be reused.
     */
    public static Map<String, String> parseData(String rawData) {
        // The parsing here is done largely "by hand" which means the code
        // gets a little tricky/messy.  The following conditions should
        // now be handled correctly:
        //   <?pi href="http://hi/a=b"?>        Reads OK
        //   <?pi href = 'http://hi/a=b' ?>     Reads OK
        //   <?pi href\t = \t'http://hi/a=b'?>  Reads OK
        //   <?pi href  =  "http://hi/a=b"?>    Reads OK
        //   <?pi?>                             Empty Map
        //   <?pi id=22?>                       Empty Map
        //   <?pi id='22?>                      Empty Map

        Map<String, String> data = new HashMap<String, String>();

        // System.out.println("rawData: " + rawData);

        // The inputData variable holds the part of rawData left to parse
        String inputData = rawData.trim();

        // Iterate through the remaining inputData string
        while (!inputData.trim().equals("")) {
            //System.out.println("parseData() looking at: " + inputData);

            // Search for "name =", "name=" or "name1 name2..."
            String name = "";
            String value = "";
            int startName = 0;
            char previousChar = inputData.charAt(startName);
            int pos = 1;
            for (; pos<inputData.length(); pos++) {
                char currentChar = inputData.charAt(pos);
                if (currentChar == '=') {
                    name = inputData.substring(startName, pos).trim();
                    // Get the boundaries on the quoted string
                    // We use boundaries so we know where to start next
                    int[] bounds = extractQuotedString(
                                     inputData.substring(pos+1));
                    // A null value means a parse error and we return empty!
                    if (bounds == null) {
                        return new HashMap<String, String>();
                    }
                    value = inputData.substring(bounds[0]+pos+1,
                                                bounds[1]+pos+1);
                    pos += bounds[1] + 1;  // skip past value
                    break;
                }
                else if (Character.isWhitespace(previousChar)
                          && !Character.isWhitespace(currentChar)) {
                    startName = pos;
                }

                previousChar = currentChar;
            }

            // Remove the first pos characters; they have been processed
            inputData = inputData.substring(pos);

            // System.out.println("Extracted (name, value) pair: ("
            //                          + name + ", '" + value+"')");

            // If both a name and a value have been found, then add
            // them to the data Map
            if (name.length() > 0 && value != null) {
                //if (data.containsKey(name)) {
                    // A repeat, that's a parse error, so return a null map
                    //return new HashMap();
                //}
                //else {
                    data.put(name, value);
                //}
            }
        }

        return data;
    }

    private static int[] extractQuotedString(String rawData) {
        // Remembers whether we're actually in a quoted string yet
        boolean inQuotes = false;

        // Remembers which type of quoted string we're in
        char quoteChar = '"';

        // Stores the position of the first character inside
        //  the quoted string (i.e. the start of the return string)
        int start = 0;

        // Iterate through the input string looking for the start
        // and end of the quoted string
        for (int pos=0; pos < rawData.length(); pos++) {
            char currentChar = rawData.charAt(pos);
            if (currentChar=='"' || currentChar=='\'') {
                if (!inQuotes) {
                    // We're entering a quoted string
                    quoteChar = currentChar;
                    inQuotes = true;
                    start = pos+1;
                }
                else if (quoteChar == currentChar) {
                    // We're leaving a quoted string
                    inQuotes = false;
                    return new int[] { start, pos };
                }
                // Otherwise we've encountered a quote
                // inside a quote, so just continue
            }
        }

        return null;
    }
}
