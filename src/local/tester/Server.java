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
	
	private void waitForHangup() {
		
		synchronized (Tester.testerUA) {
			try {
				Tester.testerUA.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}
	
//	private void sendRecordedFileBack(String filePath, DataOutputStream outputStream) {
//		
//		try ( FileInputStream recordedFile = new FileInputStream(filePath);
//				BufferedInputStream bufferedInputStream = new BufferedInputStream(recordedFile); ) {
//			outputStream.writeBytes(String.valueOf(bufferedInputStream.available()) + '\n');
//			System.out.println(String.valueOf(bufferedInputStream.available()));
//			
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		
////		try ( FileInputStream recordedFile = new FileInputStream(filePath);
////				BufferedInputStream bufferedInputStream = new BufferedInputStream( recordedFile ); ) {
////			long fileSize = recordedFile.available();
////			final int BUFFER_SIZE = 1024;
////			byte[] fileBytes = new byte[BUFFER_SIZE];
////			outputStream.writeBytes(String.valueOf(fileSize) + '\n');
////			long bytesReaded = 0;
////			long restBytes;
////			while (bytesReaded < fileSize) {
////				if (BUFFER_SIZE <= fileSize - bytesReaded) {
////					bytesReaded += bufferedInputStream.read(fileBytes, 0, fileBytes.length);
////					outputStream.write(fileBytes, 0, fileBytes.length);
////				} else {
////					bytesReaded += restBytes = bufferedInputStream.read(fileBytes, 0, (int)(fileSize - bytesReaded));
////					outputStream.write(fileBytes, 0, (int)restBytes);
////				}
////			}
////			System.out.println("Done " + filePath + " size " + fileSize + "B");
//////			outputStream.writeBytes("\n");
//////			outputStream.flush();
////		} catch (FileNotFoundException e) {
////			e.printStackTrace();
////		} catch (IOException e) {
////			e.printStackTrace();
////		}
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
								waitForHangup();
								System.out.println("Received: " + Tester.testerUA.getRemoteUser() + "_" + i + ".wav");
							}
							// sending files back
//							System.out.println("Sending recorded files back.");
//							for (int i = 0; i<callsNumber; ++i)
//								sendRecordedFileBack("results/" + Tester.testerUA.getRemoteUser() + "_" + i + ".wav", outputData);
							//
							Tester.testerUA.setIdle();
							Tester.setProgramAvailable();
							System.out.println("DONE");
						} else outputData.writeBytes(ERROR + '\n');
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}

}
