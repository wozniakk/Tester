package local.tester;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client implements Runnable {

	private String tcpRemoteAddress;
	private int tcpPortNumber;
	private int[] durations;
	private String[] testFilesPaths;
	
	public Client(String tcpRemoteAddress, int tcpPortNumber, int[] durations, String[] testFilesPaths) {
		this.tcpRemoteAddress = tcpRemoteAddress;
		this.tcpPortNumber = tcpPortNumber;
		this.durations = durations;
		this.testFilesPaths = testFilesPaths;
	}
	
	@Override
	public void run() {
		
		final String HELLO = "HELLO", BUSY = "BUSY", HI = "HI", START = "START", ERROR = "ERROR";
		
		Tester.setProgramBusy();
		try ( Socket clientSocket = new Socket(tcpRemoteAddress, tcpPortNumber);
			DataOutputStream clientOutputStream = new DataOutputStream(clientSocket.getOutputStream());
			BufferedReader clientInputStream = new BufferedReader( new InputStreamReader(clientSocket.getInputStream()) ); ) {
			while (true) {
				clientOutputStream.writeBytes(HELLO + '\n');
				String input = clientInputStream.readLine();
				if (input.equals(BUSY)) {
					System.out.println(BUSY);
					break;
				}
				if (input.equals(HI)) {
					System.out.println(HI);
					StringBuilder durationsString = new StringBuilder(Integer.valueOf(durations.length).toString());
					for (int i: durations) durationsString.append(' ' + String.valueOf(i));
					clientOutputStream.writeBytes(durationsString.toString() + '\n');
					input = clientInputStream.readLine();
					if (input.equals(START)) {
						System.out.println(START);
						//
						Tester.testerUA.setRemoteUser( clientInputStream.readLine() );
						clientOutputStream.writeBytes(Tester.testerUA.userName + '\n');
						System.out.println(Tester.testerUA.getRemoteUser());
						for (int i = 0; i<durations.length; ++i) {
							System.out.println(testFilesPaths[i]);
							Tester.testerUA.setCaller(testFilesPaths[i]);
							Tester.testerUA.ua.call("sip:" + Tester.testerUA.getRemoteUser() + '@' + tcpRemoteAddress + ":5070");
							synchronized (Tester.testerUA) {
								try {
									Tester.testerUA.wait();
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
						}
						Tester.testerUA.setIdle();
						System.out.println("DONE");
						//
						break;
					} else {
						System.out.println(ERROR);
						break;
					}
				}
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Tester.setProgramAvailable();
		
	}

}
