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
package com.braintribe.model.processing.securityservice.basic.test.runnable;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.codec.CodecException;
import com.braintribe.model.processing.time.TimeSpanCodec;
import com.braintribe.model.time.TimeSpan;
import com.braintribe.model.time.TimeUnit;

public class TimeProcessingTimeSpanCodecTest {

	private TimeSpanCodec coded = new TimeSpanCodec();

	@Test
	public void testFromMicroSeconds() { 
		assertTimeConversion(TimeUnit.microSecond, 1800000000.0      ,  TimeUnit.hour       ,     0.5   );
		assertTimeConversion(TimeUnit.microSecond,  105000000.0      ,  TimeUnit.minute     ,     1.75  );
		assertTimeConversion(TimeUnit.microSecond,   90000000.0      ,  TimeUnit.minute     ,     1.5   );
		assertTimeConversion(TimeUnit.microSecond,   60000000.0      ,  TimeUnit.minute     ,     1.0   );
		assertTimeConversion(TimeUnit.microSecond,   20000000.0      ,  TimeUnit.second     ,    20.0   );
		assertTimeConversion(TimeUnit.microSecond,   20000000.0      ,  TimeUnit.milliSecond, 20000.0   );
		assertTimeConversion(TimeUnit.microSecond,       2000.0      ,  TimeUnit.milliSecond,     2.0   );
		assertTimeConversion(TimeUnit.microSecond,       1900.0      ,  TimeUnit.milliSecond,     1.9   );
		assertTimeConversion(TimeUnit.microSecond,       1000.0      ,  TimeUnit.milliSecond,     1.0   );
		assertTimeConversion(TimeUnit.microSecond,        999.0      ,  TimeUnit.milliSecond,     0.999 );
		assertTimeConversion(TimeUnit.microSecond,         99.0      ,  TimeUnit.milliSecond,     0.099 );
		assertTimeConversion(TimeUnit.microSecond,          1.0      ,  TimeUnit.milliSecond,     0.001 );
		assertTimeConversion(TimeUnit.microSecond,          0.0      ,  TimeUnit.milliSecond,     0.0   );
		assertTimeConversion(TimeUnit.microSecond,          0.999999 ,  TimeUnit.milliSecond,     0.000999999 );
	}
	
	@Test
	public void testFromMilliSeconds() { 
		assertTimeConversion(TimeUnit.milliSecond,     2750.0      ,  TimeUnit.second     ,        2.75  );
		assertTimeConversion(TimeUnit.milliSecond,      500.0      ,  TimeUnit.second     ,        0.5   );
		assertTimeConversion(TimeUnit.milliSecond,        1.0      ,  TimeUnit.second     ,        0.001 );
		assertTimeConversion(TimeUnit.milliSecond, 20000000.9      ,  TimeUnit.milliSecond, 20000000.9   );
		assertTimeConversion(TimeUnit.milliSecond,     2000.9      ,  TimeUnit.milliSecond,     2000.9   );
		assertTimeConversion(TimeUnit.milliSecond,     1999.9      ,  TimeUnit.milliSecond,     1999.9   );
		assertTimeConversion(TimeUnit.milliSecond,     1000.9      ,  TimeUnit.milliSecond,     1000.9   );
		assertTimeConversion(TimeUnit.milliSecond,      999.9      ,  TimeUnit.milliSecond,      999.9   );
		assertTimeConversion(TimeUnit.milliSecond,       99.9      ,  TimeUnit.milliSecond,       99.9   );
		assertTimeConversion(TimeUnit.milliSecond,        1.0      ,  TimeUnit.milliSecond,        1.0   );
		assertTimeConversion(TimeUnit.milliSecond,        0.0      ,  TimeUnit.milliSecond,        0.0   );
	}
	
