package tmc.server;

import java.io.*;
import java.util.*;
import java.net.*;
import java.util.Calendar;
import java.util.logging.*;

public class ServerProcessorImpl extends ServerProcessor{

  private static final Logger _log = Logger.getLogger(ServerProcessorImpl.class.getName());
  private StringBuffer aCmd = null;
  private boolean bRegStatus = false;//not register
  private  HashMap aHM;
  public static String CMD_REG = "REG";
  public static String CMD_ONLINE = "ONLINE";
  public static String CMD_OFFLINE = "OFFLINE";
  public static String CMD_DISPLAY = "DISPLAY";
  public static String CMD_EXIT = "EXIT";

   public ServerProcessorImpl(){
    super(ServerGlobal.FILE_ENCODE,ServerGlobal.FILE_ENCODE);
  }
   
  public void destory(){
    Socket aTmpConn = null;
    OutputStreamWriter aTmpOSW = null;
    _log.info("call super.destory()");
    super.destory();
    try{
      if( aClientInfo!=null ){
        _log.info("tell all client someone offline : "+aClientInfo.getWallet());
        for(int i=0;i<pool.size();i++){
          aTmpConn = ((ServerProcessor)pool.get(i)).getConnection();
          aTmpOSW = new  OutputStreamWriter(aTmpConn.getOutputStream(),sOutputEncode);
          aTmpOSW.write(CMD_OFFLINE+aClientInfo.getWallet()+"\r\n");
          aTmpOSW.flush();
        }
      }
    }catch(Exception ex){
      _log.info("destory exception : " + ex.toString() );
    }
  }

  public void init(){
    _log.info("init cmd");
    aCmd = new StringBuffer();
  }

  private void setupClientInfo(String sInfo) throws Exception {
	  String[] sData = sInfo.split(",");
	  //System.out.println("reg token size = "+sData.length);
	  if( sData.length!=2)
		  throw new Exception("parameter size error");
	  else {
		if( AllClientInfo.get(sData[0])==null ) {
		//	System.out.println("wallet="+sData[0]+",publickey="+sData[1]);
		  aClientInfo.setWallet(sData[0]);
		  aClientInfo.setWalletPublicKey(sData[1]);
		  AllClientInfo.put(sData[0], aClientInfo);
		}
		else {
		  aClientInfo = AllClientInfo.get(sData[0]);
		}
	  }	  
  }
  public HashMap parseCmd(int iCmd){
    if( (char)iCmd!='\n' ){
      if( (char)iCmd!='\r' ){
        aCmd.append((char)iCmd);
      }
      return null;
    }
    else{
   	  //System.out.println("aCmd.toString()"+aCmd.toString());
      if( aCmd.toString().toUpperCase().indexOf(CMD_REG)!=-1 ){
        bRegStatus = true;
        try {
        	//System.out.println("REG info = " +aCmd.toString().trim().substring(CMD_REG.length()).trim()+"\r\n");
        setupClientInfo(aCmd.toString().trim().substring(CMD_REG.length()).trim());
        aHM = new HashMap();
        aHM.put("Cmd",ServerProcessor.SEND_ALL );
        aHM.put("Data","\r\nWallet:"+aClientInfo.getWallet()+" registered , from " + connection.getRemoteSocketAddress() +"\r\n");
        aCmd = new StringBuffer("");
        return aHM;
        }catch(Exception ee) {
        	ee.printStackTrace();
            aHM = new HashMap();
            aHM.put("Cmd",ServerProcessor.SEND_BACK_CLOSE);
            aHM.put("Data","parameter error! \r\n");
            aCmd = new StringBuffer("");
            return aHM;
        }
      }else if( aCmd.toString().toUpperCase().indexOf(CMD_DISPLAY)!=-1 ){ // display[message]
        if( !bRegStatus ){
          aHM = new HashMap();
          aHM.put("Cmd",ServerProcessor.SEND_BACK_CLOSE);
          aHM.put("Data","Sorry You must register! \r\n");
          aCmd = new StringBuffer("");
          return aHM;
        }
        aHM = new HashMap();
        aHM.put("Cmd",ServerProcessor.SEND_ALL );
        aHM.put("Data",CMD_DISPLAY + aCmd.toString().trim().substring(CMD_DISPLAY.length()) +"\r\n");
        aCmd = new StringBuffer("");
        return aHM;

      }else if( aCmd.toString().toUpperCase().indexOf(CMD_EXIT)!=-1 ){ // display[message]
    	  aHM = new HashMap();
          aHM.put("Cmd",ServerProcessor.SEND_CLOSE );
          aCmd = new StringBuffer("");
          return aHM; 
      }else{
        if( !bRegStatus ){
          aHM = new HashMap();
          aHM.put("Cmd",ServerProcessor.SEND_BACK_CLOSE  );
          aHM.put("Data","Sorry You must register! \r\n");
          aCmd = new StringBuffer("");
          return aHM;
        }

        aHM = new HashMap();
        aHM.put("Cmd",ServerProcessor.SEND_BACK );
        aHM.put("Data", "send back\r\n");
        aCmd = new StringBuffer("");
        return aHM;
      }
    }

  }


}