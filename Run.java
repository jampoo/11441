package logistic.regressoin.home.work;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Run {
	
	public static void main(String[] args) {
		String trainFilePath = null;
		String testFilePath = null;
		double c = 0;
		try {
			FileReader fstream = new FileReader("./DATA.txt");
			BufferedReader in = new BufferedReader(fstream);
			String strLin;
			
			while((strLin = in.readLine())!=null)
			{
				if(strLin.contains("train="))
				{
					trainFilePath = strLin.replaceAll("train=", "").replaceAll("\\s+", "");
				}
				if(strLin.contains("test="))
				{
					testFilePath = strLin.replaceAll("test=", "").replaceAll("\\s+", "");
				}
				if(strLin.contains("c="))
				{
					String cString = strLin.replaceAll("c=", "").replace("\\s+", "");
					c = Double.parseDouble(cString);
				}
			}
			
			in.close();
			
			Training training = new Training(trainFilePath, c);
//			training.trainAndWriteToFile();
			
			Testing testing = new Testing(testFilePath, c);
			testing.classifyAllDocuments();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
