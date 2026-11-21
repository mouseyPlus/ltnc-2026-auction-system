import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.EOFException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerTest {
    private ServerSocket serverSocket;

    public static final int PORT = 8888;
    private static long currentPrice = 100;
    private static String highestBidder = "Unknown";
    private final List<ObjectOutputStream> clientOutputs = new CopyOnWriteArrayList<>(); // save every Client's output for broadcasting

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("AUCTION SERVER STARTED ON PORT [" + port + "].");
        System.out.println("========================");

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

    private void send(ObjectOutputStream out, AuctionMessageTest msg) throws IOException {
        synchronized (out) {
            out.writeObject(msg);
            out.flush();
            out.reset(); // avoid referencing to old Object
        }
    }

    public void broadcast(AuctionMessageTest msg) {             // announce bidding updates to all Clients
        for (ObjectOutputStream out : clientOutputs) {
            try {
                send(out, msg);
            } catch (IOException e) {
                clientOutputs.remove(out);
            }
        }
    }

    public synchronized AuctionMessageTest placeBid(String bidderName, long newPrice) {          // synchronization
        if (newPrice > currentPrice) {
            currentPrice = newPrice;
            highestBidder = bidderName;
            return AuctionMessageTest.bidSuccessful(currentPrice, highestBidder);
        }
        if (newPrice <= 0) return AuctionMessageTest.bidRejected(currentPrice, highestBidder,
                "[STUPIDITY_ALERT]: ARE YOU STUPID, USER?");
        return AuctionMessageTest.bidRejected(currentPrice, highestBidder,
                "New price must be HIGHER than " + currentPrice + ".");
    }

    public void stop() {
        closeQuietly(serverSocket);
    }

    private static class ClientHandler extends Thread {                 // how to handle 1 Client
        private final ServerTest server;
        private final Socket clientSocket;
        private ObjectOutputStream out;
        private ObjectInputStream in;

        public ClientHandler(Socket clientSocket, ServerTest server) {  // constructor
            this.clientSocket = clientSocket;
            this.server = server;
        }

        @Override
        public void run() {
            try {
                out = new ObjectOutputStream(clientSocket.getOutputStream());
                out.flush();
                in = new ObjectInputStream(clientSocket.getInputStream());

                server.clientOutputs.add(out);      // add Client to broadcast
                String bidderName = "Anonymous";

                while (true) {
                    Object obj = in.readObject();
                    if (!(obj instanceof AuctionMessageTest msg)) {
//                        server.send(out, AuctionMessageTest.error("Unsupported bidding format."));
                        continue;
                    }

                    if (msg.getSender() != null && !msg.getSender().isBlank()) {        // make sure non-null and unblank
                        bidderName = msg.getSender().trim();
                    }

                    switch (msg.getType()) {
                        case JOIN -> {
                            System.out.println(bidderName + " joined the auction.");
                            server.send(out, AuctionMessageTest.start(currentPrice, highestBidder));
                        }
                        case BID -> {
                            if (msg.getBidAmount() == null) {
                                server.send(out, AuctionMessageTest.error("fill in A NUMBER."));
                                break;
                            }

                            long price = msg.getBidAmount();
                            System.out.println(bidderName + " bid " + price);

                            AuctionMessageTest result = server.placeBid(bidderName, price);

                            if (result.getType() == AuctionMessageTest.Type.BID_SUCCESSFUL) {
                                server.broadcast(result);
                            } else {
                                server.send(out, result);
                            }
                        }
                        case QUIT -> {
                            System.out.println(bidderName + " quit bidding.");
                            server.send(out, AuctionMessageTest.quit(bidderName));
                            return;
                        }
                        default ->
                                server.send(out, AuctionMessageTest.error("Unhandled message type: " + msg.getType()));
                    }
                }
            } catch (EOFException e) {
                System.out.println("Client disconnected unexpectedly.");
            } catch (IOException e) {
                System.out.println("Connection error with client: " + e.getMessage());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                server.clientOutputs.remove(out);
                closeQuietly(in);
                closeQuietly(out);
                closeQuietly(clientSocket);
            }
        }
    }

    public static void main(String[] args) {
        ServerTest server = new ServerTest();
        try {
            server.start(PORT);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            server.stop();
        }
    }
}