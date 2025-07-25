package servent;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import app.AppConfig;
import app.Cancellable;
import servent.handler.*;
import servent.message.FollowMessage;
import servent.message.Message;
import servent.message.util.MessageUtil;

public class SimpleServentListener implements Runnable, Cancellable {

	private volatile boolean working = true;
	
	public SimpleServentListener() {
		
	}

	/*
	 * Thread pool for executing the handlers. Each client will get it's own handler thread.
	 */
	private final ExecutorService threadPool = Executors.newWorkStealingPool();
	
	@Override
	public void run() {
		ServerSocket listenerSocket = null;
		try {
			listenerSocket = new ServerSocket(AppConfig.myServentInfo.getListenerPort(), 100);
			/*
			 * If there is no connection after 1s, wake up and see if we should terminate.
			 */
			listenerSocket.setSoTimeout(1000);
		} catch (IOException e) {
			AppConfig.timestampedErrorPrint("Couldn't open listener socket on: " + AppConfig.myServentInfo.getListenerPort());
			System.exit(0);
		}
		
		
		while (working) {
			try {
				Message clientMessage;
				
				Socket clientSocket = listenerSocket.accept();
				
				//GOT A MESSAGE! <3
				clientMessage = MessageUtil.readMessage(clientSocket);
				
				MessageHandler messageHandler = new NullHandler(clientMessage);
				
				/*
				 * Each message type has it's own handler.
				 * If we can get away with stateless handlers, we will,
				 * because that way is much simpler and less error prone.
				 */
				switch (clientMessage.getMessageType()) {
					case NEW_NODE:
						messageHandler = new NewNodeHandler(clientMessage);
						break;
					case WELCOME:
						messageHandler = new WelcomeHandler(clientMessage);
						break;
					case SORRY:
						messageHandler = new SorryHandler(clientMessage);
						break;
					case UPDATE:
						messageHandler = new UpdateHandler(clientMessage);
						break;
					case ASK_GET:
						messageHandler = new AskGetHandler(clientMessage);
						break;
					case TELL_GET:
						messageHandler = new TellGetHandler(clientMessage);
						break;
					case FOLLOW:
						messageHandler = new FollowMessageHandler(clientMessage);
						break;
					case LIST:
						messageHandler = new ListFilesMessageHandler(clientMessage);
						break;
					case LIST_RESULT:
						messageHandler = new FilesResultMessageHandler(clientMessage);
						break;
					case PRIVILEGE:
						messageHandler = new PrivilegeMessageHandler(clientMessage);
						break;
					case REQUEST_PRIVILEGE:
						messageHandler = new RequestPrivilegeMessageHandler(clientMessage);
						break;
					case REORGANIZED:
						messageHandler = new ReorganizedMessageHandler(clientMessage);
						break;
					case REPLICATE:
						messageHandler = new ReplicateMessageHandler(clientMessage);
						break;
					case STOP:
						messageHandler = new StopMessageHandler(clientMessage);
						break;
					case DELETE:
						messageHandler = new DeleteMessageHandler(clientMessage);
						break;
					case PING:
						messageHandler = new PingHandler(clientMessage);
						break;
					case PONG:
						messageHandler = new PongHandler(clientMessage);
						break;
					case PING_CHECK:
						messageHandler = new RequestPingHandler(clientMessage);
						break;
					case GATHER:
						messageHandler = new TokenArrayGathererHandler(clientMessage);
						break;
					case FAIL:
						messageHandler = new FailedMessageHandler(clientMessage);
						break;
					case POISON:
						break;
					}

				final var fuckJava = messageHandler;
				threadPool.submit(() -> {
					try {
						fuckJava.run();
					} catch (Throwable t) {
						AppConfig.timestampedErrorPrint(
								"unhandled exception by %s".formatted(
										fuckJava.getClass()
								)
						);
						t.printStackTrace();
					}
				});
			} catch (SocketTimeoutException timeoutEx) {
				//Uncomment the next line to see that we are waking up every second.
//				AppConfig.timedStandardPrint("Waiting...");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void stop() {
		this.working = false;
	}

}
