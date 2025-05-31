package servent.handler;

import app.AppConfig;
import app.ChordState;
import app.ServentInfo;
import servent.message.Message;
import servent.message.PrivilegeMessage;
import servent.message.util.MessageUtil;

public class PrivilegeMessageHandler implements MessageHandler{
    PrivilegeMessage message;

    public PrivilegeMessageHandler(Message message) {
        this.message = (PrivilegeMessage) message;
    }

    @Override
    public void run() {
        if(message.getPrivilegeReciever() == AppConfig.myServentInfo.getListenerPort()){
            AppConfig.suzukiKasamiMutex.handleRecievePrivilege(message);
        }
        else{
            ServentInfo receiver = AppConfig.chordState.getNextNodeForKey(ChordState.chordHash(message.getPrivilegeReciever()));
            Message m = new PrivilegeMessage(message.getSenderPort(), receiver.getListenerPort(), message.getQueue(), message.getLn(), message.getPrivilegeReciever());
            MessageUtil.sendMessage(m);
        }
    }
}
