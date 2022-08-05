package com.jslib.template.xhtml;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import com.jslib.api.dom.Document;
import com.jslib.api.dom.EList;
import com.jslib.api.dom.Element;
import com.jslib.api.template.TemplateException;

public class ObjectOperatorUnitTest extends TestCaseEx {
	public void testSupportedPrimitiveTypes() throws SAXException {
		String html = "" + //
				"<ul>" + //
				"   <li data-text='byte1'></li>" + //
				"   <li data-text='byte2'></li>" + //
				"   <li data-text='short1'></li>" + //
				"   <li data-text='short2'></li>" + //
				"   <li data-text='int1'></li>" + //
				"   <li data-text='int2'></li>" + //
				"   <li data-text='long1'></li>" + //
				"   <li data-text='long2'></li>" + //
				"   <li data-text='float1'></li>" + //
				"   <li data-text='float2'></li>" + //
				"   <li data-text='double1'></li>" + //
				"   <li data-text='double2'></li>" + //
				"   <li data-text='boolean1'></li>" + //
				"   <li data-text='boolean2'></li>" + //
				"   <li data-text='char1'></li>" + //
				"   <li data-text='char2'></li>" + //
				"   <li data-text='string'></li>" + //
				"</ul>";
		PrimitiveTypes model = new PrimitiveTypes();
		Document doc = run(html, model);

		EList elist = doc.findByTag("li");
		assertEquals("22", elist, 0);
		assertEquals("44", elist, 1);
		assertEquals("2222", elist, 2);
		assertEquals("4444", elist, 3);
		assertEquals("222222", elist, 4);
		assertEquals("444444", elist, 5);
		assertEquals("22222222", elist, 6);
		assertEquals("44444444", elist, 7);
		assertEquals("22.22", elist, 8);
		assertEquals("44.44", elist, 9);
		assertEquals("22222222.222", elist, 10);
		assertEquals("44444444.444", elist, 11);
		assertEquals("true", elist, 12);
		assertEquals("false", elist, 13);
		assertEquals("a", elist, 14);
		assertEquals("z", elist, 15);
		assertEquals("string", elist, 16);
	}

	public void testSupportedValueTypes() throws ParseException, MalformedURLException, UnsupportedEncodingException, SAXException {
		String html = "" + //
				"<ul>" + //
				"   <li data-text='utilDate'></li>" + //
				"   <li data-text='sqlDate'></li>" + //
				"   <li data-text='time'></li>" + //
				"   <li data-text='timestamp'></li>" + //
				"   <li data-text='file'></li>" + //
				"   <li data-text='picture'></li>" + //
				"   <li data-text='timezone'></li>" + //
				"   <li data-text='url'></li>" + //
				"</ul>";
		ValueTypes model = new ValueTypes();
		Document doc = run(html, model);

		EList elist = doc.findByTag("li");
		assertEquals("Sun Mar 15 1964 13:40:00 UTC", elist, 0);
		assertEquals("Sun Mar 15 1964 13:40:00 UTC", elist, 1);
		assertEquals("Sun Mar 15 1964 13:40:00 UTC", elist, 2);
		assertEquals("Sun Mar 15 1964 13:40:00 UTC", elist, 3);
		assertEquals("/var/log/tomcat/catalina.out", elist.item(4).getText());
		assertEquals("images/save-icon.png", elist, 5);
		assertEquals("UTC", elist, 6);
		assertEquals("http://gnotis.ro/", elist, 7);
	}

	public void testFlatObject() throws SAXException {
		String html = "" + //
				"<div>" + //
				"   <h1 data-text='title'></h1>" + //
				"   <img data-src='picture' data-title='title' />" + //
				"</div>";
		Pojo model = new Pojo("title", "picture.png");
		Document doc = run(html, model);

		assertEquals("title", doc.getByTag("h1").getText().trim());
		assertEquals("picture.png", doc.getByTag("img").getAttr("src"));
		assertEquals("title", doc.getByTag("img").getAttr("title"));
	}

