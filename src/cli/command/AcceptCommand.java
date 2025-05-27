package cli.command;

import app.AppConfig;

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
            AppConfig.chordState.getFollowers().add(Integer.parseInt(args));
            AppConfig.timestampedStandardPrint("Follower " + args + " was successfully accepted!");
        }
    }
}
