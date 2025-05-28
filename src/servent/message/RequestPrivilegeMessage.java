package servent.message;

import java.io.Serial;
import java.io.Serializable;

public class RequestPrivilegeMessage extends BasicMessage implements Serializable {
    @Serial
    private static final long serialVersionUID = -4285984487134654110L;
    private int requestNumber;
    public RequestPrivilegeMessage(int senderPort, int receiverPort, int requestNumber) {
        super(MessageType.REQUEST_PRIVILEGE, senderPort, receiverPort);
        this.requestNumber = requestNumber;
    }

    public int getRequestNumber() {
        return requestNumber;
    }
}
