package servent.handler;

import app.AppConfig;
import servent.message.Message;
import servent.message.Ping;
import servent.message.Pong;
import servent.message.RequestPing;
import servent.message.util.MessageUtil;

public class RequestPingHandler implements MessageHandler{

    private RequestPing message;

    public RequestPingHandler (Message m){
        message = (RequestPing) m;
    }
    @Override
    public void run() {
        Message m = new Ping(AppConfig.myServentInfo.getListenerPort(), message.getRequestedPing());
        MessageUtil.sendMessage(m);
        try {
            Thread.sleep(AppConfig.heartbeatTimeoutLower);
            if(AppConfig.chordState.getRequestedPings().getOrDefault(message.getRequestedPing(), false)){
                Message m1 = new Pong(message.getRequestedPing(), message.getSenderPort());
                MessageUtil.sendMessage(m1);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}
