package servent.handler;

import app.AppConfig;
import app.ChordState;
import app.ServentInfo;
import servent.message.FileListResultMessage;
import servent.message.Message;
import servent.message.util.MessageUtil;

import java.io.File;

public class FilesResultMessageHandler implements MessageHandler{
    private FileListResultMessage fileListResultMessage;

    public FilesResultMessageHandler(Message fileListResultMessage) {
        this.fileListResultMessage = (FileListResultMessage) fileListResultMessage;
    }

    @Override
    public void run() {
        if(fileListResultMessage.getRequester() == AppConfig.myServentInfo.getListenerPort()){
            if(fileListResultMessage.getFiles() == null)
                AppConfig.timestampedStandardPrint("Profile " + fileListResultMessage.getSenderPort() + " is private!");
            else{
                AppConfig.timestampedStandardPrint("Profile " + fileListResultMessage.getSenderPort() + " files are: ");
                for(String file : fileListResultMessage.getFiles()){
                    AppConfig.timestampedStandardPrint("\t" + file);
                }
            }
        }
        else{
            ServentInfo receiver = AppConfig.chordState.getNextNodeForKey(ChordState.chordHash(fileListResultMessage.getRequester()));
            Message m = new FileListResultMessage(fileListResultMessage.getSenderPort(), receiver.getListenerPort(),
                    fileListResultMessage.getFiles(), fileListResultMessage.getRequester());
            MessageUtil.sendMessage(m);
        }
    }
}
