package rcwd.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.lang.StringUtils;
import rcwd.helper.MessageHelper;

import java.io.IOException;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Log4j2
public class Command {

    private long id;
    private String name;
    private String command;
    private Directory source;
    private Destination destination;
    private List<Filter> filters;
    private StatusEnum status;
    @JsonFormat(pattern = "yyyy-MM-dd hh:mm:ss a", timezone = "America/New_York")
    private Date statusChangeDate;
    private String healthchecksUrl;
    private String schedule;

    @JsonIgnore
    public boolean hasFilters(){
        return CollectionUtils.isNotEmpty(filters);
    }

    public CommandLine getCommandLine(String rcloneBasePath){
        CommandLine cmdLine = CommandLine.parse(rcloneBasePath.trim());
        addArgs(cmdLine);
        return cmdLine;
    }

    private void addArgs(CommandLine cmdLine){
        cmdLine.addArgument(command);
        cmdLine.addArgument(source.getDirectory().trim());
        String destinationArgument = destination.getRemote();
        if (StringUtils.isNotBlank(destination.getDirectory())) {
            destinationArgument += ":" + destination.getDirectory();
        }
        cmdLine.addArgument(destinationArgument);
        if (hasFilters()) {
            for(Filter filter : filters){
                cmdLine.addArgument(filter.getFilter(), false);
            }
        }
    }

    public void sendHealthChecksIoCall(MessageHelper messageHelper, CircularFifoQueue<String> logQueue, boolean isFailure) {
        if (StringUtils.isNotEmpty(getHealthchecksUrl())) {
            OkHttpClient client = new OkHttpClient();

            String healthchecksUrl = getHealthchecksUrl();
            if (isFailure) {
                healthchecksUrl += "/fail";
            }
            Request request = new Request.Builder()
                    .url(healthchecksUrl)
                    .method("POST", RequestBody.create(messageHelper.createStringFromCircularFifoQueue(logQueue).getBytes()))
                    .build();

            try (Response ignored = client.newCall(request).execute()) {
                log.debug("Sent healthchecks.io call at end of command");
            } catch (IOException e) {
                log.error("Failed to send healthchecks.io call at end of command", e);
            }
        } else {
            log.trace("healthchecks.io url is null or empty, so not sending a call");
        }
    }
}
