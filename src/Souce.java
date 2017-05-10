
public class Souce {

	public static void main(String[] args){
		//SearchCrawler crawler = new SearchCrawler();
		//crawler.show();
		Crawler crawler = new Crawler(args[0], Integer.parseInt(args[1]));
		crawler.actionSearch();
	}
}
