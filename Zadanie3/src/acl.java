import java.io.*;
import java.nio.file.Path;
import java.util.*;

/*
* Hlavna trieda
*
* Mozne argumenty programu su:
*
* -setl [String...security_levels] - Definuju bezpecnostne urovne programu. Pri nedefinovani volitelneho argumentu sa pouziju default levely: public, confidential, secret, top secret
*
* -setu newUsername security_level changingUsername - Definuje noveho uzivatela s danou bezpecnostnou urovnou. Je mozne pouzit pouzivatelske meno "admin", ktore ma najvyssiu pravomoc
*
* -setf filename security_level username - Definuje bezpecnostnu uroven suboru. Uzivatel vsak moze definovat maximalne svoju bezpecnostnu uroven
*
* -r filename username - Odtestuje, ci dany uzivatel ma pristup na citanie daneho suboru/priecinku
*
* -w filename username - Odtestuje, ci dany uzivatel ma pristup na zapisovanie do daneho suboru/priecinku
*
* -ls - vypise vsetky bezpecnostne urovne, definovanych uzivatelov a subory
* */

class Alc {
    enum Action{
        SETL,
        SETF,
        SETU,
        R,
        W,
        LS
    }
    private static Action action;
    private static List<String> securityLevels;
    private static String username;
    private static String level;
    private static String filename;
    private static String changingUsername;

    private static void parseArgs(String[] args) throws IllegalArgumentException, IndexOutOfBoundsException{
        if(args.length == 0){
            throw new IllegalArgumentException("Arguments must have at least one argument");
        }
        try {
            switch (args[0]) {
                case "-setl":
                    action = Action.SETL;
                    securityLevels = new ArrayList<>();
                    if (args.length > 1) {
                        securityLevels.addAll(Arrays.asList(args).subList(1, args.length));
                    } else {
                        securityLevels.addAll(Arrays.asList("public", "confidential", "secret", "top secret"));
                    }
                    break;
                case "-setu":
                    action = Action.SETU;
                    username = args[1];
                    level = args[2];
                    changingUsername = args[3];
                    break;
                case "-setf":
                    action = Action.SETF;
                    filename = args[1];
                    level = args[2];
                    username = args[3];

                    filename = Path.of(filename).normalize().toAbsolutePath().toString();
                    break;
                case "-r":
                    action = Action.R;
                    filename = args[1];
                    username = args[2];

                    filename = Path.of(filename).normalize().toAbsolutePath().toString();
                    break;
                case "-w":
                    action = Action.W;
                    filename = args[1];
                    username = args[2];

                    filename = Path.of(filename).normalize().toAbsolutePath().toString();
                    break;
                case "-ls":
                    action = Action.LS;
                    break;
                default:
                    throw new IllegalArgumentException("Arguments in wrong format");
            }
        } catch (IndexOutOfBoundsException e){
            throw new IndexOutOfBoundsException("Arguments in wrong format");
        }
    }

    public static void main(String[] args) throws IOException, InvalidValueException {
        parseArgs(args);

        if (action == Action.SETL) {
            SecurityLevels.setSecurityLevels(securityLevels.toArray(new String[0]));
            return;
        }
        SecurityLevels.loadSecurityLevels();
        Users.loadUsers();
        Files.loadFiles();

        switch (action){
            case SETU:
                Users.setUser(username, level, changingUsername);
                Users.saveUsers();
                System.out.println("User was created successfully");
                break;
            case SETF:
                Files.setFile(filename, level, username);
                Files.saveFiles();
                break;
            case R:
                if(AccessRightsChecker.hasReadRights(filename, username)){
                    System.out.println("User " + username + " can read from this file/directory");
                } else {
                    System.out.println("User " + username + " can NOT read from this file/directory");
                }
                break;
            case W:
                if(AccessRightsChecker.hasWriteRights(filename, username)){
                    System.out.println("User " + username + " can write into this file/directory");
                } else {
                    System.out.println("User " + username + " can NOT write into this file/directory");
                }
                break;
            case LS:
                System.out.println(SecurityLevels.toStr());
                System.out.println(Users.toStr());
                System.out.println(Files.toStr());
        }
    }
}

class AccessRightsChecker {
    public static boolean hasReadRights(String filepath, String username) throws InvalidValueException {
        int fileLevel = Files.getFileSecurityLevelValue(filepath);
        int userLevel = Users.getUserSecurityLevelValue(username);

        return fileLevel <= userLevel;
    }

    public static boolean hasWriteRights(String filepath, String username) throws InvalidValueException {
        int fileLevel = Files.getFileSecurityLevelValue(filepath);
        int userLevel = Users.getUserSecurityLevelValue(username);

        return fileLevel >= userLevel;
    }
}

class Files {
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


class InvalidValueException extends Exception {
    public InvalidValueException(String message){
        super(message);
    }
}

class SecurityLevels {
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
        String[] securityLevelsKeysSorted = new String[securityLevelsKeys.length];

        for(String k : securityLevelsKeys){
            securityLevelsKeysSorted[securityLevels.get(k)] = k;
        }
        securityLevelsKeys = securityLevelsKeysSorted;
        for(String key : securityLevelsKeys){
            sb.append(key).append(":\t\t").append(securityLevels.get(key)).append("\n");
        }
        sb.append("\n").append("\n");
        return sb.toString();
    }
}

//Trieda reprezentujuca uzivatelov v LaPadula systeme
class Users {
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
                } catch (InvalidValueException e){
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
            throw new InvalidValueException("User creation failed because security level for new user is higher than security level of existing user");
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
        String[] usersKeys = users.keySet().toArray(new String[0]);
        Arrays.sort(usersKeys);
        for(String key : usersKeys){
            sb.append(key).append(":\t\t").append(users.get(key)).append("\n");
        }
        sb.append("\n").append("\n");
        return sb.toString();
    }
}
