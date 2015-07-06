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

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ValidationResult [parseErrorMsg=" + parseErrorMsg
				+ ", validates=" + validates + ", xmlText=" + xmlText
				+ ", xmlURL=" + xmlURL + ", filename=" + filename + "]";
	}

	/**
	 * @return the name of the file
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * @return the text for the error
	 */
	public String getParseError() {
		return parseErrorMsg;
	}

	/**
	 * @return the STIX Document text
	 */
	public String getXmlText() {
		return xmlText;
	}

	/**
	 * @return the URL for the STIX document
	 */
	public String getXmlURL() {
		return xmlURL;
	}

	/**
	 * @return 'true' or 'false'
	 */
	public String isValidates() {
		return validates;
	}

	/**
	 * @param filename
	 *            the name of the file
	 * @return this ValidationResult object
	 */
	public ValidationResult setFilename(String filename) {
		this.filename = filename;
		return this;
	}

	/**
	 * @param parseErrorMsg
	 *            the text for the error
	 * @return this ValidationResult object
	 */
	public ValidationResult setParseErrorMsg(String parseErrorMsg) {
		this.parseErrorMsg = parseErrorMsg;
		return this;
	}

	/**
	 * @param validates
	 *            holds 'true' or 'false'
	 * @return this ValidationResult object
	 */
	public ValidationResult setValidates(String validates) {
		this.validates = validates;
		return this;
	}

	/**
	 * @param xmlText
	 *            the STIX Document text
	 * @return this ValidationResult object
	 */
	public ValidationResult setXmlText(String xmlText) {
		this.xmlText = xmlText;
		return this;
	}

	/**
	 * @param xmlURL
	 *            URL for STIX Document
	 * @return this ValidationResult object
	 */
	public ValidationResult setXmlURL(String xmlURL) {
		this.xmlURL = xmlURL;
		return this;
	}
}
