package org.luma.server.database;

import org.luma.server.frontend.controller.Controller;

import javax.security.auth.login.CredentialException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class UserManagement {

    private MySQLConnection mySQLConnection;
    private Controller controller;
    private MySQLDataBase mySQLDataBase;

    private DatabaseTMP database;

    public UserManagement(MySQLConnection mySQLConnection, Controller controller, MySQLDataBase mySQLDataBase) {
        this.mySQLDataBase = mySQLDataBase;
        this.mySQLConnection = mySQLConnection;
        this.controller = controller;
        this.database = mySQLConnection.getDatabase();
    }

    public boolean createUser(String username, String password){
        String query = "INSERT INTO `userdata` (`UUID`, `Username`, `Password`, `Chatlist`, `BanStatus`) VALUES ('" + UUID.randomUUID() + "', '" + username + "', '" + password + "', '', '0')";

        if(!userExists(username)){
            mySQLDataBase.executeUpdate(query);
        }else{
            return false;
        }
        //Check if Username already exists
        //create Database entry
        return true;
    }

    public boolean loginUser(String username, String password){

        //Password und Username abgleichen
        String query = "SELECT * FROM `userdata` WHERE Username=\"" + username + "\" AND Password=\"" + password + "\"";
        ResultSet rs = mySQLDataBase.executeQuery(query);
        try {
            return rs.next();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
    }

    public boolean userExists(String username){
        //Username abgleichen
        String query = "SELECT * FROM `userdata` WHERE Username=\"" + username + "\"";
        ResultSet rs = mySQLDataBase.executeQuery(query);
        try {
            return rs.next();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
    }

    public void permaBanUser(String username){
        String query = "UPDATE userdata SET BanStatus = 3  WHERE Username=\"" + username + "\"";
        this.removeBan(username);
        mySQLDataBase.executeUpdate(query);
    }

    public void tempBanUser(String username, String expiringDate){
        String query = "UPDATE userdata SET BanStatus = 2, BanExpiry=\"" + expiringDate + "\" WHERE Username=\"" + username + "\"";
        this.removeBan(username);
        mySQLDataBase.executeUpdate(query);
    }

    public void removeBan(String username){
        String query = "UPDATE userdata SET BanStatus = 0 AND BanExpiry=\"\" WHERE Username=\"" + username + "\"";
        mySQLDataBase.executeUpdate(query);
    }

    public void warnUser(String username){
        String query = "UPDATE userdata SET BanStatus = 1  WHERE Username=\"" + username + "\"";
        this.removeBan(username);
        mySQLDataBase.executeUpdate(query);
    }

    public boolean exists(String username){
        return database.existsUser(username);
    }

    public boolean deleteUser(String username){
        if(userExists(username)){
            String query = "DELETE FROM userdata WHERE Username='" + username + "'";
            mySQLDataBase.executeUpdate(query);
        }else{
            return false;
        }
        return true;
    }

    /*
    public boolean createUser(String username, String password){
        if(!exists(username)){
            database.addUser(username, password);
            controller.updateUser(username, password, false);
            return true;
        }
        return false;
    }*/

    /*public boolean deleteUser(String username){
        if(exists(username)) {
            database.removeUser(username);
            return true;
        }
        return false;
    }*/

    /*
    public boolean loginUser(String username, String password){
        return database.checkPassword(username, password) && !database.isBanned(username);
    }*/

    public boolean banUser(String username){
        if(exists(username)){
            database.banUser(username);
            return true;
        }
        return false;
    }

    public boolean unbanUser(String username){
        if(exists(username)){
            database.unbanUser(username);
            return true;
        }
        return false;
    }

    public BanStatus isBanned(String username){
        String query = "SELECT * FROM `userdata` WHERE Username=\"" + username + "\"";
        ResultSet rs = mySQLDataBase.executeQuery(query);
        try {
            int status = rs.getInt("BanStatus");
            switch (status){
                case 0:
                    return BanStatus.NONE;
                case 1:
                    return BanStatus.WARNED;
                case 2:
                    return BanStatus.TEMPBAN;
                case 3:
                    return BanStatus.PERMABAN;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return BanStatus.NONE;
    }

    public ArrayList<Integer> getAllGroups(String username){
        ArrayList<Integer> result = new ArrayList<>();
        for(int groupID:database.getAllGroups())
            if(database.getAllUsers(groupID).contains(username))
                result.add(groupID);
        return result;
    }

    public Map<String, ArrayList<String>> getAllGroupsWithUsers(String username) {
        Map<String, ArrayList<String>> result = new HashMap<>();
        ArrayList<Integer> groups = getAllGroups(username);
        for(Integer groupID:groups){
            result.put(database.getName(groupID), database.getAllUsers(groupID));
        }
        return result;
    }

}
