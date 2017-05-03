package simulator;
import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import javax.print.DocFlavor.STRING;

import org.omg.Messaging.SyncScopeHelper;

import java.text.*;

/**
 * Ce fichier contient les m�thode permettant de cr�er les fichiers de donn�es
 * qui sont utilis�s par Weka
 **/
public class OpenData {
	public static void main(String[] args) throws Exception {
		if (args.length != 1) {
			System.out.println("Usage : <file_name_out>");
			System.exit(1);
		}
		try {
			FileInputStream fis = new FileInputStream("data/hashmap.ser");
			ObjectInputStream ois = new ObjectInputStream(fis);
			@SuppressWarnings("unchecked")
			HashMap<Integer, Map<String, Double>> data = (HashMap<Integer, Map<String, Double>>) ois
					.readObject();
			ois.close();

			/**List<String> parametres = input_parameters();
			parse_and_write(args[0], data, parametres);*/
			HashMap<Integer, Map<String, Double>> ba = new HashMap<Integer, Map<String, Double>>();
			HashMap<Integer, Map<String, Double>> bd = new HashMap<Integer, Map<String, Double>>();
			separation_data_brumisateur(data, ba, bd);

		} catch (IOException ioe) {
			System.out.println(ioe);
		}

	}

	/**
	 * Cr�� un fichier ARFF contenant les donn�es contenues dans le fichier
	 * pass� en param�tre.
	 * 
	 * @param dataPath
	 *            chemin du fichier .ser contenant les donn�es
	 * @param file_name
	 *            Nom du fichier .arff qui va �tre cr��
	 * @param colname
	 *            Nom de la colonne qui sera
	 * @param linear
	 *            Si true, les donn�es vont �tre filtr�es
	 * @throws Exception
	 */
	public static void createARFF(String dataPath, String file_name,
			String colname, boolean linear) throws Exception {

		FileInputStream fis = new FileInputStream(dataPath);
		ObjectInputStream ois = new ObjectInputStream(fis);
		@SuppressWarnings("unchecked")
		HashMap<Integer, Map<String, Double>> data = (HashMap<Integer, Map<String, Double>>) ois
				.readObject();
		ois.close();

		List<String> parametres = input_parameters(colname);
		if (linear) {
			data = filtre_passe_bas_data(data, parametres);
		}
		parse_and_write(file_name, data, parametres);

	}
	
	public static void createARFF(String dataPath, String file_name,
			String colname, boolean linear, int[] filtre) throws Exception {

		FileInputStream fis = new FileInputStream(dataPath);
		ObjectInputStream ois = new ObjectInputStream(fis);
		@SuppressWarnings("unchecked")
		HashMap<Integer, Map<String, Double>> data = (HashMap<Integer, Map<String, Double>>) ois
				.readObject();
		ois.close();

		List<String> parametres = input_parameters(colname);
		if (linear) {
			data = filtre_passe_bas_data(data, parametres, filtre);
		}
		parse_and_write(file_name, data, parametres);

	}
	
	public static void createARFF(String dataPath, String file_name,
			String colname, boolean linear, String filtre_name) throws Exception {

		FileInputStream fis = new FileInputStream(dataPath);
		ObjectInputStream ois = new ObjectInputStream(fis);
		@SuppressWarnings("unchecked")
		HashMap<Integer, Map<String, Double>> data = (HashMap<Integer, Map<String, Double>>) ois
				.readObject();
		ois.close();

		List<String> parametres = input_parameters(colname);
		if (linear) {
			if(filtre_name.equals("filtre_butterworth")){
				data = filtrage_butterworth(data, parametres);
			}
			else if(filtre_name.equals("filtre_circuit_RC")){
				data = filtrage_circuit_RC(data, parametres);
			}
			else if(filtre_name.equals("lissage")){
				lissage_courbe(data);
			}
			else if(filtre_name.equals("test_separation")){
				HashMap<Integer, Map<String, Double>> ba = new HashMap<Integer, Map<String, Double>>();
				HashMap<Integer, Map<String, Double>> bd = new HashMap<Integer, Map<String, Double>>();
				separation_data_brumisateur(data, ba, bd);
			}
		}
		parse_and_write(file_name, data, parametres);

	}

