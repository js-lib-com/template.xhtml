package js.template.xhtml;

import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.Date;
import java.util.List;

import js.converter.ConverterRegistry;
import js.dom.Document;

@SuppressWarnings({ "unused" })
public class TemplateBenchmark extends TestCaseEx {
	public void test() throws IOException {
		BenchProbe probe = new BenchProbe();// TestData.newInstance(BenchProbe.class);
		Document html = getBuilder().loadHTML(getClass().getResourceAsStream("bench-probe.html"));

		ConverterRegistry converterManager = ConverterRegistry.getInstance();
		Content content = new Content(probe);

		XhtmlTemplate template = new XhtmlTemplate("bench-probe", html);

		long start = new Date().getTime();
		for (int i = 0; i < 10000; ++i) {
			template.serialize(content, new MockWriter());
		}
		System.out.println(new Date().getTime() - start);

		start = new Date().getTime();
		for (int i = 0; i < 10000; ++i) {
			// template.inject(probe);
		}
		System.out.println(new Date().getTime() - start);
	}

	private static class MockWriter extends Writer {
		@Override
		public void close() throws IOException {
		}

		@Override
		public void flush() throws IOException {
		}

		@Override
		public void write(char[] cbuf, int off, int len) throws IOException {
		}
	}

	private static class BenchProbe {
		List<Result> results;
		List<Comment> comments;
		Release release;
		int trackId;
		String youtubeId;
		List<ReleaseItem> discography;
		List<ArtistItem> relatedArtists;
		List<Artist> artists;

		public static class Result {
			String artist;
			String title;
			int duration;
			int viewsCount;
		}

		public static class Comment {
			String firstName;
			String lastName;
			Date date;
			String text;
		}

		public static class Artist {
			URL link;
			String name;
		}

		public static class Release {
			Artist artist;
			String title;
			String label;
			String catalog;
			String country;
			Date released;
			List<TrackItem> tracklist;
		}

		public static class TrackItem {
			String title;
		}

		public static class ReleaseItem {
			String title;
			Date released;
		}

		public static class ArtistItem {
		}
	}
}
