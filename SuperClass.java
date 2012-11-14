package logistic.regressoin.home.work;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class SuperClass {
	String trainFilePath = "./train.txt";
	String testFilePath = "./test.txt";
	double C = 0.001;	//the regulation factor
	boolean debug = false;
	boolean regulationEnable = false;
	double stepLength = 0.001;	//the gradient ascent step, manual tuned
	double convergentThreshold = 0.005;	//the threshold for determining the convergent condition
	
	int xDimension;  //the is the size of elements in the X vector
	int yDimension;	//the size of labels 
	int D;	//the size of training documents
	
	protected class Pair//the class represents the position:value relationship in 
							//the input training and testing file
	{
		int position;
		double value;
		
		public Pair(int position, double value) {
			this.position = position;
			this.value = value;
		}
	}
	
	protected class Document	//the class represents the a single line in the input training and testing files
	{
		int label;
		ArrayList<Pair> xList = new ArrayList<Pair>(); 
	}
	
	protected class QueryDocument {
		int label;
		int qid;
		ArrayList<Pair> xList = new ArrayList<SuperClass.Pair>();
		int docid;
	}
	
//	protected class VectorV
//	{
//		int label;
//		ArrayList<Pair> xList;
//	}
	
	protected double crossProduct(ArrayList<Double> weights, ArrayList<Pair> X)
	{
		double sum = 0;
		for(Pair element:X)
		{
			int position = element.position;
			double value = element.value;
			sum += weights.get(position-1)*value;
		}
		return sum;
	}
	
	public ArrayList<Document> readDocFromFile(String fileName)
	{
		ArrayList<Document> docList = new ArrayList<Document>();
		
		ArrayList<ArrayList<Double>> dataList = new ArrayList<ArrayList<Double>>();

		FileInputStream fStream;
			try {
				fStream = new FileInputStream(fileName);
				DataInputStream in = new DataInputStream(fStream);
				BufferedReader br = new BufferedReader(new InputStreamReader(in));
				String strLine;
				
				while((strLine = br.readLine()) != null)
				{
					String[] components = strLine.split("\\s+",2);
							
					Document document = new Document();
					document.label = Integer.parseInt(components[0]);;
					
					String[] pairStrings = components[1].split("\\s+");
					for(String pairString:pairStrings)
					{
						String[] pairComponents = pairString.split(":");
						Integer position = Integer.parseInt(pairComponents[0]);
						Double value = Double.parseDouble(pairComponents[1]);
						document.xList.add(new Pair(position, value));
					}
					
					docList.add(document);
				}
				
//				this.D = docList.size();
				
				in.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		
		return docList;
	}

	public  ArrayList<QueryDocument> readQueryDocuments(String fileName)
	{
		ArrayList<QueryDocument> qdList = new ArrayList<SuperClass.QueryDocument>();
		
		FileInputStream fStream;
		try {
			fStream = new FileInputStream(fileName);
			DataInputStream in = new DataInputStream(fStream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			
			while((strLine = br.readLine()) != null)
			{
				QueryDocument qd = new QueryDocument();
				String[] componentStrings = strLine.split("\\s+",3);
				qd.label = Integer.parseInt(componentStrings[0]);
				qd.qid = Integer.parseInt(componentStrings[1].replace("qid:", "").replaceAll("\\s+", ""));
				
				String[] partStrings = componentStrings[2].split("#",2);
				qd.docid = Integer.parseInt(partStrings[1].replace("docid = ", "").replace("\\s+",""));
				
				//handle the featureid:value pairs
				String[] featureStrings = partStrings[0].split("\\s+");
				ArrayList<Pair> featureList = new ArrayList<SuperClass.Pair>();
				for(String feature:featureStrings)
				{
					int position = Integer.parseInt(feature.split(":",2)[0].replaceAll("\\s+", ""));
					double value = Double.parseDouble(feature.split(":",2)[1].replaceAll("\\s+", ""));
					Pair pair = new Pair(position, value);
					featureList.add(pair);
				}
				
				qd.xList = featureList;
//				if(debug)
//				{
//					System.out.print(qd.label + " " + qd.qid + " " + qd.docid);
//					for(Pair pair:qd.xList)
//					{
//						System.out.print(" " + pair.position + ":" + pair.value);
//					}
//					System.out.println();
//				}
				qdList.add(qd);
			}
			
			in.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		return qdList;
	}

	
}
