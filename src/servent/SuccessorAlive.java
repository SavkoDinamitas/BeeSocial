package servent;

import app.AppConfig;
import app.ServentInfo;
import servent.message.*;
import servent.message.util.MessageUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class SuccessorAlive implements Runnable {
    private int successor;
    @Override
    public void run() {
        try {
            while (true){
                if(AppConfig.chordState.getSuccessorTable()[0] == null)
                    continue;
                Thread.sleep(AppConfig.heartbeatTimeoutLower);
                if(AppConfig.chordState.isSuccessorAlive()){
                    AppConfig.chordState.setSuccessorAlive(false);
                    Message m = new Ping(AppConfig.myServentInfo.getListenerPort(), AppConfig.chordState.getNextNodePort());
                    successor = AppConfig.chordState.getNextNodePort();
                    MessageUtil.sendMessage(m);
                }
                else{
                    //najgori kod ikada
                    List<Integer> help = new ArrayList<>();
                    for(var x : AppConfig.chordState.getAllNodeInfo()){
                        if(x.getListenerPort() != AppConfig.chordState.getNextNodePort() && x.getListenerPort() != AppConfig.myServentInfo.getListenerPort()){
                            help.add(x.getListenerPort());
                        }
                    }
                    for(int i = 0; i < 2; i++){
                        if(help.size() > i){
                            Message m = new RequestPing(AppConfig.myServentInfo.getListenerPort(), help.get(i), AppConfig.chordState.getNextNodePort());
                            MessageUtil.sendMessage(m);
                        }
                    }
                    Thread.sleep(AppConfig.heartbeatTimeoutHigher - AppConfig.heartbeatTimeoutLower);
                    if(!AppConfig.chordState.isSuccessorAlive() && successor == AppConfig.chordState.getNextNodePort()){
                        ServentInfo del = new ServentInfo("localhost", AppConfig.chordState.getNextNodePort());
                        int bsPort = AppConfig.BOOTSTRAP_PORT;
                        Socket bsSocket = null;
                        try {
                            bsSocket = new Socket("localhost", bsPort);
                            PrintWriter bsWriter = new PrintWriter(bsSocket.getOutputStream());
                            bsWriter.write("Delete\n" + del.getListenerPort() + "\n");
                            bsWriter.flush();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        AppConfig.chordState.deleteNode(del);
                        successor = AppConfig.chordState.getNextNodePort();
                        for(var x : AppConfig.chordState.getAllNodeInfo()){
                            Message m = new FailMessage(AppConfig.myServentInfo.getListenerPort(), x.getListenerPort(), del);
                            MessageUtil.sendMessage(m);
                        }
                       Thread.sleep(3000);
                        Message m1 = new TokenArrayMessage(AppConfig.myServentInfo.getListenerPort(), AppConfig.chordState.getNextNodePort(),
                                AppConfig.suzukiKasamiMutex.getLn(), AppConfig.suzukiKasamiMutex.getRn());
                        MessageUtil.sendMessage(m1);

//                        successor = AppConfig.chordState.getNextNodePort();
//                        Thread t = new Thread(new RepeatDelete(del));
//                        t.start();
                    }
                }
            }
        } catch (InterruptedException e) {
            AppConfig.timestampedStandardPrint("Gasim stoni tenis");
        } catch (Exception exception){
            AppConfig.timestampedErrorPrint("Usro se i umro");
            exception.printStackTrace();
        }
    }
}
