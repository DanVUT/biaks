package sk.tuke;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Files {
    private static String FILENAME = "files_security_levels.config";
    private static HashMap<String, String> files = new HashMap<>();

    public static void saveFiles() throws IOException {
        Properties properties = new Properties();

        for(String s : files.keySet()){
            properties.setProperty(s, files.get(s));
        }

        try {
            properties.store(new FileOutputStream(FILENAME), "");
        } catch (IOException e){
            throw new IOException("Problem with saving files security levels");
        }
    }

    public static void loadFiles() throws IOException, InvalidValueException {
        Properties properties = new Properties();
        try {
            File file = new File(FILENAME);
            FileInputStream fis = new FileInputStream(FILENAME);
            properties.load(fis);
            fis.close();
            files.clear();
            for(Object o : properties.keySet()){
                String filename = (String)o;
                String level = properties.getProperty( filename);
                try{
                    SecurityLevels.getSecurityLevelValue(level);
                } catch (InvalidValueException e){
                    file.delete();
                    throw new InvalidValueException("File containing security level definitions for files is corrupted. It contains security level that is not defined. Deleting security levels definition to prevent lack of integrity");
                }
                files.put(filename, level);
            }
        } catch (FileNotFoundException ignored){
        } catch (IOException e) {
            throw new IOException("Problem with loading the users from: " + FILENAME);
        }
    }

    public static void deleteFiles(){
        File file = new File(FILENAME);
        file.delete();
    }

    public static void setFile(String filepath, String level, String username) throws IllegalArgumentException, FileNotFoundException, InvalidValueException {
        File path = new File(filepath);
        if(!path.exists()){
            throw new FileNotFoundException("Given file/directory does not exist");
        }
        String parent = path.getParent();
        int userLevel = Users.getUserSecurityLevelValue(username);
        int parentLevel = getFileSecurityLevelValue(parent);
        int newLevel = SecurityLevels.getSecurityLevelValue(level);
        int currentLevel = getFileSecurityLevelValue(path.getAbsolutePath());

        if(currentLevel > userLevel){
            System.out.println("User cannot assign " + level + " security level to: "+ path.getAbsolutePath() + "; because file has currently higher security level than user");
            return;
        }

        if(newLevel > userLevel){
            System.out.println("User cannot assign " + level + " security level to: "+ path.getAbsolutePath() +"; because new security level is higher than user's security level");
            return;
        }

        if(newLevel < parentLevel){
            System.out.println("Cannot assign " + level + " level to: "+ path.getAbsolutePath() + "; because new security level is lower than parent directory security level");
            return;
        }

        files.put(path.getAbsolutePath(), level);

        if(path.isDirectory()){
            List<File> directoryFiles = new ArrayList<>(Arrays.asList(Objects.requireNonNull(path.listFiles())));
            for(File file : directoryFiles){
                setFile(file.getAbsolutePath(), level, username);
            }
        }
    }

    public static int getFileSecurityLevelValue(String path) throws InvalidValueException {
        File file = new File(path);
        if(!file.exists()){
            files.remove(path);
            throw new IllegalArgumentException("Given file does not exist.");
        }
        if(!files.containsKey(path)){
            return 0;
        }
        else{
            return SecurityLevels.getSecurityLevelValue(files.get(path));
        }
    }

    public static String toStr(){
        StringBuilder sb = new StringBuilder();
        sb.append("Filepath : Level Rank\n");
        String[] filesKeys = files.keySet().toArray(new String[0]);
        Arrays.sort(filesKeys);
        for(String key : filesKeys){
            sb.append(key).append(":\t\t").append(files.get(key)).append("\n");
        }
        sb.append("\n").append("\n");
        return sb.toString();
    }
}
