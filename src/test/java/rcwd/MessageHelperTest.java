package rcwd;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;
import rcwd.helper.MessageHelper;
import rcwd.service.TelegramService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MessageHelperTest {

    private int logLinesToReport = 6;
    private MessageHelper messageHelper = new MessageHelper(logLinesToReport);

    @Test
    @Ignore
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
        assertEquals(expected, messageHelper.buildTelegramExecutionEndText("sample", sampleOutputStream));
    }

    @Test
    @Ignore
    public void testExecutionTimeString() throws InterruptedException {
        long st = System.nanoTime();
        Thread.sleep(1000*62);
        System.out.println(messageHelper.buildTelegramExecutionEndText("tasl", st, System.nanoTime(), new CircularFifoQueue<String>()));
    }

    /**
     * Tests to make sure that the last few lines from the fifo queue are being grabbed.
     */
    @Test
    public void testCorrectNumberOfLines() {
        CircularFifoQueue<String> circularFifoQueue = new CircularFifoQueue<>();
        for (int i = 0; i < logLinesToReport + 5; i++) {
            circularFifoQueue.add(Integer.toString(new Random().nextInt()));
        }
        String message = messageHelper.buildTelegramExecutionEndText("tas1", System.nanoTime(), System.nanoTime(), circularFifoQueue);
        assertEquals(logLinesToReport + 5, message.split("\n").length);
        assertTrue(message.contains(circularFifoQueue.get(circularFifoQueue.size()-1)));
    }
}
