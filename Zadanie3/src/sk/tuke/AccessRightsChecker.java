package sk.tuke;

public class AccessRightsChecker {
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