	/**
	 * Cr�� un fichier ARFF contenant les donn�es contenues dans le fichier
	 * pass� en param�tre.
	 * 
	 * @param data
	 *            HashMap contenant les donn�es
	 * @param file_name
	 *            Nom du fichier .arff qui va �tre cr��
	 * @param colname
	 *            Nom de la colonne qui sera
	 * @param linear
	 *            Si true, les donn�es vont �tre filtr�es
	 * @throws Exception
	 */
	public static void createARFF(HashMap<Integer, Map<String, Double>> data,
			String file_name, String colname, boolean linear) throws Exception {

		List<String> parametres = input_parameters(colname);
		if (linear) {
			data = filtre_passe_bas_data(data, parametres);
		}
		parse_and_write(file_name, data, parametres);

	}

	/**
	 * @YANNIS TODO
	 * @return
	 */
	public static List<String> input_parameters() {
		List<String> parametre = new ArrayList<String>();
		parametre.add("TIME");
		parametre.add("CPT_DEBI");
		parametre.add("TEMPERATURE");
		parametre.add("DRAI");
		parametre.add("PYRA");
		parametre.add("VENT");
		parametre.add("TAIR");
		// parametre.add("RAIN");
		parametre.add("Z_01");
		parametre.add("Z_02");
		parametre.add("Z_03");
		parametre.add("Z_04");
		parametre.add("Z_05");
		parametre.add("L_W1");
		parametre.add("T_W1");
		parametre.add("T_04");
		parametre.add("T_03");
		parametre.add("T_02");
		parametre.add("T_01");
		parametre.add("T_05");
		parametre.add("WSPD");
		parametre.add("CONDITION");
		parametre.add("UP01");
		parametre.add("PMP1");
		// parametre.add("P_01");
		// parametre.add("P_02");
		// parametre.add("P_03");
		parametre.add("RENDEMENT");
		for (int i = 0; i < parametre.size(); ++i) {
			System.out.println(i + " : " + parametre.get(i));
		}

		BufferedReader console = new BufferedReader(new InputStreamReader(
				System.in));
		System.out
				.println("Donnez les param�tres souhait�s : nb1 nb2 nb3 nb4 ...\n");
		String input;
		try {
			input = console.readLine();
		} catch (IOException e) {
			System.out
					.println("Un probl�me est survenu lors de la saisie. Ordre par d�fault utilis�");
			input = "1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23";
		}

		String[] p = input.split(" ");
		List<String> retour = new ArrayList<String>();
		for (int i = 0; i < p.length; ++i) {
			retour.add(parametre.get(Integer.parseInt(p[i])));
		}
		return retour;
	}

	/**
	 * @YANNIS TODO
	 * @param colname
	 * @return
	 */
	public static List<String> input_parameters(String colname) {
		List<String> parametre = new ArrayList<String>();
		parametre.add("TIME");
		parametre.add("CPT_DEBI");
		parametre.add("TEMPERATURE");
		parametre.add("DRAI");
		parametre.add("PYRA");
		parametre.add("VENT");
		parametre.add("TAIR");
		// parametre.add("RAIN");
		parametre.add("Z_01");
		parametre.add("Z_02");
		parametre.add("Z_03");
		parametre.add("Z_04");
		parametre.add("Z_05");
		parametre.add("L_W1");
		parametre.add("T_W1");
		parametre.add("T_04");
		parametre.add("T_03");
		parametre.add("T_02");
		parametre.add("T_01");
		parametre.add("T_05");
		parametre.add("WSPD");
		parametre.add("CONDITION");
		parametre.add("UP01");
		parametre.add("PMP1");
		// parametre.add("P_01");
		// parametre.add("P_02");
		// parametre.add("P_03");
		parametre.add("RENDEMENT");

		if (parametre.remove(colname))
			parametre.add(colname);

		String input = "1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23";
		String[] p = input.split(" ");
		List<String> retour = new ArrayList<String>();
		for (int i = 0; i < p.length; ++i) {
			retour.add(parametre.get(Integer.parseInt(p[i])));
		}
		return retour;
	}

