package tmc.server;

import java.util.Properties;
import java.util.logging.*;
import java.io.*;
import java.sql.*;

public class ServerGlobal{

  public static final String FILE_ENCODE = "UTF-8";
  public static final String PROP_SENDER_WALLET_PASSPHRASE = "PROP_SENDER_WALLET_PASSPHRASE";
  public static final String PROP_TRANSFER_COIN_NUMBER = "PROP_TRANSFER_COIN_NUMBER";
  public static final String PROP_GAIN_PERIOD = "PROP_GAIN_PERIOD";
  public static final String PROP_LISTEN_PORT = "PROP_LISTEN_PORT";
  public static String TRANSFER_COIN_NUMBER = ServerGlobal.getProperties(ServerGlobal.PROP_TRANSFER_COIN_NUMBER);
  public static final String PERIOD_TIME = ServerGlobal.getProperties(ServerGlobal.PROP_GAIN_PERIOD);;

  public static final String SERVER_CONFIG_FILE = "server.properties";
  private static final Logger _log = Logger.getLogger(ServerGlobal.class.getName());

  private static Properties serverSettings = null;

  public static void setTransferCoinNumber(String s) {
	  TRANSFER_COIN_NUMBER = s;
  }
  public static String getProperties(String sKey){
    if( serverSettings == null )
      loadConfig();
    return serverSettings.getProperty(sKey);
  }

  private static void loadConfig(){
    try{
    	;
      serverSettings = new Properties();
//      InputStreamReader ir = new InputStreamReader(ServerGlobal.class.getClassLoader().getResourceAsStream(SERVER_CONFIG_FILE),FILE_ENCODE);
      InputStreamReader ir = new InputStreamReader(new FileInputStream(new File(SERVER_CONFIG_FILE)),FILE_ENCODE);

 	  serverSettings.load(ir);
 	  ir.close();
 	}catch(Exception ex){
 	  ex.printStackTrace();
 	  _log.warning("read config error");
    }
  
  }
}