package sk.tuke;

import java.io.*;
import java.util.HashMap;
import java.util.Properties;

//Trieda reprezentujuca uzivatelov v LaPadula systeme
public class Users {
    private static final String FILENAME = "user_security_levels.config";

    private static HashMap<String, String> users = new HashMap<>();

    //Ulozenie uzivatelov na disk
    public static void saveUsers() throws IOException {
        Properties properties = new Properties();

        for(String s : users.keySet()){
            properties.setProperty(s, users.get(s));
        }

        try {
            properties.store(new FileOutputStream(FILENAME), "");
        } catch (IOException e){
            throw new IOException("Problem with saving security levels");
        }
    }

    //Nacitanie uzivatelov z disku
    public static void loadUsers() throws IOException, InvalidValueException {
        Properties properties = new Properties();
        try {
            File file = new File(FILENAME);
            FileInputStream fis = new FileInputStream(file);
            properties.load(fis);
            fis.close();
            users.clear();
            for(Object o : properties.keySet()){
                try {
                    String username = (String) o;
                    String userLevel = properties.getProperty(username);
                    setUser(username, userLevel);
                } catch (IllegalArgumentException e){
                    file.delete();
                    throw new InvalidValueException("Users file corrupted. It contains security level that is not defined. Deleting users file to prevent lack of integrity.");
                }
            }
        } catch (FileNotFoundException ignored){}
        catch (IOException e) {
            throw new IOException("Problem with loading the users from: " + FILENAME);
        }
    }

    //Metoda pre zmazanie suboru s uzivatelmi
    public static void deleteUsers() {
        File file = new File(FILENAME);
        file.delete();
    }

    //pomocna metoda pre vkladanie pouzivatelov pri nacitani zo suboru
    public static void setUser(String username, String level) throws InvalidValueException {
        SecurityLevels.getSecurityLevelValue(level);
        users.put(username, level);
    }
    //metoda pre pridanie pouzivatela, metoda kontroluje, ci pouzivatel, ktory vytvara noveho pouzivatela ma dostatocne prava
    public static void setUser(String username, String level, String changingUsername) throws IllegalArgumentException, InvalidValueException {
        int changingUsernameLevel = Users.getUserSecurityLevelValue(changingUsername);
        int newUsernameLevel = SecurityLevels.getSecurityLevelValue(level);
        if(newUsernameLevel > changingUsernameLevel){
            throw new InvalidValueException("User creation failed because security level for new user is higher then security level of existing user");
        }
        SecurityLevels.getSecurityLevelValue(level);
        users.put(username, level);
    }
    //metoda ziska enumeracnu hodnotu bezpecnostneho levelu pouzivatela
    public static int getUserSecurityLevelValue(String username) throws InvalidValueException {
        if(!users.containsKey(username)){
            throw new InvalidValueException("Given username does not exist");
        }
        return SecurityLevels.getSecurityLevelValue(users.get(username));
    }

    public static String toStr(){
        StringBuilder sb = new StringBuilder();
        sb.append("Username : Security Level\n");
        for(String key : users.keySet()){
            sb.append(key).append(":\t\t").append(users.get(key)).append("\n");
        }
        sb.append("\n").append("\n");
        return sb.toString();
    }
}
