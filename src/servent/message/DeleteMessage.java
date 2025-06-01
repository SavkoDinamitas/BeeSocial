package servent.message;

import app.ServentInfo;

import java.io.Serial;

public class DeleteMessage extends BasicMessage{
    @Serial
    private static final long serialVersionUID = 5688145488432742900L;

    private ServentInfo deleteNode;

    public DeleteMessage(int senderPort, int receiverPort, ServentInfo deleteNode) {
        super(MessageType.DELETE, senderPort, receiverPort);
        this.deleteNode = deleteNode;
    }

    public ServentInfo getDeleteNode() {
        return deleteNode;
    }
}
