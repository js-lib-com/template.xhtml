package com.jslib.template.xhtml;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

import org.xml.sax.SAXException;

import com.jslib.api.dom.Document;
import com.jslib.api.dom.EList;
import com.jslib.api.template.TemplateException;

public class MapOperatorUnitTest extends TestCaseEx {
	public void testSupportedPrimitiveTypes() throws SAXException {
		String html = "" + //
				"<dl data-map='.'>" + //
				"   <dt data-text='.'></dt>" + //
				"   <dd data-text='.'></dd>" + //
				"</dl>";
		Map<String, Object> model = new TreeMap<String, Object>();
		model.put("key0", 22);
		model.put("key1", 2222);
		model.put("key2", 222222);
		model.put("key3", 22222222L);
		model.put("key4", 22.22F);
		model.put("key5", 22222222.22222222);
		model.put("key6", true);
		model.put("key7", 'c');
		model.put("key8", "string");
		Document doc = run(html, model);

		EList elist = doc.findByTag("dt");
		assertEquals("key0", elist, 0);
		assertEquals("key1", elist, 1);
		assertEquals("key2", elist, 2);
		assertEquals("key3", elist, 3);
		assertEquals("key4", elist, 4);
		assertEquals("key5", elist, 5);
		assertEquals("key6", elist, 6);
		assertEquals("key7", elist, 7);
		assertEquals("key8", elist, 8);

		elist = doc.findByTag("dd");
		assertEquals("22", elist, 0);
		assertEquals("2222", elist, 1);
		assertEquals("222222", elist, 2);
		assertEquals("22222222", elist, 3);
		assertEquals("22.22", elist, 4);
		assertEquals("22222222.222", elist, 5);
		assertEquals("true", elist, 6);
		assertEquals("c", elist, 7);
		assertEquals("string", elist, 8);
	}

	public void testSupportedValueTypes() throws ParseException, MalformedURLException, UnsupportedEncodingException, SAXException {
		String html = "" + //
				"<dl data-map='.'>" + //
				"   <dt data-text='.'></dt>" + //
				"   <dd data-text='.'></dd>" + //
				"</dl>";

		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
		Date date = df.parse("1964-03-15T13:40:00.000EET");

		Map<String, Object> model = new TreeMap<String, Object>();
		model.put("key00", date);
		model.put("key01", new java.sql.Date(date.getTime()));
		model.put("key02", new Time(date.getTime()));
		model.put("key03", new Timestamp(date.getTime()));
		model.put("key04", new File("/var/log/tomcat/catalina.out"));
		model.put("key05", TimeZone.getTimeZone("UTC"));
		model.put("key06", new URL("http://gnotis.ro/"));

		Document doc = run(html, model);

		EList elist = doc.findByTag("dt");
		assertEquals("key00", elist, 0);
		assertEquals("key01", elist, 1);
		assertEquals("key02", elist, 2);
		assertEquals("key03", elist, 3);
		assertEquals("key04", elist, 4);
		assertEquals("key05", elist, 5);
		assertEquals("key06", elist, 6);

		elist = doc.findByTag("dd");
		assertEquals("Sun Mar 15 1964 13:40:00 UTC", elist, 0);
		assertEquals("Sun Mar 15 1964 13:40:00 UTC", elist, 1);
		assertEquals("Sun Mar 15 1964 13:40:00 UTC", elist, 2);
		assertEquals("Sun Mar 15 1964 13:40:00 UTC", elist, 3);
		assertEquals("/var/log/tomcat/catalina.out", elist.item(4).getText());
		assertEquals("UTC", elist, 5);
		assertEquals("http://gnotis.ro/", elist, 6);
	}

	public void testMapOfObjects() throws SAXException {
		String html = "" + //
				"<dl data-map='map'>" + //
				"   <dt data-text='.'></dt>" + //
				"   <dd data-object='.'>" + //
				"       <h2 data-text='title'></h2>" + //
				"   </dd>" + //
				"</dl>";
		MapOfObjects model = new MapOfObjects();
		model.map.put("key0", new Pojo("title0"));
		model.map.put("key1", new Pojo("title1"));
		Document doc = run(html, model);

		EList elist = doc.findByTag("dt");
		assertEquals("key0", elist, 0);
		assertEquals("key1", elist, 1);
		elist = doc.findByTag("h2");
		assertEquals("title0", elist, 0);
		assertEquals("title1", elist, 1);
	}

