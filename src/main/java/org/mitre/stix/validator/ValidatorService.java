package org.mitre.stix.validator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
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

public class ValidatorService implements ValidationErrorCallback {

	private HashMap<Version, Object> stixSchemas;

	private HashMap<Version, ClassLoader> classLoaders;

	private String msg;

	private void loadSTIXSchemas(String... versions) {

		stixSchemas = new HashMap<Version, Object>();
		classLoaders = new HashMap<Version, ClassLoader>();

		System.out.println("Classloader for ValidatorService is "
				+ this.getClass().getClassLoader());

		for (String version : versions) {
			try {
				Version semVer = Version.valueOf(version);
				
				ResourcePatternResolver patternResolver = new PathMatchingResourcePatternResolver();

				Resource[] resources = patternResolver
						.getResources("classpath*:refection-libs/v" + version
								+ "/**/*.jar");

				ArrayList<URL> urls = new ArrayList<URL>();

				for (Resource resource : resources) {
					urls.add(resource.getURL());
				}

				ClassLoader classLoader = new URLClassLoader(
						urls.toArray(new URL[urls.size()]));
				
				classLoaders.put(semVer, classLoader);

				System.out.println("Classloader created on fly is "
						+ classLoader);
				
				Class<?> cls = classLoader
						.loadClass("org.mitre.stix.STIXSchema");

				@SuppressWarnings("rawtypes")
				Constructor[] constructors = cls.getDeclaredConstructors();
				constructors[0].setAccessible(true);
				Object instance = constructors[0].newInstance();
				
				Method setValidationErrorHandlerMethod = instance.getClass().getMethod("setValidationErrorHandler", ErrorHandler.class);
				setValidationErrorHandlerMethod.invoke(instance, new ValidationErrorHandler(this));

				System.out.println("Created STIXSchema instance: " + instance);
				
				stixSchemas.put(semVer, instance);

			} catch (ClassNotFoundException | SecurityException
					| IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | IOException
					| InstantiationException | NoSuchMethodException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
	}

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

	public ValidatorService() {

		loadSTIXSchemas("1.1.1", "1.2.0");
	}

	public Result validateURL(String spec) {

		try {
			this.msg = "";

			URL url = new URL(spec);
			
			String xmlText = IOUtils.toString(url.openStream());
			//
			// xmlText = xmlText.replaceAll( "(?s)<!--.*?-->", "" ); // remove
			// comments
			// xmlText = xmlText.replaceAll("(?m)^[ \t]*\r?\n", ""); // remove
			// all blank lines
			//
			String version = retrieveVersion(xmlText);
			if (version != "") {
				
				Version lookForVersion ;
				
				try {
				lookForVersion = Version.valueOf(version);
				} catch(UnexpectedCharacterException e) { //handle versions not in proper SemVer form (i.e., "1.2")
					lookForVersion = Version.valueOf("1.2.0");
					String digits[] = version.trim().split("\\.");
					if (digits.length == 1) {
						lookForVersion = Version.forIntegers(Integer.parseInt(digits[0]));
					} else if (digits.length == 2) {
						lookForVersion = Version.forIntegers(Integer.parseInt(digits[0]), Integer.parseInt(digits[1]));
					} else {
						lookForVersion = Version.forIntegers(Integer.parseInt(digits[0]), Integer.parseInt(digits[1]), Integer.parseInt(digits[2]));
					}
				}
				System.out.println("Version of root element is " + version);
	
				for (Version knownVersion: stixSchemas.keySet()) {
					if (lookForVersion.equals(knownVersion)) {
						Object obj = stixSchemas.get(knownVersion);
						
						Method validateMethod = obj.getClass().getMethod("validate", String.class);
						System.out.println("valid = " + validateMethod.invoke(obj, xmlText));
						System.out.println("msg = " + msg);
						
					} else {
						System.out.println(version + " not equal to " + knownVersion);
					}
				}
			}

			Result result = new Result();
			result.setMsg("true");
			return result;
		} catch (NoSuchMethodException | SecurityException
				| IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | IOException e) {
			throw new RuntimeException(e);
		}

	}

	public Result validateXML(String body) {
		Result result = new Result();
		result.setMsg("true");
		return result;
	}

	public static void main(String[] args) {
		new ValidatorService();
	}

	@Override
	public void methodToCallBack(String type, SAXParseException e) {
		System.out.println("called callback");
		this.msg = "SAXParseException " + type + "\n" +
				"\tPublic ID: " + e.getPublicId() + "\n" +
				"\tSystem ID: " + e.getSystemId() + "\n" +
				"\tLine: " + e.getLineNumber() + "\n" +
				"\tColumn   : " + e.getColumnNumber() + "\n" +
				"\tMessage  : " + e.getMessage() + "\n";
	}
}