	@Test
	public void testFromSeconds() { 
		assertTimeConversion(TimeUnit.second, 3600.0        , TimeUnit.hour        ,       1.0  );
		assertTimeConversion(TimeUnit.second, 3600.0        , TimeUnit.minute      ,      60.0  );
		assertTimeConversion(TimeUnit.second,   60.0        , TimeUnit.minute      ,       1.0  );
		assertTimeConversion(TimeUnit.second,   15.0        , TimeUnit.minute      ,       0.25 );
		assertTimeConversion(TimeUnit.second,    3.0        , TimeUnit.minute      ,       0.05 );
		assertTimeConversion(TimeUnit.second,   10.5        , TimeUnit.milliSecond ,   10500.0  );
		assertTimeConversion(TimeUnit.second,    2.75005    , TimeUnit.milliSecond ,    2750.05 );
		assertTimeConversion(TimeUnit.second,    1.5        , TimeUnit.milliSecond ,    1500.0  );
		assertTimeConversion(TimeUnit.second,    1.0        , TimeUnit.milliSecond ,    1000.0  );
		assertTimeConversion(TimeUnit.second,    0.5        , TimeUnit.milliSecond ,     500.0  );
		assertTimeConversion(TimeUnit.second,    0.1        , TimeUnit.milliSecond ,     100.0  );
		assertTimeConversion(TimeUnit.second,    0.01       , TimeUnit.milliSecond ,      10.0  );
		assertTimeConversion(TimeUnit.second,    0.001      , TimeUnit.milliSecond ,       1.0  );
		assertTimeConversion(TimeUnit.second,    0.0        , TimeUnit.milliSecond ,       0.0  );
		assertTimeConversion(TimeUnit.second,    0.1        , TimeUnit.microSecond ,  100000.0  );
		assertTimeConversion(TimeUnit.second,    0.001      , TimeUnit.microSecond ,    1000.0  );
		assertTimeConversion(TimeUnit.second,    0.001      , TimeUnit.nanoSecond  , 1000000.0  );
	}
	
	@Test
	public void testFromMinutes() { 
		assertTimeConversion(TimeUnit.minute , 1440.0      , TimeUnit.hour        ,    24.0      );
		assertTimeConversion(TimeUnit.minute ,  144.0      , TimeUnit.day         ,     0.1      );
		assertTimeConversion(TimeUnit.minute ,   14.4      , TimeUnit.day         ,     0.01     );
		assertTimeConversion(TimeUnit.minute ,    0.000144 , TimeUnit.day         ,     0.0000001);
		assertTimeConversion(TimeUnit.minute ,    3.6      , TimeUnit.second      ,   216.0      );
		assertTimeConversion(TimeUnit.minute ,    0.5      , TimeUnit.second      ,    30.0      );
		assertTimeConversion(TimeUnit.minute ,    0.1      , TimeUnit.second      ,     6.0      );
		assertTimeConversion(TimeUnit.minute ,    1.5      , TimeUnit.milliSecond , 90000.0      );
		assertTimeConversion(TimeUnit.minute ,    1.0      , TimeUnit.milliSecond , 60000.0      );
		assertTimeConversion(TimeUnit.minute ,    0.5      , TimeUnit.milliSecond , 30000.0      );
		assertTimeConversion(TimeUnit.minute ,    0.1      , TimeUnit.milliSecond ,  6000.0      );
		assertTimeConversion(TimeUnit.minute ,    0.01     , TimeUnit.milliSecond ,   600.0      );
		assertTimeConversion(TimeUnit.minute ,    0.001    , TimeUnit.milliSecond ,    60.0      );
		assertTimeConversion(TimeUnit.minute ,    0.0001   , TimeUnit.milliSecond ,     6.0      );
		assertTimeConversion(TimeUnit.minute ,    0.0      , TimeUnit.milliSecond ,     0.0      );
		assertTimeConversion(TimeUnit.minute ,    0.00005  , TimeUnit.microSecond ,  3000.0      );
	}
	
