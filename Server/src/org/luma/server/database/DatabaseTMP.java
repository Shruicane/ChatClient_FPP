package org.luma.server.database;

import java.lang.reflect.Array;
import java.util.*;

public class DatabaseTMP {

    private Map<String, String> user;
    private Map<Integer, ArrayList<String>> groups;
    private Map<Integer, String> groupNames;
    private Set<String> bannedUser;


    public DatabaseTMP() {
        user = new HashMap<>();
        groups = new HashMap<>();
        bannedUser = new HashSet<>();
        groupNames = new HashMap<>();
    }

    public void addUser(String username, String password) {
        user.put(username, password);
    }

    public void removeUser(String username) {
        user.remove(username);
    }

    public boolean existsUser(String username) {
        return user.containsKey(username);
    }

    public boolean checkPassword(String username, String password) {
        return user.get(username).matches(password);
    }


    public void banUser(String username) {
        bannedUser.add(username);
    }

    public void unbanUser(String username) {
        bannedUser.remove(username);
    }

    public boolean isBanned(String username) {
        return bannedUser.contains(username);
    }


    public int addGroup(String name) {
        for(int i=0; i<groups.size(); i++){
            if(!groups.containsKey(i)) {
                groups.put(i, new ArrayList<String>());
                groupNames.put(i, name);
                return i;
            }
        }
        groupNames.put(groups.size(), name);
        groups.put(groups.size(), new ArrayList<String>());
        return groups.size() - 1;
    }

    public void removeGroup(Group group) {
        groups.remove(group.getId());
        groupNames.remove(group.getId());
    }

    public void addUserToGroup(Group group, String username){
        groups.get(group.getId()).add(username);
    }

    public void removeUserFromGroup(Group group, String username){
        groups.get(group.getId()).remove(username);
    }

    public String getName(int groupID){
        return groupNames.get(groupID);
    }

    public int getID(String name){
        for(int groupID:groups.keySet()){
            if(getName(groupID).matches(name))
                return groupID;
        }
        return -1;
    }

    public ArrayList<String> getAllUsers(int id){
        return groups.get(id);
    }

    public Set<Integer> getAllGroups(){
        return groups.keySet();
    }

    public void changeGroupName(int id, String name) {
        groupNames.put(id, name);
    }

    public Map<String, String> getUser() {
        return user;
    }

    public Map<Integer, ArrayList<String>> getGroups() {
        return groups;
    }

    public Map<Integer, String> getGroupNames() {
        return groupNames;
    }

    public Set<String> getBannedUser() {
        return bannedUser;
    }

    public void setUser(Map<String, String> user) {
        this.user = user;
    }

    public void setGroups(Map<Integer, ArrayList<String>> groups) {
        this.groups = groups;
    }

    public void setGroupNames(Map<Integer, String> groupNames) {
        this.groupNames = groupNames;
    }

    public void setBannedUser(Set<String> bannedUser) {
        this.bannedUser = bannedUser;
    }
}
