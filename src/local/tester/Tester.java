package local.tester;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Tester {
	
	private static ProgramState programState = ProgramState.AVAILABLE;
	
	protected static synchronized boolean isProgramBusy() {
		return programState == ProgramState.BUSY;
	}
	
	protected static synchronized void setProgramAvailable() {
		programState = ProgramState.AVAILABLE;
	}
	
	protected static synchronized void setProgramBusy() {
		programState = ProgramState.BUSY;
	}

	private static void serverInit(int tcpServerPort) {
		
		Thread tcpServerThread = new Thread( new Server(tcpServerPort) );
		tcpServerThread.start();
		
	}
	
	private static void runOnRemote(String tcpRemoteAddress, int tcpRemotePort, int[] durations, String[] testFilesPaths) {
		
		Thread tcpClientThread = new Thread(
				new Client(tcpRemoteAddress, tcpRemotePort, durations, testFilesPaths) );
		tcpClientThread.start();
		
	}
	
	public static TesterUA testerUA;
	
	private static void initUA(String[] args) {
		testerUA = new TesterUA(args);
	}
	
	private static int[] durations;
	private static String[] testFilesPaths;
	
	private static void readTestsConfig(String testsConfigPath) {
		
		try ( BufferedReader testsConfigFile = new BufferedReader( new InputStreamReader( new FileInputStream(testsConfigPath) ) ); ) {
			ArrayList<String> lines = (ArrayList<String>) testsConfigFile
			.lines()
			.filter( (line)-> !line.startsWith("#") && !line.isEmpty() )
			.collect(Collectors.toList());
			durations = new int[lines.size()];
			testFilesPaths = new String[lines.size()];
			for (int i = 0; i<lines.size(); ++i) {
				String[] splitted = lines.get(i).split("\\s+");
				testFilesPaths[i] = splitted[0];
				durations[i] = Integer.valueOf(splitted[1]);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void main(String[] args) {
		
		final int TCP_SERVER_PORT = Integer.valueOf(args[0]);
		final String TCP_REMOTE_ADDRESS = args[1];
		final int TCP_REMOTE_PORT = Integer.valueOf(args[2]);
		
		readTestsConfig("config/tests.cfg");
		
		initUA(Arrays.copyOfRange(args, 3, args.length));
		serverInit(TCP_SERVER_PORT);
		
		Scanner console = new Scanner(System.in);
		while (console.hasNextLine()) {
			switch (console.nextLine().toLowerCase()) {
				case "run":
					if (!isProgramBusy()) runOnRemote(TCP_REMOTE_ADDRESS, TCP_REMOTE_PORT, durations, testFilesPaths);
					break;
				case "exit":
					console.close();
					System.exit(0);
			}
		}
		
	}

}
