package simulator;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DataWork {

	public static final String pathConfig = "./SolarPanelSimulator/data/linearisation.config";
	public static String limJour;
	public static String limNuit;
	public static final Map<String, Map<String, Double>> MapBorne;
	static {
		MapBorne = Collections.unmodifiableMap(config());
	}

	/** Fonction permettant de configurer les attributs de la classe
	 * 	 * @return Map des paramatres
	 */
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

	/** Affiche les données par colonnes
	 * @param data
	 * @param file_name_out
	 */
	public static void print_data(Map<Integer, Map<String, Double>> data, String file_name_out){
		int nb_data = data.size();
		Integer keys[] = keys_to_array(data, nb_data);	
		int nb_cols = get_nb_cols(data, keys[0]);
		String cols_name[] = cols_name_to_array(data, keys[0], nb_cols);
		
		Map<String, Double[]> tab_data = map_to_tab(data, keys, cols_name, nb_data, nb_cols);		

		PrintWriter writer;
		try {
			writer = new PrintWriter(file_name_out, "UTF-8");
			for(int i = 0; i < nb_cols; ++i){
				String cname = cols_name[i];
				String separateur = "";
				writer.println(cname);
				for(int j = 0; j < nb_data; ++j){
					writer.print(separateur);
					writer.print(tab_data.get(cname)[j]);
					if(separateur == "")
						separateur = ",";
				}
				writer.print("\n");
			}
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Prends des données sous forme Map<Integer, Map<String, Double>> et les retourne sous forme de 
	 * Map<String, Double[]>. Dans cette nouvelle forme, les clés (Timestamps) ne sont pas retranscrite.
	 * En revanche, les tableaux liés à chaque paramètre (clé de la map retournée) sont triés dans l'ordre
	 * des timestamps.
	 * @param data 
	 * @param keys
	 * @param cols_name
	 * @param nb_data
	 * @param nb_cols
	 * @return
	 */
	public static Map<String, Double[]> map_to_tab(Map<Integer, Map<String, Double>> data, Integer[] keys,
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

	/** fonction permettant d'effectuer
	 *  le lissage des donnees a partir de attributs de la classe
	 * @param data
	 * @return data lisser
	 */
	public static Map<Integer, Map<String, Double>> lissage_data(Map<Integer, Map<String, Double>> data) {
		int nb_data = data.size();
		Integer keys[] = keys_to_array(data, nb_data);

		int nb_cols = get_nb_cols(data, keys[0]);
		String cols_name[] = cols_name_to_array(data, keys[0], nb_cols);

		Map<String, Double[]> data_tab = map_to_tab(data, keys, cols_name, nb_data, nb_cols);

		for (int j = 0; j < nb_cols; ++j) {
			String cname = cols_name[j];
			data_tab.put(cname, applyBorne(data_tab.get(cname), cname, nb_data));
		}

		return tab_to_Map(data_tab, keys, cols_name, nb_data, nb_cols);
	}

	/**Permet d'appliquer de corriger les donnees a la cle en entree 
	 * @param data_tab
	 * @param cname
	 * @param nb_data
	 * @return donnee modifier
	 */
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

	/** Permet d'interpoler la valeur a partir des 10 valeurs precedentes et suivantes
	 * @param data
	 * @param indice
	 * @param taille
	 * @return valeur interpoler
	 */
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

	/**Fonction permettant d'analyser les donnees afin de personaliser le fichier pathconfig
	 * @param data_tab
	 * @param cols_name
	 * @param nb_data
	 * @param nb_cols
	 * @return Map qui relie a chaque cles de donnee ces statistique selon les cles suivantes:
	 *  "Min" valeur min
	 *	"Max" valeur max
	 *  "Moy" valeur moyenne
	 *	"Ecart" ecart-type
	 *  "MinDiff" min difference entre deux valeurs
	 *	"MaxDiff" max difference entre deux valeurs
	 *	"MoyDiff" moy des differences entre deux valeurs
	 *	"EcartDiff"ecart-type des differences entre deux valeurs
	 */
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

		/** Retourne sous forme de tableau d'entier toutes les clés (timestamps) de la Map data.
	 * Le tableau est alors trié dans l'ordre.
	 * @param data
	 * @param nb_data
	 * @return
	 */
	public static Integer[] keys_to_array(Map<Integer, Map<String, Double>> data, int nb_data){
		Integer keys[] = data.keySet().toArray(new Integer[nb_data]);
		Arrays.sort(keys);
		return keys;
	}
	/** Retourne le nombre de colonne que contiennent les Map<String, Double> dans data.
	 * (En supposant que chacune de ces map contient le même nombre de colonnes).
	 * @param data
	 * @param key
	 * @return
	 */
	public static int get_nb_cols(Map<Integer, Map<String, Double>> data, int key){
		return data.get(key).keySet().size();
	}
	/** Retourne le nom de chacune des colonnes de data. Ou autrement dit, les clés des Map<String, Double>>
	 * contenues dans data.
	 * @param data
	 * @param key : Une clé de la Map data. (Souvent obtenu grâce à keys[0], keys étant le tableau obtenu
	 * par la méthode keys_to_array()
	 * @param nb_cols
	 * @return
	 */
	public static String[] cols_name_to_array(Map<Integer, Map<String, Double>> data, int key, int nb_cols){
		return data.get(key).keySet().toArray(new String[nb_cols]);
	}

	/** Retourne true si un brumisateur est activé dans data_instant_t, false sinon
	 * @param data_instant_t
	 * @return
	 */
	public static boolean brumisateur_used(Map<String, Double> data_instant_t){
		String[] cnames = {"Z_01", "Z_02", "Z_03", "Z_04", "Z_05"};

		for(int i = 0; i < cnames.length; ++i){
			String cname = cnames[i];
			try{
				if(data_instant_t.get(cname) > 0)
					return true;
			}
			catch(Exception e){}
		}
		return false;
	}
	/** Sépare les données contenu dans data en deux différentes bases. Les sorties sont data_ba et data_bd.
	 * Dans data_ba on retrouve les données où le brumisateur est activé, 
	 * dans data_bd les données où le brumisateur est désactivé.
	 * @param data
	 * @param data_ba : Nouvelle Map vide
	 * @param data_bd : Nouvelle Map vide
	 */
	public static void separation_data_brumisateur(
			Map<Integer, Map<String, Double>> data,
			Map<Integer, Map<String, Double>> data_ba, //bruimisateur activ�
			Map<Integer, Map<String, Double>> data_bd //bruimisateur d�sactiv�
			){
		int nb_data = data.size();
		Integer[] keys = keys_to_array(data, nb_data);
		
		for(int i = 0; i < nb_data; ++i){
			int key = keys[i];
			Map<String, Double> data_instant_t = data.get(key);
			if(brumisateur_used(data_instant_t))
				data_ba.put(key, data_instant_t);
			else
				data_bd.put(key, data_instant_t);
		}
	}

	/**Sépare les données contenu dans data en deux différentes bases. Les sorties sont data_ba et data_bd.
	 * Dans data_jour on retrouve les données de jour selon le pathconfig, 
	 * dans data_nuit les données de nuit selon le pathconfig.
	 * @param data
	 * @param data_jour : Nouvelle Map vide
	 * @param data_nuit : Nouvelle Map vide
	 */
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

	/** Verifie si un timestamp correspond a une valeur de jour ou de nuit
	 * @param timestamp
	 * @return boolean
	 */
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

	/** Cette méthode trasforme les données sous forme de Map<String, Double[]> en Map<Integer, Map<String, Double>>.
	 * Les clés pour le Map retourné par la fonction sont alors les clés présentes dans keys.
	 * @param data
	 * @param keys : (Timestamp que l'on obtient avec en extrayant les clés du Map initial grâce à keys_to_array()
	 * @param cols_name
	 * @param nb_data
	 * @param nb_cols
	 * @return
	 */
	public static Map<Integer, Map<String, Double>> tab_to_Map(Map<String, Double[]> data, Integer[] keys, String[] cols_name, int nb_data, int nb_cols){
		Map<Integer, Map<String, Double>> new_data = new HashMap<Integer, Map<String, Double>>();

		for(int i = 0; i < nb_data; ++i){
			int key = keys[i];
			Map<String, Double> map_instant_t = new HashMap<String, Double>();

			for(int j = 0; j < nb_cols; ++j){
				String cname = cols_name[j];
				map_instant_t.put(cname, data.get(cname)[i]);
			}

			new_data.put(key, map_instant_t);
		}
		return new_data;
	}

	
	/** Retourne true si str est dans tab, false sinon
	 * @param tab
	 * @param str
	 * @return
	 */
	public static boolean string_in_array(String[] tab, String str){
		for(int i = 0; i < tab.length; ++i){
			if(str.equals(tab[i]))
				return true;
		}
		return false;
	}


	/** Créer et renvoi une nouvelle Map des données data, où les colonnes appartenant à cols_to_remove sont supprimées. 
	 * @param data
	 * @param cols_to_remove
	 * @return
	 */
	public static Map<Integer, Map<String, Double>> remove_cols(Map<Integer, Map<String, Double>> data, String[] cols_to_remove){
		int nb_data = data.size();
		Integer keys[] = keys_to_array(data, nb_data);
		
		int nb_cols = get_nb_cols(data, keys[0]);
		String cols_name[] = cols_name_to_array(data, keys[0], nb_cols);
		int nb_new_cols = nb_cols - cols_to_remove.length;
		String new_cols_name[] = new String[nb_new_cols];
				
		Map<String, Double[]> tab_data = map_to_tab(data, keys, cols_name, nb_data, nb_cols);
		Map<String, Double[]> new_tab_data = new HashMap<String, Double[]>();

		int compteur = 0;
		for(int i = 0; i < nb_cols; ++i){
			String cname = cols_name[i];
			if(!string_in_array(cols_to_remove, cname)){
				new_tab_data.put(cname, tab_data.get(cname));
				new_cols_name[compteur] = cname;
				compteur += 1;
			}
		}

		return tab_to_Map(new_tab_data, keys, new_cols_name, nb_data, nb_new_cols);
	}
	
	/** Filtre les données contenu dans data, par le filtre
	 * @param data
	 * @param filtre 
	 * @return
	 */
	public static Map<Integer, Map<String, Double>> filtrage_data(Map<Integer, Map<String, Double>> data, int filtre[]) {
		/* Map<Integer, Map<String, Double>> data : données bruitées sous forme de Map
		int filtre[] : Filtre à utiliser (ex : [1, 2, 3, 4, 5, 6, 7, 8, 68, 8, 7, 6, 5, 4, 3, 2, 1] )*/
		
		int nb_data = data.size();
		Integer keys[] = keys_to_array(data, nb_data);
		int nb_param = get_nb_cols(data, keys[0]);
		String cols_name[] = cols_name_to_array(data, keys[0], nb_param);
		
		List<String> param = new ArrayList<String>();
		for(int i = 0; i < nb_param; ++i)
			param.add(cols_name[i]);
		
		int coef = 0;
		for(int i= 0; i < filtre.length; ++i)
			coef += filtre[i];

		Map<Integer, Map<String, Double>> new_data = new HashMap<Integer, Map<String, Double>>();

		//Pour chaque dimension
		for(int i = 0; i < nb_param; ++i){
			//On récupère les paramètres demandés, et on gère les paramètres "multi-colonnes"
			String p = param.get(i);
			List<String> ps = new ArrayList<String>();
			int taille_ps = 0;
			if(p == "RENDEMENT"){
				//Le paramètre rendement correspond à trois paramètres de la map
				//(P_01, P_02, P_03)
				ps.add("P_01");
				ps.add("P_02");
				ps.add("P_03");
				taille_ps = 3;
			}
			else{
				ps.add(p);
				taille_ps = 1;
			}

			//Dans le cas où le paramètre demandé est réparti sur plusieurs paramètre de la map 
			//(exemple le rendement, qui est sur trois paramètre : P_01, P_02, P_03)
			//On tourne sur chacun d'entre eux (la ArrayList ps)
			for(int cmp = 0; cmp < taille_ps; ++cmp){
				p = ps.get(cmp);

				int debut_parcours_data = filtre.length/2;
				int fin_parcours_data = nb_data - filtre.length/2;
				//On parcours les données (on fait gaffe aux extrémités sur lesquelles le filtre 
				//ne peut être appliqué, sinon dépassement de tableau)
				for(int j = debut_parcours_data; j < fin_parcours_data; ++j){
					if(new_data.get(keys[j]) == null){
					//Si new_data n'existe pas encore pour le timestamp j, on le créer
						new_data.put(keys[j], new HashMap<String, Double>());
					}
					double new_value = 0;
					int debut_k = -filtre.length/2;
					int fin_k = filtre.length - filtre.length/2;
					for(int k = debut_k; k < fin_k; ++k){
						//pour tous les k points avant et après, on fait la somme pondérée
						try{
							new_value += data.get(keys[j+k]).get(p)*filtre[k+filtre.length/2];
						}
						catch(Exception e){
							//Dans le cas où la donnée est manquante
							new_value += 0;
						}
					}
					//On calcule la moyenne
					new_value = new_value / coef;
					//On met la moyenne dans new_data
					new_data.get(keys[j]).put(p, new_value);
				}
			}
		}
		return new_data;
	}
}
