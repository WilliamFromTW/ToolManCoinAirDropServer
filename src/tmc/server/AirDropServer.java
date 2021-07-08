package tmc.server;

import java.io.*;
import java.util.*;
import java.net.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Calendar;

public class AirDropServer {
  public static void main(String arg[]){
    Server server  = new Server();
    try{
      server.start(Integer.parseInt(arg[0]),"tmc.server.ServerProcessorImpl");
    }catch(Exception ex){
      ex.printStackTrace();
    }
  }
}