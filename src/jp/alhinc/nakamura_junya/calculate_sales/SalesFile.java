package jp.alhinc.nakamura_junya.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

class SalesFile {
	public static void main(String args[]) {

		if (args.length != 1) {
			System.out.println("予期せぬエラーが発生しました");
			return;
		}

		HashMap<String, String> branchNameMap = new HashMap<String, String>();
		HashMap<String, String> commodityNameMap = new HashMap<String, String>();
		HashMap<String, Long> branchSaleMap = new HashMap<String, Long>();
		HashMap<String, Long> commoditySaleMap = new HashMap<String, Long>();

		BufferedReader br = null;

		if (! folders (args[0],"branch.lst", "支店","\\d{3}",branchNameMap,branchSaleMap)) {
			return;
		}
		if (! folders(args[0],"commodity.lst","商品","\\w{8}",commodityNameMap,commoditySaleMap)) {
			return;
		}

		File rcdCord = new File(args[0]);
		File files[] = rcdCord.listFiles();
		ArrayList<File> salesList1 = new ArrayList<File>();
		ArrayList<Integer> salesList2 = new ArrayList<Integer>();

		for (int i = 0; i < files.length; i++) {

			if (files[i].getName().matches("\\d{8}.rcd$") && (files[i].isFile())) {

				salesList1.add(files[i]);

				int name = Integer.parseInt(files[i].getName().substring(0, 8));

				salesList2.add(name);

			}
		}
		for (int i = 0; i < salesList2.size() - 1; i++) {

			int name1 = salesList2.get(i + 1) - salesList2.get(i);
			if (name1 != 1) {
				System.out.println("売上ファイル名が連番になっていません");
				return;
			}
		}
		try {
			for (int i = 0; i < salesList1.size(); i++) {

				br = new BufferedReader(new FileReader(salesList1.get(i)));

				String List;
				ArrayList<String> salesList3 = new ArrayList<String>();

				while ((List = br.readLine()) != null) {

					salesList3.add(List);
				}
				if (salesList3.size() != 3) {
					System.out.println(salesList1.get(i).getName() + "のフォーマットが不正です");
					return;
				}
				if (!(salesList3.get(2).matches("[0-9]*"))) {
					System.out.println("予期せぬエラーが発生しました");
					return;
				}

				if (!branchNameMap.containsKey(salesList3.get(0))) {
					System.out.println(salesList1.get(i).getName() + "の支店コードが不正です");
					return;
				}

				if (!commodityNameMap.containsKey(salesList3.get(1))) {
					System.out.println(salesList1.get(i).getName() + "の商品コードが不正です");
					return;
				}

				Long branch = branchSaleMap.get(salesList3.get(0));
				Long commodity = commoditySaleMap.get(salesList3.get(1));

				branch += Long.parseLong(salesList3.get(2));
				commodity += Long.parseLong(salesList3.get(2));

				if (!(branch <= 9999999999L) || !(commodity <= 9999999999L)) {
					System.out.println("合計金額が10桁を超えました");
					return;
				}
				branchSaleMap.put(salesList3.get(0), branch);
				commoditySaleMap.put(salesList3.get(1), commodity);
			}
		} catch (FileNotFoundException e) {
			System.out.println("予期せぬエラーが発生しました");
			return;

		} catch (IOException e) {
			System.out.println("予期せぬエラーが発生しました");
			return;

		} finally {
			if (br != null){
				try {
					br.close();
				} catch (IOException e) {
					System.out.println("予期せぬエラーが発生しました");
					return;
				}
			}
		}

		if (!resultFile (args[0],"branch.out",branchNameMap,branchSaleMap)) {
			return;
		}
		if (!resultFile(args[0],"commodity.out",commodityNameMap,commoditySaleMap)) {
			return;
		}
	}
	public static boolean resultFile (String pathName,String resultFileName,HashMap<String, String> mapName,HashMap<String, Long> MapSale) {

		List<Map.Entry<String, Long>> entries = new ArrayList<Map.Entry<String, Long>>(MapSale.entrySet());

		Collections.sort(entries, new Comparator<Map.Entry<String, Long>>() {
			@Override
			public int compare(Entry<String, Long> entry1, Entry<String, Long> entry2) {
				return ((Long) entry2.getValue()).compareTo((Long) entry1.getValue());
			}

		});

		BufferedWriter bw = null;

		try {

			File file = new File(pathName, resultFileName);
			FileWriter fw = new FileWriter(file);
			bw = new BufferedWriter(fw);

			for (Entry<String, Long> g : entries) {
				bw.write(g.getKey() + "," + mapName.get(g.getKey()) + "," + (g.getValue()));
				bw.newLine();
			}

		} catch (IOException e) {
			System.out.println("予期せぬエラーが発生しました");
			return false;

		} finally {
			if (bw != null){
				try {
					bw.close();
				} catch (IOException e) {
					System.out.println("予期せぬエラーが発生しました");
					return false;
				}
			}
		}
		return true;
	}
	public static boolean folders (String folderPathName,String folderFileName,String branchCommodity,
			String regularExpression,HashMap<String, String> folderMapName,HashMap<String, Long> folderMapSale){

		BufferedReader br = null;

		try {

			File file = new File(folderPathName,folderFileName);

			if (!file.exists()) {
				System.out.println(branchCommodity + "定義ファイルが存在しません");
				return false;
			}

			br = new BufferedReader(new FileReader(file));

			String readfile;

			while ((readfile = br.readLine()) != null) {

				String[] read =readfile.split(",");

				if (!read[0].matches(regularExpression) || (read.length != 2)) {
					System.out.println(branchCommodity + "定義ファイルのフォーマットが不正です");
					return false;
				}
				folderMapName.put(read[0], read[1]);
				folderMapSale.put(read[0], 0L);
			}

		} catch (FileNotFoundException e) {
			System.out.println(branchCommodity + "定義ファイルが存在しません");
			return false;

		} catch (IOException e) {
			System.out.println("予期せぬエラーが発生しました");
			return false;

		} finally {
			if (br != null){
				try {
					br.close();
				} catch (IOException e) {
					System.out.println("予期せぬエラーが発生しました");
					return false;
				}
			}
		}
		return true;
	}
}

