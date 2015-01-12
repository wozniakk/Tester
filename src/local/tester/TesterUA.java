package local.tester;

import java.util.UUID;
import java.util.Vector;

import org.zoolu.sip.address.NameAddress;
import org.zoolu.tools.ExceptionPrinter;
import org.zoolu.tools.Log;
import org.zoolu.tools.ScheduledWork;

import local.ua.UA;
import local.ua.UserAgent;
import local.ua.UserAgentListener;

public class TesterUA implements UserAgentListener {

	private String remoteUser;
	
	public void setRemoteUser(String remoteUser) {
		this.remoteUser = remoteUser;
	}
	
	public String getRemoteUser() {
		return remoteUser;
	}
	
	protected UserAgent ua;
	
	/** UA_IDLE=0 */
	protected static final String UA_IDLE = "IDLE";
	/** UA_INCOMING_CALL=1 */
	protected static final String UA_INCOMING_CALL = "INCOMING_CALL";
	/** UA_OUTGOING_CALL=2 */
	protected static final String UA_OUTGOING_CALL = "OUTGOING_CALL";
	/** UA_ONCALL=3 */
	protected static final String UA_ONCALL = "ONCALL";
	
	/** Call state: <P>UA_IDLE=0, <BR>UA_INCOMING_CALL=1, <BR>UA_OUTGOING_CALL=2, <BR>UA_ONCALL=3 */
	String call_state = UA_IDLE;
	
	/** Changes the call state */
	protected void changeStatus(String state) {
		call_state = state;
		printLog("state: " + call_state, Log.LEVEL_MEDIUM); 
	}
	
	/** Checks the call state */
	protected boolean statusIs(String state) {
		return call_state.equals(state); 
	}
	
	/** Gets the call state */
	protected String getStatus() {
		return call_state; 
	}
	
	public String userName;
	
	public TesterUA(String[] args) {
		
		UA.init("", args);
		userName = UUID.randomUUID().toString();
		System.out.println(userName);
		UA.ua_profile.user = userName; 
		UA.ua_profile.auth_user = userName;
		ua = new UserAgent(UA.sip_provider, UA.ua_profile, this);
		changeStatus(UA_IDLE);
		setIdle();
		run();
		
	}
	
	/** Schedules a re-inviting after <i>delay_time</i> secs. It simply changes the contact address. */
	void reInvite(final int delay_time) {
		printLog("AUTOMATIC RE-INVITING/MODIFING: " + delay_time + " secs"); 
		if (delay_time == 0) ua.modify(null);
		else new ScheduledWork(delay_time*1000) {  public void doWork() {  ua.modify(null);  }  };
	}
	
	/** Schedules a call-transfer after <i>delay_time</i> secs. */
	void callTransfer(final NameAddress transfer_to, final int delay_time) {
		printLog("AUTOMATIC REFER/TRANSFER: " + delay_time + " secs");
		if (delay_time == 0) ua.transfer(transfer_to);
		else new ScheduledWork(delay_time*1000) {  public void doWork() {  ua.transfer(transfer_to);  }  };
	}
	
	/** Schedules an automatic answer after <i>delay_time</i> secs. */
	void automaticAccept(final int delay_time) {
		printLog("AUTOMATIC ANSWER: " + delay_time + " secs");
		if (delay_time == 0) {
			if (statusIs(UA_INCOMING_CALL)) {
				ua.accept();
				changeStatus(UA_ONCALL);
			}
		}
		else new ScheduledWork(delay_time*1000) {  public void doWork() {
			if (statusIs(UA_INCOMING_CALL)) {
				ua.accept();
				changeStatus(UA_ONCALL);
			}
		}  };
	}
	
	/** Schedules an automatic hangup after <i>delay_time</i> secs. */
	void automaticHangup(final int delay_time) {
		printLog("AUTOMATIC HANGUP: " + delay_time + " secs");
		if (delay_time == 0) {
			if (!statusIs(UA_IDLE)) {
				ua.hangup();
				changeStatus(UA_IDLE);      
			}
		}
		else new ScheduledWork(delay_time*1000) {  public void doWork() {
			if (!statusIs(UA_IDLE)) {
				ua.hangup();
				changeStatus(UA_IDLE);      
			}
		}  };
	}
	
