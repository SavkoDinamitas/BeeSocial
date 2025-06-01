package servent.handler;

import app.AppConfig;
import servent.message.DeleteMessage;
import servent.message.Message;
import servent.message.StopMessage;
import servent.message.util.MessageUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class StopMessageHandler implements MessageHandler{
    private StopMessage message;

    public StopMessageHandler (Message message){
        this.message = (StopMessage) message;
    }

    @Override
    public void run() {
        //send delete message to bootstrap
        int bsPort = AppConfig.BOOTSTRAP_PORT;
        Socket bsSocket = null;
        try {
            bsSocket = new Socket("localhost", bsPort);
            PrintWriter bsWriter = new PrintWriter(bsSocket.getOutputStream());
            bsWriter.write("Delete\n" + message.getDeleteInfo().getListenerPort() + "\n");
            bsWriter.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        AppConfig.chordState.deleteNode(message.getDeleteInfo());
//        try {
//            AppConfig.suzukiKasamiMutex.lock();
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
        Message m = new DeleteMessage(AppConfig.myServentInfo.getListenerPort(), AppConfig.chordState.getNextNodePort(), message.getDeleteInfo());
        MessageUtil.sendMessage(m);
    }
}
