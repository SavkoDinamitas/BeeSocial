package servent.message;

import java.io.Serial;

public class ListFilesMessage extends BasicMessage{
    @Serial
    private static final long serialVersionUID = 3157653867578676116L;

    private int requiredUser;

    public ListFilesMessage(int senderPort, int receiverPort, int requiredUser) {
        super(MessageType.LIST, senderPort, receiverPort);
        this.requiredUser = requiredUser;
    }

    public int getRequiredUser() {
        return requiredUser;
    }
}
