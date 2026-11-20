import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerTest {
    private ServerSocket serverSocket;
    private static int currentPrice = 100;  // set up static initial price
    private final List<PrintWriter> clientWriters = new CopyOnWriteArrayList<>(); // save every Client's output for broadcasting

    public void start(int port) throws IOException {
        // start Server and make some hello hi log
        serverSocket = new ServerSocket(port);
        System.out.println("AUCTION SERVER STARTED ON PORT [" + port + "].");
        System.out.println("========================");

        // multi-thread is here guyssssssss
        // it can hold infinite entries
        while (true) {
            new ClientHandler(serverSocket.accept(), this).start(); // clientSocket and THIS own server
        }
    }

    // closeQuietly closes in, out, clientSocket, serverSocket without code smells
    private static void closeQuietly(AutoCloseable r) {
        if (r != null) {
            try {
                r.close();
            } catch (Exception e) {
                System.out.println("[ERROR]: A variable was left unwritten!");
                e.printStackTrace();
            }
        }
    }

    // synchronization to avoid data conflict
    public synchronized String getPrice() {
        return "Current price: " + currentPrice + ".";
    }
    public synchronized String placeBid(int newPrice) {
        if (newPrice > currentPrice) {
            currentPrice = newPrice;
            return "[BID_SUCCESSFUL]: New highest price is " + currentPrice + " by someone!";
        }
        else if (newPrice <= 0) return "[STUPIDITY_ALERT]: ARE YOU STUPID, USER?";
        else return "[BID_REJECTED]: New price must be HIGHER than " + currentPrice + ".";
    }

    // announce bidding updates to all Clients
    public void broadcast(String message) {
        for (PrintWriter writer : clientWriters) {
            writer.println(message);
        }
    }

    public void stop() {
        closeQuietly(serverSocket);
    }

    private static class ClientHandler extends Thread {
        private final ServerTest server;
        private final Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;

        public ClientHandler(Socket clientSocket, ServerTest server) {  // constructor
            this.clientSocket = clientSocket;
            this.server = server;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                server.clientWriters.add(out);                          // add Clients to broadcast

                out.println("[START]: Initial price: " + currentPrice); // Server's FIRST ever message to Client

                String inputLine;
                while ((inputLine = in.readLine()) != null) {           // Read Client's price
                    String line = inputLine.trim();
                    System.out.println("Someone texted: " + line);      // Client logs will be posted on Server

                    /*
                    if Client requests quit
                    quit log is printed on Server
                    Server accepts and SENDS [QUIT] to Client
                     */

                    if (".".equals(line)) {
                        System.out.println("Someone quit bidding.");
                        out.println("[QUIT]: Auction terminated.");
                        break;
                    }

                    /*
                    parse String_line to int_price
                    if not a number (stupid bidder)
                    curse them in Client
                    if a number (good bidder)
                    ClientHandler uses placeBid from Server
                    and returns to Client
                     */

                    try {
                        int price = Integer.parseInt(line);
                        System.out.println("Someone bid " + price);

                        /*
                        "result" holds the bidding message: [SUCCESSFUL, REJECTED, STUPIDITY]
                        if [BID_SUCCESSFUL]
                        show for ALL CLIENTS (so that they are aware of price changes)
                        if not
                        continue only showing for that bidder (price remains unchanged)
                         */

                        String result = server.placeBid(price);
                        if (result.startsWith("[BID_SUCCESSFUL]")) server.broadcast(result);
                        else out.println(result);

                    } catch (NumberFormatException e) {
                        out.println("[ERROR]: fill in A NUMBER.");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                server.clientWriters.remove(out); // remove Client from broadcast
                closeQuietly(in);
                closeQuietly(out);
                closeQuietly(clientSocket);
            }
        }
    }

    public static void main(String[] args) {
        ServerTest server = new ServerTest();
        try {
            server.start(8001);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            server.stop();
        }
    }
}