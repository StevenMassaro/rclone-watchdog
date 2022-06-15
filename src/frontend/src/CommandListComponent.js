import React, {Component} from 'react';
import './App.css';
import ReactTable from "react-table";
import ReactModal from "react-modal";
import "react-table/react-table.css";
import 'semantic-ui-css/semantic.min.css';
import {Button} from 'semantic-ui-react';
import {toast, ToastContainer} from "react-toastify";
import 'react-toastify/dist/ReactToastify.min.css';
import LogViewerComponent from "./LogViewerComponent";
import {handleRestResponse} from "./Utils";

class CommandListComponent extends Component {

    constructor() {
        super();
        this.state = {
            logId: undefined,
            logKey: 0,
            bandwidthLimit: 0,
            bandwidthLimitTimeout: undefined,
            autoRefreshLog: true
        };
    }

    componentWillMount() {
        this.fetchCommands();
    }

    sortById = (a, b) => {
        return a.id - b.id;
    };

    fetchCommands = () => {
        fetch("./command")
            .then(handleRestResponse)
            .then(
                (commands) => {
                    commands.sort(this.sortById);
                    this.setState({
                        commands: commands
                    });
                },
                (error) => {
                    error.text().then(errorMessage => toast.error(<div>Failed to retrieve commands with
                        error:<br/>{errorMessage}</div>));
                });
    };

    execute = (id, force = false) => {
        fetch("./command/" + id + "/execute?force=" + force)
            .then((res) => this.handleExecutionResponse(id, res, "Execution", this.execute))
            .then(this.fetchCommands);
    };

    dryRun = (id, force = false) => {
        fetch("./command/" + id + "/dryrun?force=" + force)
            .then((res) => this.handleExecutionResponse(id, res, "Dry run", this.dryRun))
            .then(this.fetchCommands)
    };

    /**
     *
     * @param res the raw response from the fetch command
     * @param messagePrefix the prefix that should be appended to the toast messages to indicate what kind of execution
     * this was (dry run or a real one)
     * @param forceRerunCallback a callback that will be invoked if the user forces execution
     */
    handleExecutionResponse = (commandId, res, messagePrefix, forceRerunCallback) => {
        if (res.ok) {
            toast.info(messagePrefix + " started");
        } else {
            res.json().then(json => {
                toast.error(<span>{messagePrefix} failed: {json.message}<br/>
                <button onClick={() => forceRerunCallback(commandId, true)}>Force execution?</button></span>);
            })
        }
    }

    kill = (id) => {
        fetch("./command/" + id + "/kill")
            .then(this.fetchCommands);
    };

    showLog = (id) => {
        this.setState({
            logId: id
        });
    };

    handleSubmit = (event) => {
        fetch("./admin/bandwidthLimit/" + this.state.bandwidthLimit +
            (this.state.bandwidthLimitTimeout ? "?minutesToWaitBeforeResettingToDefault=" + this.state.bandwidthLimitTimeout : ""), {
            method: 'POST'
        });
        event.preventDefault();
    };

    render() {
        return (
            <div>
                <ToastContainer/>

                <ReactModal
                    isOpen={this.state.logId}
                    contentLabel={"Log"}
                    ariaHideApp={false}
                >
                    <LogViewerComponent
                        showLog={this.showLog}
                        commandId={this.state.logId}
                    />
                </ReactModal>

                <form onSubmit={this.handleSubmit}>
                    <label>
                        Bandwidth limit:
                        <input type="text"
                               value={this.state.value}
                               onChange={(event => {
                                   this.setState({bandwidthLimit: event.target.value})
                               })}
                               maxLength={4}
                        />
                        Bandwidth limit timeout (minutes):
                        <input type="number"
                               value={this.state.bandwidthLimitTimeout}
                               onChange={(event => {
                                   this.setState({bandwidthLimitTimeout: event.target.value})
                               })}
                        />
                    </label>
                    <input type="submit" value="Submit"/>
                </form>
                <ReactTable
                    data={this.state.commands}
                    columns={
                        [
                            {
                                Header: "ID",
                                accessor: "id",
                                maxWidth: 30
                            },
                            {
                                Header: "Name",
                                accessor: "name",
                                maxWidth: 150
                            },
                            {
                                Header: "Source",
                                id: "source",
                                maxWidth: 250,
                                style: { 'white-space': 'unset' },
                                accessor: s => s.source.directory
                            },
                            {
                                Header: "Destination",
                                id: "destination",
                                maxWidth: 250,
                                style: { 'white-space': 'unset' },
                                accessor: d => <span>{d.destination.remote}<br/>{d.destination.directory}</span>
                            },
                            {
                                Header: "Last status",
                                id: "status",
                                maxWidth: 175,
                                accessor: s => (s.statusChangeDate && s.status) ? (<span style={{color: s.status.name.includes("failed") ? "red" : (s.status.name.includes("success") ? "green" : "orange")}}>{s.statusChangeDate}<br/>{s.status.name}</span>) : ""
                            },
                            {
                                Header: "Actions",
                                id: "Actions",
                                style: { 'white-space': 'unset' },
                                Cell: row => {
                                    return (<span>
                                        <Button
                                            onClick={() => this.showLog(row.original.id)}
                                        >Log</Button>
                                        <Button
                                            onClick={() => this.execute(row.original.id)}
                                        >Execute</Button>
                                        <Button
                                            onClick={() => this.dryRun(row.original.id)}
                                        >Dry run</Button>
                                        <Button
                                            onClick={() => this.kill(row.original.id)}
                                        >Kill</Button>
                                    </span>);
                                }
                            }
                        ]
                    }
                    minRows={0}
                    showPagination={false}
                    showPageSizeOptions={false}
                    getTfootProps={() => ({
                        style: {display: "none"}
                    })}
                />
            </div>
        );
    }
}

export default CommandListComponent;