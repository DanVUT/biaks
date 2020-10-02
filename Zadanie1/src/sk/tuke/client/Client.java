package sk.tuke.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import sk.tuke.communication.ByteConverter;
import sk.tuke.encryption.RC4;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;
import java.awt.*;


public class Client extends Application{
    //Definicia jednoducheho UI na zobrazovanie HTML v JavaFX
    @Override
    public void start(Stage stage) throws Exception {
        List<String> params = getParameters().getRaw();
        String html = params.get(params.size() - 1);

        stage.setTitle("BIaKS Zadanie 1");
        WebView web = new WebView();
        web.getEngine().loadContent(html);
        web.prefHeightProperty().bind(stage.heightProperty());
        web.prefWidthProperty().bind(stage.heightProperty());
        Label label = new Label("index.html");

        VBox pane = new VBox(label,web);
        VBox.setMargin(label, new Insets(5, 0, 5,5));
        Scene scene = new Scene(pane, 500, 500);

        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();
    }

    //Format argumentov "IP port" / "IP" / ""
    public static void main(String[] args) {
        String hostname;
        int port;
        switch (args.length){
            case 0:
                hostname = "127.0.0.1";
                port = 80;
                break;
            case 1:
                hostname = args[0];
                port = 80;
                break;
            case 2:
                hostname = args[0];
                try{
                    port = Integer.parseInt(args[1]);
                } catch (Exception e){
                    System.out.println("Zle zadany port, koniec");
                    return;
                }
                break;
            default:
                System.out.println("Zly format argumentov, koniec");
                return;
        }
        Socket connectionSocket = null;
        try{
            //Pokus o pripojenie k serveru
            connectionSocket = new Socket(hostname, port);
            //Vymena sifrovacieho kluca
            int key;
            try {
                key = exchangeKey(connectionSocket);
            } catch (Exception e){
                System.out.println("Key exchange failed");
                return;
            }
            InputStream input = connectionSocket.getInputStream();

            //Prijatie dlzky HTML
            int contentLength = RC4.receiveInt(key, input);
            //Prijatie HTML
            byte[] content = RC4.receiveContent(contentLength, key, input);
            //Uzatvorenie spojenia
            connectionSocket.close();

            //Po prijati HTML stranky sa zobrazi GUI so strankou. Do JavaFX sa ako argument posle prijate HTML ako string
            launch(new String(content));
        } catch (Exception e){
            System.out.println("Chyba spojenia k serveru, koniec");
            try{
                connectionSocket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (NullPointerException ex){}
        }
    }

    public static int exchangeKey(Socket connectionSocket){
        int key = -1;
        try {
            InputStream input = connectionSocket.getInputStream();
            OutputStream output = connectionSocket.getOutputStream();
            int p;
            int g;
            int A;
            int b = new Random().nextInt(10000);
            int B;
            //Prijme sa  p,g,A
            p = ByteConverter.fromByteArray(input.readNBytes(4));
            g = ByteConverter.fromByteArray(input.readNBytes(4));
            A = ByteConverter.fromByteArray(input.readNBytes(4));
            //Vypocita sa B
            B = (int) Math.pow(g, b) % p;
            //Odosle sa B
            output.write(ByteConverter.toByteArray(B));
            //Vypocita sa kluc
            key = (int) Math.pow(A, b) % p;

        } catch (Exception e){

        }
        return key;
    }


}
