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
package tribefire.extension.spreadsheet.exchange_processing.test;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PersonGenerator {
	final static String firstNames[] = {
			"Hans",
			"Petra",
			"Lisa",
			"Mona",
			"Tina",
			"Dirk",
			"Rosi",
			"Elli",
			"Katharina",
			"Maria",
			"Ivan",
			"Peter",
			"Jan",
			"Walter",
			"Reinhold",
			"Wolfgang",
			"Waldtraut",
			"Vincent",
			"Mattias",
			"Christina",
			"Christian",
			"Christoph",
			"Klaus",
			"Johanna",
			"Gunther",
			"Roman",
			"Johannes",
			"Anton",
			"Rudolf",
			"Rainer",
			"Emil",
			"Ewald",
			"Max",
			"Selina",
			"Susanne",
			"Thorsten",
			"Thomas",
			"Eva",
			"Sabine",
			"Georg",
			"Ester",
			"Winfried"
	};
	final static String lastNames[] = {
			"Bauer",
			"Schmidt",
			"Goldstein",
			"Schuster",
			"Mueller",
			"Silberstein",
			"Schwarz",
			"Weiss",
			"Winter",
			"Sommer",
			"Scheffler",
			"Kuefer",
			"Keller",
			"Schilling",
			"Bach",
			"Kaufmann",
			"Meister",
			"Reiter",
			"Rot",
			"Thiele",
			"Trogus",
			"Wilpernig",
			"Wanner",
			"Witzig",
			"Schneider"
	};
	
	final static String hobbies[] = {
			"Malen",
			"Lesen",
			"Musik",
			"Wissenschaft",
			"Wandern",
			"Debattieren",
			"Kochen",
			"Schreiben",
			"Tanzen",
			"Komponieren",
			"Naehen",
			"Sport",
			"Schach",
			"Philosophie",
			"Kino"
	};
	
	static Set<String> socialContractNumbers = new HashSet<>();
	
	static Random favouriteNumberRandom = new Random(42);
	static Random dateRandom = new Random(4711);
	static Random nameRandom = new Random(17);
	static Random hobbyRandom = new Random(137);
	static Random socialContractNumberRandom = new Random(23);
	
	static String nextSocialContractNumber() {
		
		while (true) {
			String socialContractNumber = String.valueOf(socialContractNumberRandom.nextInt(100000));
			if (socialContractNumbers.add(socialContractNumber))
				return socialContractNumber;
		}
	}
	
	static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy").withZone(ZoneOffset.UTC);
	
	public static void main(String[] args) {
		String delimiter = ";";
		
		try (PrintStream out = new PrintStream(new File("res/test-update.csv"), "UTF-8")) {
			out.println("socialContractNumber;firstName;lastName;birthDate;hobby;favouriteNumber");
			for (int i = 0; i < 1000; i++) {
				int favouriteNumber = favouriteNumberRandom.nextInt(10);
				String hobby = hobbies[hobbyRandom.nextInt(hobbies.length)];
				String socialContractNumber = nextSocialContractNumber();
				
				int year = dateRandom.nextInt(70) + 1950;
				int month = dateRandom.nextInt(12) + 1;
				int dayOfMonth = dateRandom.nextInt(28) + 1;
				
				Date birthDate = Date.from(LocalDate.of(year, month, dayOfMonth).atStartOfDay(ZoneOffset.UTC).toInstant());
				
				String firstName = firstNames[nameRandom.nextInt(firstNames.length)];
				String lastName = lastNames[nameRandom.nextInt(lastNames.length)];
				
				String birthDateStr = dateFormatter.format(birthDate.toInstant());
				
				String line = Stream.of(socialContractNumber,firstName, lastName, birthDateStr, hobby, String.valueOf(favouriteNumber)).map(s -> '"' + s + '"').collect(Collectors.joining(delimiter));
				out.println(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
