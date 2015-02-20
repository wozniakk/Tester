package local.tester;

import static local.tester.Tester.setProgramAvailable;
import static local.tester.Tester.setProgramBusy;
import static local.tester.Tester.testerUA;

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
	
	private String durationsToString(int[] durations) {
		
		StringBuilder durationsString = new StringBuilder(Integer.valueOf(durations.length).toString());
		for (int i: durations) durationsString.append(' ' + String.valueOf(i));
		return durationsString.toString();
		
	}
	
	@Override
	public void run() {
		
		final String HELLO = "HELLO", BUSY = "BUSY", HI = "HI", START = "START", ERROR = "ERROR";
		
		setProgramBusy();
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
						clientOutputStream.writeBytes(durationsToString(durations) + '\n');
						input = clientInputStream.readLine();
						if (input.equals(START)) {
							testerUA.setRemoteUser( clientInputStream.readLine() );
							clientOutputStream.writeBytes(testerUA.getUserName() + '\n');
							for (int i = 0; i<durations.length; ++i) {
								testerUA.setCaller(testFilesPaths[i]);
								testerUA.ua.call("sip:" + testerUA.getRemoteUser() + '@' + tcpRemoteAddress + ":5070");
								testerUA.waitForHangup();
							}
							testerUA.setIdle();
							System.out.println("DONE");
						} else {
							System.out.println(ERROR);
						}
					}
					break;
				}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			setProgramAvailable();
		}
		
	}
	
}