	public void testNestedObject() throws SAXException {
		String html = "" + //
				"<h1 data-text='title'></h1>" + //
				"<div data-object='nested'>" + //
				"    <h2 data-text='title'></h2>" + //
				"    <div data-object='object'>" + //
				"        <h3 data-text='title'></h3>" + //
				"        <img data-src='picture' data-title='title' />" + //
				"    </div>" + //
				"</div>";

		NestedObject model = new NestedObject();
		model.title = "title1";
		model.nested.title = "title2";
		model.nested.object.title = "title3";
		model.nested.object.picture = "picture.png";
		Document doc = run(html, model);

		assertEquals("title1", doc.getByTag("h1").getText().trim());
		assertEquals("title2", doc.getByTag("h2").getText().trim());
		assertEquals("title3", doc.getByTag("h3").getText().trim());
		assertEquals("picture.png", doc.getByTag("img").getAttr("src"));
	}

	public void testInvalidOperatorsList() throws SAXException {
		String html = "<div data-object='nested' data-text='title'></div>";
		try {
			run(html, new Pojo());
			fail("Invalid operators list should rise logic flaw exception.");
		} catch (TemplateException e) {
			assertTrue(e.getMessage().contains("one CONTENT operator"));
		}
	}

	public void testBadScope() throws SAXException {
		String html = "<div data-object='value'></div>";
		for (Object model : new Object[] { "string", 1234, true }) {
			try {
				run(html, model);
			} catch (TemplateException e) {
				continue;
			}
			fail("Invalid scope should throw templates exveption.");
		}
	}

	public void testAbsolutePath() throws SAXException {
		String html = "" + //
				"<h1 data-text='title'></h1>" + //
				"<div data-object='.nested.object'>" + //
				"   <h3 data-text='title'></h3>" + //
				"   <img data-src='picture' data-title='title' />" + //
				"</div>" + //
				"<div data-object='nested'>" + //
				"    <h2 data-text='title'></h2>" + //
				"</div>";

		NestedObject model = new NestedObject();
		model.title = "title1";
		model.nested.title = "title2";
		model.nested.object.title = "title3";
		model.nested.object.picture = "picture.png";
		Document doc = run(html, model);

		assertEquals("title1", doc.getByTag("h1").getText().trim());
		assertEquals("title2", doc.getByTag("h2").getText().trim());
		assertNotNull(doc.getByTag("h3"));
		assertEquals("title3", doc.getByTag("h3").getText().trim());
		assertEquals("picture.png", doc.getByTag("img").getAttr("src"));
	}

