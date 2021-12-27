package rcwd.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Profile {

    private long id;
    private String name;
    private List<Command> commands;
}