	public void testAnonymousMap() throws SAXException {
		String html = "" + //
				"<dl data-map='.'>" + //
				"   <dt data-text='.'></dt>" + //
				"   <dd data-object='.'>" + //
				"       <h2 data-text='title'></h2>" + //
				"   </dd>" + //
				"</dl>";
		Map<String, Pojo> model = new MapOfObjects().map;
		model.put("key0", new Pojo("title0"));
		model.put("key1", new Pojo("title1"));
		Document doc = run(html, model);

		EList elist = doc.findByTag("dt");
		assertEquals("key0", elist, 0);
		assertEquals("key1", elist, 1);
		elist = doc.findByTag("h2");
		assertEquals("title0", elist, 0);
		assertEquals("title1", elist, 1);
	}

	public void testMapOfPrimitives() throws IOException, SAXException {
		String html = "" + //
				"<dl data-map='map'>" + //
				"   <dt data-text='.'></dt>" + //
				"   <dd data-text='.'></dd>" + //
				"</dl>";

		MapOfPrimitives model = new MapOfPrimitives();
		model.map.put("key0", 0);
		model.map.put("key1", 1);
		Document doc = run(html, model);

		EList elist = doc.findByTag("dt");
		assertEquals("key0", elist, 0);
		assertEquals("key1", elist, 1);
		elist = doc.findByTag("dd");
		assertEquals("0", elist, 0);
		assertEquals("1", elist, 1);
	}

	public void testMapOfLists() throws SAXException {
		String html = "" + //
				"<dl data-map='map'>" + //
				"   <dt data-text='.'></dt>" + //
				"   <dd data-object='.'>" + //
				"       <ul data-list='.'>" + //
				"           <li data-object='.'>" + //
				"               <h2 data-text='title'></h2>" + //
				"           </li>" + //
				"       </ul>" + //
				"   </dd>" + //
				"</dl>";

		MapOfLists model = new MapOfLists();
		List<Pojo> list0 = new ArrayList<Pojo>();
		list0.add(new Pojo("title00"));
		list0.add(new Pojo("title01"));
		model.map.put("key0", list0);
		List<Pojo> list1 = new ArrayList<Pojo>();
		list1.add(new Pojo("title10"));
		list1.add(new Pojo("title11"));
		model.map.put("key1", list1);

		Document doc = run(html, model);

		EList elist = doc.findByTag("dt");
		assertEquals("key0", elist, 0);
		assertEquals("key1", elist, 1);
		elist = doc.findByTag("h2");
		assertEquals("title00", elist, 0);
		assertEquals("title01", elist, 1);
		assertEquals("title10", elist, 2);
		assertEquals("title11", elist, 3);
	}

	public void testNestedMaps() throws SAXException {
		String html = "" + //
				"<dl data-map='map'>" + //
				"    <dt data-text='.'></dt>" + //
				"    <dd data-object='.'>" + //
				"        <h2 data-text='title'></h2>" + //
				"        <div data-map='map'>" + //
				"            <p data-text='.'></p>" + //
				"            <div data-object='.'>" + //
				"                <h3 data-text='title'></h3>" + //
				"            </div>" + //
				"        </div>" + //
				"    </dd>" + //
				"</dl>";

		NestedMaps model = new NestedMaps();
		NestedMaps.Nested nested0 = new NestedMaps.Nested();
		nested0.title = "title0";
		nested0.map.put("key00", new Pojo("title00"));
		nested0.map.put("key01", new Pojo("title01"));
		model.map.put("key0", nested0);
		NestedMaps.Nested nested1 = new NestedMaps.Nested();
		nested1.title = "title1";
		nested1.map.put("key10", new Pojo("title10"));
		nested1.map.put("key11", new Pojo("title11"));
		model.map.put("key1", nested1);

		Document doc = run(html, model);

		EList elist = doc.findByTag("dt");
		assertEquals("key0", elist, 0);
		assertEquals("key1", elist, 1);
		elist = doc.findByTag("h2");
		assertEquals("title0", elist, 0);
		assertEquals("title1", elist, 1);
		elist = doc.findByTag("p");
		assertEquals("key00", elist, 0);
		assertEquals("key01", elist, 1);
		assertEquals("key10", elist, 2);
		assertEquals("key11", elist, 3);
		elist = doc.findByTag("h3");
		assertEquals("title00", elist, 0);
		assertEquals("title01", elist, 1);
		assertEquals("title10", elist, 2);
		assertEquals("title11", elist, 3);
	}

