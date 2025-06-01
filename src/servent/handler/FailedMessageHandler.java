package servent.handler;

import app.AppConfig;
import app.ServentInfo;
import servent.message.FailMessage;
import servent.message.Message;

public class FailedMessageHandler implements MessageHandler{
    private FailMessage message;

    public FailedMessageHandler(Message message){
        this.message = (FailMessage) message;
    }
    @Override
    public void run() {
        AppConfig.chordState.deleteNode(message.getFailedNode());
    }
}
