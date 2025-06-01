package servent.message;

public class RequestPing extends BasicMessage{
    int requestedPing;

    public RequestPing(int senderPort, int receiverPort, int requestedPing) {
        super(MessageType.PING_CHECK, senderPort, receiverPort);
        this.requestedPing = requestedPing;
    }

    public int getRequestedPing() {
        return requestedPing;
    }
}
