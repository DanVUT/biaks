package sk.tuke;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

public class Alc {
    private enum Action{
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
                System.out.println("File security level was defined successfully");
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
