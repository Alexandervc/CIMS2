/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package HeadquartersApp.Connection;

import HeadquartersApp.UI.HeadquartersController;
import HeadquartersApp.UI.HeadquartersLogInController;
import Shared.Connection.Transaction.ClientBoundTransaction;
import Shared.Connection.Transaction.ConnState;
import Shared.Connection.IResponseHandler;
import java.util.concurrent.ConcurrentLinkedQueue;
import Shared.Connection.SerializeUtils;
import Shared.Data.IData;
import Shared.Data.INewsItem;
import Shared.Data.ISortedData;
import Shared.Data.Situation;
import Shared.NetworkException;
import Shared.Tasks.IPlan;
import Shared.Tasks.ITask;
import Shared.Users.IServiceUser;
import Shared.Users.IUser;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

class ResponseHandler implements IResponseHandler {

    private HeadquartersController hqController = null;
    private HeadquartersLogInController loginController = null;
    private final ConnectionHandler connectionHandler;
    private final ConcurrentLinkedQueue<byte[]> responses;

    protected ResponseHandler(ConnectionHandler connHandler) {
        this.connectionHandler = connHandler;
        this.responses = new ConcurrentLinkedQueue<>();
    }

    protected void setLoginController(HeadquartersLogInController loginController) {
        this.loginController = loginController;
    }

    protected void setHQController(HeadquartersController hqController) {
        this.hqController = hqController;
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
     * Handles responses in queue on appropriate thread.
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
                        this.connectionHandler.notifyCommandResponse(transaction.ID);
                    }

                    System.out.println(transaction.command.toString() // debugging println
                            + ": "
                            + transaction.result.toString());

