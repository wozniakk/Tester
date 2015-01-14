package local.tester;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Tester {
	
	private static ProgramState testerState = ProgramState.AVAILABLE;
	
	protected static synchronized boolean isProgramBusy() {
		if (testerState == ProgramState.BUSY) return true;
		return false;
	}
	
	protected static synchronized void setProgramAvailable() {
		testerState = ProgramState.AVAILABLE;
	}
	
	protected static synchronized void setProgramBusy() {
		testerState = ProgramState.BUSY;
	}

	private static void serverInit(int tcpServerPort) {
		
		Server tcpServer = new Server(tcpServerPort);
		Thread tcpServerThread = new Thread(tcpServer);
		tcpServerThread.start();
		
	}
	
	private static void runOnRemote(String tcpRemoteAddress, int tcpRemotePort, int[] durations, String[] testFilesPaths) {
		
		Client tcpClient = new Client(tcpRemoteAddress, tcpRemotePort, durations, testFilesPaths);
		Thread tcpClientThread = new Thread(tcpClient);
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
			ArrayList<String> lines = new ArrayList<>();
			testsConfigFile.lines().forEach((line)->{
				if (!(line.isEmpty())) lines.add(line);				
			});
			durations = new int[lines.size()];
			testFilesPaths = new String[lines.size()];
			for (int i = 0; i<lines.size(); ++i) {
				testFilesPaths[i] = lines.get(i).split("\\s+")[0];
				durations[i] = Integer.valueOf(lines.get(i).split("\\s+")[1]);
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
					runOnRemote(TCP_REMOTE_ADDRESS, TCP_REMOTE_PORT, durations, testFilesPaths);
					break;
				case "exit":
					console.close();
					System.exit(0);
			}
		}
		
	}

}