	public void testComplexGraph() throws SAXException, XPathExpressionException {
		String html = "" + //
				"<h1 data-text='title'></h1>" + //
				"<div id='nested' data-object='nested'>" + //
				"    <h2 data-text='title'></h2>" + //
				"    <ul data-list='list'>" + //
				"        <li>" + //
				"            <h3 data-text='title'></h3>" + //
				"            <a data-href='link'><img data-src='picture' data-title='title' /></a>" + //
				"        </li>" + //
				"    </ul>" + //
				"    <dl data-map='map'>" + //
				"        <dt></dt>" + //
				"        <dd>" + //
				"            <h3 data-text='title'></h3>" + //
				"            <a data-href='link'><img data-src='picture' data-title='title' /></a>" + //
				"        </dd>" + //
				"    </dl>" + //
				"</div>" + //
				"<ul data-list='list'>" + //
				"    <li>" + //
				"        <h2 data-text='title'></h2>" + //
				"        <ul data-list='list'>" + //
				"            <li>" + //
				"                <h3 data-text='title'></h3>" + //
				"                <a data-href='link'><img data-src='picture' data-title='title' /></a>" + //
				"            </li>" + //
				"        </ul>" + //
				"        <dl data-map='map'>" + //
				"            <dt></dt>" + //
				"            <dd>" + //
				"                <h3 data-text='title'></h3>" + //
				"                <a data-href='link'><img data-src='picture' data-title='title' /></a>" + //
				"            </dd>" + //
				"        </dl>" + //
				"    </li>" + //
				"</ul>" + //
				"<dl data-map='map'>" + //
				"    <dt></dt>" + //
				"    <dd>" + //
				"        <h2 data-text='title'></h2>" + //
				"        <ul data-list='list'>" + //
				"            <li>" + //
				"                <h3 data-text='title'></h3>" + //
				"                <a data-href='link'><img data-src='picture' data-title='title' /></a>" + //
				"            </li>" + //
				"        </ul>" + //
				"        <dl data-map='map'>" + //
				"            <dt></dt>" + //
				"            <dd>" + //
				"                <h3 data-text='title'></h3>" + //
				"                <a data-href='link'><img data-src='picture' data-title='title' /></a>" + //
				"            </dd>" + //
				"        </dl>" + //
				"    </dd>" + //
				"</dl>";

		ComplexGraph model = new ComplexGraph();
		model.title = "title";
		model.nested.title = "nested-title";
		model.nested.list.add(new Pojo("nested-list-title", "nested-list-picture.png"));
		model.nested.map.put("nested-key", new Pojo("nested-map-title", "nested-map-picture.png"));

		ComplexGraph.Nested nested = new ComplexGraph.Nested();
		nested.title = "subtitle";
		nested.list.add(new Pojo("sublist-title", "sublist-picture.png"));
		nested.map.put("subkey", new Pojo("submap-title", "submap-picture.png"));
		model.list.add(nested);
		model.map.put("key", nested);

		Document doc = run(html, new ComplexGraphContent(model));

		assertEquals("title", doc.getByTag("h1").getText());

		Element nestedObject = doc.getByXPath("//DIV[@id='nested']");
		assertNotNull(nestedObject);
		assertEquals("nested-title", nestedObject.getByTag("h2").getText());

		Element li = nestedObject.getByTag("li");
		assertNotNull(li);
		assertEquals("nested-list-title", li.getByTag("h3").getText());
		assertEquals("details.xsp?title=nested-list-title", li.getByTag("a").getAttr("href"));
		assertEquals("nested-list-picture.png", li.getByTag("img").getAttr("src"));
		assertEquals("nested-list-title", li.getByTag("img").getAttr("title"));

		Element dt = nestedObject.getByTag("dt");
		assertNotNull(dt);
		assertEquals("nested-key", dt.getText().trim());

		Element dd = nestedObject.getByTag("dd");
		assertNotNull(dd);
		assertEquals("nested-map-title", dd.getByTag("h3").getText());
		assertEquals("details.xsp?title=nested-map-title", dd.getByTag("a").getAttr("href"));
		assertEquals("nested-map-picture.png", dd.getByTag("img").getAttr("src"));
		assertEquals("nested-map-title", dd.getByTag("img").getAttr("title"));

		Element nestedList = doc.getByXPath("//BODY/UL");
		assertNotNull(nestedList);
		Element nestedMap = doc.getByXPath("//BODY/DL");
		assertNotNull(nestedMap);
		assertEquals("key", nestedMap.getByTag("dt").getText().trim());

		for (Element el : new Element[] { nestedList, nestedMap }) {
			assertEquals("subtitle", el.getByTag("h2").getText());

			li = el.getByTag("li");
			assertNotNull(li);
			assertEquals("sublist-title", li.getByTag("h3").getText());
			assertEquals("details.xsp?title=sublist-title", li.getByTag("a").getAttr("href"));
			assertEquals("sublist-picture.png", li.getByTag("img").getAttr("src"));
			assertEquals("sublist-title", li.getByTag("img").getAttr("title"));

			dt = el.getByTag("dl").getByTag("dt");
			assertNotNull(dt);
			assertEquals("subkey", dt.getText().trim());

			dd = el.getByTag("dl").getByTag("dd");
			assertNotNull(dd);
			assertEquals("submap-title", dd.getByTag("h3").getText());
			assertEquals("details.xsp?title=submap-title", dd.getByTag("a").getAttr("href"));
			assertEquals("submap-picture.png", dd.getByTag("img").getAttr("src"));
			assertEquals("submap-title", dd.getByTag("img").getAttr("title"));
		}
	}

