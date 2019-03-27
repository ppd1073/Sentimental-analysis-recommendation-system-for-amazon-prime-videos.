import java.util.*;
import java.io.*;

/**
 * ISTE-612 LBE04 NB Classification
 * Sikha Jain
 */


public class NBClassifier {
	
   int numOfDocs;
   int numOfClasses;
	int[] classDocs; 
   String[] classText; 
	int[] classTokens;
   HashSet<String> vocab;
	HashMap<String,Double>[] condProb;
   Double[] classProb;
	Scanner sc;

   /**
	 * Build a Naive Bayes classifier using a training document set
	 * @param trainDataFolder the training document folder
	 */
   @SuppressWarnings("unchecked")  
	public NBClassifier(String trainDataFolder)
	{
     numOfDocs=0;
     preprocess(trainDataFolder);
     classProb = new Double[classText.length]; 		
	  for(int i=0;i<numOfClasses;i++){
			Iterator<Map.Entry<String, Double>> iterator = condProb[i].entrySet().iterator();
			int vocabSize = vocab.size();
			while(iterator.hasNext())
			{
				Map.Entry<String, Double> entry = iterator.next();
				String token = entry.getKey();
				Double count = entry.getValue();
				count = (count+1)/(classTokens[i]+vocabSize);
				condProb[i].put(token, count);
			}
			classProb[i] = 1.0*classDocs[i]/numOfDocs;
		}
   }
	
	/**
	 * Classify a test doc
	 * @param doc test doc
	 * @return class label
	 */
	public int classify(String doc){
      sc = null;
      String document = new String();
		try {
			sc = new Scanner(new File(doc));
		}catch(FileNotFoundException e){
			e.printStackTrace();
		}
		while(sc.hasNextLine()){
			document+=sc.nextLine();
		}
		int label = 0;
		int vocabSize = vocab.size();
		double[] score = new double[numOfClasses];
		String[] tokens = document.split("[^\\w']+");
		for (int i = 0; i < numOfClasses; i++) {
			score[i] = Math.log(classProb[i]);
			for (String token : tokens) {
				if(token.isEmpty())continue;	
   				if (condProb[i].containsKey(token)){
   					score[i] += Math.log(condProb[i].get(token));
   					condProb[i].put(token, condProb[i].get(token));	
   				}else{
   					score[i] += Math.log(1.0 / (classTokens[i] + vocabSize));
				   }
				}
			}
 		for (int i = 0; i < score.length; i++) {
 			if (score[i] > score[label])
 				label = i;
 		}
		return label;
	}
   
   
   public void tokenization(File file, int correctDocsClass){
		Scanner sc1 = null;
		try {
			sc1 = new Scanner(file);
			classDocs[correctDocsClass]++;
			numOfDocs++;	
		}catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		while(sc1.hasNextLine()){
			String[] tokens = sc1.nextLine().split("[^\\w']+");
			for(String token:tokens){
				if(token.isEmpty())continue;
				if(condProb[correctDocsClass].containsKey(token)){
					Double value = condProb[correctDocsClass].get(token);
					value++;
					condProb[correctDocsClass].put(token, value);
					classTokens[correctDocsClass]++;
				}else{
					Double value = 1.0;
					condProb[correctDocsClass].put(token, value);
					classTokens[correctDocsClass]++;
					if(!vocab.contains(token))vocab.add(token);
				}	
			}
		}
   }
	
	/**
	 * Load the training documents
	 * @param trainDataFolder
	 */
	public void preprocess(String trainDataFolder)
	{
      File folder = new File(trainDataFolder);
		int correctDocsFolders=0;
      File[] files=folder.listFiles();
  		correctDocsFolders = files.length;
		classDocs = new int[correctDocsFolders];
		classText = new String[correctDocsFolders];
		classTokens = new int[correctDocsFolders];
		condProb = new HashMap[correctDocsFolders];
		vocab = new HashSet<String>();
		numOfClasses=-1;
		for(File file:files){
			numOfClasses++;
			classText[numOfClasses]=file.getName();
			condProb[numOfClasses] = new HashMap<String,Double>();
			for(File f:file.listFiles()){
				tokenization(f,numOfClasses);
			}
		}
		numOfClasses++;
	}
	
	/**
	 *  Classify a set of testing documents and report the accuracy
	 * @param testDataFolder fold that contains the testing documents
	 * @return classification accuracy
	 */
	public double classifyAll(String testDataFolder)
	{
      File testFolder = new File(testDataFolder);
		double accuracy = 0;
		int correctlyclassified=0;
		int wronglyclassified=0;
		int aClass = 0;
		for(File testClassFolder:testFolder.listFiles()){
			for(File file:testClassFolder.listFiles()){
					int pClass = classify(file.getAbsolutePath());
					if(aClass==pClass){
						correctlyclassified++;
					}else{
						wronglyclassified++;
					}
				}
			aClass++;
		}
		double total = correctlyclassified+wronglyclassified;
    	double correctDocs = correctlyclassified;
		System.out.println("Correctly Classified: "+correctlyclassified+" out of "+(int)total);
		accuracy = correctDocs/total;
		return accuracy;	
	}
	
	
	public static void main(String[] args)
	{		
		NBClassifier nb = new NBClassifier("C:/Users/shikha/Desktop/RIT Courses/KPT/612Lab04/Lab4/data/train/");
		System.out.println("<---Classification Result--->");
		System.out.println("Accuracy: "+nb.classifyAll("C:/Users/shikha/Desktop/RIT Courses/KPT/612Lab04/Lab4/data/test/"));

	}
}
