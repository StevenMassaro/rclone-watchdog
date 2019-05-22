package rcwd.helper;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

public class MessageHelper {

    private final static String LINE_SEPARATOR = "\n"; // System.lineSeparator() doesn't work

    public static String buildErrorText(String task, String exceptionText, CircularFifoQueue<String> lastLogLines) {
        return buildBadTextBase("Error in", task, exceptionText, lastLogLines);
    }

    public static String buildErrorText(String task, String exceptionText) {
        return buildBadTextBase("Error in", task, exceptionText);
    }

    public static String buildFailureText(String task, String exceptionText, CircularFifoQueue<String> lastLogLines) {
        return buildBadTextBase("Failed", task, exceptionText, lastLogLines);
    }

    public static String buildFailureText(String task, String exceptionText) {
        return buildBadTextBase("Failed", task, exceptionText);
    }

    private static String buildBadTextBase(String message, String task, String exceptionText, CircularFifoQueue<String> lastLogLines) {
        String text = "*" + message + " " + task + "*" + LINE_SEPARATOR + makeTextCode(exceptionText.replaceAll("\\\\", "\\\\\\\\"));
        if (!lastLogLines.isEmpty()) {
            text += "Log: " + LINE_SEPARATOR + makeTextCode(createStringFromCircularFifoQueue(lastLogLines));
        }
        return text;
    }

    private static String buildBadTextBase(String message, String task, String exceptionText) {
        return buildBadTextBase(message, task, exceptionText, new CircularFifoQueue<String>());
    }

    public static String buildTelegramExecutionStartText(String task) {
        return "*Starting " + task + "*";
    }

    /**
     * Build the message indicating the result of execution.
     */
    @Deprecated
    public static String buildTelegramExecutionEndText(String task, OutputStream outputStream) {
        String executionResult = outputStream.toString();
        List<String> executionResultLines = Arrays.asList(executionResult.split(LINE_SEPARATOR));
        StringBuilder response = new StringBuilder();
        response.append("*");
        response.append(task);
        response.append(" execution finished.*");
        response.append(LINE_SEPARATOR);
        for (String line : executionResultLines.subList(executionResultLines.size() - 5, executionResultLines.size())) {
            line = line.replaceAll("\t", "");
            line = line.trim().replaceAll(" +", " ");
            response.append(line).append(LINE_SEPARATOR);
        }
        return response.toString().trim();
    }

    /**
     * Build the message indicating the result of execution.
     */
    public static String buildTelegramExecutionEndText(String task, long startTime, long endTime, CircularFifoQueue<String> logLines) {
        String resultText = "*Finished " + task + "*"
                + LINE_SEPARATOR + "Execution time: " + TimeHelper.elapsedTimeToHumanString(startTime, endTime)
                + LINE_SEPARATOR;

        return resultText + makeTextCode(createStringFromCircularFifoQueue(logLines));
    }

    private static String createStringFromCircularFifoQueue(CircularFifoQueue<String> queue) {
        StringBuilder logLineText = new StringBuilder();
        for (String line : queue) {
            logLineText.append(line);
            logLineText.append(LINE_SEPARATOR);
        }
        return logLineText.toString();
    }

    private static String makeTextCode(String text) {
        return "```" + LINE_SEPARATOR + text + LINE_SEPARATOR + "```";
    }
}