	@Test
	public void testFromHours() { 
		assertTimeConversion(TimeUnit.hour , 8760.0             , TimeUnit.day         ,     365.0         );
		assertTimeConversion(TimeUnit.hour ,   24.0             , TimeUnit.day         ,       1.0         );
		assertTimeConversion(TimeUnit.hour ,   23.999999976     , TimeUnit.day         ,       0.999999999 );
		assertTimeConversion(TimeUnit.hour ,   12.0             , TimeUnit.day         ,       0.5         );
		assertTimeConversion(TimeUnit.hour ,   2.4              , TimeUnit.day         ,       0.1         );
		assertTimeConversion(TimeUnit.hour ,   0.24             , TimeUnit.day         ,       0.01        );
		assertTimeConversion(TimeUnit.hour ,   1.0              , TimeUnit.minute      ,      60.0         );
		assertTimeConversion(TimeUnit.hour ,   0.25             , TimeUnit.minute      ,      15.0         );
		assertTimeConversion(TimeUnit.hour ,   0.25             , TimeUnit.minute      ,      15.0         );
		assertTimeConversion(TimeUnit.hour ,   0.05             , TimeUnit.minute      ,       3.0         );
		assertTimeConversion(TimeUnit.hour ,     1.0            , TimeUnit.milliSecond , 3600000.0         );
		assertTimeConversion(TimeUnit.hour ,     0.5            , TimeUnit.milliSecond , 1800000.0         );
		assertTimeConversion(TimeUnit.hour ,     0.1            , TimeUnit.milliSecond ,  360000.0         );
		assertTimeConversion(TimeUnit.hour ,     0.01           , TimeUnit.milliSecond ,   36000.0         );
		assertTimeConversion(TimeUnit.hour ,     0.001          , TimeUnit.milliSecond ,    3600.0         );
		assertTimeConversion(TimeUnit.hour ,     0.0001         , TimeUnit.milliSecond ,     360.0         );
		assertTimeConversion(TimeUnit.hour ,     0.00001        , TimeUnit.milliSecond ,      36.0         );
		assertTimeConversion(TimeUnit.hour ,     0.000001       , TimeUnit.milliSecond ,       3.0         );
		assertTimeConversion(TimeUnit.hour ,     0.000001       , TimeUnit.microSecond ,    3000.0         );
		assertTimeConversion(TimeUnit.hour ,     0.0000005      , TimeUnit.microSecond ,    1500.0         );
		assertTimeConversion(TimeUnit.hour ,     0.0000005      , TimeUnit.microSecond ,    1000.0         );
		assertTimeConversion(TimeUnit.hour ,     0.000001       , TimeUnit.nanoSecond  , 3000000.0         );
		assertTimeConversion(TimeUnit.hour ,     0.0000005      , TimeUnit.nanoSecond  , 1500000.0         );
	}
	
