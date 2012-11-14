package logistic.regressoin.home.work;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

public class Testing extends SuperClass{
	boolean debug = true;
	HashMap<Integer, ArrayList<Double>> weightList = new HashMap<Integer, ArrayList<Double>>();
	HashMap<Integer, Double> weightNormList = new HashMap<Integer, Double>();
	
	public Testing(String filePath, double c) {
		this.testFilePath = filePath;
		this.C = c;
		readWeightFromFile();
		if(regulationEnable)
			calculateWeightNorm();
	}
	
	public void calculateWeightNorm()
	{
		for(int label=1; label<=yDimension; label++)
		{
			double norm = 0;
			ArrayList<Double> weights = weightList.get(label);
			for(int j=0; j<xDimension; j++)
			{
				norm += weights.get(j)*weights.get(j); 
			}
			weightNormList.put(label, norm);
		}
	}
	
	public void readWeightFromFile()
	{
		try {
			File file = new File("./weightsOutput.txt");
			FileInputStream fStream;
			fStream = new FileInputStream(file);
			DataInputStream in = new DataInputStream(fStream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			
			String str;
			
			int classifyLabel = 1;
			while((str=br.readLine())!=null)
			{
				String[] wLists = str.split(",");
				ArrayList<Double> dataList = new ArrayList<Double>();
				
				for(String w:wLists)
				{
					dataList.add(Double.parseDouble(w));
				}
				weightList.put(classifyLabel, dataList);
				classifyLabel++;
				
				if(debug)
					System.out.println("the " + classifyLabel + " weights has " + wLists.length + " elements");
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public Double computeLikelihood(Document document, int label)
	{
		ArrayList<Double> weights = weightList.get(label);
		double z = crossProduct(weights, document.xList);
		double likelihood = 1/( 1 + Math.exp((-1)*z));
//		likelihood = Math.log(likelihood) -
		if(regulationEnable)
		{
//			likelihood = Math.log(likelihood) - 0.5*C*weightNormList.get(label);
			likelihood = likelihood*Math.exp((-0.5)*C*weightNormList.get(label));
		}
				
		return likelihood;
	}
	
	public Integer classify(Document document)
	{
		ArrayList<Double> likelihoodList = new ArrayList<Double>();
		int classifyLabel = 0;
		double maxLikelihood = Double.MIN_VALUE;
		
		for(int label=1; label<=17; label++)
		{
			double likelihood = computeLikelihood(document, label);
			likelihoodList.add(likelihood);
			if(maxLikelihood < likelihood)
			{
				classifyLabel = label;
				maxLikelihood = likelihood;
			}
		}
		
		return classifyLabel;
	}
	
	public ArrayList<Integer> classifyAllDocuments()
	{
		ArrayList<Document> docList = readDocFromFile(testFilePath);
		ArrayList<Integer> classifyList = new ArrayList<Integer>();
		Integer correctLabelCount = 0;
		
		try {
			FileWriter fstream = new FileWriter("./lr_results.txt");
			BufferedWriter out = new BufferedWriter(fstream);
			
			
			for(Document document:docList)
			{
				Integer classifyLabel = classify(document);
				classifyList.add(classifyLabel);
				
				boolean isTrue = (document.label == classifyLabel);
				if(isTrue)
					correctLabelCount ++;
				System.out.println(isTrue + "  real label: " + document.label + ";   classify label: " + classifyLabel);
				out.write(classifyLabel + " " + document.label + "\n");
			}
			System.out.println("Classify Precision: " + (double)correctLabelCount/(double)docList.size());
		
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return classifyList;
	}
	
	
	public static void main(String[] args) {
		if(args.length != 1)
		{
			System.out.println("Arguments format error: java Testing [filePath] [C]");
		}
		else {
			String argumentString = args[0];
			String[] arguments = argumentString.split("\\s+");
			String filePath = arguments[1].replace("test=", "").replace("\\s+", "");
			String cString = arguments[2].replace("c=", "").replace("\\s+", "");
			double c = Double.parseDouble(cString);
			
			Testing testing = new Testing(filePath, c);
			testing.classifyAllDocuments();
		}
	}
}