	/**
	 * @YANNIS TODO
	 * Ecris dans le fichier name_file les données (selectionnées en fonction des paramètres demandés)
	 * de manière à être lisible par weka.

	 * @param name_file : fichier de sortie
	 * @param data : Données sous forme de Map
	 * @param parametre : Les paramètres que l'on souhaite conserver
	 * @throws Exception
	 */
	public static void parse_and_write(String name_file,
			HashMap<Integer, Map<String, Double>> data, List<String> parametre)
			throws Exception {
		// On prépare le fichier dans lequel on écrit

		PrintWriter writer = new PrintWriter("arffFiles/" + name_file, "UTF-8");

		writer.println("@relation SuniAgri");
		writer.println("");

		int nb_parametre = parametre.size();

		for (int i = 0; i < nb_parametre; ++i)
			writer.println("@attribute " + parametre.get(i) + " numeric");

		writer.println("");
		writer.println("@data");

		// for(HashMap.Entry<Integer, Map<String, Double>> entry :
		// data.entrySet())

		for (Entry<Integer, Map<String, Double>> entry : data.entrySet()) {
			String separateur = "";
			for (int i = 0; i < nb_parametre; ++i) {
				if (parametre.get(i) == "TIME") {
					// Parametre TIME
					long time = entry.getKey();
					Date date = new Date(time * 1000);
					DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
					String dateFormatted = formatter.format(date);
					int hours = Integer.parseInt(dateFormatted.substring(0, 2));
					int minutes = Integer.parseInt(dateFormatted
							.substring(3, 5));
					int seconds = Integer.parseInt(dateFormatted
							.substring(6, 8));
					int day_time = hours * 3600 + minutes * 60 + seconds;
					writer.print(day_time);
				} else if (parametre.get(i) == "RENDEMENT") {
					// Parametre RENDEMENT
					try {
						double r = entry.getValue().get("P_01")
								+ entry.getValue().get("P_02")
								+ entry.getValue().get("P_03");
						writer.print(separateur + r);
					} catch (NullPointerException e) {
						writer.print(separateur + "?");
					}
				} else {
					try {
						if (entry.getValue().get(parametre.get(i)) == null) {
							throw new NullPointerException();
						}
						writer.print(separateur
								+ entry.getValue().get(parametre.get(i)));
					} catch (NullPointerException e) {
						writer.print(separateur + "?");
					}
				}
				separateur = ",";
			}
			writer.println("");
		}

		writer.close();

	}

