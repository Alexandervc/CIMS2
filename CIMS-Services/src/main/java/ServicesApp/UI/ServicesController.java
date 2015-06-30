/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServicesApp.UI;

import ServicesApp.Connection.ConnectionHandler;
import Shared.Data.IData;
import Shared.Data.IDataRequest;
import Shared.Data.ISortedData;
import Shared.Data.IUnsortedData;
import Shared.Data.Status;
import Shared.Data.UnsortedData;
import Shared.NetworkException;
import Shared.Tag;
import Shared.Tasks.IStep;
import Shared.Tasks.ITask;
import Shared.Tasks.TaskStatus;
import Shared.Users.IServiceUser;
import Shared.Users.IUser;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 *
 * @author Alexander
 */
public class ServicesController implements Initializable {

    @FXML
    TabPane tabPane;

    // SendInfo
    @FXML
    Tab tabSendInfo;
    @FXML
    TextField tfnTitle;
    @FXML
    TextArea tanDescription;
    @FXML
    TextField tfnSource;
    @FXML
    TextField tfnLocation;

    // UpdateInfo
    @FXML
    Tab tabUpdateInfo;
    @FXML
    ListView lvuSentData;
    @FXML
    TextField tfuTitle;
    @FXML
    TextArea tauDescription;
    @FXML
    TextField tfuSource;
    @FXML
    TextField tfuLocation;
    @FXML
    Button btnuSend;

    // ReadSortedData
    @FXML
    Tab tabReadSortedData;
    @FXML
    TableView tvsSortedData;
    @FXML
    TableColumn tcsTitle;
    @FXML
    TableColumn tcsRelevance;
    @FXML
    TableColumn tcsReliability;
    @FXML
    TableColumn tcsQuality;
    @FXML
    CheckBox chbsData;
    @FXML
    CheckBox chbsRequests;
    @FXML
    TextField tfsTitle;
    @FXML
    TextArea tasDescription;
    @FXML
    TextField tfsSource;
    @FXML
    TextField tfsLocation;
    @FXML
    Button btnAnswerRequest;

    // TaskInfo
    @FXML
    Tab tabReadTask;
    @FXML
    TableView tvtTasks;
    @FXML
    TableColumn tctTitle;
    @FXML
    TableColumn tctStatus;
    @FXML
    TextField tftTaskTitle;
    @FXML
    TextArea tatDescription;
    @FXML
    TextArea tatCondition;
    @FXML
    Button btnAcceptTask;
    @FXML
    Button btnDismissTask;
    @FXML
    Button btnFailed;
    @FXML
    Button btnSucceed;

    //report message label
    @FXML
    Label lblMessageUpdate;
    @FXML
    Label lblErrorMessageUpdate;
    @FXML
    Label lblMessageTask;
    @FXML
    Label lblMessageSend;

    //menuBar
    @FXML
    MenuBar menuHQ;

    private ConnectionHandler connectionManager;
    private boolean showingDataItem;
    private IDataRequest answeredRequest;
    private ITask selectedTask = null;
    private IServiceUser user = null;
    private HashSet<Tag> tags = new HashSet<>();

    private Services main;

    private boolean exception = false;
    private Timer timer = new Timer();

    public void setApp(Services application) {
        this.main = application;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.showingDataItem = false;
        this.answeredRequest = null;

        //labels to ""
        lblMessageUpdate.setText("");
        lblMessageTask.setText("");
        lblMessageSend.setText("");

        tvsSortedData.setEditable(false);
        tcsTitle.setResizable(false);
        tcsRelevance.setResizable(false);
        tcsReliability.setResizable(false);
        tcsQuality.setResizable(false);

        tvtTasks.setEditable(false);
        tctTitle.setResizable(false);
        tctStatus.setResizable(false);

        // Add Change Listeners
        lvuSentData.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener() {

                    @Override
                    public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                        selectSentData();
                    }
                });

        tvsSortedData.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener() {

                    @Override
                    public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                        selectData();
                    }
                });

        tvtTasks.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener() {

                    @Override
                    public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                        selectTask();
                    }

                });

        // Set cell factories
