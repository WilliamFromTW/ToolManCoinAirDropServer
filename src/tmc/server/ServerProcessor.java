package tmc.server;
import java.net.*;
import java.util.*;
import java.lang.ClassNotFoundException;
import java.io.*;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import tmc.server.ServerGlobal;


/**
 * TCP/IP Client Server Framework,
 * Using for writing your own server processor.
 * @author william chen
 */
abstract public class ServerProcessor implements Cloneable {

   private static final Logger _log = Logger.getLogger(ServerProcessor.class.getName());
   private static int iSeed = 0;
   protected int iOID = -1;
   protected static List pool = Collections.synchronizedList(new LinkedList());
   protected static HashMap<String ,ClientInfo> AllClientInfo = new HashMap<String ,ClientInfo>();
   protected Socket connection = null;
   protected OutputStreamWriter out = null;
   protected InputStreamReader in = null;
   protected ClientInfo aClientInfo = null;
   public static String sInputEncode = null;
   public static String sOutputEncode = null;
   public static final String SEND_CLOSE = "server_close";
   public static final String SEND_TO = "server_to";
   public static final String SEND_TO_CLOSE = "server_to_close";
   public static final String SEND_BACK = "server_back";
   public static final String SEND_ALL = "server_all";
   public static final String SEND_BACK_CLOSE = "server_back_close";
   public static final String SEND_ALL_CLOSE = "server_all_close";

   /**
    * default input encode and output encode is UTF-8
    */
   public ServerProcessor(){
     this(ServerGlobal.FILE_ENCODE,ServerGlobal.FILE_ENCODE );
   }

   public ClientInfo getClientInfo() {
	   return this.aClientInfo;
   }
   /**
    * customize input and output encode
    * @param sInputEncode input encode
    * @param sOutputEncode output encode
    */
   public ServerProcessor(String sInputEncode,String sOutputEncode){
     this.sInputEncode = sInputEncode;
     this.sOutputEncode = sOutputEncode;
   }

   public Socket getConnection(){
     return connection;
   }

    public void setConnection(Socket aConnection){
     connection = aConnection;
   }

   /**
    * initial object id to this object
    */
   synchronized public void setOID(){
     iSeed ++;
     iOID = iSeed;
   }

   /**
    * return object id
    */
   public int getOID(){
     return iOID;
   }
   /**
    * this class implemnet clone method, and override protected method "clone" to public
    * it will call method "init" first.
    */
   public Object clone() throws CloneNotSupportedException{
     Object obj = super.clone();
     _log.info("new Clone");
     ((ServerProcessor)obj).init();
     ((ServerProcessor)obj).setOID();
     return obj;
   };

   /**
    * @return HashMap
    * <pre>
    *   Key=Cmd, vales will be SEND_ALL,SEND_BACK....
    *   Key=Data, data
    *   [optional] if key=SEND_TO, one key="OID" must be HashMap , value will be object id
    * </pre>
    */
   public abstract HashMap parseCmd(int iCmd);

   /**
    * initial this object data , called when clone be involke
    */
   public abstract void init();

   /**
    * close all resource in object
    */
   public void destory(){
 //    try{
       deleteConnection();
       //connection.close();
       connection = null;
       out = null;
       in = null;
       _log.info("Object ServerProcessor destory");
//     }catch(IOException ex){
 //     _log.warning("socket connection close fail");
//     }
   }

   /**
    * add socket connect to pool
    */
   public boolean addConnection(Socket connection){
      synchronized (pool) {
         pool.add(pool.size(), this);
         setConnection(connection);
         pool.notifyAll();
         _log.info("Get connection,pool size="+pool.size());
         return true;
      }
   }

   /**
    * this socket connection in this object
    */
   public boolean deleteConnection(){
      synchronized(pool){
         pool.remove(this);
         pool.notifyAll();
         _log.info("delete connection,pool size="+pool.size());
      }
      try{
        connection.close();
      }catch(Exception ex){
        ex.printStackTrace();
        _log.warning("socket sonnection error");
      }
      return true;
   }

