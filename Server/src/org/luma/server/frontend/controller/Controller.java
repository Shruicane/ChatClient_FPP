package org.luma.server.frontend.controller;

import com.sun.xml.internal.bind.marshaller.NioEscapeHandler;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import org.luma.server.database.*;
import org.luma.server.network.ClientManager;
import org.luma.server.network.Logger;
import org.luma.server.network.ServerMain;
import org.luma.server.settings.ServerSettings;
import org.luma.server.settings.Settings;

import java.util.*;
import java.util.function.Consumer;

public class Controller {


    //<editor-fold desc="FXML Bindings">

    @FXML
    private TextArea logArea;

    @FXML
    private TableView<User> userTableView;
    @FXML
    private TableColumn<User, String> userTableUsername;
    @FXML
    private TableColumn<User, Password> userTablePassword;

    @FXML
    private Button warnUserButton;

    @FXML
    private Button tempBanButton;

    @FXML
    private ToggleButton permanentBanButton;

    @FXML
    private ToggleButton showPwdButton;

    @FXML
    private ListView<Group> groupList;

    @FXML
    private ListView<String> userList;

    @FXML
    private Button removeUserButton;

    @FXML
    private Button addUserButton;

    @FXML
    private Button editGroupButton;

    @FXML
    private Button deleteGroupButton;

    @FXML
    private Button createGroupButton;

    @FXML
    private TextField ipAddressTextField;

    @FXML
    private TextField portTextField;

    @FXML
    private TextField databaseTextField;

    @FXML
    private TextField dataBaseUserTextField;

    @FXML
    private TextField databasePasswordTextField;

    @FXML
    private Button saveSettingsBtn;

    @FXML
    private Button discardSettingsBtn;

    //</editor-fold>

    //<editor-fold desc="User Management Tab">
    @FXML
    private void onClickWarnUserButton() {
        if (getSelectedUser() != null) {
            showPopup("Warn!", "Warning User: " + getSelectedUser().getName(), r -> {
                cm.warn(getSelectedUser().getName(), r);
                log.administration("Warned >> " + getSelectedUser().getName() + ": " + r);
            });
        }
    }

    @FXML
    private void onClickTempBanButton() {
        if (getSelectedUser() != null) {
            showPopup("Kick!", "Kicking User: " + getSelectedUser().getName(), r -> {
                if (cm.kick(getSelectedUser().getName(), r, "You were Kicked!"))
                    log.administration("Kicked >> " + getSelectedUser().getName() + ": " + r);
            });
        }
    }

    @FXML
    private void onClickPermanentBanButton() {
        if (getSelectedUser() != null) {
            if (cm.isBanned(getSelectedUser().getName())) {
                cm.unban(getSelectedUser().getName());
                log.administration("Unbanned >> " + getSelectedUser().getName());
            } else {
                showPopup("Ban!", "Banning User: " + getSelectedUser().getName(), r -> {
                    cm.ban(getSelectedUser().getName(), r);
                    log.administration("Banned >> " + getSelectedUser().getName() + ": " + r);
                });
            }
            onClickUserTable();

            ioManager.saveBannedUser();
        }
    }

    @FXML
    private void onClickShowPwdButton() {
        ObservableList<User> items = userTableView.getItems();

        for (int i = 0; i < items.size(); i++) {
            User user = items.get(i);
            user.passwordProperty().getValue().show(showPwdButton.isSelected());
            System.out.println(user.passwordProperty().getValue().toString());
            items.set(i, user);
        }

        userTableView.setItems(items);
    }

    @FXML
    private void onClickUserTable() {
        if (getSelectedUser() != null) {
            boolean isBanned = cm.isBanned(getSelectedUser().getName());
            permanentBanButton.setSelected(isBanned);
        }
    }

    private User getSelectedUser() {
        return userTableView.getSelectionModel().getSelectedItems().get(0);
    }
    //</editor-fold>

    //<editor-fold desc="Group Management Tab">
    @FXML
    private void onClickRemoveUserButton() {
        if (getSelectedGroup() != null && getSelectedGroupUser() != null) {
            String username = getSelectedGroupUser();
            Group group = getSelectedGroup();

            groupManager.removeUser(group, username);
            ObservableList<String> user = userList.getItems();
            user.remove(username);

            userList.setItems(user);
            log.administration("Removed User >> " + group + " -> " + username);

            updateListUser();
            sendUpdateInfo(username, "group", userManager.getAllGroupsWithUsers(username));
            cm.message(group.getName(), username, username + " has left the Room!");

            ioManager.saveGroups();
        }
    }

