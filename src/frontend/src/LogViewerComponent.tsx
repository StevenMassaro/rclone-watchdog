import React, {Component} from 'react';
import { LogViewer, LogViewerSearch } from '@patternfly/react-log-viewer';
import "@patternfly/react-core/dist/styles/base-no-reset.css";
import "./LogViewerComponent.css";
import { Toolbar, ToolbarContent, ToolbarItem, Button, Checkbox } from '@patternfly/react-core';
import {handleRestResponse} from "./Utils";

// how often the log modal should reload the logs, in milliseconds
export const LOG_RELOAD_TIMEOUT = 5000;

type props = {
    showLog: (id: number | null) => void,
    commandId: number
}

type state = {
    autoRefreshLog: boolean,
    logs: string[],
    wrapText: boolean
}

class LogViewerComponent extends Component<props, state> {
    constructor(props: props | Readonly<props>) {
        super(props);
        this.state = {
            autoRefreshLog: true,
            logs: [],
            wrapText: true
        }
    }

    componentDidMount() {
        this.fetchLogs();
    }

    fetchLogs = () => {
        fetch("./command/" + this.props.commandId + "/log")
            .then(handleRestResponse)
            .then(
                (logs) => {
                    this.setState({
                        logs
                    });
                    if (this.state.autoRefreshLog) {
                        setTimeout(() => {
                            if (this.state.autoRefreshLog) {
                                this.fetchLogs();
                            }
                        }, LOG_RELOAD_TIMEOUT);
                    }
                }
            )
    }

    toggleWrapText= () => {
        this.setState(state => ({
            wrapText: !state.wrapText
        }))
    }

    render() {
        return (<LogViewer
            data={this.state.logs}
            isTextWrapped={this.state.wrapText}
            scrollToRow={this.state.logs.length}
            toolbar={
                <Toolbar>
                    <ToolbarContent>
                        <ToolbarItem>
                            <Button onClick={() => {
                                this.setState(state => ({
                                    autoRefreshLog: !state.autoRefreshLog
                                }), () => {
                                    // if auto refresh was just turned back on, then refresh the logs
                                    if (this.state.autoRefreshLog) {
                                        this.fetchLogs()
                                    }
                                });
                            }} variant="control">
                                {this.state.autoRefreshLog ? "Turn off" : "Turn on"} auto refresh
                            </Button>
                        </ToolbarItem>
                        <ToolbarItem>
                            <Checkbox label="Wrap text" aria-label="wrap text checkbox" isChecked={this.state.wrapText} id="wrap-text-checkbox" onChange={this.toggleWrapText} />
                        </ToolbarItem>
                        <ToolbarItem style={{"marginLeft": "auto"}}>
                            <Button onClick={() => this.props.showLog(null)} variant="control">
                                Close
                            </Button>
                        </ToolbarItem>
                    </ToolbarContent>
                </Toolbar>
            }
        />);
    }
}
export default LogViewerComponent;