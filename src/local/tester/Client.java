package local.tester;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

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
	
	private void waitForHangup() {
		
		synchronized (Tester.testerUA) {
			try {
				Tester.testerUA.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	private String durationsToString(int[] durations) {
		
		StringBuilder durationsString = new StringBuilder(Integer.valueOf(durations.length).toString());
		for (int i: durations) durationsString.append(' ' + String.valueOf(i));
		return durationsString.toString();
		
	}
	
	private void copyHeader(String from, String to) {

		int headerLength;
		File fromFile = new File(from);
		try ( FileInputStream fromInputStream = new FileInputStream( fromFile );
				RandomAccessFile toFile = new RandomAccessFile(new File(to), "rw"); ) {
			AudioFileFormat format = AudioSystem.getAudioFileFormat( fromFile );
			headerLength = format.getByteLength() - format.getFrameLength();
			byte[] headerBuffer = new byte[headerLength];
			fromInputStream.read(headerBuffer);
			toFile.seek(0);
			toFile.write(headerBuffer);
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
//	private void getRecordedFile(String fileName, Socket clientSocket) {
//		
//		char[] fileBytes = new char[1];
//		long fileSize;
//	    try ( BufferedReader inputStream = new BufferedReader( new InputStreamReader(clientSocket.getInputStream()) );
//	    		BufferedOutputStream fileOutputStream = new BufferedOutputStream( new FileOutputStream("received/" + fileName) ); ) {
//	    	fileSize = Long.valueOf(inputStream.readLine());
//	    	int bytesReaded = 0;
//	    	while (bytesReaded++ < fileSize) {
//	    		inputStream.read(fileBytes);
//	    		fileOutputStream.write(new String(fileBytes).getBytes(), 0, 1);
//	    		inputStream.reset();
//	    	}
//	    } catch (Exception e) {
//	    	e.printStackTrace();
//	    }
//	    
//	}
	
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
					clientOutputStream.writeBytes(durationsToString(durations) + '\n');
					input = clientInputStream.readLine();
					if (input.equals(START)) {
						Tester.testerUA.setRemoteUser( clientInputStream.readLine() );
						clientOutputStream.writeBytes(Tester.testerUA.getUserName() + '\n');
						for (int i = 0; i<durations.length; ++i) {
							System.out.println("Sending: " + testFilesPaths[i]);
							Tester.testerUA.setCaller(testFilesPaths[i]);
							Tester.testerUA.ua.call("sip:" + Tester.testerUA.getRemoteUser() + '@' + tcpRemoteAddress + ":5070");
							waitForHangup();
						}
						// waiting for recorded files
						for (int i = 0; i<durations.length; ++i) {
							copyHeader(testFilesPaths[i], "results/" + Tester.testerUA.getUserName() + "_" + i + ".wav");
						}
//							getRecordedFile(Tester.testerUA.getRemoteUser() + "_" + i + ".wav", clientSocket);
						//
						Tester.testerUA.setIdle();
						System.out.println("DONE");
					} else {
						System.out.println(ERROR);
					}
					break;
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
