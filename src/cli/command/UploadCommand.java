package cli.command;

import app.AppConfig;
import app.ChordState;

public class UploadCommand implements CLICommand {

	@Override
	public String commandName() {
		return "upload";
	}

	@Override
	public void execute(String args) {
		//just add file to the collection
		AppConfig.chordState.putValue(args);
		AppConfig.timestampedStandardPrint("File " + args + " has been uploaded");
	}

}
