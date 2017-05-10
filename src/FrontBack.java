import java.util.*;

public class FrontBack {

	// private String[] query;
	public HashMap<String, Integer> crawledList;
	public HashMap<String, Integer> rank;
	public HashMap<String, Integer> lexion;
	Factory fac;

	public FrontBack(HashMap<String, Integer> craw, HashMap<String, Integer> lex) {
		crawledList = craw;
		lexion = lex;
		fac = new Factory();
	}

	public void processQuery(String[] query, String para) {
		rank = new HashMap<String, Integer>();
		HashMap<String, Integer> tmp;
		if (para.equals("normal")) {
			for (int i = 0; i < query.length; i++) {
				tmp = fac.find(query[i]);
				Iterator<Map.Entry<String, Integer>> it = tmp.entrySet().iterator();
				while (it.hasNext()) {
					String key = it.next().getKey();
					Integer value1 = rank.getOrDefault(key, 0);
					Integer value2 = tmp.get(key);
					rank.put(key, 
							value1 + value2 + crawledList.get(key) + lexion.get(query[i]));
				}
			}
			rank = sortByValue(rank);
//			Iterator<String> it = rank.keySet().iterator();
//			while (it.hasNext()) {
//				System.out.println(it.next());
//			}
			
		}
	}

	public static <K, V extends Comparable<? super V>> HashMap<K, V> sortByValue(HashMap<K, V> map) {
		List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			@Override
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				//replace o2 and o1 to change the order
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});

		HashMap<K, V> result = new LinkedHashMap<>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}
}
