package sk.tuke.communication;

import java.nio.ByteBuffer;

public class ByteConverter {
    public static byte[] toByteArray(int number){
        return ByteBuffer.allocate(4).putInt(number).array();
    }

    public static int fromByteArray(byte[] array){
        return ByteBuffer.wrap(array).getInt();
    }
}
