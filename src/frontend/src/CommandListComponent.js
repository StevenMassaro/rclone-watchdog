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

class CommandListComponent extends Component {

    constructor() {
        super();
        this.state = {
            logId: undefined
        };
    }

    componentWillMount() {
        this.fetchCommands();
    }

    sortById = (a, b) => {
        return a.id - b.id;
    };

    fetchCommands = () => {
        this.setState({
            fetchingCommands: true,
            fetchedCommand: false
        });
        fetch("./command")
            .then(this.handleRestResponse)
            .then(
                (commands) => {
                    commands.sort(this.sortById);
                    this.setState({
                        fetchingCommands: false,
                        fetchedCommands: true,
                        commands: commands
                    });
                },
                (error) => {
                    this.setState({
                        fetchingCommands: false,
                        fetchedCommands: true,
                    });
                    error.text().then(errorMessage => toast.error(<div>Failed to retrieve commands with
                        error:<br/>{errorMessage}</div>));
                });
    };

    execute = (id) => {
        fetch("./command/" + id + "/execute");
        this.fetchCommands();
    };

    dryRun = (id) => {
        fetch("./command/" + id + "/dryrun");
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
                        <Button onClick={() => this.showLog(null)}>Close</Button>
                        <LazyLog url={"/rclone-watchdog/command/" + this.state.logId + "/log"}
                                 fetchOptions={{credentials: 'include'}}
                                 follow={true}
                        />
                    </div>
                </ReactModal>

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
                                accessor: s => s.source.directory
                            },
                            {
                                Header: "Destination",
                                id: "destination",
                                accessor: d => d.destination.remote + ":" + d.destination.directory
                            },
                            {
                                Header: "Actions",
                                id: "Actions",
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
                                            disabled={}
                                        >Dry run</Button>
                                        <Button
                                            disabled
                                        >End</Button>


                                    </span>);
                                },
                            }
                        ]
                    }
                    // defaultPageSize={stationDetail.prices.length}
                    showPagination={false}
                    showPageSizeOptions={false}
                    getTfootProps={() => ({
                        style: {display: "none"}
                    })}
                />
                {/*<Container style={{ marginTop: '3em' }}>*/}
                {/**/}
                {/*</Container>*/}
            </div>
        );

    }

}

export default CommandListComponent;