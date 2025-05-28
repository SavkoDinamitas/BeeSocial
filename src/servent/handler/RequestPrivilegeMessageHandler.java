package servent.handler;


import app.AppConfig;
import servent.message.Message;
import servent.message.RequestPrivilegeMessage;
import servent.message.util.MessageUtil;

public class RequestPrivilegeMessageHandler implements MessageHandler {
    RequestPrivilegeMessage message;

    public RequestPrivilegeMessageHandler(Message message) {
        this.message = (RequestPrivilegeMessage) message;
    }
    @Override
    public void run() {
        if(message.getSenderPort() != AppConfig.myServentInfo.getListenerPort()){
            int next = AppConfig.chordState.getNextNodePort();
            Message m = new RequestPrivilegeMessage(message.getSenderPort(), next, message.getRequestNumber());
            MessageUtil.sendMessage(m);
            AppConfig.suzukiKasamiMutex.handleRequest(message);
        }
    }
}
