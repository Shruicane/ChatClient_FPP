package org.luma.server.settings;

import org.luma.server.database.MySQLConnection;
import org.luma.server.database.MySQLDataBase;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ServerMain {
    public static void main(String[] args) throws SQLException {
        MySQLDataBase mySQLDataBase = new MySQLDataBase("127.0.0.1", "3306", "root", "", "test");


    }
}
