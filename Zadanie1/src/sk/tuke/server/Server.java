package sk.tuke.server;

import sk.tuke.communication.ByteConverter;
import sk.tuke.encryption.RC4;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;
import java.util.Scanner;

public class Server {
    //Argument vo formate "filename" / "". Pri nezadani argumentu sa pouzije defaultna cesta ./server/index.html
    public static void main(String[] args) {
        String filename = "";
        //Kontroluju sa argumenty
        switch (args.length){
            case 0:
                filename = "./server/index.html";
                break;
            case 1:
                filename = args[0];
                break;
            default:
                System.out.println("Zly format argumentov, koniec");
                break;
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
                serverSocket = new ServerSocket(80);
                connectionSocket = serverSocket.accept();
                //Vymena kluca
                int key;
                try {
                    key = exchangeKey(connectionSocket);
                } catch (Exception e) {
                    System.out.println("Key exchange failed");
                    return;
                }

                OutputStream output = connectionSocket.getOutputStream();

                File file = new File(filename);
                if(!file.exists()){
                    System.out.println("Subor bol zmazany alebo presunuty, koniec");
                    throw new IOException("Subor neexistuje");
                }

                Scanner reader = new Scanner(file);

                //Precita sa subor
                StringBuilder fileContent = new StringBuilder();
                while (reader.hasNextLine()) {
                    fileContent.append(reader.nextLine());
                    fileContent.append('\n');
                }

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

            //Nahodne vygenerovane p,g,a
            int p = new Random().nextInt(1000000);
            int g = new Random().nextInt(100);
            int a = new Random().nextInt(10000);
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
