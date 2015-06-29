/**
 * Copyright (c) 2015, The MITRE Corporation. All rights reserved.
 * See LICENSE for complete terms.
 */
package org.mitre.stix.validator;

/**
 * A Valdation Result. Use to return the results of a validation.
 * 
 * @author nemonik (Michael Joseph Walsh <github.com@nemonik.com>)
 *
 */
public class ValidationResult {
	private String parseErrorMsg;
	private String validates;
	private String xmlText;
	private String xmlURL;
	private String filename;

	@Override
	public String toString() {
		return "ValidationResult [parseErrorMsg=" + parseErrorMsg
				+ ", validates=" + validates + ", xmlText=" + xmlText
				+ ", xmlURL=" + xmlURL + ", filename=" + filename + "]";
	}

	public String getFilename() {
		return filename;
	}

	public String getParseError() {
		return parseErrorMsg;
	}

	public String getXmlText() {
		return xmlText;
	}

	public String getXmlURL() {
		return xmlURL;
	}

	public String isValidates() {
		return validates;
	}

	public ValidationResult setFilename(String filename) {
		this.filename = filename;
		return this;
	}

	public ValidationResult setParseErrorMsg(String parseErrorMsg) {
		this.parseErrorMsg = parseErrorMsg;
		return this;
	}

	public ValidationResult setValidates(String validates) {
		this.validates = validates;
		return this;
	}

	public ValidationResult setXmlText(String xmlText) {
		this.xmlText = xmlText;
		return this;
	}

	public ValidationResult setXmlURL(String xmlURL) {
		this.xmlURL = xmlURL;
		return this;
	}
}
