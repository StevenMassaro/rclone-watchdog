package rcwd.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.lang.StringUtils;

import java.util.Date;
import java.util.List;

public class Command {

    private long id;
    private String name;
    private String command;
    private Directory source;
    private Destination destination;
    private List<Filter> filters;
    private String status;
    @JsonFormat(pattern = "yyyy-MM-dd hh:mm:ss a", timezone = "America/New_York")
    private Date statusChangeDate;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public Directory getSource() {
        return source;
    }

    public void setSource(Directory source) {
        this.source = source;
    }

    public Destination getDestination() {
        return destination;
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
    }

    public List<Filter> getFilters() {
        return filters;
    }

    @JsonIgnore
    public boolean hasFilters(){
        return CollectionUtils.isNotEmpty(filters);
    }

    public void setFilters(List<Filter> filters) {
        this.filters = filters;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getStatusChangeDate() {
        return statusChangeDate;
    }

    public void setStatusChangeDate(Date statusChangeDate) {
        this.statusChangeDate = statusChangeDate;
    }

    public CommandLine getCommandLine(){
        CommandLine cmdLine = CommandLine.parse("rclone");
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