    @FXML
    private void onClickAddUserButton() {
        if (getSelectedGroup() != null) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setHeaderText("Please Enter Username:");
            dialog.setTitle("Add User");

            Optional<String> result = dialog.showAndWait();

            result.ifPresent(username -> {
                if (userManager.exists(username) && !groupManager.getAllUsers(getSelectedGroup().getId()).contains(username)) {
                    groupManager.addUser(getSelectedGroup(), username);
                    ObservableList<String> user = userList.getItems();
                    user.add(username);

                    userList.setItems(user);
                    log.administration("Added User >> " + getSelectedGroup() + " <- " + username);

                    updateListUser();
                    sendUpdateInfo(username, "group", userManager.getAllGroupsWithUsers(username));
                    cm.message(getSelectedGroup().getName(), username, username + " has joined the Room!");

                    ioManager.saveGroups();
                }
            });
        }

        // TODO: maybe add drop down menu to dialog
    }

    @FXML
    private void onClickDeleteGroupButton() {
        if (getSelectedGroup() != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Do you really want to delete this Group?", ButtonType.YES, ButtonType.NO);

            ButtonType result = alert.showAndWait().orElse(ButtonType.NO);

            if (ButtonType.YES.equals(result)) {
                Group group = getSelectedGroup();
                ArrayList<String> affectedUsers = groupManager.getAllUsers(group.getId());
                groupManager.deleteGroup(group);
                ObservableList<Group> groups = groupList.getItems();
                groups.remove(group);

                groupList.setItems(groups);
                log.administration("Deleted Group >> " + group);

                updateListGroup();
                for (String user : affectedUsers)
                    sendUpdateInfo(user, "group", userManager.getAllGroupsWithUsers(user));

                ioManager.saveGroups();
                ioManager.saveGroupNames();
            }
        }
    }

    @FXML
    private void onClickCreateGroupButton() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText("Please neter Group Name:");
        dialog.setTitle("Create Group");

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(r -> {
            if (groupList.getItems().filtered(group -> group.getName().equals(r)).size() == 0) {
                ObservableList<Group> groups = groupList.getItems();
                Group group = new Group(groupManager.createGroup(r), r);
                groups.add(group);

                groupList.setItems(groups);

                log.administration("Created Group >> " + r);

                ArrayList<String> affectedUsers = groupManager.getAllUsers(group.getId());
                updateListGroup();
                for (String user : affectedUsers)
                    sendUpdateInfo(user, "group", userManager.getAllGroupsWithUsers(user));
            } else {
                dialog.close();
                Alert alert = new Alert(AlertType.ERROR);
                alert.setHeaderText("This Name already exists!");
                alert.showAndWait();
            }

            ioManager.saveGroups();
            ioManager.saveGroupNames();
        });
    }

    @FXML
    private void onClickEditGroupButton() {
        if (getSelectedGroup() != null) {
            showPopup("Change Name", "Please Enter new Name:", name -> {
                if (groupList.getItems().filtered(group -> group.getName().equals(name)).size() == 0) {
                    log.administration("Edited Group >> " + getSelectedGroup() + " -> " + name);
                    groupManager.changeName(getSelectedGroup().getId(), name);
                    getSelectedGroup().setName(name);
                    groupList.refresh();

                    ArrayList<String> affectedUsers = groupManager.getAllUsers(getSelectedGroup().getId());
                    for (String user : affectedUsers)
                        sendUpdateInfo(user, "group", userManager.getAllGroupsWithUsers(user));
                } else {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setHeaderText("This Name already exists!");
                    alert.showAndWait();
                }
            });

            ioManager.saveGroupNames();
        }
    }

    @FXML
    private void updateListGroup() {
        if (getSelectedGroup() != null) {
            ObservableList<String> user = userList.getItems();
            if (user == null) {
                user = emptyDummyList;
            }
            user.clear();
            user.addAll(groupManager.getAllUsers(getSelectedGroup().getId()));
            userList.setItems(user);

            addUserButton.setVisible(true);
            removeUserButton.setVisible(getSelectedGroupUser() != null);
        } else {
            ObservableList<String> user = null;
            userList.setItems(user);

            addUserButton.setVisible(false);
            removeUserButton.setVisible(false);
        }
    }

    @FXML
    private void updateListUser() {
        removeUserButton.setVisible(getSelectedGroupUser() != null);
    }

    private Group getSelectedGroup() {
        return groupList.getSelectionModel().getSelectedItems().get(0);
    }

    private String getSelectedGroupUser() {
        return userList.getSelectionModel().getSelectedItems().get(0);
    }
    //</editor-fold>

    //<editor-fold desc="Settings Tab">
    @FXML
    private void onClickSaveBtn() {

        String contentText = "";
        if (!Settings.getIpAddress().equals(this.ipAddressTextField.getText())) {
            contentText += "IP-Address: " + Settings.getIpAddress() + " -> " + this.ipAddressTextField.getText() + "\n";
        }
        if (!Settings.getPort().equals(this.portTextField.getText())) {
            contentText += "Port: " + Settings.getPort() + " -> " + this.portTextField.getText() + "\n";
        }
        if (!Settings.getDatabase().equals(this.databaseTextField.getText())) {
            contentText += "Database name: " + Settings.getDatabase() + " -> " + this.databaseTextField.getText() + "\n";
        }
        if (!Settings.getDatabaseUser().equals(this.dataBaseUserTextField.getText())) {
            contentText += "Database-User: " + Settings.getDatabaseUser() + " -> " + this.dataBaseUserTextField.getText() + "\n";
        }
        if (!Settings.getDatabasePassword().equals(this.databasePasswordTextField.getText())) {
            contentText += "Database-Password: " + Settings.getDatabasePassword() + " -> " + this.databasePasswordTextField.getText() + "\n";
        }
        Settings.saveSettings(new ServerSettings(
                this.ipAddressTextField.getText(), this.portTextField.getText(), this.databaseTextField.getText(),
                this.dataBaseUserTextField.getText(), this.databasePasswordTextField.getText()));

        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setContentText(contentText);
        alert.setHeaderText("Settings saved.");
        alert.showAndWait();

        log.administration("Settings >> Saved");
    }

    @FXML
    private void onClickDiscardBtn() {
        this.ipAddressTextField.setText(Settings.getIpAddress());
        this.portTextField.setText(Settings.getPort());
        this.databaseTextField.setText(Settings.getDatabase());
        this.dataBaseUserTextField.setText(Settings.getDatabaseUser());
        this.databasePasswordTextField.setText(Settings.getDatabasePassword());
    }
    //</editor-fold>

    @FXML
    private void initialize() {
        this.ipAddressTextField.setText(Settings.getIpAddress());
        this.portTextField.setText(Settings.getPort());
        this.databaseTextField.setText(Settings.getDatabase());
        this.dataBaseUserTextField.setText(Settings.getDatabaseUser());
        this.databasePasswordTextField.setText(Settings.getDatabasePassword());

        mySQLConnection = new MySQLConnection(this);
        userManager = mySQLConnection.getUserManager();
        groupManager = mySQLConnection.getGroupManager();
        ioManager = new IOManagement(groupManager.getDatabase());
        server = new ServerMain(this, ioManager, mySQLConnection);
        cm = server.getClientManager();
        log = server.getLogger();

        ioManager.loadAll();

        logArea.setFont(Font.font("Monospaced", FontWeight.MEDIUM, FontPosture.REGULAR, 15));

        userTableUsername.setCellValueFactory(features -> features.getValue().usernameProperty());
        userTablePassword.setCellValueFactory(features -> features.getValue().passwordProperty());

        emptyDummyList = userList.getItems();

        initComponents();
    }

    private void initComponents() {
        //User Tableview
        ObservableList<User> userList = userTableView.getItems();
        Map<String, String> user = groupManager.getDatabase().getUser();
        for (Map.Entry<String, String> entry : user.entrySet())
            userList.add(new User(entry.getKey(), entry.getValue(), false, false));
        userTableView.setItems(userList);

        //Group Tableview
        ObservableList<Group> groupTableView = groupList.getItems();
        Map<Integer, String> groups = groupManager.getDatabase().getGroupNames();
        for (Map.Entry<Integer, String> entry : groups.entrySet())
            groupTableView.add(new Group(entry.getKey(), entry.getValue()));
        groupList.setItems(groupTableView);
    }


    MySQLConnection mySQLConnection;
    UserManagement userManager;
    GroupManagement groupManager;
    IOManagement ioManager;
    ServerMain server;
    ClientManager cm;
    Logger log;


    private ObservableList<String> emptyDummyList;

    private void showPopup(String title, String msg, Consumer<? super String> consumer) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText(msg);
        dialog.setTitle(title);

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(consumer);
    }

    public void updateLogArea(String log) {
        try {
            logArea.appendText(log + "\n");
        } catch (Exception e) {
            System.out.println("ERROR: org/luma/server/frontend/controller/Controller.java -> updateLogArea()");
            e.printStackTrace();
        }
    }

    public void updateUser(String username, String password, boolean online) {
        ObservableList<User> items = userTableView.getItems();
        items.add(new User(username, password, online, showPwdButton.isSelected()));
        userTableView.setItems(items);
    }

    public void sendUpdateInfo(String username, String type, Object data) {
        cm.sendUpdateInfo(username, type, data);
    }
}