	/**
	 * @YANNIS TODO
	 * @param data
	 * @param param
	 * @return
	 */
	public static HashMap<Integer, Map<String, Double>> filtre_passe_bas_data(HashMap<Integer, Map<String, Double>> data, List<String> param) {
		/* HashMap<Integer, Map<String, Double>> data : données bruitées sous forme de HashMap
		List<String> param : Liste des paramètres demandés */

		//Création du filtre passe bas 
		//int filtre[] = {1, 2, 3, 4, 5, 6, 7, 8, 9, 8, 7, 6, 5, 4, 3, 2, 1}; 470
		//int filtre[] = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}; 578
		//int filtre[] = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 110, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1}; 340
		//int filtre[] = {1, 2, 3, 4, 5, 6, 7, 8, 72, 8, 7, 6, 5, 4, 3, 2, 1}; //281
		//int filtre[] = {1, 2, 3, 4, 5, 6, 7, 8, 36, 8, 7, 6, 5, 4, 3, 2, 1}; //316
		//int filtre[] = {1, 2, 3, 4, 5, 6, 7, 8, 9, 90, 9, 8, 7, 6, 5, 4, 3, 2, 1}; 312
		//int filtre[] = {1, 2, 3, 4, 5, 6, 7, 56, 7, 6, 5, 4, 3, 2, 1}; //298
		//int filtre[] = {1, 2, 3, 4, 5, 6, 7, 8, 100, 8, 7, 6, 5, 4, 3, 2, 1}; //358
		//int filtre[] = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}; //592
		//int filtre[] = {1, 2, 3, 4, 5, 6, 7, 8, 54, 8, 7, 6, 5, 4, 3, 2, 1}; //263
		//int filtre[] = {1, 2, 3, 4, 5, 6, 7, 8, 64, 8, 7, 6, 5, 4, 3, 2, 1}; //261
		int filtre[] = {1, 2, 3, 4, 5, 6, 7, 8, 68, 8, 7, 6, 5, 4, 3, 2, 1}; //250
		//int filtre[] = {1, 2, 3, 4, 5, 6, 7, 8, 70, 8, 7, 6, 5, 4, 3, 2, 1}; //271
		//int filtre[] = {1, 2, 4, 8, 16, 32, 64, 128, 510, 128, 64, 32, 16, 8, 4, 2, 1}; //394
		//int filtre[] = {1, 2, 4, 8, 16, 32, 64, 128, 481, 128, 64, 32, 16, 8, 4, 2, 1};// 392
		//int filtre[] = {1, 2, 4, 8, 16, 32, 64, 128, 256, 128, 64, 32, 16, 8, 4, 2, 1}; //303
		//int filtre[] = {1, 2, 4, 8, 16, 32, 64, 128, 196, 128, 64, 32, 16, 8, 4, 2, 1}; //301
		//int filtre[] = {1, 2, 4, 8, 16, 32, 64, 128, 189, 128, 64, 32, 16, 8, 4, 2, 1}; //301
		//int filtre[] = {1, 2, 4, 8, 16, 32, 64, 128, 128, 128, 64, 32, 16, 8, 4, 2, 1}; //359
		//int filtre[] = {1, 16, 120, 560, 1820, 4368, 8008, 11440, 12870, 11440, 8008, 4368, 1820, 560, 120, 16, 1};//308 Pascal 17
		//int filtre[] = {1, 14, 91, 364, 1001, 2002, 3003, 3432, 3003, 2002, 1001, 364, 91, 14, 1}; //282 pascal 15
		//int filtre[] = {1, 12, 66, 220, 495, 792, 924, 792, 495, 220, 66, 12, 1}; //280 pascal 13
		
		int coef = 0;
		for(int i= 0; i < filtre.length; ++i)
			coef += filtre[i];


		HashMap<Integer, Map<String, Double>> new_data = new HashMap<Integer, Map<String, Double>>();
		int nb_param = param.size();
		int nb_data = data.size();
		//Les données étant stockés dans un map, elles ne sont pas mises dans l'ordre, il faut donc
		//les triées selon le timestamp
		Integer keys[] = data.keySet().toArray(new Integer[nb_data]);
		Arrays.sort(keys);

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
							new_value += data.get(keys[j+k]).get(p)*filtre[k+10];
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
	
	
	public static HashMap<Integer, Map<String, Double>> filtre_passe_bas_data(HashMap<Integer, Map<String, Double>> data, List<String> param, int filtre[]) {
		/* HashMap<Integer, Map<String, Double>> data : données bruitées sous forme de HashMap
		List<String> param : Liste des paramètres demandés 
		int filtre[] : Filtre à utiliser (ex : [1, 2, 3, 6, 3, 2, 1] )*/
		
		
		int coef = 0;
		for(int i= 0; i < filtre.length; ++i)
			coef += filtre[i];


		HashMap<Integer, Map<String, Double>> new_data = new HashMap<Integer, Map<String, Double>>();
		int nb_param = param.size();
		int nb_data = data.size();
		//Les données étant stockés dans un map, elles ne sont pas mises dans l'ordre, il faut donc
		//les triées selon le timestamp
		Integer keys[] = data.keySet().toArray(new Integer[nb_data]);
		Arrays.sort(keys);

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
							new_value += data.get(keys[j+k]).get(p)*filtre[k+10];
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
	
	public static HashMap<Integer, Map<String, Double>> filtrage_butterworth(HashMap<Integer, Map<String, Double>> data, List<String> param){
		int nb_param = param.size();
		int nb_data = data.size();
		
		System.out.println("Début du filtrage par Butter Worth");
		
		//Création du filtre de ButterWorth
		double[] ReFil = new double[nb_data/2];
		double[] ImFil = new double[nb_data/2];
		double c = 100.0; // cutoff frequency
		for (int f = 0; f < nb_data / 2; f++) {
			double w = 2 * Math.PI * f;
		 
			double wc = w/c;
			double gain = 1.0/(1+Math.pow(wc,4));
			double real = 1-Math.pow(wc,2);
			double imag = -Math.sqrt(2)*wc;
		 
			ReFil[f] = real * gain;
			ImFil[f] = imag * gain;
		}

		//Création du tableau de retour
		HashMap<Integer, Map<String, Double>> new_data = new HashMap<Integer, Map<String, Double>>();
		//Les données étant stockés dans un map, elles ne sont pas mises dans l'ordre, il faut donc
		//les triées selon le timestamp
		Integer keys[] = data.keySet().toArray(new Integer[nb_data]);
		Arrays.sort(keys);

		//Pour chaque dimension
		for(int i = 0; i < nb_param; ++i){
			System.out.println("dimension : " + i);
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

				//On considère un paramètre à la fois comme étant un signal
				double[] signal = new double[nb_data];
				for(int j = 0; j < nb_data; ++j){
					try{
						signal[j] = data.get(keys[j]).get(p);
					}
					catch(Exception e){
						//Dans le cas où la données est manquante, on met zéro
						signal[j] = 0;
					}
				}

				//Calcul DFT du signal
				double[] ReSig = new double[nb_data/2];
				double[] ImSig = new double[nb_data/2];	
				DFT(signal, ReSig, ImSig);

				//multiplication signal par filtre (on est dans fourier)
				double[] ReOutput = new double[nb_data/2];
				double[] ImOutput = new double[nb_data/2];
				for(int f=0;f<nb_data/2;f++) {
					ReOutput[f]=ReSig[f]*ReFil[f]-ImSig[f]*ImFil[f];
					ImOutput[f]=ReSig[f]*ImFil[f]+ImSig[f]*ReFil[f];
				}
				 
				//inverse de fourier pour obtenir le signal résultat
				double[] output = new double[nb_data];
				invDFT(ReOutput, ImOutput, output);

				//On récopie le signal dans new_data
				for(int j = 0; j < nb_data; ++j){
					try{
						new_data.get(keys[j]).put(p, output[j]);
					}
					catch(Exception e){
						new_data.put(keys[j], new HashMap<String, Double>());
						new_data.get(keys[j]).put(p, output[j]);
					}
				}
			}
		}
		System.out.println("Fin filtrage");
		return new_data;
	}

	public static HashMap<Integer, Map<String, Double>> filtrage_circuit_RC(HashMap<Integer, Map<String, Double>> data, List<String> param){
		int nb_param = param.size();
		int nb_data = data.size();
		
		//Création du filtre de ButterWorth
		double[] ReFil = new double[nb_data/2];
		double[] ImFil = new double[nb_data/2];
		 
		double rc = 1.0/40;
		for(int f=0;f<(nb_data/2);f++) {
			double w = 2*Math.PI*f;
			double wrc = w*rc;
		 
			double gain = 1.0/(1+Math.pow(wrc,2));
			double real = 1;
			double imag = -wrc;
		 
			ReFil[f]=real*gain;
			ImFil[f]=imag*gain;
		}

		//Création du tableau de retour
		HashMap<Integer, Map<String, Double>> new_data = new HashMap<Integer, Map<String, Double>>();
		//Les données étant stockés dans un map, elles ne sont pas mises dans l'ordre, il faut donc
		//les triées selon le timestamp
		Integer keys[] = data.keySet().toArray(new Integer[nb_data]);
		Arrays.sort(keys);

		//Pour chaque dimension
		for(int i = 0; i < nb_param; ++i){
			System.out.println("dimension : " + i);
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

				//On considère un paramètre à la fois comme étant un signal
				double[] signal = new double[nb_data];
				for(int j = 0; j < nb_data; ++j){
					try{
						signal[j] = data.get(keys[j]).get(p);
					}
					catch(Exception e){
						//Dans le cas où la données est manquante, on met zéro
						signal[j] = 0;
					}
				}

				//Calcul DFT du signal
				double[] ReSig = new double[nb_data/2];
				double[] ImSig = new double[nb_data/2];	
				DFT(signal, ReSig, ImSig);

				//multiplication signal par filtre (on est dans fourier)
				double[] ReOutput = new double[nb_data/2];
				double[] ImOutput = new double[nb_data/2];
				for(int f=0;f<nb_data/2;f++) {
					ReOutput[f]=ReSig[f]*ReFil[f]-ImSig[f]*ImFil[f];
					ImOutput[f]=ReSig[f]*ImFil[f]+ImSig[f]*ReFil[f];
				}
				 
				//inverse de fourier pour obtenir le signal résultat
				double[] output = new double[nb_data];
				invDFT(ReOutput, ImOutput, output);

				//On récopie le signal dans new_data
				for(int j = 0; j < nb_data; ++j){
					try{
						new_data.get(keys[j]).put(p, output[j]);
					}
					catch(Exception e){
						new_data.put(keys[j], new HashMap<String, Double>());
						new_data.get(keys[j]).put(p, output[j]);
					}
				}
			}
		}
		System.out.println("Fin du filtrage");
		return new_data;
	}
	
