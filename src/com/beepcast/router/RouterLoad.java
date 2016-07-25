package com.beepcast.router;

import com.beepcast.loadmng.LoadManagement;
import com.beepcast.loadmng.LoadManagementApi;

public class RouterLoad {

  private static LoadManagement loadMan = LoadManagement.getInstance();

  private static String hdrProf = LoadManagementApi.HDRPROF_ROUTER;
  private static String nameProf = "SYS";

  public static void hitMtIn( int count ) {
    loadMan.hit( hdrProf , LoadManagementApi.CONTYPE_SMSMT , "IN" , count ,
        true , false );
  }

  public static int hitMtIn() {
    return loadMan.getLoad( hdrProf , LoadManagementApi.CONTYPE_SMSMT , "IN" ,
        false , true );
  }

  public static void hitMtOu( int count ) {
    loadMan.hit( hdrProf , LoadManagementApi.CONTYPE_SMSMT , "OU" , count ,
        true , false );
  }

  public static int hitMtOu() {
    return loadMan.getLoad( hdrProf , LoadManagementApi.CONTYPE_SMSMT , "OU" ,
        false , true );
  }

  public static void hitDr( int count ) {
    loadMan.hit( hdrProf , LoadManagementApi.CONTYPE_SMSDR , nameProf , count ,
        true , false );
  }

  public static int hitDr() {
    return loadMan.getLoad( hdrProf , LoadManagementApi.CONTYPE_SMSDR ,
        nameProf , false , true );
  }

  public static void hitMo( int count ) {
    loadMan.hit( hdrProf , LoadManagementApi.CONTYPE_SMSMO , nameProf , count ,
        true , false );
  }

  public static int hitMo() {
    return loadMan.getLoad( hdrProf , LoadManagementApi.CONTYPE_SMSMO ,
        nameProf , false , true );
  }

}
