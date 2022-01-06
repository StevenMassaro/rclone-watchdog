package rcwd.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.lang.StringUtils;

import java.util.Date;
import java.util.List;

@Getter
@Setter
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
        cmdLine.addArgument(destination.getRemote() + ":" + destination.getDirectory());
        if (hasFilters()) {
            for(Filter filter : filters){
                cmdLine.addArgument(filter.getFilter(), false);
            }
        }
    }
}
