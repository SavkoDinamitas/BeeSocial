package servent.message;

import java.io.Serial;

public class FinishedReorganizationMessage extends BasicMessage{
    @Serial
    private static final long serialVersionUID = -2968402095087463637L;

    public FinishedReorganizationMessage(int senderPort, int receiverPort) {
        super(MessageType.REORGANIZED, senderPort, receiverPort);
    }
}
