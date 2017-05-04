package simulator;
import java.io.*;
import java.util.*;

public class TestDataWork {
	public static HashMap<Integer, Map<String, Double>> obj1 = new HashMap<Integer, Map<String, Double>>();
	public static int nb_data1;
	public static Integer[] keys1;
	public static int nb_cols1;
	public static String[] cols_name1;
	
	public static HashMap<Integer, Map<String, Double>> obj2 = new HashMap<Integer, Map<String, Double>>();
	public static int nb_data2;
	public static Integer[] keys2;
	public static int nb_cols2;
	public static String[] cols_name2;
	
	public static void main(String args[]){
		System.out.println("Démarrage du test");
		init_obj1();
		test_keys_to_array();
		test_get_nb_cols();
		test_cols_name_to_array();
		test_hashmap_to_tab();
		test_string_in_array();
		test_remove_cols();
		test_filtrage_data();
		test_brumisateur_used();
		
		init_obj2();
		test_separation_data_brumisateur();
		System.out.println("Fin du test");
	}
	
	public static void init_obj1(){
		HashMap<String, Double> m1 = new HashMap<String, Double>();
		m1.put("p1", 1.2);
		m1.put("p2", 4.1);
		m1.put("p3", 3.02);
		HashMap<String, Double> m2 = new HashMap<String, Double>();
		m2.put("p1", 4.3);
		m2.put("p2", 4.42);
		m2.put("p3", 2.76);
		HashMap<String, Double> m3 = new HashMap<String, Double>();
		m3.put("p1", 5.2);
		m3.put("p2", 4.11);
		m3.put("p3", 1.02);
		HashMap<String, Double> m4 = new HashMap<String, Double>();
		m4.put("p1", 8.3);
		m4.put("p2", 2.36);
		m4.put("p3", 0.23);
		HashMap<String, Double> m5 = new HashMap<String, Double>();
		m5.put("p1", 2.36);
		m5.put("p2", 7.35);
		m5.put("p3", 6.34);
		obj1.put(1, m1);
		obj1.put(2, m2);
		obj1.put(3, m3);
		obj1.put(4, m4);
		obj1.put(5, m5);
		
		nb_data1 = 5;
		Integer[] keys = {1, 2, 3, 4, 5};
		keys1 = keys;
		nb_cols1 = 3;
		String[] cols_name = {"p1", "p2", "p3"};
		cols_name1 = cols_name;
	}
	
	public static void init_obj2(){
		HashMap<String, Double> m1 = new HashMap<String, Double>();
		m1.put("Z_01", 1.);
		m1.put("p2", 4.1);
		m1.put("p3", 3.02);
		HashMap<String, Double> m2 = new HashMap<String, Double>();
		m2.put("Z_01", 0.);
		m2.put("p2", 4.42);
		m2.put("p3", 2.76);
		HashMap<String, Double> m3 = new HashMap<String, Double>();
		m3.put("Z_01", 1.);
		m3.put("p2", 4.11);
		m3.put("p3", 1.02);
		HashMap<String, Double> m4 = new HashMap<String, Double>();
		m4.put("Z_01", 1.);
		m4.put("p2", 2.36);
		m4.put("p3", 0.23);
		HashMap<String, Double> m5 = new HashMap<String, Double>();
		m5.put("p2", 2.36);
		m5.put("Z_01", 0.);
		m5.put("p3", 6.34);
		obj1.put(1, m1);
		obj1.put(2, m2);
		obj1.put(3, m3);
		obj1.put(4, m4);
		obj1.put(5, m5);
		
		nb_data1 = 5;
		Integer[] keys = {1, 2, 3, 4, 5};
		keys1 = keys;
		nb_cols1 = 3;
		String[] cols_name = {"Z_01", "p2", "p3"};
		cols_name1 = cols_name;
	}
		
	public static boolean test_keys_to_array(){
		Integer[] keys = DataWork.keys_to_array(obj1, nb_data1);
		if(keys.length != nb_data1){
			System.out.println("Erreur test_keys_to_array : 1");
		}
		//On test si les clés sont les bonnes, et triés dans le bon ordre
		for(int i = 0; i < nb_data1; ++i)
			if(keys[i] != keys1[i]){
				System.out.println("Erreur test_keys_to_array : 2");
				return false;
			}
		return true;
	}
	
	public static boolean test_get_nb_cols(){
		Integer keys[] = DataWork.keys_to_array(obj1, nb_data1);
		int nb_cols = DataWork.get_nb_cols(obj1, keys[0]);
		boolean ret = nb_cols == nb_cols1;
		if(!ret)
			System.out.println("Erreur test_get_nb_cols : 1");
		return ret;
	}
	
	public static boolean test_cols_name_to_array(){
		String cols_name[] = DataWork.cols_name_to_array(obj1, keys1[0], nb_cols1);
		Set<String> set = new HashSet<String>(Arrays.asList(cols_name1));
		if(nb_cols1 != cols_name.length){
			System.out.println("Erreur test_cols_name_to_array : 2");
			return false;
		}
		for(int i = 0; i < nb_cols1; ++i)
			if(!set.contains(cols_name[i])){
				System.out.println("Erreur test_cols_name_to_array : 1");
				return false;
			}
		return true;
	}
	