	public void testMapOfDefaultPrimitives() throws IOException, SAXException {
		String html = "" + //
				"<dl data-map='map'>" + //
				"   <dt></dt>" + //
				"   <dd></dd>" + //
				"</dl>";

		MapOfPrimitives model = new MapOfPrimitives();
		model.map.put("key0", 0);
		model.map.put("key1", 1);

		Document doc = run(html, model);

		EList elist = doc.findByTag("dt");
		assertEquals("key0", elist, 0);
		assertEquals("key1", elist, 1);
		elist = doc.findByTag("dd");
		assertEquals("0", elist, 0);
		assertEquals("1", elist, 1);
	}

	public void testMapOfDefaultObjects() throws SAXException {
		String html = "" + //
				"<dl data-map='map'>" + //
				"   <dt></dt>" + //
				"   <dd>" + //
				"       <h2 data-text='title'></h2>" + //
				"   </dd>" + //
				"</dl>";
		MapOfObjects model = new MapOfObjects();
		model.map.put("key0", new Pojo("title0"));
		model.map.put("key1", new Pojo("title1"));
		Document doc = run(html, model);

		EList elist = doc.findByTag("dt");
		assertEquals("key0", elist, 0);
		assertEquals("key1", elist, 1);
		elist = doc.findByTag("h2");
		assertEquals("title0", elist, 0);
		assertEquals("title1", elist, 1);
	}

	public void testMapWithoutChildren() throws SAXException {
		MapOfObjects model = new MapOfObjects();
		for (String propertyPath : new String[] { "map", "omap" }) {
			String html = String.format("<dl data-%s='map'></dl>", propertyPath);
			try {
				run(html, model);
			} catch (TemplateException e) {
				continue;
			}
			fail("Map without children should throw templates exception.");
		}
	}

	public void testNullMapValue() throws SAXException {
		MapOfObjects model = new MapOfObjects();
		model.map = null;
		for (String propertyPath : new String[] { "map", "omap" }) {
			String html = String.format("<dl data-%s='map'><dt></dt><dd></dd></dl>", propertyPath);
			Document doc = run(html, model);
			assertEquals(0, doc.findByTag("dt").size());
			assertEquals(0, doc.findByTag("dd").size());
		}
	}

	public void testBadMapValueTypes() throws SAXException {
		Object[] invalidValues = new Object[] { 123, "atring", true };

		for (String propertyPath : new String[] { "map", "omap" }) {
			String html = String.format("<dl data-%s='map'><dt></dt><dd></dd></dl>", propertyPath);
			for (int i = 0; i < invalidValues.length; ++i) {
				try {
					run(html, invalidValues[i]);
				} catch (TemplateException e) {
					continue;
				}
				fail("Bad map value types should throw templates exception.");
			}
		}
	}

	// ------------------------------------------------------
	// fixture initialization and helpers

	@SuppressWarnings("unused")
	private static class Pojo {
		String title;

		Pojo(String title) {
			this.title = title;
		}
	}

	private static class MapOfObjects {
		Map<String, Pojo> map = new TreeMap<String, Pojo>();
	}

	private static class MapOfPrimitives {
		Map<String, Object> map = new TreeMap<String, Object>();
	}

	private static class MapOfLists {
		Map<String, List<Pojo>> map = new TreeMap<String, List<Pojo>>();
	}

	@SuppressWarnings("unused")
	private static class NestedMaps {
		Map<String, Nested> map = new TreeMap<String, Nested>();

		static class Nested {
			String title;
			Map<String, Pojo> map = new TreeMap<String, Pojo>();
		}
	}
}
