/**
 * Copyright (c) 2015, The MITRE Corporation. All rights reserved.
 * See LICENSE for complete terms.
 */
package org.mitre.stix.validator;

import static spark.SparkBase.port;
import static spark.SparkBase.staticFileLocation;

/**
 * The java-stix validator application
 * 
 * @author nemonik (Michael Joseph Walsh <github.com@nemonik.com>)
 *
 */
public class App {
	private static final int PORT = System.getenv("PORT") != null ? Integer.parseInt(System.getenv("PORT")) : 8080;
	
	public App() {
		port(PORT);
		staticFileLocation("/public");

		new ValidatorResource(new ValidatorService());
	}

	public static void main(String[] args) {
		new App();
	}
}