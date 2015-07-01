/**
 * Copyright (c) 2015, The MITRE Corporation. All rights reserved.
 * See LICENSE for complete terms.
 *
 * Spock unit test for ValidatorService
 * 
 * @author nemonik (Michael Joseph Walsh <github.com@nemonik.com>)
 */

import spock.lang.*
import org.mitre.stix.validator.App;

import groovyx.net.http.HTTPBuilder
import net.sf.json.JSONObject

import javax.servlet.http.HttpServletResponse

import static groovyx.net.http.ContentType.JSON
import groovy.json.JsonOutput

class ValidatorServiceSpec extends spock.lang.Specification{

	@Shared static HTTPBuilder http
	@Shared String validXML
	@Shared String invalidXML
	
	static App app

	def setupSpec() {
		def app = new App()
		http = new HTTPBuilder('http://localhost:8080')
		
		validXML = """<?xml version="1.0" encoding="UTF-8"?>
<stix:STIX_Package 
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
	xmlns:stix="http://stix.mitre.org/stix-1" 
	xmlns:stixCommon="http://stix.mitre.org/common-1"
	xmlns:indicator="http://stix.mitre.org/Indicator-2"
	xmlns:ttp="http://stix.mitre.org/TTP-1"
	xmlns:stixVocabs="http://stix.mitre.org/default_vocabularies-1" 
	xmlns:cybox="http://cybox.mitre.org/cybox-2" 
	xmlns:DomainNameObj="http://cybox.mitre.org/objects#DomainNameObject-1" 
	xmlns:sdv="http://stix.mitre.org/stix-validator"
	id="sdv:Indicator-ba1d406e-937c-414f-9231-6e1dbe64fe8b" version="1.2">
	<stix:Indicators>
		<stix:Indicator xsi:type="indicator:IndicatorType" id="sdv:Indicator-2e20c5b2-56fa-46cd-9662-8f199c69d2c9" timestamp="2014-02-20T09:00:00.000000Z" version="2.2">
			<indicator:Title>Sample Domain Watchlist Indicator</indicator:Title>
			<indicator:Type xsi:type="stixVocabs:IndicatorTypeVocab-1.1">Domain Watchlist</indicator:Type>
			<indicator:Description>Sample domain Indicator for this watchlist</indicator:Description>
			<indicator:Valid_Time_Position>
				<indicator:Start_Time>2014-02-20T09:00:00.000000Z</indicator:Start_Time>
			</indicator:Valid_Time_Position>
			<indicator:Observable id="sdv:Observable-87c9a5bb-d005-4b3e-8081-99f720fad62b">
				<cybox:Object id="sdv:Object-12c760ba-cd2c-4f5d-a37d-18212eac7928">
					<cybox:Properties xsi:type="DomainNameObj:DomainNameObjectType" type="FQDN">
						<DomainNameObj:Value condition="Equals">www.example.com</DomainNameObj:Value>
					</cybox:Properties>
				</cybox:Object>
			</indicator:Observable>
			<indicator:Indicated_TTP>
				<stixCommon:TTP idref="sdv:TTP-2e20c5b2-56fa-46cd-9662-8f199c69d2c9" timestamp="2014-02-20T09:00:00.000000Z"/>
			</indicator:Indicated_TTP>
			<indicator:Confidence>
				<stixCommon:Value>High</stixCommon:Value>
			</indicator:Confidence>
		</stix:Indicator>
	</stix:Indicators>
	<stix:TTPs>
		<stix:TTP xsi:type="ttp:TTPType" id="sdv:TTP-2e20c5b2-56fa-46cd-9662-8f199c69d2c9" timestamp="2014-02-20T09:00:00.000000Z">
			<ttp:Title>Malware Infrastructure</ttp:Title>
		</stix:TTP>
	</stix:TTPs>
</stix:STIX_Package>"""
		
		invalidXML = """<?xml version="1.0" encoding="UTF-8"?>
<stix:STIX_Package 
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
	xmlns:stix="http://stix.mitre.org/stix-1" 
	xmlns:stixCommon="http://stix.mitre.org/common-1"
	xmlns:indicator="http://stix.mitre.org/Indicator-2"
	xmlns:ttp="http://stix.mitre.org/TTP-1"
	xmlns:stixVocabs="http://stix.mitre.org/default_vocabularies-1" 
	xmlns:cybox="http://cybox.mitre.org/cybox-2" 
	xmlns:DomainNameObj="http://cybox.mitre.org/objects#DomainNameObject-1" 
	xmlns:example="http://example.com/"
	id="example:Indicator-ba1d406e-937c-414f-9231-6e1dbe64fe8b" version="1.2" timestamp="2014-05-08T09:00:00.000000Z">
	<stix:STIX_Header>
		<stix:Title>STIX Validator Example</stix:Title>
		<stix:Package_Intent xsi:type="stixVocabs:PackageIntentVocab-1.0">INVALID</stix:Package_Intent>
		<stix:Description>This example document is STIX schema invalid.</stix:Description>
	</stix:STIX_Header>
	<stix:Indicators>
		<stix:Indicator xsi:type="indicator:IndicatorType" id="example:Indicator-2e20c5b2-56fa-46cd-9662-8f199c69d2c9" timestamp="2014-02-20T09:00:00.000000Z" version="2.1.1">
			<indicator:Title>Sample Domain Watchlist Indicator</indicator:Title>
			<indicator:Type xsi:type="stixVocabs:IndicatorTypeVocab-1.1">Domain Watchlist</indicator:Type>
			<indicator:Description>Sample domain Indicator for this watchlist</indicator:Description>
			<indicator:Valid_Time_Position>
				<indicator:Start_Time>2014-02-20T09:00:00.000000Z</indicator:Start_Time>
			</indicator:Valid_Time_Position>
			<indicator:Observable id="example:Observable-87c9a5bb-d005-4b3e-8081-99f720fad62b">
				<cybox:Object id="example:Object-12c760ba-cd2c-4f5d-a37d-18212eac7928">
					<cybox:Properties xsi:type="DomainNameObj:DomainNameObjectType" type="FQDN">
						<DomainNameObj:Value condition="Equals" apply_condition="ANY">malicious1.example.com##comma##malicious2.example.com##comma##malicious3.example.com</DomainNameObj:Value>
					</cybox:Properties>
				</cybox:Object>
			</indicator:Observable>
			<indicator:Indicated_TTP>
				<stixCommon:TTP idref="example:TTP-2e20c5b2-56fa-46cd-9662-8f199c69d2c9" timestamp="2014-02-20T09:00:00.000000Z"/>
			</indicator:Indicated_TTP>
			<indicator:Confidence>
				<stixCommon:Value>High</stixCommon:Value>
			</indicator:Confidence>
		</stix:Indicator>
	</stix:Indicators>
	<stix:TTPs>
		<stix:TTP xsi:type="ttp:TTPType" id="example:TTP-2e20c5b2-56fa-46cd-9662-8f199c69d2c9" timestamp="2014-02-20T09:00:00.000000Z">
			<ttp:Title>Malware Infrastructure</ttp:Title>
		</stix:TTP>
	</stix:TTPs>
</stix:STIX_Package>"""
	}
	
