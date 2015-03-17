package reader;

import group.models.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.deri.iris.api.basics.ILiteral;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.basics.ITuple;

import JODS.PreferencesGraph;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Helper {

	public static String getNameUser(JsonObject jsonObject) {
		String message = "";
		for (Entry<String, JsonElement> entry : jsonObject.entrySet()) {

			String key = entry.getKey();
			JsonElement value = entry.getValue();

			if (value.isJsonObject()) {
				getNameUser(value.getAsJsonObject());
			} else if (value.isJsonArray()) {
				JsonArray jsonArray = value.getAsJsonArray();

				if (jsonArray.size() == 1) {
					getNameUser((JsonObject) jsonArray.get(0));
				}
			} else {
				if (key.equals("name"))
					message += value.toString().replaceAll("^\"|\"$", "");
			}
		}
		return message;
	}

	public static double gett(JsonObject jsonObject) {
		double t = -1;
		for (Entry<String, JsonElement> entry : jsonObject.entrySet()) {

			String key = entry.getKey();
			JsonElement value = entry.getValue();

			if (value.isJsonObject()) {
				gett(value.getAsJsonObject());
			} else if (value.isJsonArray()) {
				JsonArray jsonArray = value.getAsJsonArray();

				if (jsonArray.size() == 1) {
					gett((JsonObject) jsonArray.get(0));
				}
			} else {
				if (key.equals("t"))
					t = Double.parseDouble(value.toString().replaceAll(
							"^\"|\"$", ""));
			}
		}
		return t;
	}

	public static String getIdUser(JsonObject jsonObject) {
		String message = "";
		for (Entry<String, JsonElement> entry : jsonObject.entrySet()) {

			String key = entry.getKey();
			JsonElement value = entry.getValue();

			if (value.isJsonObject()) {
				getIdUser(value.getAsJsonObject());
			} else if (value.isJsonArray()) {
				JsonArray jsonArray = value.getAsJsonArray();

				if (jsonArray.size() == 1) {
					getIdUser((JsonObject) jsonArray.get(0));
				}
			} else {
				if (key.equals("id"))
					message += value.toString().replaceAll("^\"|\"$", "");
			}
		}
		return message;
	}

	public static List<String> getVertexes(JsonObject jsonObject) {
		List<String> nodes = new ArrayList<String>();
		for (Entry<String, JsonElement> entry : jsonObject.entrySet()) {

			String key = entry.getKey();
			JsonElement value = entry.getValue();

			if (value.isJsonObject()) {
				getVertexes(value.getAsJsonObject());
			} else if (value.isJsonArray()) {
				if (key.equals("block")) {
					JsonArray jsonArray = value.getAsJsonArray();
					if (jsonArray.size() == 1) {
						getVertexes((JsonObject) jsonArray.get(0));
					} else {
						Iterator<JsonElement> msg = jsonArray.iterator();
						while (msg.hasNext()) {
							nodes.add(msg.next().getAsJsonObject()
									.get("blockId").toString()
									.replaceAll("^\"|\"$", ""));
						}
					}
				}
			}

		}
		return nodes;
	}

	public static List<Pair<IPredicate, IPredicate>> getPreferences(
			JsonArray array, List<IRule> rules) {
		Iterator<JsonElement> msg = array.iterator();
		List<Pair<IPredicate, IPredicate>> mp = new ArrayList<Pair<IPredicate, IPredicate>>();
		while (msg.hasNext()) {
			JsonObject o = msg.next().getAsJsonObject();
			List<Pair<String, String>> conn = getConnections(o);
			Map<String, IPredicate> mapPredicates = new HashMap<String, IPredicate>();
			for (IRule i : rules) {
				for (IPredicate p : i.getPredicates()) {
					mapPredicates.put(p.getPredicateSymbol(), p);
				}
			}
			List<Pair<IPredicate, IPredicate>> lp = new ArrayList<Pair<IPredicate, IPredicate>>();
			for (Pair<String, String> pair : conn) {
				lp.add(new Pair<IPredicate, IPredicate>(mapPredicates.get(pair
						.getElement0()), mapPredicates.get(pair.getElement1())));
			}
			mp.addAll(lp);
		}
		return mp;

	}



	public static List<Pair<ILiteral, ILiteral>> getPreferences(JsonObject o,
			List<IRule> rules) {
		List<Pair<String, String>> conn = getConnections(o);
		Map<String, ILiteral> mapPredicates = new HashMap<String, ILiteral>();
		for (IRule r : rules) {
			for (ILiteral p : r.getAllLiterals()) {
				if (!mapPredicates.containsKey(p.getAtom().getPredicate()
						.getPredicateSymbol())) {
					mapPredicates.put(p.getAtom().getPredicate()
							.getPredicateSymbol(), p);
				}
			}
		}
		List<Pair<ILiteral, ILiteral>> lp = new ArrayList<Pair<ILiteral, ILiteral>>();
		for (Pair<String, String> pair : conn) {
			lp.add(new Pair<ILiteral, ILiteral>(mapPredicates.get(pair
					.getElement0()), mapPredicates.get(pair.getElement1())));
		}
		return lp;

	}

	public static List<Pair<String, String>> getConnections(
			JsonObject jsonObject) {
		List<Pair<String, String>> conn = new ArrayList<Pair<String, String>>();

		if (jsonObject.get("connections") != null) {
			JsonArray jsonArray = jsonObject.get("connections")
					.getAsJsonArray();
			Iterator<JsonElement> msg = jsonArray.iterator();
			while (msg.hasNext()) {
				JsonElement js = msg.next();
				conn.add(new Pair<String, String>(js.getAsJsonObject()
						.get("pageSourceId").toString()
						.replaceAll("^\"|\"$", ""), js.getAsJsonObject()
						.get("pageTargetId").toString()
						.replaceAll("^\"|\"$", "")));
			}

		}

		return conn;
	}
}
