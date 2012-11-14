package logistic.regressoin.home.work;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class DataIO extends SuperClass{

	public  ArrayList<QueryDocument> readDataFromFile(String fileName)
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
				System.out.print(qd.label + " " + qd.qid + " " + qd.docid);
				if(debug)
				{
					for(Pair pair:qd.xList)
					{
						System.out.print(" " + pair.position + ":" + pair.value);
					}
					System.out.println();
				}
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

	
	public  ArrayList<QueryDocument> readTrainingData()
	{
		return readDataFromFile("./train.txt");
	}
	
	public  ArrayList<QueryDocument> readTestingData()
	{
		return readDataFromFile("./test.txt");
	}
	
	
	//unit testing
	public static void main(String[] args) {
		DataIO dataIO = new DataIO();
		
		dataIO.readTrainingData();
		
	}
}
