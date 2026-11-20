import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Scanner;

public class ClientTest {
    public static void main(String[] args) {
        String host = "127.0.0.1";
        final int PORT = 8001;

        try {

            /*
            Socket clientSocket: creates a Thread
            Scanner scanner: makes price input
            PrintWriter out: sends price to Server, then,
            BufferedReader in: receives response from Server.
             */

            Socket clientSocket = new Socket(host, PORT);
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            Scanner scanner = new Scanner(System.in);

            // welcome hello hi smth message. print on Client
            System.out.println("SUCCESSFULLY CONNECTED TO " + host + ":" + PORT + ".");
            System.out.println("Use numbers to bid. Press '.' to quit bidding.");
            System.out.println("============================");

            String initialPrice = in.readLine();
            // Client's FIRST ever message from Server
            // [START]: Initial price...

            if (initialPrice != null) System.out.println(initialPrice); // Makes sure non-null
            System.out.print(">> Quick! Insert higher price >> ");

            Runnable runnable = () -> {
                try {
                    String serverMsg;
                    while ((serverMsg = in.readLine()) != null) {
                        System.out.println("[SERVER]: " + serverMsg);
                        if ("[QUIT]: Auction terminated.".equals(serverMsg)) break;

                        System.out.print(">> Quick! Insert higher price >> ");
                    }
                } catch (IOException e) {
                }
            };

            Thread readerThread = new Thread(runnable);
            readerThread.setDaemon(true);
            readerThread.start();

            while (true) {
                String price = scanner.nextLine();      // Client INPUTS a price

                /*
                if Client really wanna bid
                it has to send price to Server FIRST
                then receive response from Server SECOND
                and [BID_SUCCESSFUL] or [BID_REJECTED] or [STUPIDITY_ALERT] will be printed on Client
                 */

                out.println(price);

                /*
                if Client wanna quit, press '.'
                it has to request quit FIRST
                then receive acceptance from Server SECOND
                and [QUIT] will be printed on Client.
                 */

                if (".".equals(price.trim())) {
                    readerThread.join(1000);
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("[ERROR]: Cannot connect or Disconnected.");
            e.printStackTrace();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}