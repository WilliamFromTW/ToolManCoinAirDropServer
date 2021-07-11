package tmc.server;

import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.*;
import java.util.*;
import java.util.logging.*;

import javax.net.ssl.HttpsURLConnection;

/**
 * TCP/IP Client Server Framework,
 * 
 * @author william chen
 */
public class Server {

	private static final Logger _log = Logger.getLogger(Server.class.getName());
	protected static String sSenderSecretPhrase = ServerGlobal
			.getProperties(ServerGlobal.PROP_SENDER_WALLET_PASSPHRASE);

	/**
	 * entry point of this class.
	 */
	public void start(int iPort, String sProcessName) {

		ServerSocket ss;
		// set the port to listen on
		TimerTask timerTask = new MyTimerTask(System.currentTimeMillis());
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(timerTask, 1000, Integer.parseInt(ServerGlobal.PERIOD_TIME.trim()));
		try {
			ss = new ServerSocket(iPort);
			_log.info("Accepting connections on port " + ss.getLocalPort());
			while (true) {
				ServerThread server = new ServerThread(ss.accept(), sProcessName); // fork a thread , when a client
																					// attach
				server.start();
			}
		} catch (IOException e) {
			_log.warning(" server IOException" + e.getMessage());
		}

	}

}

class MyTimerTask extends TimerTask {
	private long startTime;
	private Socket aTmpConn;
	private OutputStreamWriter aTmpOSW;
	private ServerProcessor aServerProcessor = null;
	private ClientInfo aClientInfo = null;

	public MyTimerTask(long startTime) {
		this.startTime = startTime;
	}

	@Override
	public void run() {
		for (int i = 0; i < ServerProcessor.pool.size(); i++) {
			aServerProcessor = ((ServerProcessor) ServerProcessor.pool.get(i));
			aClientInfo = aServerProcessor.getClientInfo();
			try {
				if(aClientInfo==null) continue;
				aClientInfo.setAirDropped(false);
			} catch (Exception ee) {
				ee.printStackTrace();
			}
		}
		int j = ServerProcessor.pool.size();
		for (int i = 0; i < j; i++) {
			aServerProcessor = ((ServerProcessor) ServerProcessor.pool.get(i));
			aClientInfo = aServerProcessor.getClientInfo();
			if(aClientInfo==null) continue;
			try {
				aTmpConn = aServerProcessor.getConnection();
				// System.out.println("is reachable");
				if (aClientInfo != null && aClientInfo.getWallet() != null) {
				//	if (aClientInfo.getAirDropped() == false) {
					//	System.out.println("inet addresss=" + aTmpConn.getInetAddress() + ",wallet;"
					//			+ aClientInfo.getWallet() + ",publickey:" + aClientInfo.getWalletPublicKey()
					//			+ ",airdrop:" + aClientInfo.getAirDropNumber());
					//}
					PrintWriter out = new PrintWriter(aTmpConn.getOutputStream(), true);
					/*
					OutputStreamWriter aTmpOSW = new OutputStreamWriter(aTmpConn.getOutputStream(),
							ServerGlobal.FILE_ENCODE);
					// System.out.println("is output shutdown?"+ aTmpConn.isOutputShutdown());
					aTmpOSW.write((DateTimeFormatter.ofPattern("yy-MM-dd HH:mm:ss", Locale.ENGLISH).format(LocalDateTime.now())) + " unpaid airdrop is "
							+ aClientInfo.getAirDropNumber() + "\r\n");
					aTmpOSW.write(ServerProcessorImpl.CMD_UNPAID_COIN+ aClientInfo.getAirDropNumber() + "\r\n");
					aTmpOSW.flush();
					*/
					String sTmpTime = DateTimeFormatter.ofPattern("MM/dd HH:mm:ss", Locale.ENGLISH).format(LocalDateTime.now());
				//	else 
					if (aClientInfo.getAirDropped() == false)  {
						aClientInfo.setAirDropped(true);
						aClientInfo.increase(1);
						if (aClientInfo.getAirDropNumber() >= Integer.parseInt(ServerGlobal.TRANSFER_COIN_NUMBER.trim())) {
							out.println(sTmpTime+"  minimum transfer coin number is reached, transfer coin for you ");
//							aTmpOSW.write("\r\n");
							try {
								if (transferCoin(aClientInfo.getWallet(), aClientInfo.getWalletPublicKey(),
										aClientInfo.getAirDropNumber()))
									aClientInfo.setAirDropNumber(0);
							} catch (Exception eeee) {
								eeee.printStackTrace();
							}
						}
						
					}					
					out.println(sTmpTime + " unpaid coin : "+aClientInfo.getAirDropNumber());
					out.println(ServerProcessorImpl.CMD_UNPAID_COIN+ aClientInfo.getAirDropNumber());
					out.flush();
					System.out.println(sTmpTime + " try to check PrintWriter.checkError() for inet address :\r\n"+aTmpConn.getRemoteSocketAddress());
					if (out.checkError()) {
						System.out.println("PrintWriter.checkError=true");
						continue;
			//			aClientInfo.setAirDropped(true);
				//		continue;
					}
				
				}
			} catch (Exception eee) {
				// System.out.println("is notreachable");
				aClientInfo.setAirDropped(false);
				eee.printStackTrace();
			}
		}
	}

