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
import java.util.Map;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.zafarkhaja.semver.UnexpectedCharacterException;
import com.github.zafarkhaja.semver.Version;

/**
 * A Validator Service
 * 
 * @author nemonik (Michael Joseph Walsh <github.com@nemonik.com>)
 *
 */
public class ValidatorService {

	private HashMap<Version, Object> stixSchemas;

	public static final Version[] DEFAULT_SUPORTED_SCHEMAS = {
			Version.valueOf("1.1.1"), Version.valueOf("1.2.0") };

	/**
	 * Uses individual ClassLoaders to load and create individual STIXSchema
	 * objects used for validation.
	 * 
	 * @param versions
	 *            The schema versions to create STIXSchema objects for.
	 */
	private void loadSTIXSchemas(Version... versions) {

		stixSchemas = new HashMap<Version, Object>();

		for (Version version : versions) {
			try {

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
						new Object[] { null });

				System.out.println("Created STIXSchema for v " + version
						+ " instance");

				stixSchemas.put(version, instance);

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

		System.out.println(spec);

		try {
			URL url = new URL(spec);

			return this.validateXML(IOUtils.toString(url.openStream()));
		} catch (Exception e) {
			return new ValidationResult().setParseErrorMsg(e.getMessage())
					.setValidates(Boolean.toString(false));
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

		boolean validates = false;

		String version;
		try {
			version = retrieveVersion(xmlText);
		} catch (RuntimeException e) {
			return new ValidationResult().setParseErrorMsg(e.getMessage())
					.setValidates(Boolean.toString(false));
		}

		if (version != "") {

			Version lookForVersion = null;

			try {
				lookForVersion = Version.valueOf(version);
			} catch (UnexpectedCharacterException uce) {
				// patch versioning not being Semantic Versioning 2.0.0
				// compliant
				if (uce.toString()
						.equals("Unexpected character 'EOI(null)' at position '3', expecting '[DOT]'")) {
					lookForVersion = Version.valueOf(version + ".0");
				}
			} catch (Exception e) {
				return new ValidationResult().setParseErrorMsg(e.getMessage())
						.setValidates(Boolean.toString(false));
			}

			//System.out.println("Version of root element is " + version);

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

					} catch (InvocationTargetException e) {

						String errorMsg = null;
						if (e.getTargetException().getClass().getName()
								.equals("org.xml.sax.SAXParseException")) {

							SAXParseException saxParseException = (SAXParseException) e
									.getTargetException();

							errorMsg = "SAXParseException:\n" + "\tPublic ID: "
									+ saxParseException.getPublicId()
									+ System.lineSeparator() + "\tSystem ID: "
									+ saxParseException.getSystemId()
									+ System.lineSeparator() + "\tLine     : "
									+ saxParseException.getLineNumber()
									+ System.lineSeparator() + "\tColumn   : "
									+ saxParseException.getColumnNumber()
									+ System.lineSeparator() + "\tMessage  : "
									+ saxParseException.getMessage();
						} else {
							errorMsg = e.getTargetException().getClass()
									.getName()
									+ ":\n"
									+ "\t"
									+ e.getTargetException().getMessage();
						}

						return new ValidationResult()
								.setParseErrorMsg(errorMsg).setValidates(
										Boolean.toString(false));

					} catch (IllegalAccessException | IllegalArgumentException
							| NoSuchMethodException | SecurityException e) {
						System.out.println(e + " : " + e.getMessage());
						return new ValidationResult().setParseErrorMsg(
								e.getMessage()).setValidates(
								Boolean.toString(false));
					}
				}
			}
		}

		return new ValidationResult().setParseErrorMsg("").setValidates(
				Boolean.toString(validates));
	}

	/**
	 * Validates an XML file.
	 * 
	 * @param xmlText
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public ValidationResult validateFile(String xmlFileData) {

		Map<String, Object> data;

		ObjectMapper mapper = new ObjectMapper();
		try {
			data = mapper.readValue(xmlFileData, Map.class);

			ValidationResult result = this
					.validateXML((String) data.get("xml"));

			result.setFilename((String) data.get("name"));

			return result;

		} catch (Exception e) {
			System.out.println(e);
			return new ValidationResult().setParseErrorMsg(e.getMessage())
					.setValidates(Boolean.toString(false));
		}
	}

	/**
	 * Creates a STIX Validator Service
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		new ValidatorService();
		System.out.println("STIX Validator Service.");
	}
}