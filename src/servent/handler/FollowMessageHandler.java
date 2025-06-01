package servent.handler;

import app.AppConfig;
import app.ChordState;
import app.ServentInfo;
import servent.message.FollowMessage;
import servent.message.Message;
import servent.message.ReplicateMessage;
import servent.message.util.MessageUtil;

public class FollowMessageHandler implements MessageHandler{
    private FollowMessage followMessage;

    public FollowMessageHandler(Message followMessage) {
        this.followMessage = (FollowMessage) followMessage;
    }

    @Override
    public void run() {
        //if I am the one that is getting requested
        if(AppConfig.chordState.isKeyMine(ChordState.chordHash(followMessage.getFollowPort()))){
            AppConfig.chordState.getFollowRequest(followMessage.getSenderPort(), followMessage.getFollowPort());
            AppConfig.timestampedStandardPrint("Got follow request from " + followMessage.getFollowPort());
            //replicate followers
            Message m = new ReplicateMessage(AppConfig.myServentInfo.getListenerPort(), AppConfig.chordState.getNextNodePort(), AppConfig.chordState.getUploadedFiles(),
                    AppConfig.chordState.getPendingRequests(), AppConfig.chordState.getFollowers(), false, AppConfig.chordState.getChangeId());
            AppConfig.chordState.incrementChangeId();
            MessageUtil.sendMessage(m);
        }
        else{
            //after homework 2, i don't believe in changeReciever methods...
            ServentInfo reciever = AppConfig.chordState.getNextNodeForKey(ChordState.chordHash(followMessage.getFollowPort()));
            Message m = new FollowMessage(followMessage.getSenderPort(), reciever.getListenerPort(), followMessage.getFollowPort());
            MessageUtil.sendMessage(m);
            AppConfig.timestampedStandardPrint("Redirect follow request to next node " + m);
        }
    }
}
