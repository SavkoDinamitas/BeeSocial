package servent.message;

import app.ServentInfo;

public class FailMessage extends BasicMessage{
    private ServentInfo failedNode;

    public FailMessage(int senderPort, int receiverPort, ServentInfo failedNode) {
        super(MessageType.FAIL, senderPort, receiverPort);
        this.failedNode = failedNode;
    }

    public ServentInfo getFailedNode() {
        return failedNode;
    }
}
