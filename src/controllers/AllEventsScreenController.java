package controllers;

import client.DBConnector;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import models.Event;
import models.EventSchedule;
import models.Role;
import models.Volunteer;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class AllEventsScreenController implements Initializable {

    private EventSchedule selectedSched;
    private Role selectedRole;
    private Volunteer vol;
    private SubmitConcernScreenController submitConcernScreenController;

    public VBox vboxVol1;
    public VBox vboxVol2;
    public VBox vboxVol3;
    @FXML
    private HBox windowHeader;

    @FXML
    private TextField eventSearchField;

    @FXML
    private Button eventsTypeButton;

    @FXML
    private Label name_label;

    @FXML
    private Label editInfo_button;

    @FXML
    private Label logout_button;

    @FXML
    private Label ongoingEvents_toggle;

    @FXML
    private Label finishedEvents_toggle;

    @FXML
    private VBox events_vbox;

    @FXML
    private Label userRole_label;

    @FXML
    private VBox mainCardHeader;

    @FXML
    private Label eventName_label;

    @FXML
    private Label eventDesc_label;

    @FXML
    private ComboBox<EventSchedule> sched_comboBox;

    @FXML
    private ComboBox<Role> roles_comboBox;

    @FXML
    private Label rolesDesc_label;

    @FXML
    private Button join_button;

    @FXML
    private Button concern_button;

    @FXML
    private Label volCount_label;

    @FXML
    private Label programReq_label;

    @FXML
    private Label yearReq_label;

    @FXML
    private Label location_label;

    @FXML
    private Label startDateTime_label;

    @FXML
    private Label endDateTime_label;

    public void setVol(Volunteer vol) {
        this.vol = vol;
    }

    @FXML
    void onEditInfo(MouseEvent event) {

    }

    @FXML
    void onFinishedToggle(MouseEvent event) {
        events_vbox.getChildren().clear();
        initializeEventsPanel(Objects.requireNonNull(DBConnector.getFinishedEvents(vol.getVolId())));
        join_button.setDisable(true);
    }

    @FXML
    void onJoinEvent(ActionEvent event) {

    }

    @FXML
    void onLogout(MouseEvent event) {
        // TODO: Are you sure panel
        System.exit(0);
    }

    @FXML
    void onOngoingToggle(MouseEvent event) {
        initializeEventsPanel(Objects.requireNonNull(DBConnector.getAllOngoingEvents(vol.getVolId())));
        join_button.setDisable(false);
    }

    @FXML
    void onRolesChange(ActionEvent event) {
        try {
            selectedRole = roles_comboBox.getSelectionModel().getSelectedItem();
            System.out.println(selectedRole);
            rolesDesc_label.setText(selectedRole.getDescription());
            String program = "None";
            if (!selectedRole.getDegree_program().equals(""))
                program = selectedRole.getDegree_program();
            programReq_label.setText("Degree Program: " + program);
            if (selectedRole.getYear() != 0)
                yearReq_label.setText("Year: " + selectedRole.getYear());
            else
                yearReq_label.setText("Year: " + "None");
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onScheduleChange(ActionEvent event) {
        try {
            selectedSched = sched_comboBox.getSelectionModel().getSelectedItem();
            roles_comboBox.setItems(FXCollections.observableList(selectedSched.getRoles()));
            roles_comboBox.getSelectionModel().selectFirst();

            location_label.setText(selectedSched.getLocation());
            startDateTime_label.setText(selectedSched.getStart().toString());
            endDateTime_label.setText(selectedSched.getEnd().toString());

            loadVolunteers(selectedSched.getParticipants(), selectedSched.getSchedId());
        }catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onSubmitConcern(ActionEvent event) {
        try {
            showSubmitConcernScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    private void showSubmitConcernScreen() throws Exception{
        try {
            FXMLLoader loader = new FXMLLoader(getClass()
                    .getResource("../resources/view/SubmitConcernScreen.fxml"));
            Stage stage = new Stage(StageStyle.UNDECORATED);
            stage.setScene(new Scene(loader.load()));
            submitConcernScreenController = loader.getController();
            submitConcernScreenController.setCurrentlyChosenEvent(eventName_label.getText());

            stage.show();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void loadVolunteers(ArrayList<String> volunteers, int schedId) {
//        ArrayList<String> volunteers = DBConnector.getVolunteers(schedId);
        int division = Math.round(((float)volunteers.size()/3));
        System.out.println(division + " " + volunteers.size());

        vboxVol1.getChildren().clear();
        vboxVol2.getChildren().clear();
        vboxVol3.getChildren().clear();

        for (int i = 0; i != division ; i++) {
            Label vol = new Label(volunteers.get(i));
            vboxVol1.getChildren().add(vol);
        }

        for (int i = division; i < division*2; i++) {
            Label vol = new Label(volunteers.get(i));
            vboxVol2.getChildren().add(vol);
        }

        for (int i = division*2; i != volunteers.size(); i++) {
            Label vol = new Label(volunteers.get(i));
            vboxVol3.getChildren().add(vol);
        }

        int volLimit = DBConnector.getVolunteerLimit(schedId);
        volCount_label.setText(volunteers.size() + "/" + volLimit);
    }

    private void initializeEventsPanel(ArrayList<Event> events) {
        assert events != null;
        try {
            Platform.runLater(() -> events_vbox.getChildren().clear());
            for (Event e : events) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("../resources/view/EventCard.fxml"));
                Node node = loader.load();
                String date = "date";

                // set onMouseClick
                node.setOnMouseClicked((event) -> {
                    e.setSchedules(DBConnector.getEventSchedules(e.getEvent_id()));
                    System.out.println(e.getSchedules());
                    setRightCardProperties(e);
                });

                // set card properties
                for (Node component : ((VBox)(((Pane) node).getChildren()).get(0)).getChildren()) {
                    if (component.getId().equals("name_label"))
                        ((Label) component).setText(e.getName());
                    if (component.getId().equals("date_label"))
                        ((Label) component).setText(date);
                }

                Platform.runLater(() -> events_vbox.getChildren().add(node));
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setRightCardProperties(Event e) {
        eventName_label.setText(e.getName());
        eventDesc_label.setText(e.getDescription());
        sched_comboBox.setItems(FXCollections.observableList(e.getSchedules()));
        sched_comboBox.getSelectionModel().selectFirst();
        selectedSched = sched_comboBox.getSelectionModel().getSelectedItem();
        roles_comboBox.setItems(FXCollections.observableList(
                sched_comboBox.getSelectionModel().getSelectedItem().getRoles()));
        roles_comboBox.getSelectionModel().selectFirst();
        selectedRole = roles_comboBox.getSelectionModel().getSelectedItem();

        List<Map<String, String>> list = DBConnector.getVolunteerSchedRole(vol.getVolId(), e.getEvent_id());
        String label = "You are not participating in any schedule";
        if (list.size() == 0)
            userRole_label.setText(label);
        else
            label = "";
        for (Map<String, String> m : list) {
            label += "schedule " + m.get("schedId") + " - " + m.get("role") + "\n";
        }
        userRole_label.setText(label);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeEventsPanel(Objects.requireNonNull(DBConnector.getAllOngoingEvents(vol.getVolId())));
        name_label.setText(vol.getFirstName() + " " + vol.getLastName());
    }

}
