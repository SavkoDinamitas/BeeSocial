package servent.message;

public class Pong extends BasicMessage{
    public Pong(int senderPort, int receiverPort) {
        super(MessageType.PONG, senderPort, receiverPort);
    }
}
