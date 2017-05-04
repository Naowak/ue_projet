package simulator;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class DataWork {

	public static final String pathConfig = "./SolarPanelSimulator/data/linearisation.config";
	public static String limJour;
	public static String limNuit;
	public static final Map<String, Map<String, Double>> MapBorne;
	static {
		MapBorne = Collections.unmodifiableMap(config());
	}

	public static Map<String, Map<String, Double>> config() {
		Map<String, Map<String, Double>> aMap = new HashMap<String, Map<String, Double>>();
		FileInputStream f;
		try {
			f = new FileInputStream(pathConfig);

			InputStreamReader fi = new InputStreamReader(f);
			BufferedReader brf = new BufferedReader(fi);
			String ligne;
			while ((ligne = brf.readLine()) != null) {
				if (!ligne.isEmpty() && ligne.charAt(0) == 'l') {
					Map<String, Double> tmp = new HashMap<String, Double>();
					String tab[] = ligne.split("\\s+");
					for (int i = 0; i < tab.length; i++) {
					}
					if (tab[2].equals("?")) {
						tmp.put("Min", Double.NEGATIVE_INFINITY);
					} else {
						tmp.put("Min", Double.parseDouble(tab[2]));
					}
					if (tab[3].equals("?")) {
						tmp.put("Max", Double.POSITIVE_INFINITY);
					} else {
						tmp.put("Max", Double.parseDouble(tab[3]));
					}
					if (tab[4].equals("?")) {
						tmp.put("Ecart", Double.POSITIVE_INFINITY);
					} else {
						tmp.put("Ecart", Double.parseDouble(tab[4]));
					}
					aMap.put(tab[1], tmp);
				} else if (!ligne.isEmpty() && ligne.charAt(0) == 'h') {
					String tab[] = ligne.split("\\s+");
					limJour = tab[1];
					limNuit = tab[2];
				}
			}
			brf.close();
			fi.close();
			f.close();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return aMap;
	}

	public static void main(String[] args) throws Exception {
		ObjectOutputStream oos = new ObjectOutputStream(
				new FileOutputStream("./SolarPanelSimulator/data/new_data.ser"));
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream("./SolarPanelSimulator/data/hashmap.ser"));
		@SuppressWarnings("unchecked")
		Map<Integer, Map<String, Double>> data = (Map<Integer, Map<String, Double>>) ois.readObject();
		ois.close();

		Map<Integer, Map<String, Double>> ba = new HashMap<Integer, Map<String, Double>>();
		Map<Integer, Map<String, Double>> bd = new HashMap<Integer, Map<String, Double>>();
		separation_data_brumisateur(data, ba, bd);

		oos.writeObject(data);
		oos.close();
	}

	public static Map<String, Double[]> hashmap_to_tab(Map<Integer, Map<String, Double>> data, Integer[] keys,
			String[] cols_name, int nb_data, int nb_cols) {
		Map<String, Double[]> new_data = new HashMap<String, Double[]>();

		for (int i = 0; i < nb_data; ++i) {
			for (int j = 0; j < nb_cols; ++j) {
				String cname = cols_name[j];
				if (new_data.get(cname) == null)
					new_data.put(cname, new Double[nb_data]);
				double tmp;
				try {
					tmp = data.get(keys[i]).get(cname);
				} catch (Exception e) {
					tmp = 0;
				}
				new_data.get(cname)[i] = tmp;
			}
		}
		return new_data;
	}

	public static Map<Integer, Map<String, Double>> lissage_data(Map<Integer, Map<String, Double>> data) {
		int nb_data = data.size();
		Integer keys[] = keys_to_array(data, nb_data);

		int nb_cols = get_nb_cols(data, keys[0]);
		String cols_name[] = cols_name_to_array(data, keys[0], nb_cols);

		Map<String, Double[]> data_tab = hashmap_to_tab(data, keys, cols_name, nb_data, nb_cols);

		for (int j = 0; j < nb_cols; ++j) {
			String cname = cols_name[j];
			data_tab.put(cname, applyBorne(data_tab.get(cname), cname, nb_data));
		}

		return tab_to_HashMap(data_tab, keys, cols_name, nb_data, nb_cols);
	}

	public static Double[] applyBorne(Double[] data_tab, String cname, int nb_data) {
		Double[] tab = data_tab.clone();
			for (int j = 0; j < nb_data; ++j) {
				if (tab[j] < MapBorne.get(cname).get("Min"))
					tab[j] = MapBorne.get(cname).get("Min");
				if (tab[j] > MapBorne.get(cname).get("Max"))
					tab[j] = MapBorne.get(cname).get("Max");
				if (j > 0) {
					Double diff = Math.abs(tab[j-1] - tab[j]);
					if (diff > MapBorne.get(cname).get("Ecart")) {
						tab[j] = interpolation(tab, j, nb_data);
					}
				}
		}
		return tab;
	}

	public static Double interpolation(Double[] data, int indice, int taille) {
		int range = 10;
		int indmin = indice - range > 0 ? indice - range : 0;
		int indmax = indice + range < taille - 1 ? indice + range : taille - 1;
		Double val = 0.0;
		Double diviseur = 0.0;
		Double coeff;
		for (int i = indmin; i <= indmax; i++) {
			if(i!=indice){
			coeff = 1.0 / Math.abs(indice - i);
			diviseur += coeff;
			val += coeff * data[i];
			}
		}
		return val / diviseur;
	}

	public static Map<String, Map<String, Double>> calcul_stat(Map<String, Double[]> data_tab, String[] cols_name,
			int nb_data, int nb_cols) {
		Map<String, Map<String, Double>> stat = new HashMap<String, Map<String, Double>>();
		for (int i = 0; i < nb_cols; ++i) {
			String cname = cols_name[i];
			Map<String, Double> statKey = new HashMap<String, Double>();
			statKey.put("Min", Double.POSITIVE_INFINITY);
			statKey.put("Max", Double.NEGATIVE_INFINITY);
			statKey.put("Moy", 0.0);
			// statKey.put("Med", 0.0);
			statKey.put("Ecart", 0.0);
			statKey.put("MinDiff", Double.POSITIVE_INFINITY);
			statKey.put("MaxDiff", Double.NEGATIVE_INFINITY);
			statKey.put("MoyDiff", 0.0);
			statKey.put("EcartDiff", 0.0);
			// statKey.put("MedDiff", 0.0);
			if (stat.get(cname) == null)
				stat.put(cname, statKey);
			for (int j = 0; j < nb_data; ++j) {
				double val = data_tab.get(cname)[j];
				stat.get(cname).put("Min", Math.min(stat.get(cname).get("Min"), val));
				stat.get(cname).put("Max", Math.max(stat.get(cname).get("Max"), val));
				stat.get(cname).put("Moy", stat.get(cname).get("Moy") + val);
				if (j > 0) {
					double diff = Math.abs(data_tab.get(cname)[j - 1] - data_tab.get(cname)[j]);
					stat.get(cname).put("MinDiff", Math.min(stat.get(cname).get("MinDiff"), diff));
					stat.get(cname).put("MaxDiff", Math.max(stat.get(cname).get("MaxDiff"), diff));
					stat.get(cname).put("MoyDiff", stat.get(cname).get("MoyDiff") + diff);
				}
			}
			stat.get(cname).put("Moy", stat.get(cname).get("Moy") / nb_data);
			stat.get(cname).put("MoyDiff", stat.get(cname).get("MoyDiff") / (nb_data - 1));
			for (int j = 0; j < nb_data; ++j) {
				double val = Math.abs(stat.get(cname).get("Moy") - data_tab.get(cname)[j]);
				stat.get(cname).put("Ecart", stat.get(cname).get("Ecart") + val);
				if (j > 0) {
					double diff = Math.abs(stat.get(cname).get("MoyDiff")
							- Math.abs(data_tab.get(cname)[j - 1] - data_tab.get(cname)[j]));
					stat.get(cname).put("EcartDiff", stat.get(cname).get("EcartDiff") + diff);
				}
			}
			stat.get(cname).put("Ecart", stat.get(cname).get("Ecart") / nb_data);
			stat.get(cname).put("EcartDiff", stat.get(cname).get("EcartDiff") / (nb_data - 1));
		}
		return stat;
	}

	public static Integer[] keys_to_array(Map<Integer, Map<String, Double>> data, int nb_data) {
		Integer keys[] = data.keySet().toArray(new Integer[nb_data]);
		Arrays.sort(keys);
		return keys;
	}

	public static int get_nb_cols(Map<Integer, Map<String, Double>> data, int key) {
		return data.get(key).keySet().size();
	}

	public static String[] cols_name_to_array(Map<Integer, Map<String, Double>> data, int key, int nb_cols) {
		return data.get(key).keySet().toArray(new String[nb_cols]);
	}

	public static boolean brumisateur_used(Map<String, Double> data_instant_t) {
		String[] cnames = { "Z_01", "Z_02", "Z_03", "Z_04", "Z_05" };

		for (int i = 0; i < cnames.length; ++i) {
			String cname = cnames[i];
			try {
				if (data_instant_t.get(cname) > 0)
					return true;
			} catch (Exception e) {
			}
		}
		return false;
	}

	public static void separation_data_brumisateur(Map<Integer, Map<String, Double>> data,
			Map<Integer, Map<String, Double>> data_ba, // bruimisateur
			// activé
			Map<Integer, Map<String, Double>> data_bd // bruimisateur
			// désactivé
			) {
		int nb_data = data.size();
		Integer[] keys = keys_to_array(data, nb_data);

		for (int i = 0; i < nb_data; ++i) {
			int key = keys[i];
			Map<String, Double> data_instant_t = data.get(key);
			if (brumisateur_used(data_instant_t))
				data_ba.put(key, data_instant_t);
			else
				data_bd.put(key, data_instant_t);
		}
	}

	public static void separation_data_jour_nuit(Map<Integer, Map<String, Double>> data,
			Map<Integer, Map<String, Double>> data_jour, // jour
			Map<Integer, Map<String, Double>> data_nuit // nuit
			) {
		int nb_data = data.size();
		Integer[] keys = keys_to_array(data, nb_data);
		for (int i = 0; i < nb_data; ++i) {

			if (enJournee(keys[i])) {
				data_jour.put(keys[i], data.get(keys[i]));
			} else {
				data_nuit.put(keys[i], data.get(keys[i]));
			}
		}
	}

	public static boolean enJournee(int timestamp) {
		int FinHeureJour = Integer.parseInt(limJour.substring(0, 2));
		int FinMinuteJour = Integer.parseInt(limJour.substring(3, 5));
		int FinSecondeJour = Integer.parseInt(limJour.substring(6, 8));
		int FinHeureNuit = Integer.parseInt(limNuit.substring(0, 2));
		int FinMinuteNuit = Integer.parseInt(limNuit.substring(3, 5));
		int FinSecondeNuit = Integer.parseInt(limNuit.substring(6, 8));
		String sd = new SimpleDateFormat("HH:mm:ss").format(new Date(timestamp * 1000));
		int Heure = Integer.parseInt(sd.substring(0, 2));
		int Minute = Integer.parseInt(sd.substring(3, 5));
		int Seconde = Integer.parseInt(sd.substring(6, 8));

		if (Heure < FinHeureJour && Heure > FinHeureNuit)
			return true;
		else if (Heure == FinHeureJour) {
			if (Minute < FinMinuteJour)
				return true;
			else if (Minute == FinMinuteJour) {
				if (Seconde < FinSecondeJour)
					return true;
			}
		} else if (Heure == FinHeureNuit) {
			if (Minute > FinMinuteNuit)
				return true;
			else if (Minute == FinMinuteNuit) {
				if (Seconde > FinSecondeNuit)
					return true;
			}
		}
		return false;
	}

	public static Map<Integer, Map<String, Double>> tab_to_HashMap(Map<String, Double[]> data, Integer[] keys,
			String[] cols_name, int nb_data, int nb_cols) {
		Map<Integer, Map<String, Double>> new_data = new HashMap<Integer, Map<String, Double>>();

		for (int i = 0; i < nb_data; ++i) {
			int key = keys[i];
			Map<String, Double> map_instant_t = new HashMap<String, Double>();

			for (int j = 0; j < nb_cols; ++j) {
				String cname = cols_name[j];
				map_instant_t.put(cname, data.get(cname)[i]);
			}

			new_data.put(key, map_instant_t);
		}
		return new_data;
	}

	public static boolean string_in_array(String[] tab, String str) {
		for (int i = 0; i < tab.length; ++i) {
			if (str.equals(tab[i]))
				return true;
		}
		return false;
	}

	public static Map<Integer, Map<String, Double>> remove_cols(Map<Integer, Map<String, Double>> data,
			String[] cols_to_remove) {
		int nb_data = data.size();
		Integer keys[] = keys_to_array(data, nb_data);

		int nb_cols = get_nb_cols(data, keys[0]);
		String cols_name[] = cols_name_to_array(data, keys[0], nb_cols);
		int nb_new_cols = nb_cols - cols_to_remove.length;
		String new_cols_name[] = new String[nb_new_cols];

		Map<String, Double[]> tab_data = hashmap_to_tab(data, keys, cols_name, nb_data, nb_cols);
		Map<String, Double[]> new_tab_data = new HashMap<String, Double[]>();

		int compteur = 0;
		for (int i = 0; i < nb_cols; ++i) {
			String cname = cols_name[i];
			if (!string_in_array(cols_to_remove, cname)) {
				new_tab_data.put(cname, tab_data.get(cname));
				new_cols_name[compteur] = cname;
				compteur += 1;
			}
		}
		return tab_to_HashMap(new_tab_data, keys, new_cols_name, nb_data, nb_new_cols);
	}

}
