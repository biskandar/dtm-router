package com.beepcast.router;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.beepcast.api.provider.ProviderApp;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class RouterListProviderIds {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext(
      "RouterListActiveProviderIds" );

  static final Object objectLock = new Object();

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  private boolean debug;
  private ProviderApp providerApp;
  private List listActiveWorkerProviderIdsWtFilterSuspend;
  private List listActiveWorkerProviderIdsWoFilterSuspend;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public RouterListProviderIds( boolean debug ) {

    this.debug = debug;
    providerApp = ProviderApp.getInstance();
    listActiveWorkerProviderIdsWtFilterSuspend = new ArrayList();
    listActiveWorkerProviderIdsWoFilterSuspend = new ArrayList();

    DLog.debug( lctx , "Created empty list active worker provider ids "
        + "with/out filter suspend" );

  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Set / Get Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public boolean isDebug() {
    return debug;
  }

  public void setDebug( boolean debug ) {
    this.debug = debug;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public boolean refreshLists() {
    return refreshLists( null , null );
  }

  public boolean refreshLists( List listAdditionalSuspendedProviderIds ,
      List listAdditionalActiveProviderIds ) {
    boolean result = false;
    synchronized ( objectLock ) {
      try {

        {

          // read active providers with suspended filter from provider app
          List listActiveWorkerProviderIdsWtFilterSuspendLocal = providerApp
              .listOutgoingProviderIds( null , true , true , true );

          // keep the list exist
          if ( listActiveWorkerProviderIdsWtFilterSuspendLocal == null ) {
            listActiveWorkerProviderIdsWtFilterSuspendLocal = new ArrayList();
          }

          // remove with additional suspended list if found any
          if ( ( listAdditionalSuspendedProviderIds != null )
              && ( listAdditionalSuspendedProviderIds.size() > 0 ) ) {
            listActiveWorkerProviderIdsWtFilterSuspendLocal
                .removeAll( listAdditionalSuspendedProviderIds );
          }

          // add with additional active list if found any
          if ( ( listAdditionalActiveProviderIds != null )
              && ( listAdditionalActiveProviderIds.size() > 0 ) ) {
            listActiveWorkerProviderIdsWtFilterSuspendLocal = mergeList(
                listActiveWorkerProviderIdsWtFilterSuspendLocal ,
                listAdditionalActiveProviderIds );
          }

          // synchronize with global data member and log if found any changes
          if ( !isListEquals( listActiveWorkerProviderIdsWtFilterSuspendLocal ,
              listActiveWorkerProviderIdsWtFilterSuspend ) ) {
            DLog.debug( lctx ,
                "Refreshed list active worker providers with suspended : "
                    + listActiveWorkerProviderIdsWtFilterSuspend + " -> "
                    + listActiveWorkerProviderIdsWtFilterSuspendLocal );
            listActiveWorkerProviderIdsWtFilterSuspend.clear();
            listActiveWorkerProviderIdsWtFilterSuspend
                .addAll( listActiveWorkerProviderIdsWtFilterSuspendLocal );
          }

        }

        {

          // read active worker providers without suspended filter from provider
          // app
          List listActiveWorkerProviderIdsWoFilterSuspendLocal = providerApp
              .listOutgoingProviderIds( null , true , true , false );

          // keep the list exist
          if ( listActiveWorkerProviderIdsWoFilterSuspendLocal == null ) {
            listActiveWorkerProviderIdsWoFilterSuspendLocal = new ArrayList();
          }

          // add with additional active list if found any
          if ( ( listAdditionalActiveProviderIds != null )
              && ( listAdditionalActiveProviderIds.size() > 0 ) ) {
            listActiveWorkerProviderIdsWoFilterSuspendLocal = mergeList(
                listActiveWorkerProviderIdsWoFilterSuspendLocal ,
                listAdditionalActiveProviderIds );
          }

          // synchronize with global data member and log if found any changes
          if ( !isListEquals( listActiveWorkerProviderIdsWoFilterSuspendLocal ,
              listActiveWorkerProviderIdsWoFilterSuspend ) ) {
            DLog.debug( lctx ,
                "Refreshed list active worker providers without suspended : "
                    + listActiveWorkerProviderIdsWoFilterSuspend + " -> "
                    + listActiveWorkerProviderIdsWoFilterSuspendLocal );
            listActiveWorkerProviderIdsWoFilterSuspend.clear();
            listActiveWorkerProviderIdsWoFilterSuspend
                .addAll( listActiveWorkerProviderIdsWoFilterSuspendLocal );
          }

        }

        result = true;
      } catch ( Exception e ) {
        DLog.warning( lctx , "Failed to refresh lists , " + e );
      }
    }
    return result;
  }

  public List listActiveWorkerProviderIdsWithFilterSuspend() {
    List list = null;
    synchronized ( objectLock ) {
      try {
        list = new ArrayList( listActiveWorkerProviderIdsWtFilterSuspend );
      } catch ( Exception e ) {
        DLog.warning( lctx , "Failed to list active worker "
            + "provider ids with filter suspend , " + e );
      }
    }
    return list;
  }

  public List listActiveWorkerProviderIdsWithoutFilterSuspend() {
    List list = null;
    synchronized ( objectLock ) {
      try {
        list = new ArrayList( listActiveWorkerProviderIdsWoFilterSuspend );
      } catch ( Exception e ) {
        DLog.warning( lctx , "Failed to list active worker "
            + "provider ids without filter suspend , " + e );
      }
    }
    return list;
  }

  public boolean isExistInListActiveWorkerProviderIdsWithFilterSuspend(
      String providerId ) {
    boolean result = false;
    synchronized ( objectLock ) {
      try {

        if ( providerId == null ) {
          return result;
        }

        if ( debug ) {
          DLog.debug( lctx , "is provider " + providerId
              + " exist in listActiveWorkerProviderIdsWtFilterSuspend : "
              + listActiveWorkerProviderIdsWtFilterSuspend );
        }

        result = listActiveWorkerProviderIdsWtFilterSuspend
            .indexOf( providerId ) > -1;

      } catch ( Exception e ) {
        DLog.warning( lctx , "Failed to find provider " + providerId
            + " in the list , " + e );
      }
    }
    return result;
  }

  public boolean isExistInListActiveWorkerProviderIdsWithoutFilterSuspend(
      String providerId ) {
    boolean result = false;
    synchronized ( objectLock ) {
      if ( providerId == null ) {
        return result;
      }
      try {
        result = listActiveWorkerProviderIdsWoFilterSuspend
            .indexOf( providerId ) > -1;
      } catch ( Exception e ) {
        DLog.warning( lctx , "Failed to find provider " + providerId
            + " in the list , " + e );
      }
    }
    return result;
  }

  public List mergeList( List listA , List listB ) {
    List listC = null;

    if ( ( listA == null ) && ( listB == null ) ) {
      return listC;
    }

    listC = new ArrayList();

    String val = null;

    if ( listA != null ) {
      Iterator iter = listA.iterator();
      while ( iter.hasNext() ) {
        val = (String) iter.next();
        if ( ( val == null ) || ( val.equals( "" ) ) ) {
          continue;
        }
        if ( listC.indexOf( val ) > -1 ) {
          continue;
        }
        listC.add( val );
      }
    }

    if ( listB != null ) {
      Iterator iter = listB.iterator();
      while ( iter.hasNext() ) {
        val = (String) iter.next();
        if ( ( val == null ) || ( val.equals( "" ) ) ) {
          continue;
        }
        if ( listC.indexOf( val ) > -1 ) {
          continue;
        }
        listC.add( val );
      }
    }

    return listC;
  }

  public boolean isListEquals( List listA , List listB ) {
    boolean result = false;

    if ( ( listA == listB ) ) {
      result = true;
      return result;
    }

    if ( ( listA == null ) || ( listB == null ) ) {
      return result;
    }

    if ( listA.size() != listB.size() ) {
      return result;
    }

    List listC = new ArrayList( listA );
    listC.removeAll( listB );
    if ( listC.size() > 0 ) {
      return result;
    }

    result = true;
    return result;
  }

}