	/**
	 * Compute the DFT of real data contained in input[] 
	 * The result is stored in rDFT[] and iDFT[]
	 * 
	 * @param input array of size N containing the data
	 * @param rDFT empty array of size N/2 (real part of the DFT)
	 * @param iDFT empty array of size N/2 (imaginary part of the DFT)
	 */
	public static void DFT(double[] input, double[] rDFT, double[] iDFT) {
		int N = input.length;
		for (int f = 0; f < N/2; f++) {
			rDFT[f] = 0;
			iDFT[f] = 0;
			for (int i = 0; i < N; i++) {
				double w = 2 * Math.PI * (double) i / N;
				rDFT[f] += input[i] * Math.cos(f * w);
				iDFT[f] -= input[i] * Math.sin(f * w);
			}
			rDFT[f] /= N/2;
			iDFT[f] /= N/2;
		}
	}

		/**
	 * Compute the inverse DFT of complex data contained in rDFT[] and iDFT[] 
	 * The result is stored in output[]
	 * 
	 * @param rDFT array of size N/2 containing the real part of the data
	 * @param iDFT array of size N/2 containing the real part of the data
	 * @param output empty array of size N
	 */
	public static void invDFT(double[] rDFT, double[] iDFT, double[] output) {

		int N = output.length;
		for (int i = 0; i < N; i++) {
			output[i] = 0;
			for (int f = 0; f < N/2; f++) {
				double w = 2 * Math.PI * (double) i / N;
				output[i] += rDFT[f] * Math.cos(f * w) - iDFT[f] * Math.sin(f * w);
			}
		}
	}
	
	
	/* Recopie nos données dans une map :
	 * HashMap<String, Double[]>
	 * où String = nom du paramètre
	 * et Double[] = tableau de valeur pour le paramètre String
	 */
	public static Map<String, Double[]> hashmap_to_tab(HashMap<Integer, Map<String, Double>> data, Integer[] keys, String[] cols_name, int nb_data, int nb_cols){
		Map<String, Double[]> new_data = new HashMap<String, Double[]>();
		
		for(int i=0; i < nb_data; ++i){
			for(int j=0; j < nb_cols; ++j){
				String cname = cols_name[j];
				if(new_data.get(cname) == null)
					new_data.put(cname, new Double[nb_data]);
				double tmp;
				try{
					tmp = data.get(keys[i]).get(cname);
				}
				catch(Exception e){
					tmp = 0;
				}
				new_data.get(cname)[i] = tmp;
			}
		}
		return new_data;
	}
	
