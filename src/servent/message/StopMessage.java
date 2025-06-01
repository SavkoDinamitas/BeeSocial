package servent.message;

import app.ServentInfo;

public class StopMessage extends BasicMessage{
    private ServentInfo deleteInfo;

    public StopMessage(int senderPort, int receiverPort, ServentInfo deleteInfo) {
        super(MessageType.STOP, senderPort, receiverPort);
        this.deleteInfo = deleteInfo;
    }

    public ServentInfo getDeleteInfo() {
        return deleteInfo;
    }
}
