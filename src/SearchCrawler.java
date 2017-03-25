import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.*;
import javax.swing.*;
import javax.swing.table.*;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.List;
import java.util.concurrent.TimeUnit ;
// The Search Web Crawler
public class SearchCrawler extends JFrame {
	private Map<String, String[]> CrawlerStruct = new HashMap<String, String[]>();
	private String pageBody;
	// Max URLs drop-down values.
	private static final String[] MAX_URLS = { "50", "100", "500", "1000" };
	// Cache of robot disallow lists.
	private HashMap disallowListCache = new HashMap();

	// Search GUI controls.
	private JTextField startTextField;
	private JComboBox maxComboBox;
	private JCheckBox limitCheckBox;
	private JTextField logTextField;
	private JTextField searchTextField;
	private JCheckBox caseCheckBox;
	private JButton searchButton;

	// Search stats GUI controls.
	private JLabel crawlingLabel2;
	private JLabel crawledLabel2;
	private JLabel toCrawlLabel2;
	private JProgressBar progressBar;
	private JLabel matchesLabel2;

	// Table listing search matches.
	private JTable table;

	// Flag for whether or not crawling is underway.
	private boolean crawling;

	// Matches log file print writer.
	private PrintWriter logFileWriter;

	private Document htmlDocumenty;
	private String text;
	private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.112 Safari/535.1";
	Factory fac = new Factory();

