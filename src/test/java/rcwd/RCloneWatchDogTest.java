package rcwd;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class RCloneWatchDogTest {

    private TelegramHelper telegramHelper = new TelegramHelper(null,null,null, null);

    @Test
    public void testTelegramString() throws IOException {
        String sample = "2019/05/13 09:29:58 INFO  : Encrypted drive 'google:test': Waiting for checks to finish\n" +
                "2019/05/13 09:29:58 INFO  : Encrypted drive 'google:test': Waiting for transfers to finish\n" +
                "2019/05/13 09:29:58 INFO  : Waiting for deletions to finish\n" +
                "2019/05/13 09:29:58 INFO  : \n" +
                "Transferred:   \t         0 / 0 Bytes, -, 0 Bytes/s, ETA -\n" +
                "Errors:                 0\n" +
                "Checks:                 2 / 2, 100%\n" +
                "Transferred:            0 / 0, -\n" +
                "Elapsed time:        1.5s\n" +
                "\n";
        String expected = "*sample execution finished.*\n"+
                "Transferred: 0 / 0 Bytes, -, 0 Bytes/s, ETA -\n" +
                "Errors: 0\n" +
                "Checks: 2 / 2, 100%\n" +
                "Transferred: 0 / 0, -\n" +
                "Elapsed time: 1.5s";

        OutputStream sampleOutputStream = new ByteArrayOutputStream();
        IOUtils.copy(IOUtils.toInputStream(sample, StandardCharsets.UTF_8), sampleOutputStream);
        assertEquals(expected, telegramHelper.buildTelegramExecutionEndText("sample", sampleOutputStream));
    }

    @Test
    @Ignore
    public void testExecutionTimeString() throws InterruptedException {
        long st = System.nanoTime();
        Thread.sleep(1000*62);
        System.out.println(telegramHelper.buildTelegramExecutionEndText("tasl", st, System.nanoTime(), new CircularFifoQueue<String>()));
    }
}