	public static Map<String, Double> calcul_max_diff(Map<String, Double[]> data_tab, String[] cols_name, int nb_data, int nb_cols){
		Map<String, Double> max_diff = new HashMap<String, Double>();
		
		for(int i = 0; i < nb_cols; ++i){
			String cname = cols_name[i]; 
			if(max_diff.get(cname) == null)
				max_diff.put(cname, 0.);
			for(int j = 1; j < nb_data; ++j){
				double diff = data_tab.get(cname)[j] - data_tab.get(cname)[j-1];
				diff = (diff < 0) ? -diff : diff;
				if(max_diff.get(cname) < diff)
					max_diff.put(cname, diff);
			}
		}
		
		return max_diff;
	}
	
	public static Integer[] keys_to_array(HashMap<Integer, Map<String, Double>> data, int nb_data){
		Integer keys[] = data.keySet().toArray(new Integer[nb_data]);
		Arrays.sort(keys);
		return keys;
	}
	
	public static int get_nb_cols(HashMap<Integer, Map<String, Double>> data, int key){
		return data.get(key).keySet().size();
	}
	
	public static String[] cols_name_to_array(HashMap<Integer, Map<String, Double>> data, int key, int nb_cols){
		return data.get(key).keySet().toArray(new String[nb_cols]);
	}
	
