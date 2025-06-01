package servent;

import app.AppConfig;
import app.ServentInfo;
import servent.message.DeleteMessage;
import servent.message.Message;
import servent.message.TokenArrayMessage;
import servent.message.util.MessageUtil;

public class RepeatDelete implements Runnable{
    ServentInfo del;

    public RepeatDelete(ServentInfo del){
        this.del = del;
    }
    @Override
    public void run() {
        while (!AppConfig.chordState.isGotDeleteBack(del.getListenerPort())){
            Message m = new DeleteMessage(AppConfig.myServentInfo.getListenerPort(), AppConfig.chordState.getNextNodePort(), del);
            MessageUtil.sendMessage(m);
            Message m1 = new TokenArrayMessage(AppConfig.myServentInfo.getListenerPort(), AppConfig.chordState.getNextNodePort(),
                    AppConfig.suzukiKasamiMutex.getLn(), AppConfig.suzukiKasamiMutex.getRn());
            MessageUtil.sendMessage(m1);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

