package servent.handler;

import app.AppConfig;
import app.ChordState;
import app.ServentInfo;
import servent.message.FollowMessage;
import servent.message.ListFilesMessage;
import servent.message.Message;
import servent.message.util.MessageUtil;

public class ListFilesMessageHandler implements MessageHandler {
    private ListFilesMessage listFilesMessage;

    public ListFilesMessageHandler(Message listFilesMessage) {
        this.listFilesMessage = (ListFilesMessage) listFilesMessage;
    }

    @Override
    public void run() {
        //if I have the one that is getting requested
        if(AppConfig.chordState.isKeyMine(ChordState.chordHash(listFilesMessage.getRequiredUser()))){
            AppConfig.chordState.solveFileRequest(listFilesMessage.getRequiredUser(), listFilesMessage.getSenderPort());
        }
        else{
            ServentInfo reciever = AppConfig.chordState.getNextNodeForKey(ChordState.chordHash(listFilesMessage.getRequiredUser()));
            Message m = new ListFilesMessage(listFilesMessage.getSenderPort(), reciever.getListenerPort(), listFilesMessage.getRequiredUser());
            MessageUtil.sendMessage(m);
            AppConfig.timestampedStandardPrint("Redirect listFiles request to next node " + m);
        }
    }
}
