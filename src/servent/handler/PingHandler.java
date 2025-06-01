package servent.handler;

import app.AppConfig;
import servent.message.Message;
import servent.message.Ping;
import servent.message.Pong;
import servent.message.util.MessageUtil;

public class PingHandler implements MessageHandler{
    Ping ping;
    public PingHandler(Message ping) {
        this.ping = (Ping) ping;
    }
    @Override
    public void run() {
        Message m = new Pong(AppConfig.myServentInfo.getListenerPort(), ping.getSenderPort());
        MessageUtil.sendMessage(m);
    }
}