	public void testNullContentValue() throws SAXException {
		String html = "" + //
				"<div data-object='object'>" + //
				"   <h1>Heading One</h1>" + //
				"   <div data-object='subobject'>" + //
				"       <h2>Heading Two</h2>" + //
				"       <ul data-list='sublist'>" + //
				"           <li data-text='.'></li>" + //
				"       </ul>" + //
				"   </div>" + //
				"</div>";
		NestedObject.Nested model = new NestedObject.Nested();
		model.object = null;
		Document doc = run(html, model);
		doc.dump();

		Element h1 = doc.getByTag("h1");
		assertNotNull(h1);
		assertEquals("Heading One", h1.getText());
		Element h2 = doc.getByTag("h2");
		assertNotNull(h2);
		assertEquals("Heading Two", h2.getText());
		assertNotNull(doc.getByTag("ul"));
		EList li = doc.findByTag("li");
		assertEquals(0, li.size());
	}

	/**
	 * Test OBJECT operator with value type scope. If property path operand points to a value type field it cannot be used as
	 * scope and switch to it. Such condition is considered content exception: log event to warnings and returns null. Templates
	 * engine process branch as for null object condition, see {@link #testNullContentValue()}.
	 * @throws SAXException 
	 */
	public void testBadType() throws SAXException {
		@SuppressWarnings("unused")
		Object model = new Object() {
			String string = "string";
			int number = 1234;
			boolean bool = true;
		};

		for (Field field : model.getClass().getDeclaredFields()) {
			if (field.getName().startsWith("this")) {
				continue;
			}
			String html = String.format("<div data-object='%s'><h1>Heading Value</h1></div>", field.getName());
			try {
				run(html, model);
			} catch (TemplateException e) {
				continue;
			}
			fail("Bad object type should throw templates exception.");
		}
	}

	// ------------------------------------------------------
	// fixture initialization and helpers

	@SuppressWarnings("unused")
	private static class PrimitiveTypes {
		byte byte1 = 22;
		Byte byte2 = 44;
		short short1 = 2222;
		Short short2 = 4444;
		int int1 = 222222;
		Integer int2 = 444444;
		long long1 = 22222222L;
		Long long2 = 44444444L;
		float float1 = 22.22F;
		Float float2 = 44.44F;
		double double1 = 22222222.22222222;
		Double double2 = 44444444.44444444;
		boolean boolean1 = true;
		Boolean boolean2 = false;
		char char1 = 'a';
		Character char2 = 'z';
		String string = "string";
	}

	@SuppressWarnings("unused")
	private static class ValueTypes {
		Date utilDate;
		java.sql.Date sqlDate;
		Time time;
		Timestamp timestamp;
		File file;
		String picture;
		TimeZone timezone;
		URL url;

		ValueTypes() throws ParseException, MalformedURLException, UnsupportedEncodingException {
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
			df.setTimeZone(TimeZone.getTimeZone("UTC"));
			this.utilDate = df.parse("1964-03-15T13:40:00.000EET");
			this.sqlDate = new java.sql.Date(this.utilDate.getTime());
			this.time = new Time(this.utilDate.getTime());
			this.timestamp = new Timestamp(this.utilDate.getTime());
			this.file = new File("/var/log/tomcat/catalina.out");
			this.picture = "images/save-icon.png";
			this.timezone = TimeZone.getTimeZone("UTC");
			this.url = new URL("http://gnotis.ro/");
		}
	}

	private static class Pojo {
		String title;
		@SuppressWarnings("unused")
		String picture;

		Pojo() {
		}

		Pojo(String title, String picture) {
			this.title = title;
			this.picture = picture;
		}
	}

	@SuppressWarnings("unused")
	private static class NestedObject {
		String title;
		Nested nested = new Nested();

		static class Nested {
			String title;
			Pojo object = new Pojo();
		}
	}

	@SuppressWarnings("unused")
	private static class ComplexGraph {
		String title;
		Nested nested = new Nested();
		List<Nested> list = new ArrayList<Nested>();
		Map<String, Nested> map = new HashMap<String, Nested>();

		static class Nested {
			String title;
			List<Pojo> list = new ArrayList<Pojo>();
			Map<String, Pojo> map = new HashMap<String, Pojo>();
		}
	}

	@SuppressWarnings("unused")
	private static class ComplexGraphContent extends Content {
		ComplexGraphContent(Object model) {
			super(model);
		}

		Object getLink(Pojo pojo) {
			return "details.xsp?title=" + pojo.title;
		}
	}
}