	private boolean transferCoin(String sWallet, String sWalletPublicKey, int iTransferNumber) throws Exception {

		String urlParameters = getUrl(sWallet, sWalletPublicKey, "" + iTransferNumber, Server.sSenderSecretPhrase);
		byte[] postData = urlParameters.getBytes("UTF-8");
		int postDataLength = postData.length;
		String request = "http://localhost:6876/nxt";
		URL url = new URL(request);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoOutput(true);
		conn.setInstanceFollowRedirects(false);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		conn.setRequestProperty("charset", "utf-8");
		conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
		conn.setUseCaches(false);
		DataOutputStream wr = new DataOutputStream(conn.getOutputStream());

		wr.write(postData);
		wr.flush();
		String result = "";
		String line;
		BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

		while ((line = reader.readLine()) != null) {
			result += line;
		}
		System.out.println(result);
		wr.close();
		reader.close();
		if (result.indexOf("error") == -1)
			return true;
		else
			return false;
	}

	private String getUrl(String sWallet, String sWalletPublicKey, String sAirDropNumber, String sSenderSecretPhrase) {
		// String sReturn =
		// "requestType=sendMoney&recipient=TMC-FPZJ-27JU-6UF2-ASF34&recipientPublicKey=3c3164bec2d060306780cc0a199999853f7e98fafcffa611e616740dce5a4453&amountNQT=1000000000&feeNQT=100000000&secretPhrase=allow
		// scene possess grade spiral control like mountain nature autumn bring
		// any&deadline=1440";
		String sReturn = "requestType=sendMoney&recipient=" + sWallet + "&recipientPublicKey=" + sWalletPublicKey
				+ "&amountNQT=" + sAirDropNumber + "00000000&feeNQT=100000000&secretPhrase=" + sSenderSecretPhrase
				+ "&deadline=1440";
		System.out.println(sReturn);
		return sReturn;
	}

}

class ServerThread extends Thread {

	private static final Logger _log = Logger.getLogger(ServerThread.class.getName());
	private Socket theConnection;
	private ServerProcessor aServerProcessor;
	private String sServerProcessName;

	public ServerThread(Socket s, String sServerProcessName) {
		theConnection = s;
		this.sServerProcessName = sServerProcessName;
		try {
			aServerProcessor = (ServerProcessor) ((ServerProcessor) Reflection.newInstance(sServerProcessName, null))
					.clone();
			aServerProcessor.addConnection(theConnection);
		} catch (Exception ex) {
			ex.printStackTrace();
			_log.warning("new Instance '" + sServerProcessName + "' error");

		}
	}

	/**
	 * when method "start" called , this method will called next if class extends
	 * thread.
	 */
	public void run() {
		try {
			aServerProcessor.run();
			theConnection.close();
			aServerProcessor = null;
			Runtime rt = java.lang.Runtime.getRuntime();
			_log.info("Total heap: " + rt.totalMemory());
			_log.info("Total free: " + rt.freeMemory());
			_log.info("do gc: ");
			rt.gc();

		} catch (Exception ex) {
			ex.printStackTrace();
			_log.warning("get ServerProcessor Clone fail");
		}

	}

}