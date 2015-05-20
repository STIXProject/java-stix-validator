package org.mitre.stix.validator;

import static spark.Spark.get;
import static spark.Spark.port;

import java.util.HashMap;
import java.util.Map;

import spark.ModelAndView;
import spark.template.jade.JadeTemplateEngine;

public class App {
	private static final int PORT = System.getenv("OPENSHIFT_DIY_PORT") != null ? Integer.parseInt(System.getenv("OPENSHIFT_DIY_PORT")) : Integer.parseInt(System.getProperty("port"));

	public static void main(String[] args) {
		
		port(PORT);
		
		Map<String, String> map = new HashMap<>();
		map.put("message", "Hello World!");
		get("/", (req, res) -> new ModelAndView(map, "hello"), new JadeTemplateEngine());
	}
}