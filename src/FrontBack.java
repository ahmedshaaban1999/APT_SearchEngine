import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FrontBack {

	// private String[] query;
	public ConcurrentHashMap<String, Integer> crawledList;
	public HashMap<String, Integer> rank;
	public HashMap<String, Integer> lexion;
	Factory fac;

	public FrontBack(ConcurrentHashMap<String, Integer> craw, HashMap<String, Integer> lex) {
		crawledList = craw;
		lexion = lex;
		fac = new Factory();
	}

	public String[] processQuery(String[] query, String para) {
		rank = new HashMap<String, Integer>();
		String[] result = new String[] {};
		HashMap<String, Integer> tmp;
		if (para.equals("normal")) {
			for (int i = 0; i < query.length; i++) {
				tmp = fac.find(query[i]);
				Iterator<Map.Entry<String, Integer>> it = tmp.entrySet().iterator();
				while (it.hasNext()) {
					String key = it.next().getKey();
					Integer value1 = rank.getOrDefault(key, 0);
					Integer value2 = tmp.getOrDefault(key, 0);
					if (!(value1 == null) && !(value2 == null)) {
						rank.put(key,
								value1 + value2 + crawledList.getOrDefault(key, 0) + lexion.getOrDefault(query[i], 0));
					}
				}
			}
			rank = sortByValue(rank);
			result = rank.keySet().toArray(result);
			// Iterator<String> it = rank.keySet().iterator();
			// while (it.hasNext()) {
			// System.out.println(it.next());
			// }

		}
		return result;
	}

	public static <K, V extends Comparable<? super V>> HashMap<K, V> sortByValue(HashMap<K, V> map) {
		List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			@Override
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				// replace o2 and o1 to change the order
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
