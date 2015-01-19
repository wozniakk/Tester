package local.tester;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable {

	private int tcpPortNumber;
	
	public Server(int tcpPortNumber) {
		this.tcpPortNumber = tcpPortNumber;
	}
	
//	private void sendRecordedFileBack(String filePath, DataOutputStream outputStream) {
//		
//	}
	
	@Override
	public void run() {
		
		final String HELLO = "HELLO", BUSY = "BUSY", HI = "HI", START = "START", ERROR = "ERROR";
		
		while (true) {
			try ( ServerSocket serverSocket = new ServerSocket(tcpPortNumber);
				Socket connectionSocket = serverSocket.accept();
				BufferedReader inputData = new BufferedReader( new InputStreamReader(connectionSocket.getInputStream()) );
				DataOutputStream outputData = new DataOutputStream(connectionSocket.getOutputStream()); ) {
					String input = inputData.readLine();
					if (input.equals(HELLO)) {
						if (Tester.isProgramBusy()) {
							outputData.writeBytes(BUSY + '\n');
						} else {
							outputData.writeBytes(HI + '\n');
							input = inputData.readLine();
							int callsNumber = Integer.valueOf(input.split("\\s+")[0]);
							int[] callsLength = new int[input.split("\\s+").length - 1];
							for (int i = 0; i<callsLength.length; ++i) callsLength[i] = Integer.valueOf(input.split("\\s+")[i+1]);
							if (callsNumber == callsLength.length) {
								Tester.setProgramBusy();
								outputData.writeBytes(START + '\n');
								outputData.writeBytes(Tester.testerUA.getUserName() + '\n');
								Tester.testerUA.setRemoteUser( inputData.readLine() );
								for (int i = 0; i<callsNumber; ++i) {
									Tester.testerUA.setCalee(callsLength[i],
											"results/" + Tester.testerUA.getRemoteUser() + "_" + i + ".wav");
									Tester.testerUA.waitForHangup();
									System.out.println("Received: " + Tester.testerUA.getRemoteUser() + "_" + i + ".wav");
								}
//								sending files back
//								for (int i = 0; i<callsNumber; ++i) {
//									
//								}
//								for (int i = 0; i<callsNumber; ++i)
//									sendRecordedFileBack("results/" + Tester.testerUA.getRemoteUser() + "_" + i + ".wav", outputData);
//								
								Tester.testerUA.setIdle();
								Tester.setProgramAvailable();
								System.out.println("DONE");
							} else {
								outputData.writeBytes(ERROR + '\n');
							}
						}
					}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}

}