	public static boolean test_hashmap_to_tab(){
		Map<String, Double[]> tab_data = DataWork.hashmap_to_tab(obj1, keys1, cols_name1, nb_data1, nb_cols1);
		for(int i = 0; i < nb_cols1; ++i){
			String cname = cols_name1[i];
			for(int j = 0; j < nb_data1; ++j){
				double a = tab_data.get(cname)[j];
				double b = obj1.get(keys1[j]).get(cname);
				if(a != b){
					System.out.println("Erreur test_hashmap_to_tab : 1");
					return false;
				}
			}
		}
		return true;
	}
	
	public static boolean test_string_in_array(){
		String tab[] = {"salut", "patate", "pomme", "terre", "carotte"};
		boolean r1 = DataWork.string_in_array(tab, "patate");
		boolean r2 = DataWork.string_in_array(tab, "Tartuffe");
		boolean res = (r1 == true && r2 == false);
		if(!res){
			System.out.println("Erreur : test_string_in_array 1");
		}
		return res;
	}
	
	public static boolean test_remove_cols(){
		String[] cols_to_remove = {"p1", "p3"};
		HashMap<Integer, Map<String, Double>> new_data = DataWork.remove_cols(obj1, cols_to_remove);
		String[] cols_name = DataWork.cols_name_to_array(new_data, keys1[0], 1);
		if(cols_name.length != 1){
			System.out.println("Erreur test_remove_cols : 1");
			return false;
		}
		if(cols_name[0] != "p2"){
			System.out.println("Erreur test_remove_cols : 2");
			return false;
		}
		return true;
	}
	
	public static boolean test_filtrage_data(){
		int[] filtre = {1, 2, 1};
		HashMap<Integer, Map<String, Double>> new_data = DataWork.filtrage_data(obj1, filtre);
		
		for(int j = 0; j < nb_cols1; ++j){
			String cname = cols_name1[j];
			double a = (obj1.get(1).get(cname) + 2*obj1.get(2).get(cname) + obj1.get(3).get(cname))/4;
			double b = (obj1.get(2).get(cname) + 2*obj1.get(3).get(cname) + obj1.get(4).get(cname))/4;
			double c = (obj1.get(3).get(cname) + 2*obj1.get(4).get(cname) + obj1.get(5).get(cname))/4;
			
			double x = new_data.get(2).get(cname);
			if(a != x){
				System.out.println("Erreur test_filtrage_data : 1");
				return false;
			}
			if(b != new_data.get(3).get(cname)){
				System.out.println("Erreur test_filtrage_data : 2");
				return false;
			}
			if(c != new_data.get(4).get(cname)){
				System.out.println("Erreur test_filtrage_data : 3");
				return false;
			}
		}
		return true;
	}
	
	public static boolean test_brumisateur_used(){
		Map<String, Double> m1 = new HashMap<String, Double>();
		m1.put("Z_01", 1.);
		m1.put("ma", 0.);
		m1.put("pa", 0.);
		Map<String, Double> m2 = new HashMap<String, Double>();
		m2.put("Z_01", 0.);
		m2.put("ma", 0.);
		m2.put("pa", 0.);
		Map<String, Double> m3 = new HashMap<String, Double>();
		m3.put("Z_02", 1.);
		m3.put("ma", 0.);
		m3.put("pa", 0.);
		Map<String, Double> m4 = new HashMap<String, Double>();
		m4.put("Z_02", 0.);
		m4.put("ma", 0.);
		m4.put("pa", 0.);
		
		boolean a = DataWork.brumisateur_used(m1);
		boolean b = DataWork.brumisateur_used(m2);
		boolean c = DataWork.brumisateur_used(m3);
		boolean d = DataWork.brumisateur_used(m4);
		
		if(a == false){
			System.out.println("Erreur test_brumisateur_used 1");
			return false;
		}
		if(b == true){
			System.out.println("Erreur test_brumisateur_used 2");
			return false;
		}
		if(c == false){
			System.out.println("Erreur test_brumisateur_used 3");
			return false;
		}
		if(d == true){
			System.out.println("Erreur test_brumisateur_used 4");
			return false;
		}
		return true;
	}
	
	public static boolean test_separation_data_brumisateur(){
		HashMap<Integer, Map<String, Double>> ba = new HashMap<Integer, Map<String, Double>>();
		HashMap<Integer, Map<String, Double>> bd = new HashMap<Integer, Map<String, Double>>();
		
		DataWork.separation_data_brumisateur(obj2, ba, bd);
		
		int taille_ba = ba.size();
		Integer[] keys_ba = ba.entrySet().toArray(new Integer[taille_ba]);
		for(int i = 0; i < taille_ba; ++i){
			boolean r = DataWork.brumisateur_used(ba.get(keys_ba[i]));
			if(!r){
				System.out.println("Erreur test_separation_data_brumisateur 1");
				return false;
			}
		}
		
		int taille_bd = bd.size();
		Integer[] keys_bd = bd.entrySet().toArray(new Integer[taille_bd]);
		for(int i = 0; i < taille_bd; ++i){
			boolean r = DataWork.brumisateur_used(bd.get(keys_bd[i]));
			if(!r){
				System.out.println("Erreur test_separation_data_brumisateur 2");
				return false;
			}
		}
		return true;
	}
}