	public static Map<String, Double> calcul_moyenne(Map<String, Double[]> data, String[] cols_name, int nb_data, int nb_cols){
		Map<String, Double> moyenne = new HashMap<String, Double>();
		
		for(int j = 0; j < nb_cols; ++j){
			String cname = cols_name[j];
			if(moyenne.get(cname) == null)
				moyenne.put(cname, 0.);
			for(int i = 0; i < nb_data; ++i){
				moyenne.put(cname, moyenne.get(cname) + data.get(cname)[i]);
			}
		}
		
		for(int j = 0; j < nb_cols; ++j){
			String cname = cols_name[j];
			moyenne.put(cname, moyenne.get(cname) / nb_data);
		}
		
		return moyenne;
	}
	
	public static Map<String, Double> calcul_ecart_type(Map<String, Double[]> data, String[] cols_name, int nb_data, int nb_cols){
		Map<String, Double> ecart_type = new HashMap<String, Double>();
		Map<String, Double> moyenne = calcul_moyenne(data, cols_name, nb_data, nb_cols);
		
		for(int j = 0; j < nb_cols; ++j){
			String cname = cols_name[j];
			double cmoyenne = moyenne.get(cname);
			double s = 0;
			for(int i = 0; i < nb_data; ++i){
				s += Math.abs(cmoyenne - data.get(cname)[i]);
			}
			ecart_type.put(cname, s / nb_data);
		}
		
		return ecart_type;
	}
	
	public static Map<String, Double> calcul_variance_moyenne(Map<String, Double[]> data, String[] cols_name, int nb_data, int nb_cols){
		Map<String, Double> variance_moyenne = new HashMap<String, Double>();
		
		for(int j = 0; j < nb_cols; ++j){
			String cname = cols_name[j];
			double s = 0;
			for(int i = 1; i < nb_data; ++i){
				s += Math.abs(data.get(cname)[i] - data.get(cname)[i-1]);
			}
			variance_moyenne.put(cname, s / (nb_data-1));
		}
		
		return variance_moyenne;
	}
	
	public static Map<String, Double> calcul_ecart_type_variance(Map<String, Double[]> data, String[] cols_name, int nb_data, int nb_cols){
		Map<String, Double> ecart_type_variance = new HashMap<String, Double>();
		Map<String, Double> variance_moyenne = calcul_variance_moyenne(data, cols_name, nb_data, nb_cols);
		
		for(int j = 0; j < nb_cols; ++j){
			String cname = cols_name[j];
			double cmoyenne = variance_moyenne.get(cname);
			double s = 0;
			for(int i = 1; i < nb_data; ++i){
				double diff = Math.abs(data.get(cname)[i] - data.get(cname)[i-1]);
				s += Math.abs(diff - cmoyenne);
			}
			ecart_type_variance.put(cname, s / (nb_data-1));
		}
		
		return ecart_type_variance;
	}
	
