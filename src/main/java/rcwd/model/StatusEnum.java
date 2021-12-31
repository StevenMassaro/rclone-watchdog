package rcwd.model;

import lombok.Getter;

@Getter
public enum StatusEnum {
    EXECUTION_START(0, "Execution started"),
    EXECUTION_FAIL(1, "Execution failed"),
    EXECUTION_SUCCESS(2, "Execution success"),
    DRY_RUN_EXECUTION_START(3, "Dry run execution started"),
    DRY_RUN_EXECUTION_FAIL(4, "Dry run execution failed"),
    DRY_RUN_EXECUTION_SUCCESS(5, "Dry run execution success");

    private final long statusType;
    private final String name;

    StatusEnum(long statusType, String name) {
        this.statusType = statusType;
        this.name = name;
    }
}
