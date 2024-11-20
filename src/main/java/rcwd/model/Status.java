package rcwd.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class Status {

    private int id;
    private int commandId;
    private int statusType;
    private String description;
    private Date modifiedDate;
}
