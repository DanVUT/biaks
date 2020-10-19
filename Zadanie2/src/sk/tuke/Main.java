package sk.tuke;

public class Main {
    public static String toHexString(byte[] b)
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < b.length; i++)
        {
            sb.append(String.format("%02X", b[i] & 0xFF));
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        byte[] result = MD5.calculateMD5("epgojreopgjwrpogjwrpogjepofjopqrjqopšfjopwejfpowejfpowefjpoewfjpoefjopewjfopwejfpowejpofwejpfowejfpowejfpowejfwepowefjpweojfpweofjpwoefjopwefjopwefjwpeofwej");
        System.out.println(toHexString(result));
    }
}
