
/**
 * Imports
 */
import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * "Main" class Chat
 */
/**
 * Performs the main functions of the program
 */
public class Chat {

    /**
     * Declarations
     */
    static Semaphore semaThingy = new Semaphore(1);
    String myId;
    int myPort;
    String myIP;
    String nextIP;
    int nextPort;

    /**
     * Class Server
     */
    /**
     * Handles server-side operations
     */
    public class Server implements Runnable {

        /**
         * run() - Server version
         */
        public void run() {
            ServerSocket servSock = null;
            /**
             * Attempt to create new ServerSocket
             */
            try {
                servSock = new ServerSocket(myPort);
            } catch (IOException e) {
                e.printStackTrace();
            }
            /**
             * Runs while the overall program is running
             */
            while (true) {
                MainMessage reply = null;
                Socket clntSock;
                /**
                 * Gets the users input
                 */
                try {
                    clntSock = servSock.accept();
                    ObjectInputStream ois = new ObjectInputStream(clntSock.getInputStream());
                    ObjectOutputStream oos = new ObjectOutputStream(clntSock.getOutputStream());
                    MainMessage m = (MainMessage) ois.readObject();
                    switch (m.messageID) {
                        case REQUEST:
                            System.out.println("'REQUEST' received: " + m.myPort);
                             {
                                try {
                                    semaThingy.acquire();
                                    reply = new MainMessage(nextPort, nextIP);
                                    semaThingy.release();
                                } catch (InterruptedException ex) {
                                    Logger.getLogger(Chat.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                             {
                                try {
                                    semaThingy.acquire();
                                    nextIP = m.myIP;
                                    nextPort = m.myPort;
                                    semaThingy.release();
                                } catch (InterruptedException ex) {
                                    Logger.getLogger(Chat.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }

                            oos.writeObject(reply);
                            break;
                        case PUT:
                            System.out.println("'PUT' received from: " + m.idSource);
                            if ((myId).equals(m.idDest)) {
                                System.out.println(m.text);
                            } else if ((myId).equals(m.idSource)) {
                                System.out.println("Message was not received!");
                                //reply = new MainMessage(myPort, myIP);
                                //oos.writeObject(reply);
                            } else {
                                Socket socket;
                                try {
                                    try {
                                        semaThingy.acquire();
                                        socket = new Socket(nextIP, nextPort);
                                        semaThingy.release();
                                        oos = new ObjectOutputStream(socket.getOutputStream());
                                        ois = new ObjectInputStream(socket.getInputStream());
                                        oos.writeObject(m);
                                        socket.close();
                                    } catch (InterruptedException ex) {
                                        Logger.getLogger(Chat.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            break;
                        case LEAVE:
                            System.out.println("'LEAVE' received: " + m.myPort);
                            if (myIP.equals(m.myIP) && myPort == m.myPort) {
                                try {
                                    semaThingy.acquire();
                                    nextIP = myIP;
                                    nextPort = myPort;
                                    semaThingy.release();
                                } catch (InterruptedException ex) {
                                    Logger.getLogger(Chat.class.getName()).log(Level.SEVERE, null, ex);
                                }
                                System.out.println("Successfully left chat");
                                return;
                            }
                            Socket socket;
                            try {
                                socket = new Socket(nextIP, nextPort);
                                oos = new ObjectOutputStream(socket.getOutputStream());
                                ois = new ObjectInputStream(socket.getInputStream());
                                oos.writeObject(m);
                                socket.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (nextIP.equals(m.myIP) && nextPort == m.myPort) {
                                try {
                                    semaThingy.acquire();
                                    nextIP = m.nextIP;
                                    nextPort = m.nextPort;
                                    semaThingy.release();
                                } catch (InterruptedException ex) {
                                    Logger.getLogger(Chat.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                            break;
                        default:
                            break;
                    }
                    clntSock.close();

                } /**
                 * 2 catch statements for the potential errors
                 */
                catch (IOException e) {
                    e.printStackTrace();

                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Class Server
     */
    /**
     * Handles client-side operations
     */
    public class Client implements Runnable {

        /**
         * run() - Client version
         */
        public void run() {
            /**
             * Uses a scanner to get user input from the command line
             */
            Scanner scanner = new Scanner(System.in);
            /**
             * Resets the variables
             */
            MainMessage m = null;
            String ip = null;
            boolean createSocket = true;
            int port = 0;
            /**
             * Runs while the overall program is running
             */
            while (true) {
                createSocket = true;
                /**
                 * Get User input
                 */
                System.out.println("Enter your message (REQUEST, PUT, LEAVE): ");
                String msgInput = scanner.next().toUpperCase();
                /**
                 * Responds based on input
                 */
                switch (msgInput) {
                    case "REQUEST":
                        System.out.println("Input IP to connect to:");
                        ip = scanner.next();
                        System.out.println("Input port to connect to:");
                        port = scanner.nextInt();
                        System.out.println("Connecting on specified port: " + port);
                        System.out.println("Sending Request...");
                        m = new MainMessage(myIP, myPort);
                        break;
                    case "PUT":
                        String receiverId;
                        String msgToSend;
                        System.out.println("Input the recipient of message: ");
                        receiverId = scanner.next();
                        scanner.nextLine();
                        System.out.println("Input message: ");
                        msgToSend = scanner.nextLine();
                        m = new MainMessage(myId, receiverId, msgToSend);
                        break;
                    case "LEAVE":
                        System.out.println("Leaving chat");
                        m = new MainMessage(myIP, myPort, nextIP, nextPort);
                        break;
                }
                /**
                 * Runs when we want to create a new socket
                 */
                if (createSocket == true) {
                    Socket socket;
                    try {
                        if (m.messageID != MSG.REQUEST) {
                            try {
                                semaThingy.acquire();
                                ip = nextIP;
                                port = nextPort;
                                semaThingy.release();
                            } catch (InterruptedException ex) {
                                Logger.getLogger(Chat.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        socket = new Socket(ip, port);//ip and port to connect to
                        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                        oos.writeObject(m);
                        if (m.messageID == MSG.REQUEST) {
                            try {
                                System.out.println("Receiving Reply");
                                MainMessage received = (MainMessage) ois.readObject();
                                if (received.messageID == MSG.REPLY) {
                                    System.out.println("IP: " + received.nextIP + " " + received.nextPort);
                                    try {
                                        semaThingy.acquire();
                                        nextIP = received.nextIP;
                                        nextPort = received.nextPort;
                                        semaThingy.release();
                                    } catch (InterruptedException ex) {
                                        Logger.getLogger(Chat.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                        socket.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                if (msgInput == "LEAVE") {
                    return;
                }
            }
        }
    }

    /**
     *
     * @param Id
     * @param port
     * @param Ip
     */
    public Chat(String Id, int port, String Ip) {
        myIP = Ip;
        myPort = port;
        myId = Id;
        nextPort = port;
        nextIP = Ip;
        /**
         * Initialises this part of the P2P network
         */
        Thread server = new Thread(new Server());
        Thread client = new Thread(new Client());
        server.start();
        client.start();
        try {
            client.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            server.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Main()
     *
     * @param args
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("(err1)Parameter: <id> <port>");
            return;
        }
        /**
         * Attempts a connection
         */
        try {
            InetAddress ip = InetAddress.getLocalHost();
            Chat chat = new Chat(args[0], Integer.parseInt(args[1]), ip.getHostAddress());
        } catch (UnknownHostException p) {
            System.out.println("(err2)Parameter: <id> <port>");
        }
    }
}
