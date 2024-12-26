# Update

I suggest using [backrest](https://github.com/garethgeorge/backrest) instead of this project. Backrest is more fully featured, and operates as a true backup service. This project will receive limited updates as I've almost fully transitioned to using backrest.

# rclone-watchdog

Run your rclone commands on a schedule.

## Environment variables

|Variable|Required|Description|
|---|---|---|
|`RCLONE_BANDWIDTH_SCHEDULE`|no|If specified, this will be used as the scheduled bandwidth limit for all jobs. See https://rclone.org/docs/#bwlimit-bandwidth-spec|
|`ignoreDiscardedClosedSshConnection`|no|Defaults to true, if set to false, any errors thrown by rclone that look like `Discarding closed SSH connection` will be thrown as actual errors and will kill the job if too many occur. It appears that though rclone throws these as errors, they are really debug messages.|
