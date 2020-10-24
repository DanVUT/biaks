package sk.tuke;
import sk.tuke.model.PrivateKey;
import sk.tuke.model.PublicKey;

import java.io.*;
import java.nio.file.Path;

//Hlavna trieda podpisoveho algoritmu
public class Podpis {
    public enum Actions{
        generate,
        sign,
        verify
    }
    //Staticke premenne obsahujuce informacie z argumentov
    public static Actions ACTION;
    public static String INPUTFILE = null;
    public static String KEYPATH = null;

    //Vytiahnutie argumentov
    private static void parseArgs(String[] args) throws IllegalArgumentException{
        if(args.length == 0){
            throw new IllegalArgumentException("You must enter the arguments");
        }

        switch(args[0]){
            case "-g":
                ACTION = Actions.generate;
                if(args.length == 1){
                    KEYPATH = "./";
                } else {
                    KEYPATH = args[1];
                }
                break;
            case "-s":
                ACTION = Actions.sign;
                if(args.length < 2){
                    throw new IllegalArgumentException("Argument format for -s is: INPUT_FILE_PATH [PRIVATE_KEY_PATH]");
                }
                INPUTFILE = args[1];
                if(args.length == 3){
                    KEYPATH = args[2];
                }
                break;
            case "-v":
                ACTION = Actions.verify;
                if(args.length != 3){
                    throw new IllegalArgumentException("Argument format for -v is: SIGNED_FILE_PATH PUBLIC_KEY_PATH");
                }
                INPUTFILE = args[1];
                KEYPATH = args[2];
                break;
            default:
                throw new IllegalArgumentException("Arguments in wrong format");
        }

    }

    /*Format povolenych argumentov:
    *
    * -g [RESULT_DIRECTORY] - vygeneruje par klucov. V pripade, ze sa nezada RESULT_DIRECTORY, tak sa kluce ulozia do aktualneho priecinka z kontextu vykonavania programu
    *
    * -s INPUT_FILE [PRIVATE_KEY] - podpise zadany subor. V pripade, ze sa nezada PRIVATE_KEY, tak sa vygeneruje novy par klucov v priecinku kde sa nachadza INPUT_FILE
    *
    * -v SIGNED_FILE PUBLIC_KEY - verifikuje sa podpisany subor voci zadanemu PUBLIC_KEY*/
    public static void main(String[] args) {
        try {
            parseArgs(args);
        } catch (Exception e){
            System.out.println(e.getMessage());
            return;
        }
        switch (ACTION){
            case generate:
                try {
                    RSA.generateKeyPair(Path.of(KEYPATH));
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                    return;
                }
                break;
            case sign:
                if(KEYPATH != null){
                    PrivateKey privateKey;
                    try {
                        privateKey = RSA.readPrivateKeyFromFile(KEYPATH);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                        return;
                    }
                    try {
                        FileOperations.signFile(INPUTFILE, privateKey);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                        return;
                    }
                }
                else {
                    try {
                        FileOperations.signFile(INPUTFILE);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                        return;
                    }
                }
                System.out.println("File was signed successfully");
                break;
            case verify:
                PublicKey publicKey;
                try {
                    publicKey = RSA.readPublicKeyFromFile(KEYPATH);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    return;
                }
                boolean result;
                try{
                    result = FileOperations.verifySignature(INPUTFILE, publicKey);
                } catch (Exception e){
                    System.out.println(e.getMessage());
                    return;
                }

                if(result){
                    System.out.println("Signature was successfully verified with given Public Key");
                } else{
                    System.out.println("Signature could NOT be verified with given Public Key");
                }
        }
    }
}