  /**
   * run.
   */
  public void run(){
    HashMap aReturnHash = null;
    String sCmd = null;
    String sData = null;
    Socket aTmpConn = null;
    OutputStreamWriter aTmpOSW = null;
    if(true){
      try {
    	if( connection.getKeepAlive()==false) {
    	  System.out.println("Socket keep alive = false , try to set enable");
    	  connection.setKeepAlive(true);
    	}
        out = new OutputStreamWriter(connection.getOutputStream(),sOutputEncode);
        in = new InputStreamReader(connection.getInputStream(),sInputEncode);
        int c;
        //System.out.println("output encode = " + out.getEncoding() );
        //System.out.println("input encode = " + in.getEncoding() );
        while (true) {

          c = in.read();
          if(c==-1){
            _log.info("Connection has broken");
            if( aReturnHash!=null)  aReturnHash.clear();
            destory();
            return;
          }
          aReturnHash = parseCmd(c);
          if( aReturnHash!=null ){
            sCmd = (String) aReturnHash.get("Cmd");
            sData = (String) aReturnHash.get("Data");
            if( sCmd.equals( SEND_BACK ) ){
              out.write(sData);
              out.flush();
            }else if( sCmd.equals( SEND_BACK_CLOSE ) ){
              out.write(sData);
              out.flush();
              if( aReturnHash!=null)  aReturnHash.clear();
              destory();
              return;
            }else if( sCmd.equals( SEND_ALL ) ){
              for(int i=0;i<pool.size();i++){
                aTmpConn = ((ServerProcessor)pool.get(i)).getConnection();
                aTmpOSW = new  OutputStreamWriter(aTmpConn.getOutputStream(),sOutputEncode);
                aTmpOSW.write(sData);
                aTmpOSW.flush();
              }
            }else if( sCmd.equals( SEND_ALL_CLOSE ) ){
              for(int i=0;i<pool.size();i++){
                aTmpConn = ((ServerProcessor)pool.get(i)).getConnection();
                aTmpOSW = new  OutputStreamWriter(aTmpConn.getOutputStream(),sOutputEncode);
                aTmpOSW.write(sData);
                aTmpOSW.flush();
              }
              if( aReturnHash!=null)  aReturnHash.clear();
              destory();
              return;
            }else if( sCmd.equals( SEND_TO ) ){
              int iTmpOID = Integer.parseInt( (String)aReturnHash.get("OID") );
              for(int i=0;i<pool.size();i++){
                if( iTmpOID == ((ServerProcessor)pool.get(i)).getOID() ){
                  aTmpConn = ((ServerProcessor)pool.get(i)).getConnection();
                  aTmpOSW = new  OutputStreamWriter(aTmpConn.getOutputStream(),sOutputEncode);
                  aTmpOSW.write(sData);
                  aTmpOSW.flush();
                  break;
                }
              }
            }else if( sCmd.equals( SEND_TO_CLOSE ) ){
              int iTmpOID = Integer.parseInt( (String)aReturnHash.get("OID") );
              for(int i=0;i<pool.size();i++){
                if( iTmpOID == ((ServerProcessor)pool.get(i)).getOID() ){
                  aTmpConn = ((ServerProcessor)pool.get(i)).getConnection();
                  aTmpOSW = new  OutputStreamWriter(aTmpConn.getOutputStream(),sOutputEncode);
                  aTmpOSW.write(sData);
                  aTmpOSW.flush();
                  break;
                }
              }
              if( aReturnHash!=null)  aReturnHash.clear();
              destory();
              return;
            }else if( sCmd.equals( SEND_CLOSE ) ){
              out.flush();
              if( aReturnHash!=null)  aReturnHash.clear();
              destory();
              return;
            }
          }
        }
      }
      catch (Exception e) {
        destory();
        e.printStackTrace();
        _log.warning("connection broken , "+e.getMessage());
      }
    }// end of while
  }

}