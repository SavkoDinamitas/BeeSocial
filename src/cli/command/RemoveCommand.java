package cli.command;

import app.AppConfig;

public class RemoveCommand implements CLICommand{
    @Override
    public String commandName() {
        return "remove_file";
    }

    @Override
    public void execute(String args) {
        if(!AppConfig.chordState.getUploadedFiles().contains(args)){
            AppConfig.timestampedStandardPrint("You don't have this file in directory!");
        }
        else{
            AppConfig.chordState.getUploadedFiles().remove(args);
        }
    }
}
