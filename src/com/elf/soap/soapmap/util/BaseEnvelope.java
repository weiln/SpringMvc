package com.elf.soap.soapmap.util;

import org.dom4j.Element;
import org.dom4j.VisitorSupport;

/**
 * 
 * @author xujiakun
 * 
 */
public class BaseEnvelope extends VisitorSupport {

	private String msgId;

	private String faultCode;

	private String faultString;

	private String faultActor;

	private String messageCode;

	private String insertion;

	private String faultRelatedException;

	private String samlArtifact;

	private boolean successFlag;

	private String errorMessage;

	private int iterationCount;

	private String status;

	public void visit(Element node) {
		String name = node.getName();
		String text = node.getText();

		if ("msg-id".equals(name)) {
			msgId = text;
		} else if ("faultcode".equals(name)) {
			faultCode = text;
		} else if ("faultstring".equals(name)) {
			faultString = text;
		} else if ("faultactor".equals(name)) {
			faultActor = text;
		} else if ("MessageCode".equals(name)) {
			messageCode = text;
		} else if ("Insertion".equals(name)) {
			insertion = text;
		} else if ("FaultRelatedException".equals(name)) {
			faultRelatedException = text;
		} else if ("AssertionArtifact".equals(name)) {
			samlArtifact = text;
		} else if ("error".equals(name)) {
			errorMessage = text;
		} else if ("Deleted".equals(name)) {
			// deleteAutoDelegationsForUser
			if ("true".equals(text)) {
				successFlag = true;
			}
		} else if ("ITERATION_COUNT".equals(name)) {
			iterationCount = Integer.parseInt(text);
		} else if ("STATUS".equals(name)) {
			status = text;
		}
	}

	public String getMsgId() {
		return msgId;
	}

	public void setMsgId(String msgId) {
		this.msgId = msgId;
	}

	public String getFaultCode() {
		return faultCode;
	}

	public void setFaultCode(String faultCode) {
		this.faultCode = faultCode;
	}

	public String getFaultString() {
		return faultString;
	}

	public void setFaultString(String faultString) {
		this.faultString = faultString;
	}

	public String getFaultActor() {
		return faultActor;
	}

	public void setFaultActor(String faultActor) {
		this.faultActor = faultActor;
	}

	public String getMessageCode() {
		return messageCode;
	}

	public void setMessageCode(String messageCode) {
		this.messageCode = messageCode;
	}

	public String getInsertion() {
		return insertion;
	}

	public void setInsertion(String insertion) {
		this.insertion = insertion;
	}

	public String getFaultRelatedException() {
		return faultRelatedException;
	}

	public void setFaultRelatedException(String faultRelatedException) {
		this.faultRelatedException = faultRelatedException;
	}

	public String getSamlArtifact() {
		return samlArtifact;
	}

	public void setSamlArtifact(String samlArtifact) {
		this.samlArtifact = samlArtifact;
	}

	public boolean isSuccessFlag() {
		return successFlag;
	}

	public void setSuccessFlag(boolean successFlag) {
		this.successFlag = successFlag;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public int getIterationCount() {
		return iterationCount;
	}

	public void setIterationCount(int iterationCount) {
		this.iterationCount = iterationCount;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
