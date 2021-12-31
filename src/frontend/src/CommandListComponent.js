import React, {Component} from 'react';
import './App.css';
import ReactTable from "react-table";
import ReactModal from "react-modal";
import "react-table/react-table.css";
import 'semantic-ui-css/semantic.min.css';
import {Button, Container, Dropdown, Grid, Image} from 'semantic-ui-react';
import {toast, ToastContainer} from "react-toastify";
import 'react-toastify/dist/ReactToastify.min.css';
import {LazyLog} from "react-lazylog";

// how often the log modal should reload the logs, in milliseconds
export const LOG_RELOAD_TIMEOUT = 5000;

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
            .then(this.handleRestResponse)
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

    execute = (id) => {
        fetch("./command/" + id + "/execute")
            .then(() => toast.info("Execution started"))
            .then(this.fetchCommands);
    };

    dryRun = (id) => {
        fetch("./command/" + id + "/dryrun")
            .then(() => toast.info("Dry run started"))
            .then(this.fetchCommands)
    };

    kill = (id) => {
        fetch("./command/" + id + "/kill")
            .then(this.fetchCommands);
    };

    handleRestResponse = (res) => {
        if (res.ok) {
            return res.json();
        } else {
            throw res;
        }
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
                    <div className={"modalContentWrapper"} style={{
                        "display": "flex",
                        "width": "100%",
                        "height": "100%",
                        "flex-direction": "column",
                        "overflow": "hidden"
                    }}>
                        <div style={{"width": "100%"}}>
                            <div style={{"float": "left", "width": "50%"}}>
                                <Button onClick={() => {
                                    this.setState((state) => ({
                                        autoRefreshLog: !state.autoRefreshLog,
                                        logKey: state.autoRefreshLog ? state.logKey : state.logKey + 1
                                    }));
                                }}>{this.state.autoRefreshLog ? "Turn off" : "Turn on"} auto refresh</Button>
                            </div>
                            <div style={{"float": "right", "width": "50%", "text-align": "right"}}>
                                <Button onClick={() => this.showLog(null)}>Close</Button>
                            </div>
                        </div>
                        <LazyLog url={"./command/" + this.state.logId + "/log"}
                                 fetchOptions={{credentials: 'include'}}
                                 follow={true}
                                 key={"log" + this.state.logKey}
                                 onLoad={() => {
                                     if (this.state.autoRefreshLog) {
                                         setTimeout(() => {
                                             if (this.state.autoRefreshLog) {
                                                 this.setState((state) => ({
                                                     logKey: state.logKey + 1
                                                 }));
                                             }
                                         }, LOG_RELOAD_TIMEOUT);
                                     }
                                 }}
                        />
                    </div>
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
                                accessor: s => (s.statusChangeDate && s.status) ? (<span>{s.statusChangeDate}<br/>{s.status}</span>) : ""
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