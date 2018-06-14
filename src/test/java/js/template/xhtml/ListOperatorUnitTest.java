package js.template.xhtml;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import js.dom.Document;
import js.dom.EList;
import js.dom.Element;
import js.template.TemplateException;

@SuppressWarnings({ "unused" })
public class ListOperatorUnitTest extends TestCaseEx {
	public void testSupportedPrimitiveTypes() {
		String html = "" + //
				"<ul data-list='.'>" + //
				"   <li data-text='.'></li>" + //
				"</ul>";
		List<Object> model = new ArrayList<Object>();
		model.add(22);
		model.add(2222);
		model.add(222222);
		model.add(22222222L);
		model.add(22.22F);
		model.add(22222222.22222222);
		model.add(true);
		model.add('c');
		model.add("string");

		Document doc = run(html, model);

		EList elist = doc.findByTag("li");
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

	public void testSupportedValueTypes() throws ParseException, MalformedURLException, UnsupportedEncodingException {
		String html = "" + //
				"<ul data-list='.'>" + //
				"   <li data-text='.'></li>" + //
				"</ul>";

		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
		Date date = df.parse("1964-03-15T13:40:00.000EET");

		List<Object> model = new ArrayList<Object>();
		model.add(date);
		model.add(new java.sql.Date(date.getTime()));
		model.add(new Time(date.getTime()));
		model.add(new Timestamp(date.getTime()));
		model.add(new File("/var/log/tomcat/catalina.out"));
		model.add(TimeZone.getTimeZone("UTC"));
		model.add(new URL("http://gnotis.ro/"));

		Document doc = run(html, model);
		doc.dump();

		EList elist = doc.findByTag("li");
		assertEquals("Sun Mar 15 1964 13:40:00 UTC", elist, 0);
		assertEquals("Sun Mar 15 1964 13:40:00 UTC", elist, 1);
		assertEquals("Sun Mar 15 1964 13:40:00 UTC", elist, 2);
		assertEquals("Sun Mar 15 1964 13:40:00 UTC", elist, 3);
		assertEquals("/var/log/tomcat/catalina.out", elist.item(4).getText());
		assertEquals("UTC", elist, 5);
		assertEquals("http://gnotis.ro/", elist, 6);
	}

	public void testAnonymousListOfPrimitives() {
		String html = "" + //
				"<ul data-list='.'>" + //
				"   <li data-text='.'></li>" + //
				"</ul>";
		List<Object> model = new Primitives().items;

		Document doc = run(html, model);

		EList elist = doc.findByTag("li");
		assertEquals("1", elist, 0);
		assertEquals("2", elist, 1);
	}

	public void testListOfPrimitives() {
		String html = "" + //
				"<ul data-list='items'>" + //
				"   <li data-text='.'></li>" + //
				"</ul>";
		Primitives model = new Primitives();

		Document doc = run(html, model);

		EList elist = doc.findByTag("li");
		assertEquals("1", elist, 0);
		assertEquals("2", elist, 1);
	}

	public void testListOfAttributeValues() {
		String html = "" + //
				"<ul data-list='items'>" + //
				"   <li data-id='.'></li>" + //
				"</ul>";
		Primitives model = new Primitives();

		Document doc = run(html, model);

		assertEquals("1", doc.findByTag("li").item(0).getAttr("id"));
		assertEquals("2", doc.findByTag("li").item(1).getAttr("id"));
	}

	public void testListOfLinks() throws MalformedURLException {
		String html = "" + //
				"<div data-list='.'>" + //
				"   <a data-id='id' data-href='url' data-title='tooltip' data-text='display'></a>" + //
				"</div>";
		List<Link> model = new ArrayList<Link>();
		model.add(new Link(1, new URL("http://server.com/url1"), "tooltip1", "link1"));
		model.add(new Link(2, new URL("http://server.com/url2"), "tooltip2", "link2"));

		Document doc = run(html, model);

		EList elist = doc.findByTag("a");
		Element a = elist.item(0);
		assertEquals("1", a.getAttr("id"));
		assertEquals("http://server.com/url1", a.getAttr("href"));
		assertEquals("tooltip1", a.getAttr("title"));
		assertEquals("link1", a.getText());
		a = elist.item(1);
		assertEquals("2", a.getAttr("id"));
		assertEquals("http://server.com/url2", a.getAttr("href"));
		assertEquals("tooltip2", a.getAttr("title"));
		assertEquals("link2", a.getText());
	}

	public void testListOfObjects() {
		String html = "" + //
				"<ul data-list='items'>" + //
				"   <li data-object='.'>" + //
				"       <h2 data-text='title'></h2>" + //
				"   </li>" + //
				"</ul>";
		Objects model = new Objects();

		Document doc = run(html, model);

		EList elist = doc.findByTag("h2");
		assertEquals("title1", elist, 0);
		assertEquals("title2", elist, 1);
	}

	public void testAnonymousListOfObjects() {
		String html = "" + //
				"<ul data-list='.'>" + //
				"   <li data-object='.'>" + //
				"       <h2 data-text='title'></h2>" + //
				"   </li>" + //
				"</ul>";
		List<Pojo> model = new Objects().items;

		Document doc = run(html, model);

		EList elist = doc.findByTag("h2");
		assertEquals("title1", elist, 0);
		assertEquals("title2", elist, 1);
	}

	public void testListOfMaps() {
		String html = "" + //
				"<ul data-list='items'>" + //
				"   <li>" + //
				"       <dl data-map='.'>" + //
				"           <dt></dt>" + //
				"           <dd>" + //
				"               <h2 data-text='title'></h2>" + //
				"           </dd>" + //
				"       </dl>" + //
				"   </li>" + //
				"</ul>";
		ListOfMaps model = new ListOfMaps();
		model.items.add(new HashMap<String, Pojo>());
		model.items.add(new HashMap<String, Pojo>());
		model.items.get(0).put("key0", new Pojo("title0"));
		model.items.get(0).put("key1", new Pojo("title1"));
		model.items.get(1).put("key2", new Pojo("title2"));
		model.items.get(1).put("key3", new Pojo("title3"));

		Document doc = run(html, model);

		Element dt, dd;
		for (int i = 0; i < 4; ++i) {
			dt = doc.getByXPath("//DT[starts-with(text(),'key%d')]", i);
			assertNotNull(dt);
			dd = dt.getNextSibling();
			assertNotNull(dd);
			assertEquals("title" + i, dd.getByTag("h2").getText().trim());
		}
	}

	public void testNestedLists() {
		String html = "" + //
				"<h1 data-text='title'></h1>" + //
				"<table>" + //
				"   <tbody data-list='list'>" + //
				"       <tr data-object='.'>" + //
				"            <td>" + //
				"                <h2 data-text='title'></h2>" + //
				"                <ul data-list='list'>" + //
				"                    <li data-object='.'>" + //
				"                        <h3 data-text='title'></h3>" + //
				"                    </li>" + //
				"                </ul>" + //
				"            </td>" + //
				"        </tr>" + //
				"    </tbody>" + //
				"</table>";
		NestedLists model = new NestedLists();
		model.title = "title";
		model.list.add(new NestedLists.Nested("title0"));
		model.list.add(new NestedLists.Nested("title1"));
		model.list.get(0).list.add(new Pojo("title00"));
		model.list.get(0).list.add(new Pojo("title01"));
		model.list.get(1).list.add(new Pojo("title10"));
		model.list.get(1).list.add(new Pojo("title11"));

		Document doc = run(html, model);

		assertEquals("title", doc.getByTag("h1").getText());
		EList elist = doc.findByTag("h2");
		assertEquals(2, elist.size());
		assertEquals("title0", elist, 0);
		assertEquals("title1", elist, 1);
		elist = doc.findByTag("h3");
		assertEquals(4, elist.size());
		assertEquals("title00", elist, 0);
		assertEquals("title01", elist, 1);
		assertEquals("title10", elist, 2);
		assertEquals("title11", elist, 3);
	}

	public void testEmptyList() {
		String html = "" + //
				"<ul data-list='.'>" + //
				"   <li data-text='.'></li>" + //
				"</ul>";
		List<Integer> model = Collections.emptyList();

		Document doc = run(html, model);

		EList elist = doc.findByTag("li");
		assertEquals(0, elist.size());
	}

	public void testListOfDefaultPrimitives() {
		String html = "" + //
				"<ul data-list='items'>" + //
				"   <li></li>" + //
				"</ul>";
		Primitives model = new Primitives();

		Document doc = run(html, model);
		doc.dump();

		EList elist = doc.findByTag("li");
		assertEquals("1", elist, 0);
		assertEquals("2", elist, 1);
	}

	public void testListOfDefaultObjects() {
		String html = "" + //
				"<ul data-list='items'>" + //
				"   <li>" + //
				"       <h2 data-text='title'></h2>" + //
				"   </li>" + //
				"</ul>";
		Objects model = new Objects();

		Document doc = run(html, model);

		EList elist = doc.findByTag("h2");
		assertEquals("title1", elist, 0);
		assertEquals("title2", elist, 1);
	}

	public void testListWithoutChildren() {
		String html = "<ul data-list='.'></ul>";
		List<Integer> model = Collections.emptyList();
		try {
			run(html, model);
		} catch (TemplateException e) {
			assertTrue(e.getMessage().contains("Missing item template"));
			return;
		}
		fail("List operator on element whithout children should rise exception.");
	}

	public void testNullListValue() {
		Objects model = new Objects();
		model.items = null;

		for (String opcode : new String[] { "list", "olist" }) {
			String html = String.format("<ul data-%s='items'><li></li></ul>", opcode);
			Document doc = run(html, model);
			EList elist = doc.findByTag("li");
			assertEquals(0, elist.size());
		}
	}

	public void testBadListValueTypes() {
		Object[] invalidValues = new Object[] { 123, "string", true };

		for (String opcode : new String[] { "list", "olist" }) {
			String html = String.format("<ul data-%s='items'><li></li></ul>", opcode);
			for (int i = 0; i < invalidValues.length; ++i) {
				try {
					run(html, invalidValues[i]);
				} catch (TemplateException e) {
					continue;
				}
				fail("Bad list value types should throw templates exception.");
			}
		}
	}

	// ------------------------------------------------------
	// fixture initialization and helpers

	private static class Primitives {
		List<Object> items = new ArrayList<Object>();

		Primitives() {
			this.items.add(1);
			this.items.add(2);
		}
	}

	private static class Objects {
		List<Pojo> items = new ArrayList<Pojo>();

		Objects() {
			this.items.add(new Pojo("title1"));
			this.items.add(new Pojo("title2"));
		}
	}

	private static class Pojo {
		String title;

		Pojo(String title) {
			this.title = title;
		}
	}

	private static class ListOfObjects {
		List<Pojo> items = new ArrayList<Pojo>();
	}

	private static class ListOfMaps {
		List<Map<String, Pojo>> items = new ArrayList<Map<String, Pojo>>();
	}

	private static class NestedLists {
		String title;
		List<Nested> list = new ArrayList<Nested>();

		static class Nested {
			String title;
			List<Pojo> list = new ArrayList<Pojo>();

			Nested(String title) {
				this.title = title;
			}
		}
	}

	private static class Link {
		int id;
		URL url;
		String tooltip;
		String display;

		Link(int id, URL url, String tooltip, String display) {
			this.id = id;
			this.url = url;
			this.tooltip = tooltip;
			this.display = display;
		}
	}
}
