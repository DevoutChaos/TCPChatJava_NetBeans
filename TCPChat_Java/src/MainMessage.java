
import java.io.Serializable;

/**
 * Class MainMessage
 */
public class MainMessage implements Serializable {

    /**
     * Declaration
     */
    MSG messageID; // Id of the message REQUEST = 0, REPLY = 1, LEAVE =2, PUT = 3
    int myPort;
    String myIP;
    String nextIP;
    int nextPort;
    String idSource;
    String idDest;
    String text;

    /**
     *
     */
    public MainMessage() {

    }

    /**
     * Used when user input is 'LEAVE'
     * @param ip
     * @param nxtIp
     * @param port
     * @param nxtPort
     */
    public MainMessage(String ip, int port, String nxtIp, int nxtPort) {
        messageID = MSG.LEAVE;
        myIP = ip;
        myPort = port;
        nextIP = nxtIp;
        nextPort = nxtPort;
    }

    /**
     * Used when user input is 'REQUEST'
     * @param ip
     * @param port
     */
    public MainMessage(String ip, int port) {
        messageID = MSG.REQUEST;
        myIP = ip;
        myPort = port;
    }

    /**
     * Used when user input is 'REPLY'
     * @param port
     * @param ip
     */
    public MainMessage(int port, String ip) {
        messageID = MSG.REPLY;
        nextPort = port;
        nextIP = ip;
    }

    /**
     * Used when user input is 'PUT'
     * @param idSrc
     * @param idDes
     * @param txt
     */
    public MainMessage(String idSrc, String idDes, String txt) {
        messageID = MSG.PUT;
        idSource = idSrc;
        idDest = idDes;
        text = txt;
    }

    /**
     *
     * @param msg
     * @param Ip
     * @param Port
     */
    public MainMessage(MSG msg, String Ip, int Port) {
        this.messageID = msg;
        myIP = Ip;
        myPort = Port;
    }
}
