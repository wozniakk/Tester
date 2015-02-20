package local.tester;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import static local.tester.Tester.*;
import static local.tester.DSP.*;

public class Server implements Runnable {

	private int tcpPortNumber;
	
	public Server(int tcpPortNumber) {
		this.tcpPortNumber = tcpPortNumber;
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
						if (isProgramBusy()) {
							outputData.writeBytes(BUSY + '\n');
						} else {
							outputData.writeBytes(HI + '\n');
							input = inputData.readLine();
							int callsNumber = Integer.valueOf(input.split("\\s+")[0]);
							int[] callsLength = new int[input.split("\\s+").length - 1];
							for (int i = 0; i<callsLength.length; ++i) callsLength[i] = Integer.valueOf(input.split("\\s+")[i+1]);
							if (callsNumber == callsLength.length) {
								setProgramBusy();
								outputData.writeBytes(START + '\n');
								outputData.writeBytes(testerUA.getUserName() + '\n');
								testerUA.setRemoteUser( inputData.readLine() );
								for (int i = 0; i<callsNumber; ++i) {
									testerUA.setCalee(callsLength[i], "results/" + testerUA.getRemoteUser() + "_" + i + ".wav");
									testerUA.waitForHangup();
								}
								for (int i = 0; i<callsNumber; ++i) {
									copyHeader("tests/" + "test_" + i + ".wav", "results/" + testerUA.getRemoteUser() + "_" + i + ".wav");
									analyse("tests/" + "test_" + i + ".wav", "results/" + testerUA.getRemoteUser() + "_" + i + ".wav");
								}
								testerUA.setIdle();
								setProgramAvailable();
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
