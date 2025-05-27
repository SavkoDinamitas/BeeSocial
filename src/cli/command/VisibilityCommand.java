package cli.command;

import app.AppConfig;

public class VisibilityCommand implements CLICommand{
    @Override
    public String commandName() {
        return "visibility";
    }

    @Override
    public void execute(String args) {
        if(args.equals("public"))
            AppConfig.chordState.setPublicProfile(true);
        else if (args.equals("private"))
            AppConfig.chordState.setPublicProfile(false);
        else
            AppConfig.timestampedStandardPrint("Unsupported visibility command");
        AppConfig.timestampedStandardPrint("Profile visibility changed!");
    }
}
