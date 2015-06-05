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
	String parseErrorMsg;
	String validates;
	String xmlText;
	String xmlURL;

	public String getXmlText() {
		return xmlText;
	}

	public ValidationResult setXmlText(String xmlText) {
		this.xmlText = xmlText;
		return this;
	}

	public String getXmlURL() {
		return xmlURL;
	}

	public ValidationResult setXmlURL(String xmlURL) {
		this.xmlURL = xmlURL;
		return this;
	}

	public String isValidates() {
		return validates;
	}

	public ValidationResult setValidates(String validates) {
		this.validates = validates;
		return this;
	}

	public String getParseError() {
		return parseErrorMsg;
	}

	public ValidationResult setParseErrorMsg(String parseErrorMsg) {
		this.parseErrorMsg = parseErrorMsg;
		return this;
	}
}