	@Test
	public void testFromDays() { 
		assertTimeConversion(TimeUnit.day ,      0.999999999 , TimeUnit.day         ,                   0.999999999);
		assertTimeConversion(TimeUnit.day ,      0.000000025 , TimeUnit.day         ,                   0.000000025);
		assertTimeConversion(TimeUnit.day ,      0.999999999 , TimeUnit.hour        ,                  23.999999976);
		assertTimeConversion(TimeUnit.day , 990000.000000075 , TimeUnit.hour        ,            23760000.0000018  );
		assertTimeConversion(TimeUnit.day , 990000.0         , TimeUnit.hour        ,            23760000.0        );
		assertTimeConversion(TimeUnit.day ,   7300.0         , TimeUnit.hour        ,              175200.0        );
		assertTimeConversion(TimeUnit.day ,    365.0         , TimeUnit.hour        ,                8760.0        );
		assertTimeConversion(TimeUnit.day ,      1.0         , TimeUnit.hour        ,                  24.0        );
		assertTimeConversion(TimeUnit.day ,      0.5         , TimeUnit.hour        ,                  12.0        );
		assertTimeConversion(TimeUnit.day ,      0.1         , TimeUnit.hour        ,                   2.4        );
		assertTimeConversion(TimeUnit.day ,      0.25        , TimeUnit.hour        ,                   6.0        );
		assertTimeConversion(TimeUnit.day ,      0.125       , TimeUnit.minute      ,                 180.0        );
		assertTimeConversion(TimeUnit.day ,      0.1         , TimeUnit.second      ,                8640.0        );
		assertTimeConversion(TimeUnit.day ,      0.05        , TimeUnit.minute      ,                  72.0        );
		assertTimeConversion(TimeUnit.day ,      0.01        , TimeUnit.minute      ,                  14.4        );
		assertTimeConversion(TimeUnit.day ,      0.01        , TimeUnit.second      ,                 864.0        );
		assertTimeConversion(TimeUnit.day ,   1000.0         , TimeUnit.milliSecond ,         86400000000.0        );
		assertTimeConversion(TimeUnit.day ,      1.0         , TimeUnit.milliSecond ,            86400000.0        );
		assertTimeConversion(TimeUnit.day ,      0.5         , TimeUnit.milliSecond ,            43200000.0        );
		assertTimeConversion(TimeUnit.day ,      0.50000005  , TimeUnit.milliSecond ,            43200004.0        );
		assertTimeConversion(TimeUnit.day ,      0.1         , TimeUnit.milliSecond ,             8640000.0        );
		assertTimeConversion(TimeUnit.day ,      0.00001     , TimeUnit.milliSecond ,                 864.0        );
		assertTimeConversion(TimeUnit.day ,      0.000001    , TimeUnit.milliSecond ,                  86.0        );
		assertTimeConversion(TimeUnit.day ,      0.0000001   , TimeUnit.milliSecond ,                   8.0        );
		assertTimeConversion(TimeUnit.day ,      0.00000005  , TimeUnit.milliSecond ,                   4.0        );
		assertTimeConversion(TimeUnit.day ,      0.000000025 , TimeUnit.milliSecond ,                   2.0        );
		assertTimeConversion(TimeUnit.day , 100000.000000025 , TimeUnit.milliSecond ,       8640000000002.0        );
		assertTimeConversion(TimeUnit.day ,   1000.0         , TimeUnit.microSecond ,      86400000000000.0        );
		assertTimeConversion(TimeUnit.day , 100000.000000025 , TimeUnit.microSecond ,    8640000000002000.0        );
		assertTimeConversion(TimeUnit.day ,   1000.0         , TimeUnit.nanoSecond  ,   86400000000000000.0        );
		assertTimeConversion(TimeUnit.day , 100000.0         , TimeUnit.nanoSecond  , 8640000000000000000.0        );
		assertTimeConversion(TimeUnit.day ,   1000.000000025 , TimeUnit.nanoSecond  ,   86400000002000000.0        );
	} 

	private void assertTimeConversion(TimeUnit fromUnit, double fromValue, TimeUnit toUnit, double toValue) {
		try {
			double fromInMillis = coded.encode(createTimeSpan(fromUnit, fromValue));
			double toInMillis = coded.encode(createTimeSpan(toUnit, toValue));
			Assert.assertEquals("milliseconds comparison failed. "+fromValue+" "+fromUnit+"(s) found inequivalent to "+toValue+" "+toUnit+"(s)", toInMillis, fromInMillis, 1);
		} catch (CodecException e) {
			e.printStackTrace();
			Assert.fail("unexpected exception while encoding TimeSpan: "+e.getMessage());
		}
	}
	
	private static TimeSpan createTimeSpan(TimeUnit unit, double value) {
		TimeSpan maxIdleTime = TimeSpan.T.create();
		maxIdleTime.setUnit(unit);
		maxIdleTime.setValue(value);
		return maxIdleTime;
	}
}
