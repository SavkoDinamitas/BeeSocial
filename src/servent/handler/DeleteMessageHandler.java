package servent.handler;

import app.AppConfig;
import servent.message.DeleteMessage;
import servent.message.Message;
import servent.message.util.MessageUtil;

public class DeleteMessageHandler implements MessageHandler{
    private DeleteMessage message;

    public DeleteMessageHandler(Message message){
        this.message = (DeleteMessage) message;
    }

    @Override
    public void run() {
        if(message.getSenderPort() != AppConfig.myServentInfo.getListenerPort()){
            AppConfig.chordState.deleteNode(message.getDeleteNode());
            Message m = new DeleteMessage(message.getSenderPort(), AppConfig.chordState.getNextNodePort(), message.getDeleteNode());
            MessageUtil.sendMessage(m);
        }
        else{
            //AppConfig.chordState.setGotDeleteBack(message.getDeleteNode().getListenerPort());
        }
    }
}
