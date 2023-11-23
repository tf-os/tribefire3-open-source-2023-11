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
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.assertj.core.util.Arrays;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

public class CsvNullLab {
	public static void main(String[] args) {
		try {
			InputStream in = new FileInputStream("res/test.csv");
			
			CSVParser csvParser = new CSVParserBuilder().withSeparator(',').withStrictQuotes(true).withQuoteChar('"').build();
			CSVReader reader = new CSVReaderBuilder(new BufferedReader(new InputStreamReader(in, "UTF-8"))) //
					.withCSVParser(csvParser).build();
			
			String line[];
			while ((line = reader.readNext()) != null) {
				System.out.println(Arrays.asList(line));
			}
			
		} catch (Exception e) {
			throw new RuntimeException("", e);
		}

	}
}