	// Constructor for Search Web Crawler.
	public SearchCrawler() {
		// Set application title.
		setTitle("APT Project");
		// Set window size.
		setSize(600, 400);
		// Handle window closing events.
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				actionExit();
			}
		});
		// Set up File menu.
		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		JMenuItem fileExitMenuItem = new JMenuItem("Exit", KeyEvent.VK_X);
		fileExitMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionExit();
			}
		});
		fileMenu.add(fileExitMenuItem);
		menuBar.add(fileMenu);
		setJMenuBar(menuBar);
		// Set up search panel.
		JPanel searchPanel = new JPanel();
		GridBagConstraints constraints;
		GridBagLayout layout = new GridBagLayout();
		searchPanel.setLayout(layout);
		JLabel startLabel = new JLabel("URLs file list:");
		constraints = new GridBagConstraints();

		constraints.anchor = GridBagConstraints.EAST;
		constraints.insets = new Insets(5, 5, 0, 0);
		layout.setConstraints(startLabel, constraints);
		searchPanel.add(startLabel);
		startTextField = new JTextField();
		constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.insets = new Insets(5, 5, 0, 5);
		layout.setConstraints(startTextField, constraints);
		searchPanel.add(startTextField);
		JLabel maxLabel = new JLabel("Max URLs to Crawl:");
		constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.EAST;
		constraints.insets = new Insets(5, 5, 0, 0);
		layout.setConstraints(maxLabel, constraints);
		searchPanel.add(maxLabel);
		maxComboBox = new JComboBox(MAX_URLS);
		maxComboBox.setEditable(true);
		constraints = new GridBagConstraints();
		constraints.insets = new Insets(5, 5, 0, 0);
		layout.setConstraints(maxComboBox, constraints);
		searchPanel.add(maxComboBox);
		limitCheckBox = new JCheckBox("Limit crawling to Start URL site");
		constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.WEST;
		constraints.insets = new Insets(0, 10, 0, 0);
		layout.setConstraints(limitCheckBox, constraints);
		searchPanel.add(limitCheckBox);
		JLabel blankLabel = new JLabel();
		constraints = new GridBagConstraints();
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		layout.setConstraints(blankLabel, constraints);
		searchPanel.add(blankLabel);
		JLabel logLabel = new JLabel("Debug File:");
		constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.EAST;
		constraints.insets = new Insets(5, 5, 0, 0);
		layout.setConstraints(logLabel, constraints);
		searchPanel.add(logLabel);
		String file = System.getProperty("user.dir") + System.getProperty("file.separator") + "crawler.log";
		logTextField = new JTextField(file);
		constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.insets = new Insets(5, 5, 0, 5);
		layout.setConstraints(logTextField, constraints);
		searchPanel.add(logTextField);
		//JLabel searchLabel = new JLabel("Search String:");
		constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.EAST;
		constraints.insets = new Insets(5, 5, 0, 0);
		//layout.setConstraints(searchLabel, constraints);
		//searchPanel.add(searchLabel);
		searchTextField = new JTextField();
		constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.insets = new Insets(5, 5, 0, 0);
		constraints.gridwidth = 2;
		constraints.weightx = 1.0d;
		//layout.setConstraints(searchTextField, constraints);
		//searchPanel.add(searchTextField);
		//caseCheckBox = new JCheckBox("Case Sensitive");
		constraints = new GridBagConstraints();
		constraints.insets = new Insets(5, 5, 0, 5);
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		//layout.setConstraints(caseCheckBox, constraints);
		//searchPanel.add(caseCheckBox);
		searchButton = new JButton("Crawl");
		searchButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionSearch();
			}
		});
		constraints = new GridBagConstraints();

		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.insets = new Insets(5, 5, 5, 5);
		layout.setConstraints(searchButton, constraints);
		searchPanel.add(searchButton);
		JSeparator separator = new JSeparator();
		constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.insets = new Insets(5, 5, 5, 5);
		layout.setConstraints(separator, constraints);
		searchPanel.add(separator);
		JLabel crawlingLabel1 = new JLabel("Crawling:");
		constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.EAST;
		constraints.insets = new Insets(5, 5, 0, 0);
		layout.setConstraints(crawlingLabel1, constraints);
		searchPanel.add(crawlingLabel1);
		crawlingLabel2 = new JLabel();
		crawlingLabel2.setFont(crawlingLabel2.getFont().deriveFont(Font.PLAIN));
		constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.insets = new Insets(5, 5, 0, 5);
		layout.setConstraints(crawlingLabel2, constraints);
		searchPanel.add(crawlingLabel2);
		JLabel crawledLabel1 = new JLabel("Crawled URLs:");
		constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.EAST;
		constraints.insets = new Insets(5, 5, 0, 0);
		layout.setConstraints(crawledLabel1, constraints);
		searchPanel.add(crawledLabel1);
		crawledLabel2 = new JLabel();
		crawledLabel2.setFont(crawledLabel2.getFont().deriveFont(Font.PLAIN));
		constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.insets = new Insets(5, 5, 0, 5);

		layout.setConstraints(crawledLabel2, constraints);
		searchPanel.add(crawledLabel2);
		JLabel toCrawlLabel1 = new JLabel("URLs to Crawl:");
		constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.EAST;
		constraints.insets = new Insets(5, 5, 0, 0);
		layout.setConstraints(toCrawlLabel1, constraints);
		searchPanel.add(toCrawlLabel1);
		toCrawlLabel2 = new JLabel();
		toCrawlLabel2.setFont(toCrawlLabel2.getFont().deriveFont(Font.PLAIN));
		constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.insets = new Insets(5, 5, 0, 5);
		layout.setConstraints(toCrawlLabel2, constraints);
		searchPanel.add(toCrawlLabel2);
		JLabel progressLabel = new JLabel("Crawling Progress:");
		constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.EAST;
		constraints.insets = new Insets(5, 5, 0, 0);
		layout.setConstraints(progressLabel, constraints);
		searchPanel.add(progressLabel);
		progressBar = new JProgressBar();
		progressBar.setMinimum(0);
		progressBar.setStringPainted(true);
		constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.insets = new Insets(5, 5, 0, 5);
		layout.setConstraints(progressBar, constraints);
		searchPanel.add(progressBar);
		//JLabel matchesLabel1 = new JLabel("Search Matches:");
		constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.EAST;
		constraints.insets = new Insets(5, 5, 10, 0);
		//layout.setConstraints(matchesLabel1, constraints);
		//searchPanel.add(matchesLabel1);
		matchesLabel2 = new JLabel();
		matchesLabel2.setFont(matchesLabel2.getFont().deriveFont(Font.PLAIN));
		constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.insets = new Insets(5, 5, 10, 5);
		layout.setConstraints(matchesLabel2, constraints);
		searchPanel.add(matchesLabel2);
		// Add panels to display.
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(searchPanel, BorderLayout.NORTH);
	}

	// Exit this program.
	private void actionExit() {
		System.exit(0);
	}

	// Handle Crawl/Stop button being clicked.
        private void actionSearch() {
            try {
                // If stop button clicked, turn crawling flag off.
                if (crawling) {
                    crawling = false;
                    return;
                }
                ArrayList errorList = new ArrayList();
                
                // Validate that start URL has been entered.
                String startUrl = startTextField.getText().trim();
                
                // Read the file of Urls
                Scanner sc = new Scanner(new File(startUrl));
                List<String> lines = new ArrayList<String>();
                while (sc.hasNextLine()) {
                    lines.add(removeWwwFromUrl(sc.nextLine()));
                }
                
                String[] arrSeed = lines.toArray(new String[0]);
                
                if (startUrl.length() < 1) {
                    errorList.add("Missing Start URL.");
                }
                // Verify start URL.
                
                // Validate that Max URLs is either empty or is a number.
                int maxUrls = 0;
                String max = ((String) maxComboBox.getSelectedItem()).trim();
                if (max.length() > 0) {
                    try {
                        maxUrls = Integer.parseInt(max);
                    }
                    catch (NumberFormatException e) {
                    }
                    if (maxUrls < 1) {
                        errorList.add("Invalid Max URLs value.");
                    }
                }
                // Validate that matches log file has been entered.
                String logFile = logTextField.getText().trim();
                if (logFile.length() < 1) {
                    errorList.add("Missing Matches Log File.");
                }
                // Show errors, if any, and return.
                if (errorList.size() > 0) {
                    StringBuffer message = new StringBuffer();
                    // Concatenate errors into single message.
                    for (int i = 0; i < errorList.size(); i++) {
                        message.append(errorList.get(i));
                        if (i + 1 < errorList.size()) {
                            message.append("\n");
                        }
                    }
                    showError(message.toString());
                    return;
                }
                
                for (String stringSeed: arrSeed) {
                    System.out.println(stringSeed+"NEW THREAD IS CREATED");
                    // Start the Search Crawler.
                    search(logFile,stringSeed,maxUrls);
                }
            } catch (FileNotFoundException ex) {
                Logger.getLogger(SearchCrawler.class.getName()).log(Level.SEVERE, null, ex);
            }
		
	}
	private void search(final String logFile, final String startUrl, final int maxUrls) {
		// Start the search in a new thread.
                
		Thread thread = new Thread(new Runnable() {
			public void run() {
                
				// Show hour glass cursor while crawling is under way.
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                                searchButton.setText("Stop");
                                //update GUI
				updateStats(startUrl, 0, 0, maxUrls);
				// Open matches log file.
				try {
					logFileWriter = new PrintWriter(new FileWriter(logFile));
				} catch (Exception e) {
					showError("Unable to open matches log file.");
					return;
				}
				// Turn crawling flag on.
				crawling = true;
				try {
					// Perform the actual crawling.
					crawl(startUrl, maxUrls, limitCheckBox.isSelected());
				} catch (IOException ex) {
					Logger.getLogger(SearchCrawler.class.getName()).log(Level.SEVERE, null, ex);
				}
				// Close matches log file.
				try {
					logFileWriter.close();
				} catch (Exception e) {
					showError("Unable to close matches log file.");
				}
				// Mark search as done.
				crawlingLabel2.setText("Done");
				// Enable search controls.
				startTextField.setEnabled(true);
				maxComboBox.setEnabled(true);
				limitCheckBox.setEnabled(true);
				logTextField.setEnabled(true);
				//searchTextField.setEnabled(true);
				//caseCheckBox.setEnabled(true);
				// Switch search button back to "Search."
				searchButton.setText("Crawl");
				// Return to default cursor.
				setCursor(Cursor.getDefaultCursor());
				// Show message if search string not found.

			}
                        
		});

		thread.start();
	}

	// Show dialog box with error message.
	private void showError(String message) {
		JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
	}

	// Update crawling stats.
	private void updateStats(String crawling, int crawled, int toCrawl, int maxUrls) {
		crawlingLabel2.setText(crawling);
		crawledLabel2.setText("" + crawled);
		toCrawlLabel2.setText("" + toCrawl);
		// Update progress bar.
		if (maxUrls == -1) {
			progressBar.setMaximum(crawled + toCrawl);
		} else {
			progressBar.setMaximum(maxUrls);
		}
		progressBar.setValue(crawled);
		//matchesLabel2.setText("" + table.getRowCount());
	}

	private void populateIndex(String Url) {
                System.out.println("Current THrea at "+Url);
		fac.Tokenize(pageBody);
		fac.Index(Url);
	}

	
        //check connection time out
	private static boolean pingUrl()
        {
            try 
            {
            final URL url = new URL("https://" + "google.com");
            Connection googleConnection = Jsoup.connect(url.toString()).userAgent(USER_AGENT);            
            
            }
             catch (final IOException e)
            {
                return false;    
            }
            return true;
	}
        

        //connecting to the URL
	private Document connectToServer(URL pageUrl) throws IOException 
        {
            try {
                Connection connection = Jsoup.connect(pageUrl.toString()).userAgent(USER_AGENT);	
                Document htmlDocument = connection.get();
                pageBody = htmlDocument.body().text();
                return htmlDocument;
                }
            catch(IOException e)
            {
                if(pingUrl())
                {
                    return null;
                }
                return connectToServer(pageUrl);
            }
            
	}

        
	// Perform the actual crawling.
	public void crawl(String startUrl, int maxUrls, boolean limitHost)
            throws IOException 
        {
            System.out.println("I am here in Crawl");
            System.out.println(startUrl);
            // Set up crawl lists.
            HashSet crawledList = new HashSet();
            LinkedHashSet toCrawlList = new LinkedHashSet();

            // Add start URL to the tocrawllist.
            toCrawlList.add(startUrl);

            /*
             * Perform actual crawling by looping through the To Crawl list.
             */
            while (crawling && toCrawlList.size() > 0)
            {
                /*
                 * Check to see if the max URL count has been reached, if it was
                 * specified.
                 */
                if (maxUrls != -1) {
                        if (crawledList.size() == maxUrls) {
                                break;
                        }
                }
                String url = (String)toCrawlList.iterator().next();
                // Get URL at bottom of the list.
                // Remove URL from the To Crawl list.
                toCrawlList.remove(url);
                // Convert string url to URL object.
                URL verifiedUrl = verifyUrl(url);
                // Skip URL if robots are not allowed to access it.
                if (!isRobotAllowed(verifiedUrl)) {
                  continue;
                }

                // Update crawling stats.
                updateStats(url, crawledList.size(), toCrawlList.size(), maxUrls);

                // Add page to the crawled list.
                crawledList.add(url);

                ///////////////////////////////////////
                // Connect to the given URL and busy///
                /// waiting if connection timed out.///
                ///////////////////////////////////////
                while(connectToServer(verifiedUrl)==null)
                {
                }
                Document htmlDocumentq = connectToServer(verifiedUrl);

                // Extract metadata from the the document //
                // String metaData=retrieveMetadata(htmlDocumentq);

                /*
                 * retrieve all its links
                 */
                if (pageBody != null && pageBody.length() > 0) {
                        // Retrieve list of valid links from page.
                        ArrayList links = retrieveLinks(verifiedUrl, htmlDocumentq, crawledList, limitHost);
                        // Add this links to the toCrawlList //
                        toCrawlList.addAll(links);

                        populateIndex(url);


                }
                // Update crawling stats.
                updateStats(url, crawledList.size(), toCrawlList.size(), maxUrls);
            }
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
            if (disallowList == null) 
            {
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
	private ArrayList retrieveLinks(URL pageUrl, Document htmlDocument, HashSet crawledList, boolean limitHost)
        throws IOException {
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
                /*
                 * If specified, limit links to those having the same host as the
                 * start URL.
                 */
                if (limitHost && !pageUrl.getHost().toLowerCase().equals(verifiedLink.getHost().toLowerCase())) {
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
