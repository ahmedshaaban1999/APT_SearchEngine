import java.awt.Cursor;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Crawler {

	String startUrl;
	int maxUrls;

	private Document htmlDocumenty;
	private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 "
			+ "(KHTML, like Gecko) Chrome/13.0.782.112 Safari/535.1";
	private Map<String, String[]> CrawlerStruct = new HashMap<String, String[]>();
	private String pageBody;
	// Cache of robot disallow lists.
	private HashMap disallowListCache = new HashMap();

	// Set up crawl lists.
	ConcurrentHashMap<String, Integer> crawledList = new ConcurrentHashMap<String, Integer>();
	ConcurrentHashMap<String, Integer> toCrawlList = new ConcurrentHashMap<String, Integer>();
	Factory fac;

	
	public Crawler(String url, int max) {
		startUrl = url;
		maxUrls = max;
		fac = new Factory();
	}

	public void actionSearch() {

		// Validate that start URL has been entered.
		if (startUrl.length() < 1) {
			System.out.println("Missing Start URL.");
			System.exit(0);
		}

		// Verify start URL.
		else if (verifyUrl(startUrl) == null) {
			System.out.println("Missing Start URL.");
			System.exit(0);
		}

		// Remove "www" from start URL if present.
		startUrl = removeWwwFromUrl(startUrl);
		// Add start URL to the tocrawllist.
		// toCrawlList.keySet().add(startUrl);
		toCrawlList.put(startUrl, 1);
		search(maxUrls);
	}
	
	private void search(int maxUrls) {
		System.out.println("max urls are " +  maxUrls);
		for (int i = 0; i < 4; i++) {
			// Start the search in a new thread.
			Thread thread = new Thread(new Runnable() {
				public void run() {
					try {
						// Perform the actual crawling.
						crawl(maxUrls);
					} catch (IOException ex) {
						Logger.getLogger(SearchCrawler.class.getName()).log(Level.SEVERE, null, ex);
					}
					System.out.println("");
					System.out.println("a Crawler has finished");
					System.out.println("crawled sites are " + crawledList.size());
					System.out.println("to crawl sites are " + toCrawlList.size());
					System.out.println("");
				}
			});

			thread.start();
		}
	}

	public static boolean pingUrl() {
		try {
			final URL url = new URL("https://" + "google.com");
			final HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
			urlConn.connect();
			if (urlConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
				return true;
			}
		} catch (final MalformedURLException e1) {

		} catch (final IOException e) {

		}
		return false;
	}
	
	private Document connectToServer(URL pageUrl) throws IOException {
		while (pingUrl()) {
			Connection connection = Jsoup.connect(pageUrl.toString()).userAgent(USER_AGENT);
			connection.ignoreHttpErrors(true);
			Document htmlDocument = connection.get();
			pageBody = htmlDocument.body().text();
			// System.out.println(pageBody);
			return htmlDocument;
		}
		return null;
	}
	
	public void crawl(int maxUrls) throws IOException {
		System.out.println("I am here in Crawl");

		/*
		 * Perform actual crawling by looping through the To Crawl list.
		 */
		while (crawledList.size() < maxUrls) {
			String url;
			URL verifiedUrl;
			Document htmlDocumentq = null;
			// Get URL at bottom of the list.
			do {

				synchronized (this) {

					while (! toCrawlList.keySet().iterator().hasNext()) {
						
					}
					url = (String) toCrawlList.keySet().iterator().next();
					// Add page to the crawled list.
					crawledList.put(url, toCrawlList.get(url));
					toCrawlList.remove(url);
					/*if (toCrawlList.keySet().iterator().hasNext()) {
						url = (String) toCrawlList.keySet().iterator().next();
						// toCrawlList.keySet().iterator().remove();
						// Remove URL from the To Crawl list.
						toCrawlList.remove(url);
					} else {
						System.out.println("no more links");
						break;
					}*/
				}
				System.out.println("processing" + url);
				// Convert string url to URL object.
				verifiedUrl = verifyUrl(url);

				// Skip URL if robots are not allowed to access it.
				if (!isRobotAllowed(verifiedUrl)) {
					continue;
				}

				///////////////////////////////////////
				// Connect to the given URL./
				///////////////////////////////////////
				htmlDocumentq = connectToServer(verifiedUrl);
			} while (htmlDocumentq == null);

			///////////////////////////////////////
			// Connect to the given URL./
			///////////////////////////////////////
			// String metaData=retrieveMetadata(htmlDocumentq);
			
			/*
			 * retrieve all its links see if it contains the search string.
			 */
			if (pageBody != null && pageBody.length() > 0) {
				// Retrieve list of valid links from page.
				ArrayList links = retrieveLinks(verifiedUrl, htmlDocumentq);
				// toCrawlList.entrySet().addAll(links);
				for (Object link : links) {
					if (!crawledList.containsKey((String)link)) {
						toCrawlList.put((String) link, toCrawlList.getOrDefault((String) link, 1)
								+ crawledList.get(url));
					}else {
						crawledList.put((String) link, crawledList.get(link.toString())
								+ crawledList.get(url));
					}
				}
				System.out.println("tocrawllist size is " + toCrawlList.size());
				populateIndex(url);
			}
		}
		System.out.println("crawled too many sites");
	}
	
	private void populateIndex(String Url) {
		fac.Tokenize(pageBody);
		fac.Index(Url);
	}

	// Verify URL format.
		private URL verifyUrl(String url) {
			// Only allow HTTP URLs or HTTPS
			if (!(url.toLowerCase().startsWith("http://") || url.toLowerCase().startsWith("https://")))
				return null;
			// Verify format of URL.
			URL verifiedUrl = null;
			try {
				verifiedUrl = new URL(url);
			} catch (Exception e) {
				return null;
			}
			return verifiedUrl;
		}

		// Check if robot is allowed to access the given URL.
		private boolean isRobotAllowed(URL urlToCheck) {
			String host = urlToCheck.getHost().toLowerCase();
			// Retrieve host's disallow list from cache.
			ArrayList disallowList = (ArrayList) disallowListCache.get(host);
			// If list is not in the cache, download and cache it.
			if (disallowList == null) {
				disallowList = new ArrayList();
				try {
					URL robotsFileUrl = new URL("http://" + host + "/robots.txt");
					// Open connection to robot file URL for reading.
					BufferedReader reader = new BufferedReader(new InputStreamReader(robotsFileUrl.openStream()));
					// Read robot file, creating list of disallowed paths.
					String line;
					while ((line = reader.readLine()) != null) {
						if (line.indexOf("Disallow:") == 0) {
							String disallowPath = line.substring("Disallow:".length());
							// Check disallow path for comments and remove if
							// present.
							int commentIndex = disallowPath.indexOf("#");
							if (commentIndex != -1) {
								disallowPath = disallowPath.substring(0, commentIndex);
							}
							// Remove leading or trailing spaces from disallow path.
							disallowPath = disallowPath.trim();
							// Add disallow path to list.
							disallowList.add(disallowPath);
						}
					}
					// Add new disallow list to cache.
					disallowListCache.put(host, disallowList);
				} catch (Exception e) {
					/*
					 * Assume robot is allowed since an exception is thrown if the
					 * robot file doesn't exist.
					 */
					return true;
				}
			}
			/*
			 * Loop through disallow list to see if crawling is allowed for the
			 * given URL.
			 */
			String file = urlToCheck.getFile();
			for (int i = 0; i < disallowList.size(); i++) {
				String disallow = (String) disallowList.get(i);
				if (file.startsWith(disallow)) {
					return false;
				}
			}
			return true;
		}

		// Parse through page contents and retrieve links.
		private ArrayList retrieveLinks(URL pageUrl, Document htmlDocument) throws IOException {
			ArrayList linkList = new ArrayList();
			htmlDocumenty = htmlDocument;
			Elements linksOnPage = htmlDocument.select("a[href]");

			for (Element a : linksOnPage) {
				String link = a.absUrl("href");
				// Skip empty links.
				if (link.length() < 1) {
					continue;
				}
				// Skip links that are just page anchors.
				if (link.charAt(0) == '#') {
					continue;
				}
				// Skip mailto links.
				if (link.indexOf("mailto:") != -1) {
					continue;
				}
				// Skip JavaScript links.
				if (link.toLowerCase().indexOf("javascript") != -1) {
					continue;
				}
				// Prefix absolute and relative URLs if necessary.
				if (link.indexOf("://") == -1) {
					// Handle absolute URLs.
					if (link.charAt(0) == '/') {
						link = "http://" + pageUrl.getHost() + link;
						// Handle relative URLs.
					} else {
						String file = pageUrl.getFile();
						if (file.indexOf('/') == -1) {
							link = "http://" + pageUrl.getHost() + "/" + link;
						} else {
							String path = file.substring(0, file.lastIndexOf('/') + 1);
							link = "http://" + pageUrl.getHost() + path + link;
						}
					}
				}
				// Remove anchors from link.
				int index = link.indexOf('#');
				if (index != -1) {
					link = link.substring(0, index);
				}
				// Remove leading "www" from URL's host if present.
				link = removeWwwFromUrl(link);
				// Verify link and skip if invalid.
				URL verifiedLink = verifyUrl(link);
				if (verifiedLink == null) {
					continue;
				}
				// Skip link if it has already been crawled.
				if (crawledList.contains(link)) {
					continue;
				}
				// Add link to list.
				linkList.add(link);
			}
			return (linkList);
		}

		// Remove leading "www" from a URL's host if present.
		private String removeWwwFromUrl(String url) {
			int index = url.indexOf("://www.");
			if (index != -1) {
				return url.substring(0, index + 3) + url.substring(index + 7);
			}
			return (url);
		}

		private String retrieveMetadata(Document doc) {
			int count = 0;
			String keywords = doc.select("meta[name=keywords]").first().attr("content");
			System.out.println(keywords);
			if (keywords.length() > 1)
				return keywords;
			else
				keywords = "";
			return keywords;

		}
}
