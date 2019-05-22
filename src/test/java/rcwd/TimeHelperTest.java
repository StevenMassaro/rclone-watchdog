package rcwd;

import org.apache.commons.lang.mutable.MutableLong;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static rcwd.helper.TimeHelper.convertUnitToHumanString;

public class TimeHelperTest {

    @Test
    public void testPlurals(){
        assertEquals("1 second ",
                convertUnitToHumanString(TimeUnit.SECONDS, new MutableLong(TimeUnit.NANOSECONDS.convert(1, TimeUnit.SECONDS))));
        assertEquals("2 seconds ",
                convertUnitToHumanString(TimeUnit.SECONDS, new MutableLong(TimeUnit.NANOSECONDS.convert(2, TimeUnit.SECONDS))));
    }
}
