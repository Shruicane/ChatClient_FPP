package org.luma.server.database;

public enum GroupType {

    PRIVATE(0), GROUP(1);

    private final int privacy;

    GroupType(int groupType){
        privacy = groupType;
    }

    public int getBanLvl(){
        return privacy;
    }

    public GroupType resolvebanLvl(int i){
        return GroupType.GROUP;
    }

}
