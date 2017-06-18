
public class Souce {

	public static void main(String[] args){
		//SearchCrawler crawler = new SearchCrawler();
		//crawler.show();
		Crawler crawler = new Crawler(args[1], Integer.parseInt(args[2]),args);
		crawler.actionSearch();
	}
}
