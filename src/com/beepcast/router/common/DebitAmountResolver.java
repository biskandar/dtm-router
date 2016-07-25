package com.beepcast.router.common;

import org.apache.commons.lang.StringUtils;

import com.beepcast.dbmanager.table.TCountry;
import com.beepcast.model.transaction.TransactionCountryUtil;
import com.beepcast.oproperties.OnlinePropertiesApp;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class DebitAmountResolver {

  static final DLogContext lctx = new SimpleContext( "DebitAmountResolver" );

  static final OnlinePropertiesApp opropsApp = OnlinePropertiesApp
      .getInstance();

  public static double resolveDebitAmount( String headerLog , int messageCount ,
      String phoneNumber ) {
    double debitAmount = 0;

    // validate must be params

    if ( messageCount < 1 ) {
      return debitAmount;
    }

    if ( StringUtils.isBlank( phoneNumber ) ) {
      return debitAmount;
    }

    try {

      // resolve country profile

      TCountry countryOut = TransactionCountryUtil.getCountryBean( phoneNumber );
      if ( countryOut == null ) {
        DLog.warning( lctx , headerLog + "Failed to resolve debit amount "
            + ", found invalid phone = " + phoneNumber );
        return debitAmount;
      }

      // calculate debit amount

      debitAmount = countryOut.getCreditCost();
      debitAmount = debitAmount * (double) messageCount;

      // log it

      DLog.debug( lctx , headerLog + "Resolved debitAmount = " + debitAmount
          + " : countryCreditCost = " + countryOut.getCreditCost()
          + " x messageCount = " + messageCount );

    } catch ( Exception e ) {
      DLog.warning( lctx , headerLog + "Failed to resolve debit amount , " + e );
    }

    return debitAmount;
  }

}
