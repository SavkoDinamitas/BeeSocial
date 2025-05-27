package cli.command;

import app.AppConfig;
import app.ChordState;
import app.ServentInfo;
import servent.message.FollowMessage;
import servent.message.Message;
import servent.message.util.MessageUtil;

public class FollowCommand implements CLICommand{
    @Override
    public String commandName() {
        return "follow";
    }

    @Override
    public void execute(String args) {
        //send follow request to the servent on port
        ServentInfo reciever = AppConfig.chordState.getNextNodeForKey(ChordState.chordHash(Integer.parseInt(args)));
        Message m = new FollowMessage(AppConfig.myServentInfo.getListenerPort(), reciever.getListenerPort(), Integer.parseInt(args));
        MessageUtil.sendMessage(m);
    }
}
