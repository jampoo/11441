package logistic.regressoin.home.work;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class Training extends SuperClass{
	boolean debug = true;
	ArrayList<QueryDocument> qdList = new ArrayList<SuperClass.QueryDocument>();
	HashMap<Integer, Boolean> qidHashMap = new HashMap<Integer, Boolean>();
	static double lastConvergeRate = 1.0;
	
	public Training(String trainFilePath, double c)
	{
		this.trainFilePath = trainFilePath;
		this.C = c;
		this.qdList = readQueryDocuments(trainFilePath);
		
		for(int i=0; i<qdList.size(); i++)
		{
			QueryDocument qd = qdList.get(i);

			qd = L2NormalizeQDpair(qd);
			qdList.set(i, qd);
		}
		
		this.D = qdList.size();
		this.xDimension = qdList.get(0).xList.size();
		this.yDimension = 1;
		
		for(QueryDocument qd:qdList)
		{
			if(!qidHashMap.containsKey(qd.qid))
			{
				qidHashMap.put(qd.qid, true);
			}
		}
	}
	
	public ArrayList<Document> constructVectorList(int qid)
	{
		ArrayList<Integer> positiveList = new ArrayList<Integer>();
		ArrayList<Integer> negativeList = new ArrayList<Integer>();
		
		//separate query document pair into two categories based on their label 
		for(int i=0; i<D; i++)
		{
			QueryDocument qd = qdList.get(i);
			if(qd.qid == qid)
			{
				if(qd.label == 0)
					negativeList.add(i);
				if(qd.label == 1)
					positiveList.add(i);
			}
		}
		
		
		ArrayList<Document> vectorVList = new ArrayList<Document>();
		for(Integer neg_index:negativeList)
		{
			for(Integer pos_index:positiveList)
			{
				Document vectorV = createSingleVectorV(qdList.get(pos_index),qdList.get(neg_index));
				vectorVList.add(vectorV);
			}
		}
		System.out.println("for qid=" + qid + " Positive:" + positiveList.size() + " Negative:" + negativeList.size() + " Product:" + positiveList.size()*negativeList.size() + " VectorList:" + vectorVList.size() );
		
		return vectorVList;
	}
	
	public Document createSingleVectorV(QueryDocument qd1, QueryDocument qd2)
	{
		ArrayList<Pair> xList = new ArrayList<SuperClass.Pair>();
		
		for(int k=0; k<qd1.xList.size(); k++)
		{
			int position = qd1.xList.get(k).position;
			double value = qd1.xList.get(k).value - qd2.xList.get(k).value;
			Pair pair = new Pair(position, value);
			xList.add(pair);
		}
		
		Document vectorV = new Document();
		vectorV.xList = xList;
		vectorV.label = 1; //assume the label for qd1 is +1, label for qd2 is -1
		
		//print the qd1
//		printQueryDocument(qd1);
//		printQueryDocument(qd2);
//		printVector(vectorV);
		
		return vectorV;
	}
	
	public void printQueryDocument(QueryDocument qd)
	{
		System.out.print("QD2 label:" + qd.label + " qid:" + qd.qid + " docid:" + qd.docid + " ");
		for(Pair pair:qd.xList)
		{
			System.out.print(pair.position + ":" + pair.value + "\t");
		}
		System.out.println();
	}
	
	public void printVector(Document vector)
	{
		System.out.print("Vector V is label:" + vector.label + "\t\t");
		for(Pair pair:vector.xList)
		{
			System.out.print( pair.position + ":" + pair.value + "\t");
		}
		System.out.println();
	}
	
	public void convertToVector(String fileName)
	{
		Iterator<Integer> iterator = qidHashMap.keySet().iterator();
		ArrayList<Document> docList = new ArrayList<SuperClass.Document>();
		while(iterator.hasNext())
		{
			int qid = (Integer) iterator.next();
			docList.addAll(constructVectorList(qid));
		}
		
		try {
			FileWriter fileWriter = new FileWriter(fileName);
			BufferedWriter out = new BufferedWriter(fileWriter);
			
			for(Document vector:docList)
			{
				out.write(vector.label + " ");
				
//				for(Pair pair:vector.xList)
				for(int k=0; k<vector.xList.size()-1; k++)
				{
					Pair pair = vector.xList.get(k);
					out.write(pair.position + ":" + pair.value + " ");
				}
				Pair pair = vector.xList.get(vector.xList.size()-1);
				out.write(pair.position + ":" + pair.value + "\n");
			}
			
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public ArrayList<Double> GA()
	{
		ArrayList<Double> weightList = new ArrayList<Double>();
		ArrayList<Double> weightUpdates = new ArrayList<Double>();
		
		for(int j=0; j<xDimension; j++)
		{
			weightList.add((double)0);
			weightUpdates.add((double)0);
		}
		
		Iterator<Integer> iterator = qidHashMap.keySet().iterator();
		
		ArrayList<Document> docList = new ArrayList<SuperClass.Document>();
		while(iterator.hasNext())
		{
			int qid = (Integer) iterator.next();
			docList.addAll(constructVectorList(qid));
		}
		
//		if(false)
//		{
//			FileWriter fileWriter;
//			try {
//				fileWriter = new FileWriter("./vectorV.txt");
//				BufferedWriter out = new BufferedWriter(fileWriter);
//				
//				for(Document vector:docList)
//				{
//					out.write(vector.label + " ");
//					for(Pair pair:vector.xList)
//					{
//						out.write(pair.position + ":" + pair.value + " ");
//					}
//					out.write("\n");
//				}
//				out.close();
//			} catch (IOException e) {
//		e.printStackTrace();
//			}
//			
//		}
		
		while(true)
		{
			ArrayList<Double> derivativeList = new ArrayList<Double>();
			//compute the derivative for all documents
			for(int i=0; i<docList.size(); i++)
			{
				Document document = docList.get(i);
//				double y_i = (document.label == classLabel)? 1:0;
				double y_i = document.label;
				double z_i = crossProduct(weightList, document.xList);
				double sigma_i = 1/(1 + Math.exp((-1)*z_i));
				derivativeList.add(y_i - sigma_i);
			}

			//set all updated weights to zero
			for(int j=0; j<xDimension; j++)
			{
				weightUpdates.set(j, (double) 0);
			}
			
			for(int i=0; i<docList.size(); i++)
			{
				Document document = docList.get(i);
				for(Pair element:document.xList)
				{
					int j = element.position;
					double x_i_j = element.value;
					double weight_update_j = weightUpdates.get(j-1);
					weight_update_j += stepLength*derivativeList.get(i)*x_i_j;
					weightUpdates.set(j-1, weight_update_j);
				}
			}
			
			
				for(int j=0; j<weightList.size(); j++)
				{
					if(regulationEnable)
						weightList.set(j, (1-stepLength*C)*weightList.get(j) + weightUpdates.get(j) ) ;
					else {
						weightList.set(j, weightList.get(j) + weightUpdates.get(j) ) ;
					}
				}

//				System.out.println("Before L2 normalization");
//				for(Double weight:weightList)
//				{
//					System.out.print(weight + "\t");
//				}
//				System.out.println();
				
				//normalize the weights with l2 norm
//				weightList = L2Normalize(weightList);

				System.out.println("After L2 normalization");
				for(Double weight:weightList)
				{
					System.out.print(weight + "\t");
				}
				System.out.println();

				System.out.println("One iteration is finished");
				if(isConvergent(weightUpdates,weightList))
					break;
			}
		
		return weightList;
	}
	
	private boolean isConvergent(ArrayList<Double> weight_updates, ArrayList<Double> weightList)
	{
		
		double update_l2norm = 0;
		//obtain the l1 norm of weight update vector
		for(Double update:weight_updates)
		{
			update_l2norm += update*update;
		}
		update_l2norm /= weight_updates.size();
		update_l2norm = Math.sqrt(update_l2norm);
		
		//obtain the l2 norm of weight vector
		double weight_l2norm = 0;
		for(Double weight:weightList)
		{
			weight_l2norm += weight*weight;
		}
		weight_l2norm = weight_l2norm/weightList.size();
		weight_l2norm = Math.sqrt(weight_l2norm);
		
		//normalize the update by the weight size
		update_l2norm = update_l2norm/weight_l2norm;
		
		boolean convergent = (update_l2norm<convergentThreshold); 
		
//		//increase the step if converge too slow
//		if(update_l2norm < 0.02)
//			stepLength = 10*stepLength;
//
//		//decrease the step if not converge
//		if(update_l2norm > lastConvergeRate)
//			stepLength = 0.1*stepLength;
		
		if(debug)
			System.out.println("::::step Length is" + stepLength + ":::::" + update_l2norm + "/" +convergentThreshold);

		return convergent;
	}
	
	public ArrayList<Double> L2Normalize(ArrayList<Double> array)
	{
		double norm = 0;
		int length = array.size();
		
		for(double element:array)
		{
			norm += element*element;
		}
		
		norm = Math.sqrt(norm);
		
		for(int i=0; i<length; i++)
		{
			double nWeight = array.get(i)/norm;
			array.set(i, nWeight);
		}
		
		return array;
	}
	
	public QueryDocument L2NormalizeQDpair(QueryDocument qd)
	{
		double norm = 0;
		for(Pair pair:qd.xList)
		{
			norm += pair.value*pair.value;
		}
		
		norm = Math.sqrt(norm);
		
		for(int i=0; i<qd.xList.size(); i++)
		{
			Pair pair = qd.xList.get(i);
			pair.value = pair.value/norm;
			qd.xList.set(i, pair);
		}
		
		return qd;
	}
	
	public void writeVectorToFile(ArrayList<Double> vector, String fileName)
	{
		FileWriter fileWriter;
		try {
			fileWriter = new FileWriter(fileName);
			BufferedWriter out = new BufferedWriter(fileWriter);
			
			for(int k=0; k<vector.size()-1; k++)
			{
				out.write(vector.get(k)+" ");
			}
			out.write(vector.get(vector.size()-1) + "\n");

			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void evaluateData(ArrayList<Double> weightList, String outputFile)
	{
		ArrayList<Double> scoreList = new ArrayList<Double>();
		
		for(QueryDocument qd:this.qdList)
		{
			double score = crossProduct(weightList, qd.xList);
			scoreList.add(score);
		}

		FileWriter fileWriter;
		try {
			fileWriter = new FileWriter(outputFile);
			BufferedWriter out = new BufferedWriter(fileWriter);
			
			for(Double score:scoreList)
			{
				out.write(score + "\n");
			}
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args) 
	{
		Training training = new Training("./train.txt", 0.0000001);
		ArrayList<Double> weightList = training.GA();
		training.evaluateData(weightList, "predications.txt");
		
		training.writeVectorToFile(weightList, "weights.txt");
	}
}
