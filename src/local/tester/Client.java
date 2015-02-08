package local.tester;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import static local.tester.Tester.*;

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
	
	private void copyHeader(String from, String to) {

		int headerLength;
		File fromFile = new File(from);
		try ( FileInputStream fromInputStream = new FileInputStream( fromFile );
				RandomAccessFile toFile = new RandomAccessFile(new File(to), "rw"); ) {
			AudioFileFormat format = AudioSystem.getAudioFileFormat( fromFile );
			headerLength = format.getByteLength() - format.getFrameLength();
			byte[] headerBuffer = new byte[headerLength];
			byte[] fileBuffer = new byte[(int) toFile.length()];
			fromInputStream.read(headerBuffer);
			toFile.read(fileBuffer);
			toFile.seek(0);
			toFile.write(headerBuffer);
			toFile.write(fileBuffer);
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private void getRecordedFile(String fileName, BufferedReader clientInputStream) {
		
		try {
			FileOutputStream outputFile = new FileOutputStream(fileName);
			long fileSize = Long.valueOf( clientInputStream.readLine() );
			for (long i = 0; i<fileSize; ++i) {
				int temp = clientInputStream.read();
				outputFile.write(temp);
			}
			outputFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
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
								System.out.println("Sending: " + testFilesPaths[i]);
								testerUA.setCaller(testFilesPaths[i]);
								testerUA.ua.call("sip:" + testerUA.getRemoteUser() + '@' + tcpRemoteAddress + ":5070");
								testerUA.waitForHangup();
							}
//							waiting for recorded files
							for (int i = 0; i<durations.length; ++i) {
//								getRecordedFile("results/" + Tester.testerUA.getRemoteUser() + "_" + i + "_recv.wav", clientInputStream);
								copyHeader(testFilesPaths[i], "results/" + testerUA.getUserName() + "_" + i + ".wav");
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
