import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class ClientTest {
    public static void main(String[] args) {
        String host = "127.0.0.1";
        final int PORT = 8001;

        try {
            Socket clientSocket = new Socket("127.0.0.1", ServerTest.PORT);
            ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
            out.flush();
            ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
            Scanner scanner = new Scanner(System.in);

            System.out.println("SUCCESSFULLY CONNECTED TO " + host + ":" + PORT + ".");
            System.out.println("Use numbers to bid. Press '.' to quit bidding.");
            System.out.println("============================");

            System.out.print("-- Enter your name: ");
            String bidderName = scanner.nextLine().trim();
            out.writeObject(AuctionMessageTest.join(bidderName));
            out.flush();

            String initialPrice = in.readLine();                        // Client's FIRST ever message from Server
            if (initialPrice != null) System.out.println(initialPrice); // Makes sure non-null

//            System.out.print(">> Quick! Insert higher price >> ");

            Runnable runnable = () -> {
                try {
                    while (true) {
                        Object obj = in.readObject();
                        if (obj instanceof AuctionMessageTest message) {
                            System.out.println("\n[SERVER]: " + message);
                            if (message.getType() == AuctionMessageTest.Type.QUIT) break;
                        }
                            System.out.print(">> Quick! Insert higher price >> ");
                    }
                } catch (IOException _) {
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            };

            Thread readerThread = new Thread(runnable);
            readerThread.setDaemon(true);
            readerThread.start();

            while (true) {
                String line = scanner.nextLine().trim();        // Client INPUTS a price
                out.writeObject(line);                          // Client SENDS to Server

                if (".".equals(line)) {
                    out.writeObject(AuctionMessageTest.quit(bidderName));
                    readerThread.join(1000);
                    break;
                }
                try {
                    long price = Long.parseLong(line);
                    out.writeObject(AuctionMessageTest.bid(bidderName, price));
                    out.flush();
                } catch (NumberFormatException e) {
                    System.out.println("[ERROR]: fill in A NUMBER.");
                    System.out.print(">> Quick! Insert higher price >> ");
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