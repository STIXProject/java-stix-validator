/**
 * Copyright (c) 2015, The MITRE Corporation. All rights reserved.
 * See LICENSE for complete terms.
 */
package org.mitre.stix.validator;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;
import org.xml.sax.SAXException;

/**
 * Parsing and validating error handler
 * 
 * @author nemonik (Michael Joseph Walsh <github.com@nemonik.com>) *
 */


public class ValidationErrorHandler implements ErrorHandler {

	private ValidatorService callback;

	public ValidationErrorHandler(ValidatorService callback) {
		super();
		this.callback = callback;
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
	 */
	@Override
	public void error(SAXParseException e) throws SAXException {
		callback.methodToCallBack("ERROR", e);
		throw e;
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
	 */
	@Override
	public void fatalError(SAXParseException e) throws SAXException {
		callback.methodToCallBack("FATAL ERROR", e);
		throw e;
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
	 */
	@Override
	public void warning(SAXParseException e) throws SAXException {
		callback.methodToCallBack("WARNING", e);
		throw e;
	}
}