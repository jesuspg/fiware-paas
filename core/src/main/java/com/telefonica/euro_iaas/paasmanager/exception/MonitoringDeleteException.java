/*

  (c) Copyright 2011 Telefonica, I+D. Printed in Spain (Europe). All Rights
  Reserved.

  The copyright to the software program(s) is property of Telefonica I+D.
  The program(s) may be used and or copied only with the express written
  consent of Telefonica I+D or in accordance with the terms and conditions
  stipulated in the agreement/contract under which the program(s) have
  been supplied.

 */
package com.telefonica.euro_iaas.paasmanager.exception;

/**
 * @author bmmanso
 * 
 */
@SuppressWarnings("serial")
public class MonitoringDeleteException extends Exception {

	public MonitoringDeleteException(String msg) {
		super(msg);
	}

	public MonitoringDeleteException(Throwable e) {
		super(e);
	}
}