package com.beepcast.router;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.beepcast.util.properties.GlobalEnvironment;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;
import com.firsthop.common.util.xml.TreeUtil;

public class RouterConfFactory {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "RouterConfFactory" );

  static final GlobalEnvironment globalEnv = GlobalEnvironment.getInstance();

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public static RouterConf generateRouterConf( String propertyFile ) {
    RouterConf routerConf = new RouterConf();

    if ( ( propertyFile == null ) || ( propertyFile.equals( "" ) ) ) {
      return routerConf;
    }

    DLog.debug( lctx , "Loading from property = " + propertyFile );

    Element element = globalEnv.getElement( RouterConf.class.getName() ,
        propertyFile );
    if ( element != null ) {
      boolean result = validateTag( element );
      if ( result ) {
        extractElement( element , routerConf );
      }
    }

    return routerConf;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Core Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  private static boolean validateTag( Element element ) {
    boolean result = false;

    if ( element == null ) {
      DLog.warning( lctx , "Found empty in element xml" );
      return result;
    }

    Node node = TreeUtil.first( element , "router" );
    if ( node == null ) {
      DLog.warning( lctx , "Can not find root tag <router>" );
      return result;
    }

    result = true;
    return result;
  }

  private static boolean extractElement( Element element , RouterConf routerConf ) {
    boolean result = false;

    Node nodeRouter = TreeUtil.first( element , "router" );
    if ( nodeRouter == null ) {
      DLog.warning( lctx , "Can not find tag of router , using default ." );
      return result;
    }

    String stemp;

    stemp = TreeUtil.getAttribute( nodeRouter , "debug" );
    if ( ( stemp != null ) && ( stemp.equalsIgnoreCase( "true" ) ) ) {
      routerConf.setDebug( true );
    }

    Node nodeMessage = TreeUtil.first( nodeRouter , "message" );
    extractNodeMessage( nodeMessage , routerConf );

    Node nodeRetry = TreeUtil.first( nodeRouter , "retry" );
    extractNodeRetry( nodeRetry , routerConf );

    Node nodeDeliveryStatus = TreeUtil.first( nodeRouter , "deliveryStatus" );
    extractDeliveryStatus( nodeDeliveryStatus , routerConf );

    Node nodeSubmitMessage = TreeUtil.first( nodeRouter , "submitMessage" );
    extractNodeSubmitMessage( nodeSubmitMessage , routerConf );

    Node nodeDeliveryMessage = TreeUtil.first( nodeRouter , "deliveryMessage" );
    extractNodeDeliveryMessage( nodeDeliveryMessage , routerConf );

    Node nodeManagement = TreeUtil.first( nodeRouter , "management" );
    extractNodeManagement( nodeManagement , routerConf );

    result = true;
    return result;
  }

  private static void extractNodeMessage( Node nodeMessage ,
      RouterConf routerConf ) {
    if ( nodeMessage == null ) {
      DLog.warning( lctx , "Failed to extract node message , found null node" );
      return;
    }

    String stemp;
    int itemp;

    stemp = TreeUtil.getAttribute( nodeMessage , "expiry" );
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        if ( itemp > 0 ) {
          routerConf.setMsgExpiry( itemp );
        }
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = TreeUtil.getAttribute( nodeMessage , "priority" );
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        if ( itemp > 0 ) {
          routerConf.setMsgPriority( itemp );
        }
      } catch ( NumberFormatException e ) {
      }
    }

  }

  private static void extractNodeRetry( Node nodeRetry , RouterConf routerConf ) {
    if ( nodeRetry == null ) {
      DLog.warning( lctx , "Failed to extract node retry , found null node" );
      return;
    }

    String stemp;
    int itemp;

    stemp = TreeUtil.getAttribute( nodeRetry , "tries" );
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        if ( itemp > 0 ) {
          routerConf.setRetryTries( itemp );
        }
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = TreeUtil.getAttribute( nodeRetry , "duration" );
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        if ( itemp > 0 ) {
          routerConf.setRetryDuration( itemp );
        }
      } catch ( NumberFormatException e ) {
      }
    }

    Node nodeTry = TreeUtil.first( nodeRetry , "try" );
    while ( nodeTry != null ) {

      String key = null;
      stemp = TreeUtil.getAttribute( nodeTry , "num" );
      if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
        try {
          itemp = Integer.parseInt( stemp );
          if ( itemp > 0 ) {
            key = stemp;
          }
        } catch ( NumberFormatException e ) {
        }
      }

      String value = null;
      if ( key != null ) {
        stemp = TreeUtil.getAttribute( nodeTry , "duration" );
        if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
          try {
            itemp = Integer.parseInt( stemp );
            if ( itemp > 0 ) {
              value = stemp;
            }
          } catch ( NumberFormatException e ) {
          }
        }
      }
      if ( value != null ) {
        routerConf.getRetryMap().put( key , value );
      }

      nodeTry = TreeUtil.next( nodeTry , "try" );
    }

  }

  private static void extractDeliveryStatus( Node nodeDeliveryStatus ,
      RouterConf routerConf ) {
    if ( nodeDeliveryStatus == null ) {
      DLog.warning( lctx , "Failed to extract node delivery status "
          + ", found null node" );
      return;
    }

    String stemp;
    int itemp;

    stemp = TreeUtil.getAttribute( nodeDeliveryStatus , "worker" );
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        if ( itemp > 0 ) {
          routerConf.setDrWorker( itemp );
        }
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = TreeUtil.getAttribute( nodeDeliveryStatus , "queueSize" );
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        if ( itemp > 0 ) {
          routerConf.setDrQueueSize( itemp );
        }
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = TreeUtil.getAttribute( nodeDeliveryStatus , "minSleep" );
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        if ( itemp > 0 ) {
          routerConf.setDrMinSleep( itemp );
        }
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = TreeUtil.getAttribute( nodeDeliveryStatus , "maxSleep" );
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        if ( itemp > 0 ) {
          routerConf.setDrMaxSleep( itemp );
        }
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = TreeUtil.getAttribute( nodeDeliveryStatus , "minDBSleep" );
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        if ( itemp > 0 ) {
          routerConf.setDrMinDBSleep( itemp );
        }
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = TreeUtil.getAttribute( nodeDeliveryStatus , "maxDBSleep" );
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        if ( itemp > 0 ) {
          routerConf.setDrMaxDBSleep( itemp );
        }
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = TreeUtil.getAttribute( nodeDeliveryStatus , "limitRecord" );
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        if ( itemp > 0 ) {
          routerConf.setDrLimitRecord( itemp );
        }
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = TreeUtil.getAttribute( nodeDeliveryStatus , "burstSize" );
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        if ( itemp > 0 ) {
          routerConf.setDrBurstSize( itemp );
        }
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = TreeUtil.getAttribute( nodeDeliveryStatus , "maxLoad" );
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        if ( itemp > 0 ) {
          routerConf.setDrMaxLoad( itemp );
        }
      } catch ( NumberFormatException e ) {
      }
    }

  }

  private static void extractNodeSubmitMessage( Node nodeSubmitMessage ,
      RouterConf routerConf ) {
    if ( nodeSubmitMessage == null ) {
      DLog.warning( lctx , "Failed to extract node submit message "
          + ", found null node" );
      return;
    }

    String stemp;
    int itemp;

    stemp = TreeUtil.getAttribute( nodeSubmitMessage , "worker" );
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        if ( itemp > 0 ) {
          routerConf.setMtWorker( itemp );
        }
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = TreeUtil.getAttribute( nodeSubmitMessage , "queueSize" );
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        if ( itemp > 0 ) {
          routerConf.setMtQueueSize( itemp );
        }
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = TreeUtil.getAttribute( nodeSubmitMessage , "minSleep" );
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        if ( itemp > 0 ) {
          routerConf.setMtMinSleep( itemp );
        }
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = TreeUtil.getAttribute( nodeSubmitMessage , "maxSleep" );
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        if ( itemp > 0 ) {
          routerConf.setMtMaxSleep( itemp );
        }
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = TreeUtil.getAttribute( nodeSubmitMessage , "minDBSleep" );
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        if ( itemp > 0 ) {
          routerConf.setMtMinDBSleep( itemp );
        }
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = TreeUtil.getAttribute( nodeSubmitMessage , "maxDBSleep" );
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        if ( itemp > 0 ) {
          routerConf.setMtMaxDBSleep( itemp );
        }
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = TreeUtil.getAttribute( nodeSubmitMessage , "limitRecord" );
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        if ( itemp > 0 ) {
          routerConf.setMtLimitRecord( itemp );
        }
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = TreeUtil.getAttribute( nodeSubmitMessage , "burstSize" );
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        if ( itemp > 0 ) {
          routerConf.setMtBurstSize( itemp );
        }
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = TreeUtil.getAttribute( nodeSubmitMessage , "maxLoad" );
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        if ( itemp > 0 ) {
          routerConf.setMtMaxLoad( itemp );
        }
      } catch ( NumberFormatException e ) {
      }
    }

  }

  private static void extractNodeDeliveryMessage( Node nodeDeliveryMessage ,
      RouterConf routerConf ) {
    if ( nodeDeliveryMessage == null ) {
      DLog.warning( lctx , "Failed to extract node delivery message "
          + ", found null node" );
      return;
    }

    String stemp;
    int itemp;

    stemp = TreeUtil.getAttribute( nodeDeliveryMessage , "worker" );
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        if ( itemp > 0 ) {
          routerConf.setMoWorker( itemp );
        }
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = TreeUtil.getAttribute( nodeDeliveryMessage , "queueSize" );
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        if ( itemp > 0 ) {
          routerConf.setMoQueueSize( itemp );
        }
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = TreeUtil.getAttribute( nodeDeliveryMessage , "minSleep" );
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        if ( itemp > 0 ) {
          routerConf.setMoMinSleep( itemp );
        }
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = TreeUtil.getAttribute( nodeDeliveryMessage , "maxSleep" );
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        if ( itemp > 0 ) {
          routerConf.setMoMaxSleep( itemp );
        }
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = TreeUtil.getAttribute( nodeDeliveryMessage , "minDBSleep" );
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        if ( itemp > 0 ) {
          routerConf.setMoMinDBSleep( itemp );
        }
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = TreeUtil.getAttribute( nodeDeliveryMessage , "maxDBSleep" );
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        if ( itemp > 0 ) {
          routerConf.setMoMaxDBSleep( itemp );
        }
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = TreeUtil.getAttribute( nodeDeliveryMessage , "limitRecord" );
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        if ( itemp > -1 ) {
          routerConf.setMoLimitRecord( itemp );
        }
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = TreeUtil.getAttribute( nodeDeliveryMessage , "burstSize" );
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        if ( itemp > 0 ) {
          routerConf.setMoBurstSize( itemp );
        }
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = TreeUtil.getAttribute( nodeDeliveryMessage , "maxLoad" );
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        if ( itemp > 0 ) {
          routerConf.setMoMaxLoad( itemp );
        }
      } catch ( NumberFormatException e ) {
      }
    }

  }

  private static void extractNodeManagement( Node nodeManagement ,
      RouterConf routerConf ) {
    if ( nodeManagement == null ) {
      DLog.warning( lctx , "Failed to extract node management "
          + ", found null node" );
      return;
    }

    String stemp;
    int itemp;

    stemp = TreeUtil.getAttribute( nodeManagement , "sleep" );
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        if ( itemp > 0 ) {
          routerConf.setManagementSleep( itemp );
        }
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = TreeUtil.getAttribute( nodeManagement , "cleanIdle" );
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        if ( itemp > 0 ) {
          routerConf.setManagementCleanIdle( itemp );
        }
      } catch ( NumberFormatException e ) {
      }
    }

  }

}
