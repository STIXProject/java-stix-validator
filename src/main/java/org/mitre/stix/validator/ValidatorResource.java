/**
 * Copyright (c) 2015, The MITRE Corporation. All rights reserved.
 * See LICENSE for complete terms.
 */
package org.mitre.stix.validator;

import static spark.Spark.post;

public class ValidatorResource {
	private static final String API_CONTEXT = "/api/v1";

	private  final ValidatorService validatorService;

	public ValidatorResource(ValidatorService validatorService) {
		this.validatorService = validatorService;
		setupEndpoints();
	}

	private void setupEndpoints() {
		post(API_CONTEXT + "/validate/url", "application/json", (request, response)
				-> validatorService.validateURL(request.body()), new JsonTransformer());
		
		post(API_CONTEXT + "/validate/string", "application/json", (request, response)
				-> validatorService.validateXML(request.body()), new JsonTransformer());
	}
}