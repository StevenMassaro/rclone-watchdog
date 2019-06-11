package rcwd.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.lang.StringUtils;

public class Command {

    private long id;
    private String name;
    private String command;
    private Directory source;
    private Destination destination;
    private String filters;

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

    public String getFilters() {
        return filters;
    }

    @JsonIgnore
    public boolean hasFilters(){
        return StringUtils.isNotEmpty(filters);
    }

    public void setFilters(String filters) {
        this.filters = filters;
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
            cmdLine.addArgument(filters, false);
        }
    }
}
