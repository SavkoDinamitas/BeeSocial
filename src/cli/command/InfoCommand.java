package cli.command;

import app.AppConfig;

public class InfoCommand implements CLICommand {

	@Override
	public String commandName() {
		return "info";
	}

	@Override
	public void execute(String args) {
		AppConfig.timestampedStandardPrint("My info: " + AppConfig.myServentInfo + " Predecessor: " + AppConfig.chordState.getPredecessor());
		AppConfig.timestampedStandardPrint("All stored files including replicas: \n" + AppConfig.chordState.getAllFilesStored());
	}

}
