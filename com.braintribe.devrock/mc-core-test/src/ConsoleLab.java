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
import com.braintribe.console.ConsoleConfiguration;
import com.braintribe.console.ConsoleOutputs;
import com.braintribe.console.PrintStreamConsole;
import com.braintribe.console.output.ConfigurableConsoleOutputContainer;

public class ConsoleLab {
	public static void main(String[] args) {
		ConsoleConfiguration.install(new PrintStreamConsole(System.out, true));
		
		for (int i = 0; i < 10; i++) {
		
			ConfigurableConsoleOutputContainer configurableSequence = ConsoleOutputs.configurableSequence();
			configurableSequence.resetPosition(true);
			
			int partCount = 100;
			
			int percent = i * 100 / partCount;
			
			String progressMessage = String.format("Copied %d of %d artifact parts (%d%%) to target path  ", i, partCount, percent);
			configurableSequence.append(progressMessage);
			configurableSequence.append("\n");
			
			ConsoleOutputs.print(configurableSequence);
			
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
}
