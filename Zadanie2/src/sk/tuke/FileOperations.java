package sk.tuke;

import sk.tuke.model.KeyPair;
import sk.tuke.model.PrivateKey;
import sk.tuke.model.PublicKey;

import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileOperations {
    //Metoda vytvori meno pre podpisany subor v tvare FILENAME_signed.originalExtension
    private static Path getSignedFilename(Path inputFilePath){
        Path inputFileName = inputFilePath.getFileName();
        String inputFileExtension = "";
        try {
            inputFileExtension = inputFileName.toString().substring(inputFileName.toString().lastIndexOf('.'));
        } catch (IndexOutOfBoundsException ignored){}
        String inputFileNameWithoutExtension = inputFileName.toString().replaceFirst(inputFileExtension, "");
        inputFileNameWithoutExtension += "_signed";
        String signedFileName = inputFileNameWithoutExtension.concat(inputFileExtension);
        return Paths.get(inputFilePath.getParent().toString(), signedFileName);
    }
    //Metoda podpise subor v pripade ze nebol zadany privatny kluc. Takze vygeneruje par klucov v priecinku kde sa nachadza aj podpisovany subor
    public static void signFile(String filename) throws IOException {
        Path inputFilePath = Paths.get(filename).toAbsolutePath();
        byte[] fileContent;
        try {
            fileContent = Files.readAllBytes(inputFilePath);
        }
        catch (IOException e){
            throw new IOException("Reading of input file failed");
        }
        BigInteger md5 = new BigInteger(1,MD5.calculateMD5(fileContent));
        KeyPair keyPair = RSA.generateKeyPair(inputFilePath.getParent());
        BigInteger md5cipher = RSA.cipherMD5withPrivateKey(md5, keyPair.getPrivateKey());
        createSignedFile(inputFilePath, fileContent, md5cipher);
    }

    //Metoda podpise subor, ak bol zadany privatny kluc
    public static void signFile(String filename, PrivateKey privateKey) throws IOException {
        Path inputFilePath = Paths.get(filename).toAbsolutePath();
        byte[] fileContent;
        try {
            fileContent = Files.readAllBytes(inputFilePath);
        }
        catch (IOException e){
            throw new IOException("Reading of input file failed");
        }
        BigInteger md5 = new BigInteger(1,MD5.calculateMD5(fileContent));
        BigInteger md5cipher = RSA.cipherMD5withPrivateKey(md5, privateKey);
        createSignedFile(inputFilePath, fileContent, md5cipher);
    }

    //Pomocna metoda. Iba zapise vsetky informacie do suboru
    //Implementacia obsahu podpisaneho suboru je inspirovana implementaciou CrypToolom
    private static void createSignedFile(Path inputFilePath, byte[] fileContent, BigInteger md5cipher) throws IOException {
        try {
            FileOutputStream fos = new FileOutputStream(getSignedFilename(inputFilePath).toString(), false);
            fos.write("Signature length: 304\n".getBytes());
            fos.write("Signature: ".getBytes());
            fos.write(md5cipher.toString(16).getBytes());
            fos.write("\n".getBytes());
            fos.write("Message:\n".getBytes());
            fos.write(fileContent);
            fos.flush();
            fos.close();
        } catch (Exception e){
            throw new IOException("Creation of signed document failed");
        }
    }

//    //Pomocna metoda pre citanie znakov
//    private static String read(BufferedReader reader, char delimiter) throws IOException {
//        int ch;
//        StringBuilder sb = new StringBuilder();
//        ch = reader.read();
//        while(ch != delimiter && ch != -1){
//            sb.append((char) ch);
//            ch = reader.read();
//        }
//        return sb.toString();
//    }
//
//    //Pomocna metoda pre precitanie tela spravy
//    private static byte[] readContent(BufferedReader reader) throws IOException {
//        int ch;
//        ch = reader.read();
//        StringBuilder sb = new StringBuilder();
//        while(ch != -1){
//            sb.append((char) ch);
//            ch = reader.read();
//        }
//        return sb.toString().getBytes();
//    }

    //Metoda verifikuje podpisany subor pomocou zadaneho verejneho kluca
//    public static boolean verifySignature(String filename, PublicKey publicKey) throws IOException, RuntimeException {
//        BufferedReader reader = new BufferedReader(new FileReader(filename));
//        try{
//            String str = read(reader, '\n');
//            if(!str.equals("Signature length: 304")){
//                throw new RuntimeException("Signature file in wrong format");
//            }
//            str = read(reader, ' ');
//            if(!str.equals("Signature:")){
//                throw new RuntimeException("Signature file in wrong format");
//            }
//            str = read(reader, '\n');
//            BigInteger signature = new BigInteger(str, 16);
//            str = read(reader, '\n');
//            if(!str.equals("Message:")){
//                throw new RuntimeException("Signature file in wrong format");
//            }
//            byte[] content = readContent(reader);
//            BigInteger md5deciphered = RSA.cipherMD5withPublicKey(signature, publicKey);
//            BigInteger md5 = new BigInteger(1, MD5.calculateMD5(content));
//
//            return md5.compareTo(md5deciphered) == 0;
//        } catch (FileNotFoundException e) {
//            throw new FileNotFoundException("Signature file does not exist");
//        } catch (IOException e){
//            throw new IOException("Problem with reading the signature file");
//        }
//    }

    public static boolean verifySignature(String filename, PublicKey publicKey) throws IOException, RuntimeException{
        byte[] content;
        try {
            content = Files.readAllBytes(Path.of(filename));
        } catch (IOException e){
            throw new FileNotFoundException("Signed file not found");
        }
        int index = 0;
        BigInteger md5cipher;

        //Read Signature length: 304
        StringBuilder sb = new StringBuilder();
        for (; content[index] != '\n'; index++) {
            sb.append((char) content[index]);
        }
        if (!sb.toString().equals("Signature length: 304")) {
            throw new RuntimeException("Signature file in wrong format");
        }

        //Read Signature:
        sb = new StringBuilder();
        for (index++; content[index] != ' '; index++) {
            sb.append((char) content[index]);
        }
        if (!sb.toString().equals("Signature:")) {
            throw new RuntimeException("Signature file in wrong format");
        }

        //Read signature
        sb = new StringBuilder();
        for (index++; content[index] != '\n'; index++) {
            sb.append((char) content[index]);
        }

        //Convert string to BigInt
        try {
            md5cipher = new BigInteger(sb.toString(), 16);
        } catch (Exception e){
            throw new RuntimeException("Signature is corrupted");
        }

        //Read Message:
        sb = new StringBuilder();
        for (index++; content[index] != '\n'; index++) {
            sb.append((char) content[index]);
        }
        index++;
        if (!sb.toString().equals("Message:")) {
            throw new RuntimeException("Signature file in wrong format");
        }

        byte[] message = new byte[content.length - index];
        for(int i = 0; i < message.length; i++){
            message[i] = content[index++];
        }

        BigInteger md5 = new BigInteger(1, MD5.calculateMD5(message));
        BigInteger md5decipher = RSA.cipherMD5withPublicKey(md5cipher, publicKey);

        System.out.println("Generated MD5 from message: " + md5.toString(16));
        System.out.println("Deciphered MD5 from signature: " + md5decipher.toString(16));
        return md5.compareTo(md5decipher) == 0;
    }
}
