package com.elf.soap.soapmap.engine.mapping.soap;

import java.util.Arrays;

import com.elf.soap.soapmap.engine.mapping.parameter.ParameterMapping;

/**
 * 
 * @author
 * 
 */
public class SoapText implements SoapChild {

	private String text;
	private boolean isWhiteSpace;
	private boolean postParseRequired;

	private ParameterMapping[] parameterMappings;

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text.replace('\r', ' ').replace('\n', ' ').replace('\t', ' ');
		this.isWhiteSpace = text.trim().length() == 0;
	}

	public boolean isWhiteSpace() {
		return isWhiteSpace;
	}

	public ParameterMapping[] getParameterMappings() {
		return parameterMappings;
	}

	public void setParameterMappings(ParameterMapping[] parameterMappings) {
		if (parameterMappings != null) {
			this.parameterMappings = Arrays.copyOf(parameterMappings, parameterMappings.length);
		}
	}

	public boolean isPostParseRequired() {
		return postParseRequired;
	}

	public void setPostParseRequired(boolean postParseRequired) {
		this.postParseRequired = postParseRequired;
	}

}
