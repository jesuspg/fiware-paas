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
 * Exception thrown when the application type is not found.
 * 
 * @author Jesus M. Movilla
 * @version $Id: $
 */
@SuppressWarnings("serial")
public class FileUtilsException extends Exception {

	public FileUtilsException() {
		super();
	}

	public FileUtilsException(String msg) {
		super(msg);
	}

	public FileUtilsException(Throwable e) {
		super(e);
	}

	public FileUtilsException(String msg, Throwable e) {
		super(msg, e);
	}

}