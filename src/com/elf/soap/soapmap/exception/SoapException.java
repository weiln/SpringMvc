package com.elf.soap.soapmap.exception;

/**
 * Soap Exception.
 * 
 * @author xujiakun
 * 
 */
public class SoapException extends Exception {

	private static final long serialVersionUID = 938741855533002339L;

	private final String errorCode;

	public SoapException(String errorCode, String message, Throwable cause) {
		super(message, cause);
		this.errorCode = errorCode;
	}

	public SoapException(String errorCode, Throwable cause) {
		super(errorCode, cause);
		this.errorCode = errorCode;
	}

	public SoapException(String errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}

	public SoapException(String errorCode) {
		super(errorCode);
		this.errorCode = errorCode;
	}

	public String getErrorCode() {
		return errorCode;
	}

}
