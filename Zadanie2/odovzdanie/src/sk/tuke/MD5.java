package sk.tuke;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

public class MD5 {
    //Inicializacna cast MD5 podla RFC1321
    private static final int AI = 0x67452301;
    private static final int BI = (int)0xEFCDAB89L;
    private static final int CI = (int)0x98BADCFEL;
    private static final int DI = 0x10325476;
    private static final int[] T = new int[64];
    static {
        for(int i = 1; i <= 64; i++){
            T[i - 1] = (int)(long)((4294967296L) * Math.abs(Math.sin(i)));
        }
    }

    private static final int[] SHIFT_CONST = {
            7, 12, 17, 22,
            5,  9, 14, 20,
            4, 11, 16, 23,
            6, 10, 15, 21
    };

    //Metoda kalkulujuca md5
    public static byte[] calculateMD5(byte[] messageBytes){
        byte[] paddedMessageBytes = addPaddingBits(messageBytes);
        byte[] appendedLengthMessageBytes = appendLength(paddedMessageBytes, messageBytes);
        return digestMessage(appendedLengthMessageBytes);
    }

    //Metoda prida padding bity k hashovanej sprave tak, aby vysledok bol 64 bitov od delitelnosti 512
    private static byte[] addPaddingBits(byte[] messageBytes){
        int messageLengthInBits = Helper.calculateBits(messageBytes);
        int modulo =  messageLengthInBits % 512;
        int noBitsToAppend;
        if(modulo < 448){
            noBitsToAppend = 448 - modulo;
        } else {
            noBitsToAppend = (512 - modulo) + 448;
        }

        byte[] paddedMessageBytes = Arrays.copyOf(messageBytes, messageBytes.length + (noBitsToAppend / 8));
        for(int i = messageBytes.length; i < messageBytes.length + (noBitsToAppend / 8); i++){
            if(i == messageBytes.length){
                paddedMessageBytes[i] = (byte)0x80;
            } else {
                paddedMessageBytes[i] = 0;
            }
        }
        return paddedMessageBytes;
    }

    //K vysledku predchadzajucej metody prida 64 bitovu reprezentaciu dlzky povodnej spravy v bitoch
    private static byte[] appendLength(byte[] paddedMessageBytes, byte[] originalMessage){
        int lengthInBytes = originalMessage.length;
        long lengthInBits = (lengthInBytes * 8);
        byte[] appendedLengthMessageBytes = Arrays.copyOf(paddedMessageBytes, paddedMessageBytes.length + 8);

        for(int i = (appendedLengthMessageBytes.length - 8); i < appendedLengthMessageBytes.length; i++){
            appendedLengthMessageBytes[i] = (byte)lengthInBits;
            lengthInBits >>>= 8;
        }

        return appendedLengthMessageBytes;
    }

    //Algoritmus vytvori vysledny MD5 hash podla RFC1321 standardu
    private static byte[] digestMessage(byte[] message){
        int numberOfBlocks = message.length / 64;
        int blockWidthInBytes = 64;
        int[] block = new int[16];
        int A = AI;
        int B = BI;
        int C = CI;
        int D = DI;

        for(int i = 0; i < numberOfBlocks; i++){
            for(int j = 0; j < blockWidthInBytes; j++){
                int tmp = ((int)message[i * blockWidthInBytes + j]) << 24;
                block[j / 4] = tmp | (block[j/4] >>> 8);
            }
            int AA = A;
            int BB = B;
            int CC = C;
            int DD = D;
            for(int j = 0; j < 4; j++){
                int func;
                int t;
                int b;
                int s;
                for(int k = 0; k < 16; k++) {
                    t = (j * 16) + k;
                    s = SHIFT_CONST[(j * 4) + (k % 4)];
                    switch (j) {
                        case 0:
                            func = (B & C) | (~B & D);
                            b = k;
                            break;
                        case 1:
                            func = (B & D) | (C & ~D);
                            b = ((k * 5) + 1) % 16;
                            break;
                        case 2:
                            func = B ^ C ^ D;
                            b = ((k * 3) + 5) % 16;
                            break;
                        case 3:
                            func = C ^ (B | ~D);
                            b = (k * 7) % 16;
                            break;
                        default:
                            func = 0;
                            b = 0;
                            break;
                    }
                    int temp = B + Integer.rotateLeft(A + func + block[b] + T[t], s);
                    A = D;
                    D = C;
                    C = B;
                    B = temp;

                }
            }
            A += AA;
            B += BB;
            C += CC;
            D += DD;
        }
        byte[] md5 = new byte[16];
        int count = 0;
        for (int i = 0; i < 4; i++)
        {
            int num = 0;
            switch (i){
                case 0:
                    num = A;
                    break;
                case 1:
                    num = B;
                    break;
                case 2:
                    num = C;
                    break;
                case 3:
                    num = D;
                    break;
                default:
                    break;
            }
            for (int j = 0; j < 4; j++)
            {
                md5[count++] = (byte)num;
                num >>>= 8;
            }
        }
        return md5;
    }



}