	def "Sending URL for valid XML document returns an expected result"() {
		when:
			def (json, responseStatus) = http.post(path: '/api/v1/validate/url', contentType: JSON, body: 'https://raw.githubusercontent.com/STIXProject/stix-validator/3ceeac537a38fe889ee505b5f82712ab731b0a18/examples/stix/all_valid.xml') { resp, reader -> [reader, resp.status] }
		then:
			responseStatus == HttpServletResponse.SC_OK
			json.get('validates') == 'true'
	}
	
	def "Sending URL for an invalid XML document returns an expected result"() {
		when:
			def (json, responseStatus) = http.post(path: '/api/v1/validate/url', contentType: JSON, body: 'https://raw.githubusercontent.com/STIXProject/stix-validator/6c944ca35f1ae27cc2a3901b95030b00f375802e/examples/stix/schema_invalid.xml') { resp, reader -> [reader, resp.status] }
		then:
			responseStatus == HttpServletResponse.SC_OK
			json.get('validates') == 'false'
	}
	
	def "Sending valid XML document text returns an expected result"() {
		when:
			def (json, responseStatus) = http.post(path: '/api/v1/validate/xml', contentType: JSON, body: validXML) { resp, reader -> [reader, resp.status] }
		then:
			responseStatus == HttpServletResponse.SC_OK
			json.get('validates') == 'true'
	}
	
	def "Sending invalid XML document text returns an expected result"() {
		when:
			def (json, responseStatus) = http.post(path: '/api/v1/validate/xml', contentType: JSON, body: invalidXML) { resp, reader -> [reader, resp.status] }
		then:
			responseStatus == HttpServletResponse.SC_OK
			json.get('validates') == 'false'
	}

	def "Sending valid XML document file returns an expected result"() {
		when:
			def data = JsonOutput.toJson([name: 'all_valid.xml', type: 'text/xml', size: 2150, xml: validXML])
			
			def (json, responseStatus) = http.post(path: '/api/v1/validate/file', contentType: JSON, body: data) { resp, reader -> [reader, resp.status] }
		then:
			responseStatus == HttpServletResponse.SC_OK
			json.get('validates') == 'true'
	}
	
	def "Sending invalid XML document file returns an expected result"() {
		when:
			def data = JsonOutput.toJson([name: 'schema_invalid.xml', type: 'text/xml', size: 2568, xml: invalidXML])
			
			def (json, responseStatus) = http.post(path: '/api/v1/validate/file', contentType: JSON, body: data) { resp, reader -> [reader, resp.status] }
		then:
			responseStatus == HttpServletResponse.SC_OK
			json.get('validates') == 'false'
	}
}