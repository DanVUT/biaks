package sk.tuke;

import sk.tuke.model.KeyPair;
import sk.tuke.model.PrivateKey;
import sk.tuke.model.PublicKey;

import java.io.*;
import java.math.BigInteger;
import java.nio.file.Path;
import java.util.Random;
import java.util.Scanner;

//Trieda generuje RSA kluce, ktore vyuziva k sifrovaniu a desifrovaniu md5 hashov
public class RSA {

    //Metoda ulozi privatny kluc do suboru
    private static void savePrivateKeyToFile(PrivateKey privateKey, Path folder) throws IOException {
        try {
            FileWriter writer = new FileWriter(Path.of(folder.toString(), "private_key.pk").toString());
            writer.write(privateKey.getD().toString(16));
            writer.write(" ");
            writer.write(privateKey.getModulus().toString(16));
            writer.close();
        } catch (IOException e) {
            throw new IOException("Problem with saving Private Key to File. Probably non-existent directory.");
        }
    }

    //Metoda ulozi verejny kluc do suboru
    private static void savePublicKeyToFile(PublicKey publicKey, Path folder) throws IOException {
        try {
            FileWriter writer = new FileWriter(Path.of(folder.toString(), "public_key.pk").toString());
            writer.write(publicKey.getE().toString(16));
            writer.write(" ");
            writer.write(publicKey.getModulus().toString(16));
            writer.close();
        } catch (IOException e) {
            throw new IOException("Problem with saving Public Key to File. Probably non-existent directory.");
        }
    }

    //Metoda nacita privatny kluc zo suboru
    public static PrivateKey readPrivateKeyFromFile(String filename) throws RuntimeException, FileNotFoundException{
        String d;
        String modulus;
        try {
            Scanner reader = new Scanner(new File(filename));
            d = reader.next();
            modulus = reader.next();
        } catch (FileNotFoundException e){
            throw new FileNotFoundException("Private key file was not found");
        }

        try{
            return new PrivateKey(new BigInteger(d, 16), new BigInteger(modulus, 16));
        } catch (Exception ex){
            throw new RuntimeException("Private key format is corrupted");
        }
    }

    //Metoda nacita verejny kluc zo suboru
    public static PublicKey readPublicKeyFromFile(String filename) throws FileNotFoundException, RuntimeException {
        String e;
        String modulus;

        try {
            Scanner reader = new Scanner(new File(filename));
            e = reader.next();
            modulus = reader.next();
        } catch (FileNotFoundException ex){
            throw new FileNotFoundException("Public key file was not found");
        }

        try{
            return new PublicKey(new BigInteger(e, 16), new BigInteger(modulus, 16));
        } catch (Exception ex){
            throw new RuntimeException("Public key format is corrupted");
        }
    }

    /*
    * Metoda vygeneruje par kryptografickych klucov a ulozi ich do suboru
    *
    * p a q su vygenerovane ako nahodne prvocisla o bitovej dlzke 150 bitov
    * e je zvolena konstanta 2^16 + 1, pretoze sa vysktovala vo viacerych zdrojoch ako pouzivana konstanta
    *
    * Implementacia je inspirovana generaciou RSA klucov v programe CrypTool
    * */
    public static KeyPair generateKeyPair(Path folderForKeys) throws IOException {
        BigInteger p = BigInteger.probablePrime(150, new Random());
        BigInteger q = BigInteger.probablePrime(150, new Random());
        BigInteger modulus = p.multiply(q);
        BigInteger PhiN = p.subtract(BigInteger.valueOf(1)).multiply(q.subtract(BigInteger.valueOf(1)));
        BigInteger e = new BigInteger("65537");
        BigInteger d = e.modInverse(PhiN);

        PrivateKey privateKey = new PrivateKey(d, modulus);
        PublicKey publicKey = new PublicKey(e, modulus);



        savePrivateKeyToFile(privateKey, folderForKeys);
        savePublicKeyToFile(publicKey, folderForKeys);

        return new KeyPair(privateKey, publicKey);
    }

    //Sifrovanie pomocou privatneho kluca. Sifrovanie prebieha ako m^d % modulus
    public static BigInteger cipherMD5withPrivateKey(BigInteger md5, PrivateKey privateKey){
        return md5.modPow(privateKey.getD(), privateKey.getModulus());
    }

    //Sifrovanie pomocou verejneho kluca. Sifrovanie prebieha ako m^e % modulus
    public static BigInteger cipherMD5withPublicKey(BigInteger md5cipher, PublicKey publicKey){
        return md5cipher.modPow(publicKey.getE(), publicKey.getModulus());
    }
}
