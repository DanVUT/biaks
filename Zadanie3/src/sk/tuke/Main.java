package sk.tuke;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
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
                    break;
                case "-r":
                    action = Action.R;
                    filename = args[1];
                    username = args[2];
                    break;
                case "-w":
                    action = Action.W;
                    filename = args[1];
                    username = args[2];
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
        try {
            parseArgs(args);
        } catch (Exception e){
            System.out.println(e.getMessage());
            return;
        }

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
