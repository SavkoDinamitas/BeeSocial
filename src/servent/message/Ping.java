package servent.message;

public class Ping extends BasicMessage{
    public Ping(int senderPort, int receiverPort) {
        super(MessageType.PING, senderPort, receiverPort);
    }
}
