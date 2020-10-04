package sk.tuke;

import sk.tuke.ByteConverter;
import sk.tuke.RC4;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;
import java.util.Scanner;

public class Server {
    //Argumenty vo formate [filename] [port] / [filename]
    public static void main(String[] args) {
        String filename = "";
        int port = 80;
        //Kontroluju sa argumenty
        switch (args.length){
            case 1:
                filename = args[0];
                break;
            case 2:
                filename = args[0];
                try {
                    port = Integer.parseInt(args[1]);
                } catch (Exception e){
                    System.out.println("Zly format portu, koniec");
                    return;
                }
                break;
            default:
                System.out.println("Zly format argumentov, koniec");
                return;
        }
        //Ak neexistuje subor, tak koniec
        if(Files.notExists(Paths.get(filename))){
            System.out.println("Zadany subor neexistuje");
            return;
        }

	    ServerSocket serverSocket = null;
        Socket connectionSocket = null;
        while(true) {
            try {
                serverSocket = new ServerSocket(port);
                connectionSocket = serverSocket.accept();

                //Vymena kluca
                int key;
                try {
                    key = exchangeKey(connectionSocket);
                } catch (Exception e) {
                    System.out.println("Key exchange failed");
                    return;
                }

                //Otvorenie suboru
                File file = new File(filename);
                if(!file.exists()){
                    System.out.println("Subor bol zmazany alebo presunuty, koniec");
                    throw new IOException("Subor neexistuje");
                }
                Scanner reader = new Scanner(file);

                //Precita sa HTML subor
                StringBuilder fileContent = new StringBuilder();
                while (reader.hasNextLine()) {
                    fileContent.append(reader.nextLine());
                    fileContent.append('\n');
                }
                reader.close();

                OutputStream output = connectionSocket.getOutputStream();

                //Zasifrovane RC4 sifrou sa odosle dlzka HTML v Bajtoch a nasledne samotny obsah HTML
                RC4.send(fileContent.length(), key, output);
                RC4.send(fileContent.toString(), key, output);

                //Po odoslani server uzatvara spojenie
                connectionSocket.close();
                serverSocket.close();
            } catch (IOException e) {
                try {
                    connectionSocket.close();
                } catch (IOException | NullPointerException ex) {
                    ex.printStackTrace();
                }

                try {
                    serverSocket.close();
                } catch (IOException | NullPointerException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public static int exchangeKey(Socket connectionSocket){
        int key = -1;
        try {
            InputStream input = connectionSocket.getInputStream();
            OutputStream output = connectionSocket.getOutputStream();

            //p sa vygeneruje ako jedno z prvych 100 tisic prvocisel
            int p = Prime.generatePrime(new Random().nextInt(100000));
            //g sa vygeneruje ako jedno z prvych 8 prvocisel
            int g = Prime.generatePrime(new Random().nextInt(7));
            //a sa vygeneruje ako nahodne cislo z rozmedzia od 0 do 7 z dôvodu, aby zbytocne nepretekal int buffer
            int a = new Random().nextInt(7);
            //Vypocitanie A podla rovnice (g^a)%p
            int A = (int)Math.pow(g, a) % p;
            int B;
            //Odoslanie p,g,A
            output.write(ByteConverter.toByteArray(p));
            output.write(ByteConverter.toByteArray(g));
            output.write(ByteConverter.toByteArray(A));
            //Prijatie B
            B = ByteConverter.fromByteArray(input.readNBytes(4));
            //Vypocitanie kluca na zaklade rovnice (B^a)%p
            key = (int) Math.pow(B, a) % p;
        } catch (Exception e){
        }
        return key;
    }
}
