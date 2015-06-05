/**
 * Copyright (c) 2015, The MITRE Corporation. All rights reserved.
 * See LICENSE for complete terms.
 */
package org.mitre.stix.validator;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.github.zafarkhaja.semver.UnexpectedCharacterException;
import com.github.zafarkhaja.semver.Version;

/**
 * A Validator Service
 * 
 * @author nemonik (Michael Joseph Walsh <github.com@nemonik.com>)
 *
 */
public class ValidatorService implements ValidationErrorCallback {

	private HashMap<Version, Object> stixSchemas;

	private String parseErrorMsg;

	private boolean validates;

	public static final String[] DEFAULT_SUPORTED_SCHEMAS = { "1.1.1", "1.2.0" };

	/**
	 * Uses individual ClassLoaders to load and create individual STIXSchema
	 * objects used for validation.
	 * 
	 * @param versions
	 *            The schema versions to create STIXSchema objects for.
	 */
	/**
	 * @param versions
	 */
	private void loadSTIXSchemas(String... versions) {

		stixSchemas = new HashMap<Version, Object>();

		for (String version : versions) {
			try {

				Version semVer = Version.valueOf(version);

				ResourcePatternResolver patternResolver = new PathMatchingResourcePatternResolver();

				Resource[] resources = patternResolver
						.getResources("classpath:reflection-libs/v" + version
								+ "/**/*.jar");

				ArrayList<URL> urls = new ArrayList<URL>();

				// work around for URLClassLoader's inability to retrieve
				// classes from Jars contained within Jars as resources
				for (Resource resource : resources) {
					ReadableByteChannel readableByteChannel = Channels
							.newChannel(resource.getURL().openStream());
					File tempFile = File.createTempFile("validator-", null);
					FileOutputStream fileOutputStream = new FileOutputStream(
							tempFile);
					fileOutputStream.getChannel().transferFrom(
							readableByteChannel, 0, Long.MAX_VALUE);
					fileOutputStream.close();
					urls.add(tempFile.toURI().toURL());
				}

				@SuppressWarnings({ "resource" })
				ClassLoader classLoader = new URLClassLoader(
						urls.toArray(new URL[urls.size()]));

				Class<?> cls = classLoader
						.loadClass("org.mitre.stix.STIXSchema");

				@SuppressWarnings("rawtypes")
				Constructor[] constructors = cls.getDeclaredConstructors();
				constructors[0].setAccessible(true);
				Object instance = constructors[0].newInstance();

				Method setValidationErrorHandlerMethod = instance.getClass()
						.getMethod("setValidationErrorHandler",
								ErrorHandler.class);
				setValidationErrorHandlerMethod.invoke(instance,
						new ValidationErrorHandler(this));

				System.out.println("Created STIXSchema for v " + version
						+ " instance");

				stixSchemas.put(semVer, instance);

			} catch (ClassNotFoundException | SecurityException
					| IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | IOException
					| InstantiationException | NoSuchMethodException e) {

				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * Retrieves the versioning from the document's root node.
	 * 
	 * @param xmlText
	 *            The XML text String to retrieve the version from
	 * @return The versioning as a text String.
	 */
	private String retrieveVersion(String xmlText) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);

		try {
			DocumentBuilder b = factory.newDocumentBuilder();
			Document document = b.parse(new ByteArrayInputStream(xmlText
					.getBytes()));

			Element root = document.getDocumentElement();

			return root.getAttribute("version");
		} catch (ParserConfigurationException | SAXException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Creates a Valdiator Service using the default schemas.
	 */
	public ValidatorService() {
		loadSTIXSchemas(DEFAULT_SUPORTED_SCHEMAS);
	}

	/**
	 * Validates XML existing at the URL.
	 * 
	 * @param spec
	 *            THe URL for the XML text to be validated.
	 * @return The results of the validation.
	 */
	public ValidationResult validateURL(String spec) {

		try {
			URL url = new URL(spec);

			return this.validateXML(IOUtils.toString(url.openStream()));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	/**
	 * Validates an XML text String
	 * 
	 * @param xmlText
	 *            the XML text String to be valdiated.
	 * @return The results of the validation.
	 */
	public ValidationResult validateXML(String xmlText) {
		this.parseErrorMsg = "";

		String version;
		try {
			version = retrieveVersion(xmlText);
		} catch (RuntimeException e) {
			return new ValidationResult().setParseErrorMsg(e.getMessage())
					.setValidates(Boolean.toString(false));
		}

		if (version != "") {

			Version lookForVersion;

			try {
				lookForVersion = Version.valueOf(version);
			} catch (UnexpectedCharacterException e) { // handle versions
														// not in proper
														// SemVer form
														// (i.e., "1.2")
														// or retrieve
														// closest known
														// schema
				lookForVersion = Version.valueOf("1.2.0");
				String digits[] = version.trim().split("\\.");
				if (digits.length == 1) {
					lookForVersion = Version.forIntegers(Integer
							.parseInt(digits[0]));
				} else if (digits.length == 2) {
					lookForVersion = Version.forIntegers(
							Integer.parseInt(digits[0]),
							Integer.parseInt(digits[1]));
				} else {
					lookForVersion = Version.forIntegers(
							Integer.parseInt(digits[0]),
							Integer.parseInt(digits[1]),
							Integer.parseInt(digits[2]));
				}
			}

			System.out.println("Version of root element is " + version);

			validates = false;

			for (Version knownVersion : stixSchemas.keySet()) {
				if (lookForVersion.equals(knownVersion)) {
					Object obj = stixSchemas.get(knownVersion);

					Method validateMethod;
					try {
						validateMethod = obj.getClass().getMethod("validate",
								String.class);
						validates = (boolean) validateMethod.invoke(obj,
								xmlText);
						break;

					} catch (NoSuchMethodException | SecurityException
							| IllegalAccessException | IllegalArgumentException
							| InvocationTargetException e) {
						new RuntimeException(e);
					}
					System.out.println("msg = " + parseErrorMsg);

				} else {
					System.out.println(version + " not equal to "
							+ knownVersion);
				}
			}
		}

		return new ValidationResult().setParseErrorMsg(parseErrorMsg)
				.setValidates(Boolean.toString(validates));
	}

	/**
	 * Creates a Validator Service
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		new ValidatorService();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mitre.stix.validator.ValidationErrorCallback#methodToCallBack(java
	 * .lang.String, org.xml.sax.SAXParseException)
	 */
	@Override
	public void methodToCallBack(String type, SAXParseException e) {
		System.out.println("called callback");
		this.parseErrorMsg = "SAXParseException " + type + "\n"
				+ "\tPublic ID: " + e.getPublicId() + "\n" + "\tSystem ID: "
				+ e.getSystemId() + "\n" + "\tLine: " + e.getLineNumber()
				+ "\n" + "\tColumn   : " + e.getColumnNumber() + "\n"
				+ "\tMessage  : " + e.getMessage() + "\n";
	}
}