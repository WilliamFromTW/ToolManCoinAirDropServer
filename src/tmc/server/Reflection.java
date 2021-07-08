package tmc.server;

import java.lang.reflect.*;
import java.util.logging.*;

public class Reflection {
  private static final Logger _log = Logger.getLogger(Reflection.class.getName());

  /**
   * dynamic load class.
   * <pre>
   *   if we create a class and want to use it dynamically
   *   ex:
   *     1:  com.fromtw.mybean aObj = new mybean(String,String);
   *     2:
   *     Object obj[] = new Object[2];
   *     obj[0] = "ddd";
   *     obj[1] = "eee";
   *     com.fromtw.mybean aObject =
   *     (mybean) getObject("com.fromtw.mybean",obj);
   * </pre>
   * @param sClassName dynamic class name
   * @param objs Object array used as parameter
   * @return Object u want to new dynamically
   */
  public static Object newInstance(String sClassName ,Object[] objs) throws Exception{
      _log.info("preparing load class");
      Class aClass = Class.forName(sClassName);
      _log.info("Class Got name=" + aClass.toString());
      Class aParameterClasses[] ;

      // Get all Constructors
      _log.info("preparing get all Constructor");
      java.lang.reflect.Constructor[] aClassConstructors = aClass.getConstructors();
      _log.info("Class Constructor Got! Total # =" + aClassConstructors.length );
      for(int i=0;i<aClassConstructors.length;i++){
        aParameterClasses = aClassConstructors[i].getParameterTypes();
        _log.info("Class Constructor # =" + i );
        _log.info("ParameterClasses total # =" + aParameterClasses.length );
        if( aParameterClasses.length == 0 && objs==null )
          return aClassConstructors[i].newInstance();
        else if( objs!=null && aParameterClasses.length == objs.length ){
          Object aReturnObject = null;
          try{
            _log.info("Constructor.newInstance");

            aReturnObject = aClassConstructors[i].newInstance( objs);
            return aReturnObject;
          }catch(Exception ee){
            _log.info("Parameter match, but type no the same!");
          }
        }
      }
      _log.info("no constructor found");
      return null;
  }
}