package cli.command;

import app.AppConfig;
import servent.message.Message;
import servent.message.ReplicateMessage;
import servent.message.util.MessageUtil;

public class AcceptCommand implements CLICommand{
    @Override
    public String commandName() {
        return "accept";
    }

    @Override
    public void execute(String args) {
        if(!AppConfig.chordState.getPendingRequests().contains(Integer.parseInt(args))) {
            AppConfig.timestampedStandardPrint("That servent is not in the pending requests...");
        }
        else{
            AppConfig.chordState.getPendingRequests().remove(Integer.parseInt(args));
            AppConfig.chordState.acceptFollower(Integer.parseInt(args));
            AppConfig.timestampedStandardPrint("Follower " + args + " was successfully accepted!");
            //replicate followers
            Message m = new ReplicateMessage(AppConfig.myServentInfo.getListenerPort(), AppConfig.chordState.getNextNodePort(), AppConfig.chordState.getUploadedFiles(),
                    AppConfig.chordState.getPendingRequests(), AppConfig.chordState.getFollowers(), false, AppConfig.chordState.getChangeId());
            AppConfig.chordState.incrementChangeId();
            MessageUtil.sendMessage(m);
        }
    }
}
