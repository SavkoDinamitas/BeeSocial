package servent.handler;

import app.AppConfig;
import servent.message.FinishedReorganizationMessage;
import servent.message.Message;

public class ReorganizedMessageHandler implements MessageHandler{
    private FinishedReorganizationMessage finishedReorganizationMessage;

    public ReorganizedMessageHandler(Message finishedReorganizationMessage) {
        this.finishedReorganizationMessage = (FinishedReorganizationMessage) finishedReorganizationMessage;
    }

    @Override
    public void run() {
        AppConfig.suzukiKasamiMutex.unlock();
    }
}
