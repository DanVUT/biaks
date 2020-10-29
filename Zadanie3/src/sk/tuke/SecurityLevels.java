package sk.tuke;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;

//Trieda reprezentujuca bezpecnostne urovne
public class SecurityLevels {
    private static final String FILENAME = "security_levels.config";
    //Bezpecnostne urovne su ulozene v hashmape
    private static HashMap<String, Integer> securityLevels = new HashMap<>();

    //Ulozenie bezpecnostnych urovni do suboru
    public static void saveSecurityLevels() throws IOException {
        Properties properties = new Properties();

        for(String s : securityLevels.keySet()){
            properties.setProperty(s, securityLevels.get(s).toString());
        }

        try {
            properties.store(new FileOutputStream(FILENAME), "");
        } catch (IOException e){
            throw new IOException("Problem with saving security levels. Probably insufficient access rights or file is being blocked");
        }
    }

    //Nacitanie bezpecnostnych urovni zo suboru
    public static void loadSecurityLevels() throws IOException {
        Properties properties = new Properties();
        try {
            FileInputStream fis = new FileInputStream(FILENAME);
            properties.load(fis);
            fis.close();
            securityLevels.clear();
            for(Object o : properties.keySet()){
                String name = (String)o;
                int value = Integer.parseInt(properties.getProperty(name));
                setSecurityLevel(name, value);
            }
        } catch (FileNotFoundException e){
            throw new FileNotFoundException("Security levels are not defined. Please run the program with argument(s) -setl [String...security_levels]");
        }
        catch (IOException e) {
            throw new IOException("Problem with reading security levels from: " + FILENAME);
        }
    }

    private static void setSecurityLevel(String level, int value){
        securityLevels.put(level, value);
    }

    //Nastavi bezpecnostne urovne z pola
    public static void setSecurityLevels(String...levels) throws IOException, InvalidValueException {
        securityLevels.clear();
        for (String level: levels) {
            securityLevels.put(level, securityLevels.size());
        }
        saveSecurityLevels();
        Users.deleteUsers();
        Files.deleteFiles();
        Users.setUser("admin", levels[levels.length - 1]);
        Users.saveUsers();
    }

    //Zo stringu bezpecnostnej urovne suboru alebo uzivatela vrati jeho enumeracnu hodnotu
    public static int getSecurityLevelValue(String level) throws InvalidValueException {
        if(!securityLevels.containsKey(level)){
            throw new InvalidValueException("Given security level is not valid");
        }
        return securityLevels.get(level);
    }

    public static String toStr(){
        StringBuilder sb = new StringBuilder();
        sb.append("Level Name : Level Rank\n");
        String[] securityLevelsKeys = securityLevels.keySet().toArray(new String[0]);
        Arrays.sort(securityLevelsKeys);
        for(String key : securityLevelsKeys){
            sb.append(key).append(":\t\t").append(securityLevels.get(key)).append("\n");
        }
        sb.append("\n").append("\n");
        return sb.toString();
    }
}
