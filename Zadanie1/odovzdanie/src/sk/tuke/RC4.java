package sk.tuke;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class RC4 {
    public static void send(int data, int key, OutputStream output) throws IOException {
        try {
            output.write(cipher(ByteBuffer.allocate(4).putInt(data).array(), key));
        } catch (Exception e){
            throw e;
        }
    }

    public static void send(String data, int key, OutputStream output) throws IOException {
        try{
            output.write(cipher(data.getBytes(),key));
        } catch (Exception e){
            throw e;
        }
    }

    public static int receiveInt(int key , InputStream input) throws IOException {
        try {
            return ByteBuffer.wrap(decipher(input.readNBytes(4), key)).getInt();
        } catch (Exception e){
            throw e;
        }
    }

    public static byte[] receiveContent(int length, int key, InputStream input) throws IOException{
        try{
            return decipher(input.readNBytes(length), key);
        } catch (Exception e){
            throw e;
        }
    }

    private static byte[] cipher(byte[]data, int key){
        byte[] keyStream = generateKeyStream(data, key);
        byte[] cipher = new byte[data.length];
        //Prechadza sa pole dat a xoruje sa aktualny bajt s aktualnym bajtom keyStream
        for(int i = 0; i < data.length; i++){
            cipher[i] = (byte)(data[i] ^ keyStream[i]);
        }
        return cipher;
    }

    private static byte[] decipher(byte[]data, int key){
        return cipher(data, key);
    }

    private static byte[] generateKeyStream(byte[]data, int key){
        byte[] keyBytes = ByteBuffer.allocate(4).putInt(key).array();

        //Naplnenie pola S k pseudonahodnemu generovaniu
        //Naplnenia pola K opakovanymi bajtmi kluca
        byte[] K = new byte[256];
        byte[] S = new byte[256];
        for(int i = 0; i < 256; i++){
            S[i] = (byte)i;
            K[i] = keyBytes[i % keyBytes.length];
        }

        //Poprehadzovanie hodnot v poli S
        int j = 0;
        for(int i = 0; i < 256; i++){
            j = (j + Byte.toUnsignedInt(S[i]) + Byte.toUnsignedInt(K[i])) % 256;
            byte tmp = S[i];
            S[i] = S[j];
            S[j] = tmp;
        }
        //Vygenerovanie vyslednej postupnosti keyStream, ktora bude sluzit na sifrovanie/odsifrovanie spravy
        byte[] keyStream = new byte[data.length];
        int a = 0;
        int b = 0;
        int c = 0;
        for(int i = 0; i < data.length; i++){
            a = (a + 1) % 256;
            b = (b + Byte.toUnsignedInt(S[j])) % 256;
            c = (Byte.toUnsignedInt(S[a]) + Byte.toUnsignedInt(S[b])) % 256;
            keyStream[i] = S[c];
        }
        return keyStream;
    }

}
