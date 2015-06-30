/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServicesApp.Connection;

import ServicesApp.UI.ServicesController;
import ServicesApp.UI.ServicesLogInController;
import Shared.Connection.Transaction.ClientBoundTransaction;
import Shared.Connection.Transaction.ConnState;
import Shared.Connection.IResponseHandler;
import java.util.concurrent.ConcurrentLinkedQueue;
import Shared.Connection.SerializeUtils;
import Shared.Data.IData;
import Shared.Data.IDataRequest;
import Shared.Data.ISortedData;
import Shared.NetworkException;
import Shared.Tasks.ITask;
import Shared.Users.IUser;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class ResponseHandler implements IResponseHandler {

    private ServicesController servicesController = null;
    private ServicesLogInController loginController = null;
    private ConnectionHandler connHandler = null;
    private final ConcurrentLinkedQueue<byte[]> responses;

    protected ResponseHandler(ConnectionHandler handler) {
        this.connHandler = handler;
        this.responses = new ConcurrentLinkedQueue<>();
    }

    protected void setLoginController(ServicesLogInController loginController) {
        this.loginController = loginController;
    }

    protected void setServicesController(ServicesController hqController) {
        this.servicesController = hqController;
    }

    /**
     * Loads given byte array into a queue, waiting to be processed by run().
     * This method is called by the thread in charge of run() in
     * ClientConnection.
     *
     * @param rsp
     * @return
     */
    @Override
    public synchronized boolean handleResponse(byte[] rsp) {
        this.responses.add(rsp);
        this.notify();
        return true;
    }

    /**
     * Reads the queue of data, and handles transactions accordingly.
     */
    @Override
    public void run() {
        while (true) {
            while (this.responses.isEmpty()) {
                try {
                    synchronized (this) {
                        this.wait();
                    }
                } catch (InterruptedException e) {
                }
            }
            while (!this.responses.isEmpty()) {
                byte[] rsp = this.responses.poll();
                try {
                    ClientBoundTransaction transaction
                            = (ClientBoundTransaction) SerializeUtils.deserialize(rsp);

                    // notifies handler of completed query.
                    // pushed transactions have a commandID of -1 and are not relevant
                    if (transaction.ID > -1) {
                        this.connHandler.notifyCommandResponse(transaction.ID);
                    }

                    System.out.println(transaction.command.toString() // debugging println
                            + ": "
                            + transaction.result.toString());

                    switch (transaction.command) {
                        default:
                            throw new NetworkException("(Unknown Command) - "
                                    + transaction.command.toString());
                        case USERS_REGISTER:
                            handleRegisterResult(transaction);
                            break;
                        case SORTED_SEND:
                            this.handleGenericResult(transaction);
                            break;
                        case SORTED_GET:
                            this.handleSortedResponse(transaction);
                            break;
                        case UNSORTED_GET_ID:
                            this.handleDataItemResult(transaction);
                            break;
                        case UNSORTED_GET_SOURCE:
                            this.handleSentDataResult(transaction);
                            break;
                        case TASKS_GET:
                            this.handleTasksResult(transaction);
                            break;
                        case USERS_SIGN_IN:
                            this.handleLoginResult(transaction);
                            break;
                        case UPDATE_REQUEST_GET:
                            this.handleRequestsResult(transaction);
                            break;
                        case TASKS_PUSH:
                            this.handleTaskPush(transaction);
                            break;
                        case UNSORTED_SEND:
                            this.handleGenericResult(transaction);
                            break;
                        case UNSORTED_GET:
                            this.handleGenericResult(transaction);
                            break;
                        case USERS_UNSORTED_SUBSCRIBE:
                            this.handleGenericResult(transaction);
                            break;
                        case USERS_UNSORTED_UNSUBSCRIBE:
                            this.handleGenericResult(transaction);
                            break;
                        case PLAN_SEARCH:
                            this.handleGenericResult(transaction);
                            break;
                        case USERS_GET_SERVICEUSERS:
                            this.handleGenericResult(transaction);
                            break;
                        case SITUATIONS_GET:
                            this.handleGenericResult(transaction);
                            break;
                        case UNSORTED_STATUS_RESET:
                            this.handleGenericResult(transaction);
                            break;
                        case UNSORTED_UPDATE_SEND:
                            this.handleGenericResult(transaction);
                            break;
                        case UNSORTED_DISCARD:
                            this.handleGenericResult(transaction);
                            break;
                        case UPDATE_REQUEST_SEND:
                            this.handleGenericResult(transaction);
                            break;
                        case TASK_SEND:
                            this.handleGenericResult(transaction);
                            break;
                        case PLAN_SEND_NEW:
                            this.handleGenericResult(transaction);
                            break;
                        case PLAN_APPLY:
                            this.handleGenericResult(transaction);
                            break;
                        case TASK_UPDATE:
                            this.handleGenericResult(transaction);
                            break;
                        case NEWSITEM_SEND:
                            this.handleGenericResult(transaction);
                            break;
                        case NEWSITEM_UPDATE:
                            this.handleGenericResult(transaction);
                            break;
                    }

                } catch (NetworkException nEx) {
                    System.err.println(nEx.getMessage());
                } catch (Exception ex) {
                    System.out.println("Error handling file from Server");
                    ex.printStackTrace();
                }
            }

        }
    }

    private void handleGenericResult(ClientBoundTransaction transaction) {
        if(transaction.result == ConnState.COMMAND_FAIL && transaction.data instanceof Exception) {
            servicesController.showDialog("Error", ((Exception) transaction.data).getMessage(), true);
            ((Exception)transaction.data).printStackTrace();
        }
    }

    private void handleLoginResult(ClientBoundTransaction transaction) {
        if(transaction.result == ConnState.COMMAND_FAIL && transaction.data instanceof Exception) {
            loginController.showDialog("Error", ((Exception) transaction.data).getMessage(), true);
        }
        if (transaction.result == ConnState.COMMAND_SUCCESS) {
            IUser user = (IUser) transaction.data;
            this.loginController.logIn(user);
        }
    }

    private void handleSortedResponse(ClientBoundTransaction transaction) {
        if(transaction.result == ConnState.COMMAND_FAIL && transaction.data instanceof Exception) {
            servicesController.showDialog("Error", ((Exception) transaction.data).getMessage(), true);
        }
        if (transaction.result == ConnState.COMMAND_SUCCESS) {
            List<ISortedData> data = (List) transaction.data;
            this.servicesController.displaySortedData(data);
        }
    }

    private void handleTasksResult(ClientBoundTransaction transaction) {
        if(transaction.result == ConnState.COMMAND_FAIL && transaction.data instanceof Exception) {
            servicesController.showDialog("Error", ((Exception) transaction.data).getMessage(), true);
        }
        if (transaction.result == ConnState.COMMAND_SUCCESS) {
            List<ITask> tasks = (List) transaction.data;
            this.servicesController.displayTasks(tasks);
        }
    }

    private void handleDataItemResult(ClientBoundTransaction transaction) {
        if(transaction.result == ConnState.COMMAND_FAIL && transaction.data instanceof Exception) {
            servicesController.showDialog("Error", ((Exception) transaction.data).getMessage(), true);
        }
        if (transaction.result == ConnState.COMMAND_SUCCESS) {
            IData data = (IData) transaction.data;
            this.servicesController.displayDataItem(data);
        }
    }

    private void handleRequestsResult(ClientBoundTransaction transaction) {
        if(transaction.result == ConnState.COMMAND_FAIL && transaction.data instanceof Exception) {
            servicesController.showDialog("Error", ((Exception) transaction.data).getMessage(), true);
        }
        if (transaction.result == ConnState.COMMAND_SUCCESS) {
            List<IDataRequest> requests = (List) transaction.data;
            this.servicesController.displayRequests(requests);
        }
    }

    private void handleSentDataResult(ClientBoundTransaction transaction) {
        if(transaction.result == ConnState.COMMAND_FAIL && transaction.data instanceof Exception) {
            servicesController.showDialog("Error", ((Exception) transaction.data).getMessage(), true);
        }
        if (transaction.result == ConnState.COMMAND_SUCCESS) {
            List<IData> data = (List) transaction.data;
            this.servicesController.displaySentData(data);
        }
    }

    private void handleRegisterResult(ClientBoundTransaction transaction) {
        if(transaction.result == ConnState.COMMAND_FAIL && transaction.data instanceof Exception) {
            servicesController.showDialog("Error", ((Exception) transaction.data).getMessage(), true);
        }
    }

    private void handleTaskPush(ClientBoundTransaction transaction) {
        if(transaction.result == ConnState.COMMAND_FAIL && transaction.data instanceof Exception) {
            servicesController.showDialog("Error", ((Exception) transaction.data).getMessage(), true);
        }
        if (transaction.result == ConnState.COMMAND_SUCCESS) {
            ITask task = (ITask) transaction.data;
            List<ITask> tasks = Arrays.asList(new ITask[]{task});
            List<ITask> myTasks = new ArrayList<>();
            for(ITask t : tasks) {
                if(this.connHandler.getCurrentUser().getUsername().equals(t.getExecutor().getUsername())) {
                    myTasks.add(t);
                }
            }
            this.servicesController.displayTasks(myTasks);
        }
    }

}