	void run() {
		// Set the re-invite
		if (UA.ua_profile.re_invite_time>0) {
			reInvite(UA.ua_profile.re_invite_time);
		}
		
		// Set the transfer (REFER)
		if (UA.ua_profile.transfer_to!=null && UA.ua_profile.transfer_time>0) {
			callTransfer(UA.ua_profile.transfer_to, UA.ua_profile.transfer_time);
		}
		
		// ########## unregisters ALL contact URLs
		if (UA.ua_profile.do_unregister_all) {
			ua.printLog("UNREGISTER ALL contact URLs");
			ua.unregisterall();
		}
		// unregisters the contact URL
		if (UA.ua_profile.do_unregister) {
			ua.printLog("UNREGISTER the contact URL");
			ua.unregister();
		}
		// ########## registers the contact URL with the registrar server
		if (UA.ua_profile.do_register) {
			ua.printLog("REGISTRATION");
			ua.loopRegister(UA.ua_profile.expires, UA.ua_profile.expires/2, UA.ua_profile.keepalive_time);
		}
		// ########## make a call with the remote URL
//		if (UA.ua_profile.call_to!=null) {
//			ua.printLog("UAC: CALLING " + UA.ua_profile.call_to);
//			jComboBox1.setSelectedItem(null);
//			comboBoxEditor1.setItem(ua_profile.call_to.toString());
//			display.setText("CALLING "+ua_profile.call_to);
//			ua.call(UA.ua_profile.call_to);
//			changeStatus(UA_OUTGOING_CALL);       
//		}
		  
		if (!UA.ua_profile.audio && !UA.ua_profile.video) ua.printLog("ONLY SIGNALING, NO MEDIA");
		
	}
	
	private TesterFunction testerFunction = TesterFunction.IDLE;
	
	public void setCaller(String sendFile) {
		
		testerFunction = TesterFunction.CALLER;
		UA.ua_profile.hangup_time = -1;
		UA.ua_profile.send_only = true;
		UA.ua_profile.recv_only = false;
		
		UA.ua_profile.send_file = sendFile;
		
	}
	
	public void setCalee(int hangupTime, String recvFile) {
		
		testerFunction = TesterFunction.CALEE;
		UA.ua_profile.send_only = false;
		UA.ua_profile.recv_only = true;
		UA.ua_profile.hangup_time = hangupTime;
		UA.ua_profile.accept_time = 0;
		
		UA.ua_profile.recv_file = recvFile;
		
	}
	
	public void setIdle() {
		
		testerFunction = TesterFunction.IDLE; 
		UA.ua_profile.hangup_time = 0;//auto hang up
		UA.ua_profile.accept_time = -1;//manual accept
		
	}
	
	/* UserAgentListener interface implementation */
	@Override
	public void onUaRegistrationSucceeded(UserAgent ua, String result) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onUaRegistrationFailed(UserAgent ua, String result) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onUaIncomingCall(UserAgent ua, NameAddress callee,
			NameAddress caller, Vector media_descs) {
		
		changeStatus(UA_INCOMING_CALL);
		if (testerFunction.equals(TesterFunction.IDLE) || testerFunction.equals(TesterFunction.CALLER)) {
			ua.hangup();
			changeStatus(UA_IDLE);
		}
		if (testerFunction.equals(TesterFunction.CALEE)) {
			if (caller.getAddress().getUserName().equals(getRemoteUser())) {
		         automaticAccept(UA.ua_profile.accept_time);
		         automaticHangup(UA.ua_profile.hangup_time);
			}
		}
			
	}
	
	@Override
	public void onUaCallCancelled(UserAgent ua) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onUaCallProgress(UserAgent ua) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onUaCallRinging(UserAgent ua) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onUaCallAccepted(UserAgent ua) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onUaCallTransferred(UserAgent ua) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onUaCallFailed(UserAgent ua, String reason) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onUaCallClosed(UserAgent ua) {
		// TODO Auto-generated method stub
		synchronized (this) {
			this.notify();
		}
		
	}

	@Override
	public void onUaMediaSessionStarted(UserAgent ua, String type, String codec) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onUaMediaSessionStopped(UserAgent ua, String type) {
		// TODO Auto-generated method stub
		synchronized (this) {
			this.notify();
		}

	}
	/* Logs */
	/** Adds a new string to the default Log */
	private void printLog(String str) {
		printLog(str, Log.LEVEL_HIGH);
	}
	
	/** Adds a new string to the default Log */
	private void printLog(String str, int level) {
		if (UA.sip_provider.getLog() != null) UA.sip_provider.getLog().println("GraphicalUA: " + str, UserAgent.LOG_OFFSET + level);  
	}
	
	/** Adds the Exception message to the default Log */
	private void printException(Exception e,int level) {
		printLog("Exception: " + ExceptionPrinter.getStackTraceOf(e), level);
	}
	
}