	/* On lisse la courbe en vérifiant ajustant les croissances ou décroissance de courbe trop grande */
	public static void lissage_courbe(HashMap<Integer, Map<String, Double>> data){
		int nb_data = data.size(); 
				
		Integer keys[] = keys_to_array(data, nb_data);
		int nb_cols = get_nb_cols(data, keys[0]);
		String[] cols_name = cols_name_to_array(data, keys[0], nb_cols);
		
		Map<String, Double[]> data_tab = hashmap_to_tab(data, keys, cols_name, nb_data, nb_cols);
		Map<String, Double> max_diff = calcul_max_diff(data_tab, cols_name, nb_data, nb_cols);
		Map<String, Double> moyenne = calcul_moyenne(data_tab, cols_name, nb_data, nb_cols);
		Map<String, Double> ecart_type = calcul_ecart_type(data_tab, cols_name, nb_data, nb_cols);
		Map<String, Double> variance_moyenne = calcul_variance_moyenne(data_tab, cols_name, nb_data, nb_cols);
		Map<String, Double> ecart_type_variance = calcul_ecart_type_variance(data_tab, cols_name, nb_data, nb_cols);
		
		//Print le résultat
//		for(int i = 0; i < nb_cols; ++i){
//			String cname = cols_name[i];
//			double res = max_diff.get(cname);
//			System.out.println(cname + " : " + res);
//		}
		System.out.println("Variance Max :");
		System.out.println(max_diff);
		System.out.println("Moyenne :");
		System.out.println(moyenne);
		System.out.println("Ecart type :");
		System.out.println(ecart_type);
		System.out.println("Variance Moyenne :");
		System.out.println(variance_moyenne);
		System.out.println("Ecart Type de la Variance");
		System.out.println(ecart_type_variance);
		
	}
	
	public static boolean brumisateur_used(Map<String, Double> data_instant_t){
		String[] cnames = {"Z_01", "Z_02", "Z_03", "Z_04", "Z_05"};
		int cmp = 0;
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
	
	public static void separation_data_brumisateur(
			HashMap<Integer, Map<String, Double>> data,
			HashMap<Integer, Map<String, Double>> data_ba, //bruimisateur activé
			HashMap<Integer, Map<String, Double>> data_bd //bruimisateur désactivé
			){
		int nb_data = data.size();
		Integer[] keys = keys_to_array(data, nb_data);
		String[] cnames = {"Z_01", "Z_02", "Z_03", "Z_04", "Z_05"};
		
		for(int i = 0; i < nb_data; ++i){
			int key = keys[i];
			Map<String, Double> data_instant_t = data.get(key);
			if(brumisateur_used(data_instant_t))
				data_ba.put(key, data_instant_t);
			else
				data_bd.put(key, data_instant_t);
		}
		
		System.out.println(data_ba);
		System.out.println("\n\n\n\n\n");
		System.out.println(data_bd);
	}
	
	public static HashMap<Integer, Map<String, Double>> tab_to_HashMap(Map<String, Double[]> data, Integer[] keys, String[] cols_name, int nb_data, int nb_cols){
		HashMap<Integer, Map<String, Double>> new_data = new HashMap<Integer, Map<String, Double>>();

		for(int i = 0; i < nb_data; ++i){
			int key = keys[i];
			HashMap<String, Double> map_instant_t = new HashMap<String, Double>();

			for(int j = 0; j < nb_cols; ++j){
				String cname = cols_name[j];
				map_instant_t.put(cname, data.get(cname)[i]);
			}

			new_data.put(key, map_instant_t);
		}
		return new_data;
	}

	public static boolean string_in_array(String[] tab, String str){
		for(int i = 0; i < tab.length; ++i){
			if(str.equals(tab[i]))
				return true;
		}
		return false;
	}


	public static HashMap<Integer, Map<String, Double>> remove_cols(HashMap<Integer, Map<String, Double>> data, String[] cols_to_remove){
		int nb_data = data.size();
		Integer keys[] = keys_to_array(data, nb_data);
		
		int nb_cols = get_nb_cols(data, keys[0]);
		String cols_name[] = cols_name_to_array(data, keys[0], nb_cols);
		int nb_new_cols = nb_cols - cols_to_remove.length;
		String new_cols_name[] = new String[nb_new_cols];
				
		Map<String, Double[]> tab_data = hashmap_to_tab(data, keys, cols_name, nb_data, nb_cols);
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

		return tab_to_HashMap(new_tab_data, keys, new_cols_name, nb_data, nb_new_cols);
	}
}