//        Callback callback = new Callback<TableColumn<IData>, TableCell<IData>>() {};
//        tcsTitle.setCellFactory(new Callback<TableColumn<IData>, TableCell<IData>> (
//
//            @Override
//              public TableCell<IData> call(TableColumn<IData> param) {
//                return new TableCell<IData>() {
//
//                    protected void updateItem(IData item, boolean empty) {
//                        super.updateItem(item, empty);
//                        lblMessageUpdate.setText("");
//                        lblMessageTask.setText("");
//                        lblMessageSend.setText("");
//                        if (!empty) {
//                            setItem(item);
//                            setText(item.toString());
//
//                            if (item instanceof IDataRequest) {
//                                setTextFill(Color.RED);
//                            } else if (item instanceof ISortedData) {
//                                setTextFill(Color.BLACK);
//                            }
//                        } else {
//                            setItem(null);
//                            setText("");
//                        }
//
//                    }
//                };
//            }
//        });
//        tctStatus.setCellFactory(new Callback<ListView<ITask>, ListCell<ITask>>() {
//
//            @Override
//            public ListCell<ITask> call(ListView<ITask> param) {
//                selectTask();
//                return new ListCell<ITask>() {
//
//                    @Override
//                    protected void updateItem(ITask item, boolean empty) {
//                        super.updateItem(item, empty);
//                        lblMessageUpdate.setText("");
//                        lblMessageTask.setText("");
//                        lblMessageSend.setText("");
//                        if (!empty) {
//                            setItem(item);
//                            setText(item.toString());
//
//                            if (item.getStatus() == TaskStatus.INPROCESS) {
//                                setTextFill(Color.GREEN);
//                            } else {
//                                setTextFill(Color.BLACK);
//                            }
//                        } else {
//                            setItem(null);
//                            setText("");
//                        }
//                    }
//                };
//            };
//        });
        tcsTitle.setCellValueFactory(new PropertyValueFactory<IData, String>("title"));
        tcsRelevance.setCellValueFactory(new PropertyValueFactory<ISortedData, Integer>("relevance"));
        tcsReliability.setCellValueFactory(new PropertyValueFactory<ISortedData, Integer>("reliability"));
        tcsQuality.setCellValueFactory(new PropertyValueFactory<ISortedData, Integer>("quality"));

        tctTitle.setCellValueFactory(new PropertyValueFactory<ITask, String>("title"));
        tctStatus.setCellValueFactory(new PropertyValueFactory<ITask, TaskStatus>("status"));
    }

    /**
     * Configures connectionManager and fills GUI with initial values
     *
     * @param ipAdressServer
     */
    public void configure(ConnectionHandler manager, IUser user) {
        this.connectionManager = manager;
        if (user instanceof IServiceUser) {
            this.user = (IServiceUser) user;
            this.tags.add(this.user.getType());
            tfnSource.setText(this.user.getUsername());
        }
        this.connectionManager.setServicesController(this);

        // Fill GUI with initial values
        try {
            if (this.connectionManager == null) {
                throw new NetworkException("Kon geen data ophalen");
            }

            // Subscribe
            this.connectionManager.registerForUpdates(this.user);

            // Get initial values
            if (chbsRequests.isSelected()) {
                this.connectionManager.getRequests(tags);
            }
            if (chbsData.isSelected()) {
                this.connectionManager.getSortedData(tags);
            }

            this.connectionManager.getSentData(this.user.getUsername());
            this.connectionManager.getTasks(this.user.getUsername());
        } catch (NetworkException nEx) {
            showDialog("Geen verbinding met server", nEx.getMessage(), true);
        }
    }

    /**
     * Displays the sentData that came from connectionManager.getSentData and
     * updates
     *
     * @param sentData
     */
    public void displaySentData(List<IData> sentData) {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                if (sentData != null && !sentData.isEmpty()) {
                    for(IData d : sentData) {
                        if(lvuSentData.getItems().contains(d) || d.getSource().equals(user.getUsername())) {
                            lvuSentData.getItems().remove(d);
                            lvuSentData.getItems().add(d);
                        }
                    }
                    if (lvuSentData.getSelectionModel().getSelectedItem() == null) {
                        lvuSentData.getSelectionModel().selectFirst();
                    }
                }
            }
        });
    }

    /**
     * Displays the task that came from connectionManager.getTasks
     *
     * @param tasks
     */
    public void displayTasks(List<ITask> newTasks) {
        if (newTasks == null) {
            throw new IllegalArgumentException("Geen nieuwe taken om weer te geven");
        }

        Platform.runLater(new Runnable() {

            @Override
            public void run() {

                List<ITask> tasks = tvtTasks.getItems();
                for(ITask t : newTasks) {
                    if(tasks.contains(t)) {
                        tasks.remove(t);
                        tasks.add(t);
                    } else {
                        tasks.add(t);
                    }
                }

                if (tvtTasks.getSelectionModel().getSelectedItem() == null) {
                    tvtTasks.getSelectionModel().selectFirst();
                }
            }
        });
    }

    /**
     * Displays the requests that came from connectionManager.getRequests and
     * updates
     *
     * @param requests
     */
    public void displayRequests(List<IDataRequest> requests) {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                if (chbsRequests.isSelected() && requests != null) {
                    tvsSortedData.getItems().addAll(requests);
                    if (tvsSortedData.getSelectionModel().getSelectedItem() == null) {
                        tvsSortedData.getSelectionModel().selectFirst();
                    }
                }
            }

        });
    }

    /**
     * Displays the sortedData that came from connectionManager.getSortedData
     * and updates
     *
     * @param sortedData
     */
    public void displaySortedData(List<ISortedData> sortedData) {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                if (chbsData.isSelected()) {
                    tvsSortedData.getItems().addAll(sortedData);
                    if (tvsSortedData.getSelectionModel().getSelectedItem() == null) {
                        tvsSortedData.getSelectionModel().selectFirst();
                    }
                    selectSentData();
                }
            }

        });
    }

    /**
     * Displays the requestData that came from connectionManager.getDataItem
     *
     * @param dataItem
     */
    public void displayDataItem(IData dataItem) {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                if (showingDataItem) {
                    lvuSentData.getItems().clear();
                    lvuSentData.getItems().add(dataItem);
                    lvuSentData.getSelectionModel().selectFirst();
                }
            }

        });
    }

    /**
     * Unsubscribe to the information from the connectionManager
     */
    public void close(boolean logout) {
        if (!logout) {
            this.connectionManager.close();
        }
        timer.cancel();
    }

    /**
     * log out on server
     */
    public void logOutClick() {
        try {
            //log out connectionmanager
            this.close(true);
            main.goToLogIn();

        } catch (Exception ex) {
            Logger.getLogger(ServicesController.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Sends the unsortedData to the server
     */
    public void sendUnsortedData() {
        try {
            if (this.connectionManager == null) {
                throw new NetworkException("Kon data niet wegschrijven");
            }

            // Load values from GUI
            String title = tfnTitle.getText();
            String description = tanDescription.getText();
            String source = tfnSource.getText();
            String location = tfnLocation.getText();

            // Make and send new data
            IData data = new UnsortedData(-1, title, description,
                    location, source, Status.NONE);
            this.connectionManager.sendUnsortedData(data);

            // Clear tab
            this.clearSendInfo();

            this.removeAnsweredRequest();

            // Bevestiging tonen
            this.displaySuccessMessage(lblMessageSend, "Verzenden van ongesorteerde data is"
                    + " geslaagd");
        } catch (IllegalArgumentException iaEx) {
            showDialog("Invoer onjuist", iaEx.getMessage(), true);
        } catch (NetworkException nEx) {
            showDialog("Geen verbinding met server", nEx.getMessage(), true);
        }
    }

    /**
     * Clear the tab sendInfo
     */
    private void clearSendInfo() {
        tfnTitle.clear();
        tanDescription.clear();
        tfnLocation.clear();
    }

    /**
     * Removes the request that the user just answered
     */
    private void removeAnsweredRequest() {
        if (this.answeredRequest != null) {
            tvsSortedData.getItems().remove(this.answeredRequest);
            this.answeredRequest = null;
        }
    }

    /**
     * Fills the GUI with information of the selected SentData
     */
    public void selectSentData() {
        IData sentData = (IData) lvuSentData.getSelectionModel().getSelectedItem();
        if (sentData != null) {
            tfuTitle.setText(sentData.getTitle());
            tauDescription.setText(sentData.getDescription());
            tfuSource.setText(sentData.getSource());
            tfuLocation.setText(sentData.getLocation());
        } else {
            tfuTitle.clear();
            tauDescription.clear();
            tfuSource.clear();
            tfuLocation.clear();
        }

        boolean inSorted = false;

        if (sentData != null) {
            for (Object o : tvsSortedData.getItems()) {
                if (o instanceof ISortedData) {
                    ISortedData s = (ISortedData) o;
                    if (s.getId() == sentData.getId()) {
                        inSorted = true;
                    }
                }
            }
        }

        if (inSorted) {
            // No editing allowed
            tfuTitle.setEditable(false);
            tauDescription.setEditable(false);
            tfuLocation.setEditable(false);
            btnuSend.setDisable(true);
            lblErrorMessageUpdate.setText("Al toegevoegd aan gesorteerde data");
        } else {
            tfuTitle.setEditable(true);
            tauDescription.setEditable(true);
            tfuLocation.setEditable(true);
            btnuSend.setDisable(false);
            lblErrorMessageUpdate.setText(null);
        }
    }

    /**
     * Sends an update to the server
     */
    public void sendUpdate() {
        try {
            if (this.connectionManager == null) {
                throw new NetworkException("Kon data niet wegschrijven");
            }

            // Load values from GUI
            IData sentData = (IData) lvuSentData.getSelectionModel().getSelectedItem();
            String title = tfuTitle.getText();
            String description = tauDescription.getText();
            String source = tfuSource.getText();
            String location = tfuLocation.getText();

            // Make and send update
            IData update = new UnsortedData(sentData.getId(), title,
                    description, location, source, Status.NONE);
            this.connectionManager.updateUnsortedData(update);

            this.removeAnsweredRequest();

            // Reset SentData
            if (this.showingDataItem) {
                timer.schedule(new TimerTask() {

                    @Override
                    public void run() {
                        Platform.runLater(new Runnable() {

                            @Override
                            public void run() {
                                if (!exception) {
                                    resetSentData();
                                }
                            }

                        });
                    }

                }, 900);
            }

            // Bevestiging tonen
            this.displaySuccessMessage(lblMessageUpdate, "Het verzenden van de update is geslaagd");
        } catch (IllegalArgumentException iaEx) {
            showDialog("Invoer onjuist", iaEx.getMessage(), true);
        } catch (NetworkException nEx) {
            showDialog("Geen verbinding met server", nEx.getMessage(), true);
        }
    }

    /**
     * Resets the filter of sentData, all sentData becomes visible
     */
    public void resetSentData() {
        try {
            if (this.connectionManager == null) {
                throw new NetworkException("Kon geen data ophalen");
            }

            this.showingDataItem = false;
            this.answeredRequest = null;

            // Clear sentData
            lvuSentData.getItems().clear();

            this.connectionManager.getSentData(this.user.getUsername());
        } catch (NetworkException nEx) {
            showDialog("Geen verbinding met server", nEx.getMessage(), true);
        }
    }

    /**
     * Fills the GUI with the information of the selected data
     */
    public void selectData() {
        IData data = (IData) tvsSortedData.getSelectionModel().getSelectedItem();
        if (data != null) {
            // Fill GUI with information
            tfsTitle.setText(data.getTitle());
            tasDescription.setText(data.getDescription());
            tfsSource.setText(data.getSource());
            tfsLocation.setText(data.getLocation());

            // Determine visibility button requests
            btnAnswerRequest.setVisible(data instanceof IDataRequest);
        } else {
            // Clear GUI
            tfsTitle.clear();
            tasDescription.clear();
            tfsSource.clear();
            tfsLocation.clear();
            btnAnswerRequest.setVisible(false);
        }
    }

    /**
     * Fills the GUI with the information of the tasks
     */
    public void selectTask() {
        ITask task = (ITask) tvtTasks.getSelectionModel().getSelectedItem();
        //all buttons invisible
        btnAcceptTask.setVisible(false);
        btnDismissTask.setVisible(false);
        btnFailed.setVisible(false);
        btnSucceed.setVisible(false);

        if (task != null) {
            this.selectedTask = task;
            if (task instanceof IStep) {
                IStep step = (IStep) task;
                // Fill GUI with information
                tftTaskTitle.setText(step.getTitle());
                tatDescription.setText(step.getDescription());
                tatCondition.setText(step.getCondition());
            } else {
                tftTaskTitle.setText(task.getTitle());
                tatDescription.setText(task.getDescription());
            }

            // Determine visibility button requests
            //if status is sent -- accept and dismiss button
            //if status is accept -- failed, notdone and succeed button
            if (task.getStatus().equals(TaskStatus.SENT)) {
                btnAcceptTask.setVisible(true);
                btnDismissTask.setVisible(true);
            } else if (task.getStatus().equals(TaskStatus.INPROCESS)) {
                btnFailed.setVisible(true);
                btnSucceed.setVisible(true);
            }
        } else {
            // Clear GUI
            this.selectedTask = null;
            tftTaskTitle.clear();
            tatDescription.clear();
            tatCondition.clear();
        }
    }

    /**
     * Changes the display of the sorted data
     *
     * @param evt
     */
    public void changeDisplay(Event evt) {
        CheckBox source = (CheckBox) evt.getSource();
        if (source.isSelected()) {
            try {
                if (this.connectionManager == null) {
                    throw new NetworkException("Kon geen data ophalen");
                }

                // Add
                if (source == chbsData) {
                    this.connectionManager.getSortedData(tags);
                } else if (source == chbsRequests) {
                    this.connectionManager.getRequests(tags);
                }
            } catch (NetworkException nEx) {
                showDialog("Geen verbinding met server", nEx.getMessage(), true);
            }
        } else {
            // Remove
            List<ISortedData> sortedData = new ArrayList<>();
            List<IDataRequest> requests = new ArrayList<>();

            for (Object o : tvsSortedData.getItems()) {
                if (o instanceof ISortedData) {
                    sortedData.add((ISortedData) o);
                } else if (o instanceof IDataRequest) {
                    requests.add((IDataRequest) o);
                }
            }

            if (source == chbsData) {
                // Remove sortedData
                for (ISortedData s : sortedData) {
                    tvsSortedData.getItems().remove(s);
                }
            } else if (source == chbsRequests) {
                // Remove dataRequest
                for (IDataRequest r : requests) {
                    tvsSortedData.getItems().remove(r);
                }
            }
        }
    }

    /**
     * Go to the tab sendUpdate
     */
    public void goToSendUpdate() {
        try {
            IDataRequest request = (IDataRequest) tvsSortedData.getSelectionModel().getSelectedItem();

            if (request != null) {
                if (request.getRequestId() == -1) {
                    // new data
                    tabPane.getSelectionModel().select(tabSendInfo);
                } else {
                    // update
                    if (this.connectionManager == null) {
                        throw new NetworkException("Kon geen data ophalen");
                    }

                    this.showingDataItem = true;

                    this.connectionManager.getDataItem(request.getRequestId());
                    tabPane.getSelectionModel().select(tabUpdateInfo);
                }
            }
        } catch (NetworkException nEx) {
            showDialog("Geen verbinding met server", nEx.getMessage(), true);
        }
    }

    /**
     * Accept Task
     */
    public void acceptTask() {
        try {
            if (selectedTask != null) {
                selectedTask.setStatus(TaskStatus.INPROCESS);
                this.connectionManager.updateTask(selectedTask);
                btnAcceptTask.setVisible(false);
                btnDismissTask.setVisible(false);
                btnFailed.setVisible(true);
                btnSucceed.setVisible(true);
                //resetTaskData();
                //dismiss task succeed message
                this.displaySuccessMessage(lblMessageTask, "Het accepteren van de taak is gelukt.");
            } else {
                showDialog("Taak selectie", "Geen taak geselecteerd", false);
            }

        } catch (Exception nEx) {
            showDialog("Geen verbinding met server", nEx.getMessage(), true);
        }
//        
    }

    /**
     * Dismiss Task with argument
     */
    public void dismissTask() {
        String argument = showArgumentDialog();
        if (argument == null) {
            return;
        }
        if (argument.isEmpty() || argument.equals(" ")) {
            showDialog("Argument", "Er moet een argument ingevuld worden", true);
        } else {
            if (selectedTask != null) {
                selectedTask.setStatus(TaskStatus.REFUSED);
                selectedTask.setDeclineReason(argument);
                this.connectionManager.updateTask(selectedTask);
                tvtTasks.getItems().remove(selectedTask);
                //resetTaskData();
                //dismiss task succeed message
                this.displaySuccessMessage(lblMessageTask, "Het weigeren van de taak is gelukt.");
            } else {
                showDialog("Taak selectie", "Geen taak geselecteerd", false);
            }
        }
    }

    /**
     * Status Task to failed
     */
    public void failedTask() {
        try {
            if (selectedTask != null) {
                selectedTask.setStatus(TaskStatus.FAILED);
                this.connectionManager.updateTask(selectedTask);
                tvtTasks.getItems().remove(selectedTask);
                //resetTaskData();
                //dismiss task succeed message
                this.displaySuccessMessage(lblMessageTask, "Het veranderen van de status is gelukt.");
            } else {
                showDialog("Taak selectie", "Geen taak geselecteerd", false);
            }

        } catch (Exception nEx) {
            showDialog("Geen verbinding met server", nEx.getMessage(), true);
        }
    }

    /**
     * Status Task to succeed
     */
    public void succeedTask() {
        try {
            if (selectedTask != null) {
                selectedTask.setStatus(TaskStatus.SUCCEEDED);
                this.connectionManager.updateTask(selectedTask);
                tvtTasks.getItems().remove(selectedTask);
                //resetTaskData();
                //dismiss task succeed message
                this.displaySuccessMessage(lblMessageTask, "Het veranderen van de status is gelukt.");
            } else {
                showDialog("Taak selectie", "Geen taak geselecteerd", false);
            }

        } catch (Exception nEx) {
            showDialog("Geen verbinding met server", nEx.getMessage(), true);
        }
    }

    public void showDialog(String title, String melding, boolean warning) {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                exception = true;

                Alert alert = null;

                if (warning) {
                    alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Foutmelding");
                } else {
                    alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Melding");
                }

                if (!title.isEmpty()) {
                    alert.setHeaderText(title);
                } else {
                    alert.setHeaderText(null);
                }

                alert.setContentText(melding);
                alert.showAndWait();
            }

        });
    }

    public String showArgumentDialog() {
        TextInputDialog dialog = new TextInputDialog("Argument");
        dialog.setTitle("Argument");
        dialog.setContentText("Voer een argument in:");

        // Traditional way to get the response value.
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            return result.get();
        } else {
            return null;
        }
    }

    private void displaySuccessMessage(Label lblMessage, String message) {
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                Platform.runLater(new Runnable() {

                    @Override
                    public void run() {
                        if (!exception) {
                            lblMessage.setText(message);
                        } else {
                            exception = false;
                        }
                    }

                });
            }

        }, 1000);

        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                Platform.runLater(new Runnable() {

                    @Override
                    public void run() {
                        lblMessage.setText(null);
                    }

                });
            }

        }, 6000);
    }
}