                    switch (transaction.command) {
                        default:
                            throw new NetworkException("(Unknown Command) - "
                                    + transaction.command.toString());
                        case SORTED_SEND:
                            this.handleGenericResult(transaction);
                            break;
                        case SORTED_GET:
                            this.handleSortedResponse(transaction);
                            break;
                        case UNSORTED_GET:
                            this.handleUnsortedResult(transaction);
                            break;
                        case UNSORTED_GET_UPDATE:
                            this.handleUnsortedUpdateResult(transaction);
                            break;
                        case TASKS_GET:
                            this.handleTasksResult(transaction);
                            break;
                        case PLAN_SEARCH:
                            this.handleSearchPlansResult(transaction);
                            break;
                        case USERS_GET_SERVICEUSERS:
                            this.handleServiceUsersResult(transaction);
                            break;
                        case USERS_SIGN_IN:
                            this.handleLoginResult(transaction);
                            break;
                        case NEWSITEMS_GET:
                            this.handleNewsItemsResult(transaction);
                            break;
                        case SITUATIONS_GET:
                            this.handleSituationsResult(transaction);
                            break;
                        case TASKS_PUSH:
                            this.handleTaskPush(transaction);
                            break;
                        case USERS_UNSORTED_SUBSCRIBE:
                            this.handleSubscribeUnsorted(transaction);
                            break;
                        case USERS_UNSORTED_UNSUBSCRIBE:
                            this.handleUnsubscribeUnsorted(transaction);
                            break;
                        case UNSORTED_GET_ID:
                            this.handleGenericResult(transaction);
                            break;
                        case UNSORTED_SEND:
                            this.handleGenericResult(transaction);
                            break;
                        case UNSORTED_GET_SOURCE:
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
                        case UPDATE_REQUEST_GET:
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
                        case USERS_REGISTER:
                            this.handleGenericResult(transaction);
                            break;
                        case UNSORTED_STATUS_UPDATE:
                            this.handleGenericResult(transaction);
                            break;
                    }

                } catch (NetworkException nEx) {
                    System.err.println("Server failure handling command: " + nEx.getMessage());
                } catch (Exception ex) {
                    System.out.println("Error handling file from Server");
                    ex.printStackTrace();
                }
            }

        }
    }

    /**
     * Handles command results that do not need any specific handling (boolean
     * results)
     * // TODO: notify user of these things
     * @param transaction
     */
    private void handleGenericResult(ClientBoundTransaction transaction) {
        if(transaction.result == ConnState.COMMAND_FAIL && transaction.data instanceof Exception) {
            hqController.showDialog("Error", ((Exception) transaction.data).getMessage(), true);
        }
    }

    private void handleLoginResult(ClientBoundTransaction transaction) {
        if(transaction.result == ConnState.COMMAND_FAIL && transaction.data instanceof Exception) {
            loginController.showDialog("Error", ((Exception) transaction.data).getMessage(), true);
        }
        if(transaction.result == ConnState.COMMAND_SUCCESS) {
            IUser user = (IUser) transaction.data;
            this.loginController.logIn(user);
        }
    }

    private void handleUnsortedResult(ClientBoundTransaction transaction) {
        if(transaction.result == ConnState.COMMAND_FAIL && transaction.data instanceof Exception) {
            hqController.showDialog("Error", ((Exception) transaction.data).getMessage(), true);
        }
        if (transaction.result == ConnState.COMMAND_SUCCESS) {
            List<IData> list = (List) transaction.data;
            this.hqController.displayData(list);
        }
    }
    
    private void handleUnsortedUpdateResult(ClientBoundTransaction transaction) {
        if(transaction.result == ConnState.COMMAND_FAIL && transaction.data instanceof Exception) {
            hqController.showDialog("Error", ((Exception) transaction.data).getMessage(), true);
        }
        if (transaction.result == ConnState.COMMAND_SUCCESS) {
            List<IData> list = (List) transaction.data;
            this.hqController.displayUpdatedData(list);
        }
    }

    private void handleSearchPlansResult(ClientBoundTransaction transaction) {
        if(transaction.result == ConnState.COMMAND_FAIL && transaction.data instanceof Exception) {
            hqController.showDialog("Error", ((Exception) transaction.data).getMessage(), true);
        }
        if (transaction.result == ConnState.COMMAND_SUCCESS) {
            List<IPlan> plans = (List) transaction.data;
            this.hqController.displayPlans(plans);
        }
    }

    private void handleSortedResponse(ClientBoundTransaction transaction) {
        if(transaction.result == ConnState.COMMAND_FAIL && transaction.data instanceof Exception) {
            hqController.showDialog("Error", ((Exception) transaction.data).getMessage(), true);
        }
        if (transaction.result == ConnState.COMMAND_SUCCESS) {
            List<ISortedData> data = (List) transaction.data;
            this.hqController.displaySortedData(data);
        }
    }

    private void handleServiceUsersResult(ClientBoundTransaction transaction) {
        if(transaction.result == ConnState.COMMAND_FAIL && transaction.data instanceof Exception) {
            hqController.showDialog("Error", ((Exception) transaction.data).getMessage(), true);
        }
        if (transaction.result == ConnState.COMMAND_SUCCESS) {
            List<IServiceUser> users = (List) transaction.data;
            this.hqController.displayServiceUsers(users);
        }
    }

    private void handleTasksResult(ClientBoundTransaction transaction) {
        if(transaction.result == ConnState.COMMAND_FAIL && transaction.data instanceof Exception) {
            hqController.showDialog("Error", ((Exception) transaction.data).getMessage(), true);
        }
        if (transaction.result == ConnState.COMMAND_SUCCESS) {
            List<ITask> tasks = (List) transaction.data;
            this.hqController.displayTasks(tasks);
        }
    }
    
    private void handleNewsItemsResult(ClientBoundTransaction transaction) {
        if(transaction.result == ConnState.COMMAND_FAIL && transaction.data instanceof Exception) {
            hqController.showDialog("Error", ((Exception) transaction.data).getMessage(), true);
        }
        if (transaction.result == ConnState.COMMAND_SUCCESS) {
            List<INewsItem> news = (List) transaction.data;
            this.hqController.displayNewsItems(news);
        }
    }

    private void handleSituationsResult(ClientBoundTransaction transaction) {
        if(transaction.result == ConnState.COMMAND_FAIL && transaction.data instanceof Exception) {
            hqController.showDialog("Error", ((Exception) transaction.data).getMessage(), true);
        }
        if (transaction.result == ConnState.COMMAND_SUCCESS) {
            Set<Situation> situations = (Set) transaction.data;
            this.hqController.displaySituations(situations);
        }
    }

    private void handleTaskPush(ClientBoundTransaction transaction) {
        if(transaction.result == ConnState.COMMAND_FAIL && transaction.data instanceof Exception) {
            hqController.showDialog("Error", ((Exception) transaction.data).getMessage(), true);
        }
        if (transaction.result == ConnState.COMMAND_SUCCESS) {
            ITask task = (ITask) transaction.data;
            this.hqController.displayTasks(Arrays.asList(new ITask[]{task}));
        }
    }

    private void handleSubscribeUnsorted(ClientBoundTransaction transaction) {
        if(transaction.result == ConnState.COMMAND_FAIL && transaction.data instanceof Exception) {
            hqController.showDialog("Error", ((Exception) transaction.data).getMessage(), true);
        }
        if(transaction.result == ConnState.COMMAND_SUCCESS) {
            this.connectionHandler.setSubscribedUnsorted(true);
        }
    }
    
    private void handleUnsubscribeUnsorted(ClientBoundTransaction transaction) {
        if(transaction.result == ConnState.COMMAND_FAIL && transaction.data instanceof Exception) {
            hqController.showDialog("Error", ((Exception) transaction.data).getMessage(), true);
        }
        if(transaction.result == ConnState.COMMAND_SUCCESS) {
            this.connectionHandler.setSubscribedUnsorted(false);
        }
    }
}
