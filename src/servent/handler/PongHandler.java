package servent.handler;

import app.AppConfig;
import servent.message.Message;
import servent.message.Pong;

public class PongHandler implements MessageHandler{
    Pong pong;

    public PongHandler(Message message){
        pong = (Pong) message;
    }
    @Override
    public void run() {
        if(pong.getSenderPort() == AppConfig.chordState.getNextNodePort())
            AppConfig.chordState.setSuccessorAlive(true);
        else{
            AppConfig.chordState.getRequestedPings().put(pong.getSenderPort(), true);
        }
    }
}
