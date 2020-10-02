package sk.tuke.encryption;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.rmi.server.ExportException;
import java.util.Random;

public class RC4 {
    public static void send(int data, int key, OutputStream output) throws IOException {
        try {
            output.write(cypher(ByteBuffer.allocate(4).putInt(data).array(), key));
        } catch (Exception e){
            throw e;
        }
    }

    public static void send(String data, int key, OutputStream output) throws IOException {
        try{
            output.write(cypher(data.getBytes(),key));
        } catch (Exception e){
            throw e;
        }
    }

    public static int receiveInt(int key , InputStream input) throws IOException {
        try {
            return ByteBuffer.wrap(decypher(input.readNBytes(4), key)).getInt();
        } catch (Exception e){
            throw e;
        }
    }

    public static byte[] receiveContent(int length, int key, InputStream input) throws IOException{
        try{
            return decypher(input.readNBytes(length), key);
        } catch (Exception e){
            throw e;
        }
    }

    private static byte[] cypher(byte[]data, int key){
        byte[] keyStream = generateKeyStream(data, key);
        byte[] cypher = new byte[data.length];
        for(int i = 0; i < data.length; i++){
            cypher[i] = (byte)(data[i] ^ keyStream[i]);
        }
        return cypher;
    }

    private static byte[] decypher(byte[]data, int key){
        byte[] keyStream = generateKeyStream(data, key);
        byte[] originalData = new byte[data.length];
        for(int i = 0; i < data.length; i++){
            originalData[i] = (byte)(data[i] ^ keyStream[i]);
        }
        return originalData;
    }

    private static byte[] generateKeyStream(byte[]data, int key){
        byte[] keyBytes = ByteBuffer.allocate(4).putInt(key).array();
        byte[] K = new byte[256];

        //Create 256 element array from key bytes
        for(int i = 0; i < 256; i++){
            K[i] = keyBytes[i % keyBytes.length];
        }

        //Create S array used for pseudorandomisation
        byte[] S = new byte[256];
        for(int i = 0; i < 256; i++){
            S[i] = (byte)i;
        }
        //Swapping elements in S array
        int j = 0;
        for(int i = 0; i < 256; i++){
            j = (j + Byte.toUnsignedInt(S[i]) + Byte.toUnsignedInt(K[i])) % 256;
            byte tmp = S[i];
            S[i] = S[j];
            S[j] = tmp;
        }
        //Generate Key Stream from data length, K array and S array
        byte[] keyStream = new byte[data.length];
        for(int i = 0; i < data.length; i++){
            j = 0;
            j = j + Byte.toUnsignedInt(S[i]) % 256;
            int k = (Byte.toUnsignedInt(S[i]) + Byte.toUnsignedInt(S[j])) % 256;
            keyStream[i] = S[k];
        }
        return keyStream;
    }

}
