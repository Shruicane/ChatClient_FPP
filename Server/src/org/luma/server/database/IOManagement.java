package org.luma.server.database;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class IOManagement {

    private final DatabaseTMP database;

    private final String logPath = "logs/";
    private final String dataPath = "data/";

    public IOManagement(DatabaseTMP database) {
        this.database = database;
        if (!fileExists(logPath + "latest.txt"))
            createFile(logPath + "latest.txt");
        else {
            String date = getLastModified(logPath + "latest.txt");
            renameFile(logPath + "latest.txt", logPath + date + "-" + (countDateFiles(date) + 1) + ".txt");
        }
    }

    public void saveUser() {
        try {
            ArrayList<String> out = new ArrayList<>();
            createFile(dataPath + "user.dat");
            Map<String, String> user = database.getUser();
            for (Map.Entry<String, String> entry : user.entrySet())
                out.add(entry.getKey() + ":" + entry.getValue());
            write(out, dataPath + "user.dat");
        } catch (Exception e) {
            System.err.println("Failed Saving User List!");
        }
    }

    public void saveGroups() {
        try {
            ArrayList<String> out = new ArrayList<>();
            createFile(dataPath + "groups.dat");
            Map<Integer, ArrayList<String>> groups = database.getGroups();
            for (Map.Entry<Integer, ArrayList<String>> entry : groups.entrySet()) {
                StringBuilder str = new StringBuilder();
                for (String item : entry.getValue())
                    str.append(",").append(item);
                out.add("" + entry.getKey() + ":" + (str.length() > 0 ? str.deleteCharAt(0).toString() : ""));
            }
            write(out, dataPath + "groups.dat");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed Saving Groups List!");
        }
    }

    public void saveGroupNames() {
        try {
            ArrayList<String> out = new ArrayList<>();
            createFile(dataPath + "groupNames.dat");
            Map<Integer, String> groupNames = database.getGroupNames();
            for (Map.Entry<Integer, String> entry : groupNames.entrySet())
                out.add(entry.getKey() + ":" + entry.getValue());
            write(out, dataPath + "groupNames.dat");
        } catch (Exception e) {
            System.err.println("Failed Saving GroupNames List!");
        }
    }

    public void saveBannedUser() {
        try {
            ArrayList<String> out = new ArrayList<>();
            createFile(dataPath + "bannedUser.dat");
            Set<String> bannedUser = database.getBannedUser();
            out = new ArrayList<String>(bannedUser);
            write(out, dataPath + "bannedUser.dat");
        } catch (Exception e) {
            System.err.println("Failed Saving BannedUser List!");
        }
    }

    public void saveAll() {
        saveUser();
        saveGroups();
        saveGroupNames();
        saveBannedUser();
    }

    public void loadUser() {
        try {
            ArrayList<String> in = read(dataPath + "user.dat");
            Map<String, String> out = new HashMap<>();
            for (String line : in) {
                String[] str = line.split(":");
                out.put(str[0], str[1]);
            }
            database.setUser(out);
        } catch (Exception e) {
            System.err.println("Failed Loading User List!");
        }
    }

    public void loadGroups() {
        try {
            ArrayList<String> in = read(dataPath + "groups.dat");
            Map<Integer, ArrayList<String>> out = new HashMap<>();
            for (String line : in) {
                String[] str = line.split(":");
                out.put(Integer.parseInt(str[0]), str.length > 1 ? new ArrayList<>(Arrays.asList(str[1].split(","))) : new ArrayList<>());
            }
            database.setGroups(out);
        } catch (Exception e) {
            System.err.println("Failed Loading Groups List!");
        }
    }

    public void loadGroupNames() {
        try {
            ArrayList<String> in = read(dataPath + "groupNames.dat");
            Map<Integer, String> out = new HashMap<>();
            for (String line : in) {
                String[] str = line.split(":");
                out.put(Integer.parseInt(str[0]), str[1]);
            }
            database.setGroupNames(out);
        } catch (Exception e) {
            System.err.println("Failed Loading GroupNames List!");
        }
    }

    public void loadBannedUser() {
        try {
            ArrayList<String> in = read(dataPath + "bannedUser.dat");
            Set<String> out = new HashSet<>(in);
            database.setBannedUser(out);
        } catch (Exception e) {
            System.err.println("Failed Loading BannedUser List!");
        }
    }

    public void loadAll() {
        loadUser();
        loadGroups();
        loadGroupNames();
        loadBannedUser();
    }

    public void writeLog(String log) {
        if (!fileExists(logPath + "latest.txt"))
            createFile(logPath + "latest.txt");
        write(log, logPath + "latest.txt", true);
    }

    public void write(String content, String path, boolean oldContentStay) {
        try {
            if (oldContentStay) {
                FileWriter writer = new FileWriter(path, true);
                writer.write(content + "\n");
                writer.close();
            } else {
                PrintWriter writer = new PrintWriter(path, "UTF-8");
                writer.print(content);
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write(ArrayList<String> content, String path) {
        try {
            PrintWriter writer = new PrintWriter(path, "UTF-8");
            for (String line : content)
                writer.print(line + "\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<String> read(String path) {
        if(!fileExists(path))
            createFile(path);
        ArrayList<String> result = new ArrayList<String>();

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                result.add(result.size(), line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    private String getLastModified(String path) {
        File file = new File(path);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        return sdf.format(file.lastModified());
    }

    private int countDateFiles(String date) {
        ArrayList<String> files = listFolderContentFILE(logPath);
        int count = 0;
        for (String file : files) {
            if (file.contains(date))
                count++;
        }
        return count;
    }

    private boolean fileExists(String path) {
        return new File(path).exists();
    }

    public void renameFile(String src, String dest) {
        new File(src).renameTo(new File(dest));
    }

    public void createFile(String path) {
        try {
            new File(new File(path).getParent()).mkdirs();
            new File(path).createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<String> listFolderContentFILE(String path) {
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();
        ArrayList<String> result = new ArrayList<>();

        for (File file : listOfFiles) {
            if (!file.isDirectory())
                result.add(file.getName());
        }

        return result;
    }
}
