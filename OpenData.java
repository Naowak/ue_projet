import java.io.*;
import java.util.*;
import java.text.*;

/** Read the data in hashmap.ser 
**/
public class OpenData {
	public static void main(String[] args) throws Exception{
		if(args.length != 1){
			System.out.println("Usage : <file_name_out>");
			System.exit(1);
		}

			FileInputStream fis = new FileInputStream("hashmap.ser");
			ObjectInputStream ois = new ObjectInputStream(fis);
			@SuppressWarnings("unchecked")
			HashMap<Integer, Map<String, Double>> data = (HashMap<Integer, Map<String, Double>>) ois.readObject();
			ois.close();

			List<String> parametres = input_parameters();
			data = filtre_passe_bas_data(data, parametres);

			try{
				FileOutputStream fos = new FileOutputStream("new_data.ser");
				ObjectOutputStream oos = new ObjectOutputStream(fos);
	          	oos.writeObject(data);
	          	oos.close();
	          	fos.close();
	          }
	        catch(IOException ioe){
	        	ioe.printStackTrace();
	        }

			parse_and_write(args[0], data, parametres);

	}

	public static List<String> input_parameters(){
		List<String> parametre = new ArrayList<String>();
		//parametre.add("TIME");
		parametre.add("CPT_DEBI");
		parametre.add("TEMPERATURE");
		parametre.add("DRAI");
		parametre.add("PYRA");
		parametre.add("VENT");
		parametre.add("TAIR");
		//parametre.add("RAIN");
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
		for(int i = 0; i < parametre.size(); ++i){
			System.out.println(i + " : " + parametre.get(i));
		}

		Console console = System.console();
		String input = console.readLine("Donnez les paramètres souhaités : nb1 nb2 nb3 nb4 ...\n");

		String[] p = input.split(" ");
		List<String> retour = new ArrayList<String>();
		for(int i = 0; i < p.length; ++i){
			retour.add(parametre.get(Integer.parseInt(p[i])));
		}
		return retour;
	}

	public static void parse_and_write(String name_file, HashMap<Integer, Map<String, Double>> data, List<String> parametre) throws Exception{
		// On prépare le fichier dans lequel on écrit
		PrintWriter writer = new PrintWriter(name_file, "UTF-8");
		writer.println("@relation SuniAgri");
		writer.println("");

		int nb_parametre = parametre.size();

		for(int i = 0; i < nb_parametre; ++i)
			writer.println("@attribute " + parametre.get(i) + " numeric");

		writer.println("");
		writer.println("@data");

		for(Map.Entry<Integer, Map<String, Double>> entry : data.entrySet())
		{
			String separateur = "";
			for(int i = 0; i < nb_parametre; ++i)
			{
				if(parametre.get(i) == "TIME"){
					//Parametre TIME
					long time = entry.getKey();					
					Date date = new Date(time*1000);
					DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
					String dateFormatted = formatter.format(date);
					int hours = Integer.parseInt(dateFormatted.substring(0, 2));
					int minutes = Integer.parseInt(dateFormatted.substring(3, 5));
					int seconds = Integer.parseInt(dateFormatted.substring(6, 8));
					int day_time = hours*3600 + minutes*60 + seconds;
					writer.print(day_time);
				}
				else if(parametre.get(i) == "RENDEMENT"){
					//Parametre RENDEMENT
					try{
						double r = entry.getValue().get("P_01") + entry.getValue().get("P_02") + entry.getValue().get("P_03");
						writer.print(separateur + r);
					}
					catch(NullPointerException e){
						writer.print(separateur + "?");
					}
				}
				else{
					try{
						if(entry.getValue().get(parametre.get(i)) == null){
							throw new NullPointerException();
						}
						writer.print(separateur + entry.getValue().get(parametre.get(i)));
					}
					catch(NullPointerException e){
						writer.print(separateur + "?");
					}
				}
				separateur = ",";
			}
			writer.println("");
		}

		writer.close();

	}

	public static HashMap<Integer, Map<String, Double>> filtre_passe_bas_data(HashMap<Integer, Map<String, Double>> data, List<String> param) {
		/* HashMap<Integer, Map<String, Double>> data : données bruitées sous forme de HashMap
		List<String> param : Liste des paramètres demandés */

		//Création du filtre passe bas 
		int filtre[] = {1, 1, 1};
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
		//Création du filtre de ButterWorth
		double[] ReFil = new double[N/2];
		double[] ImFil = new double[N/2];
		double c = 100.0; // cutoff frequency
		for (int f = 0; f < N / 2; f++) {
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

				//On considère un paramètre à la fois comme étant un signal
				double[] signal = new double[nb_data];
				for(int j = 0; j < nb_data; ++j){
					try{
						tmp[j] = data.get(keys[j]).get(p);
					}
					catch(Exception e){
						//Dans le cas où la données est manquante, on met zéro
						tmp[j] = 0;
					}
				}

				//Calcul DFT du signal
				double[] ReSig = new double[N/2];
				double[] ImSig = new double[N/2];	
				DFT(signal, ReSig, ImSig);

				//multiplication signal par filtre (on est dans fourier)
				double[] ReOutput = new double[N/2];
				double[] ImOutput = new double[N/2];
				for(int f=0;f<N/2;f++) {
					ReOutput[f]=ReSig[f]*ReFil[f]-ImSig[f]*ImFil[f];
					ImOutput[f]=ReSig[f]*ImFil[f]+ImSig[f]*ReFil[f];
				}
				 
				//inverse de fourier pour obtenir le signal résultat
				double[] output = new double[N];
				invDFT(ReOutput, ImOutput, output);

				//On récopie le signal dans new_data
				for(int j = 0; j < nb_data; ++j){
					new_data.get(key[j]).put(p, output(j));
				}
			}
		}
		return new_data;
	}

	// public static HashMap<Integer, Map<String, Double>> filtrage_circuit_RC(HashMap<Integer, Map<String, Double>> data, List<String> param){
	// 	double[] ReFil = new double[N/2];
	// 	double[] ImFil = new double[N/2];
		 
	// 	double rc = 1.0/40;
	// 	for(int f=0;f<(N/2);f++) {
	// 		double w = 2*Math.PI*f;
	// 		double wrc = w*rc;
		 
	// 		double gain = 1.0/(1+Math.pow(wrc,2));
	// 		double real = 1;
	// 		double imag = -wrc;
		 
	// 		ReFil[f]=real*gain;
	// 		ImFil[f]=imag*gain;
	// 	}


	// }


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


}
