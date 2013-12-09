package kr.ac.snu.bi.cogtv.testbed;

import java.io.File;

import org.apache.mahout.cf.taste.common.NoSuchItemException;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.slopeone.SlopeOneRecommender;
import org.apache.mahout.cf.taste.impl.recommender.svd.RatingSGDFactorizer;
import org.apache.mahout.cf.taste.impl.recommender.svd.SVDRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

public class QuantileEvaluator
{
  static
  {
    System.setProperty("org.apache.commons.logging.Log",
        "org.apache.commons.logging.impl.NoOpLog");
  }
  public static void main(String[] args) throws Exception
  { 
    RecommenderBuilder builder = new RecommenderBuilder()
    {
      @Override
      public Recommender buildRecommender(DataModel model)
          throws TasteException
      {
        switch (3)
        {
          case 1:
            UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
            UserNeighborhood neighborhood = new NearestNUserNeighborhood(2,
                similarity, model);
            return new GenericUserBasedRecommender(model, neighborhood,
                similarity);
            
          case 2:
            return new SlopeOneRecommender(model);
            
          case 3:
            return new SVDRecommender(model, 
//                new ALSWRFactorizer(model, 30, 0.05, 50));
              new RatingSGDFactorizer(model, 30, 100));
//              new SVDPlusPlusFactorizer(model, 10, 10));

          default:
            return null;
        }
      }
    };
    
    float prcSum[] = {0.0f, 0.0f, 0.0f};
    int NUM_OF_SETS = 5;
    for (int i = 1; i <= NUM_OF_SETS; i++)
    {
      System.out.println("\nu" + i);
      DataModel trainingModel = new FileDataModel(new File("ml-100k/u" + i + "base"));
      DataModel testModel = new FileDataModel(new File("ml-100k/u" + i + "test"));
      Recommender recommender = builder.buildRecommender(trainingModel);
      float prc[] = new float[3];
      evaluate(recommender, testModel, 0.25, prc);
      for (int j = 0; j < 3; j++) {
        prcSum[j] += prc[j] / NUM_OF_SETS;
      }
    }
    System.out.println("\nAverage Result:\n");
    QuantileEvaluator.printResult(prcSum);
  }
  
  public static void evaluate(Recommender recommender,
      DataModel model,
      double likeRatio,
      float[] result) throws TasteException
  {
    DataModel trainingDataModel = recommender.getDataModel();
    LongPrimitiveIterator users = model.getUserIDs();
    
    // [0]: true positive, [1]: false positive, [2]: false negative, [3]: true negative
    int[] testTable = {0, 0, 0, 0};
    int sum = 0;
    int failed = 0;
    
    while (users.hasNext())
    {
      long userID = users.nextLong();
      PreferenceArray prefs1 = trainingDataModel.getPreferencesFromUser(userID);
      prefs1.sortByValueReversed();
      int thresholdOrder = (int) (prefs1.length() * likeRatio);
      float thresholdValue = prefs1.getValue(thresholdOrder);
      //System.out.println(thresholdOrder + " / " + prefs1.length() + " " + thresholdValue);
      
      PreferenceArray prefs2 = model.getPreferencesFromUser(userID);
      sum += prefs2.length();
      for (Preference prfItem : prefs2)
      {
        double estimatedValue = Double.NaN;
        try {
          estimatedValue = recommender.estimatePreference(userID, prfItem.getItemID());
        } catch (NoSuchItemException e) {
          failed++;
          continue;
        }
        if (0 == Double.compare(Double.NaN, estimatedValue))
        {
          failed++;
          continue;
        } else {
          // as threshold value is integer.
          estimatedValue = (float) Math.round(estimatedValue);
        }
        
        if (thresholdValue < 3.0) thresholdValue = 3.0f;
        boolean recItemLike = (estimatedValue >= thresholdValue) ? true : false;
        boolean prfItemLike = (prfItem.getValue() >= thresholdValue) ? true : false;
        
        if (estimatedValue != prfItem.getValue())
        {
          if (recItemLike && prfItemLike)
          {
//            System.out.println(estimatedValue + "(" + recItemLike + ")" + " | " + prfItem.getValue() + "(" + prfItemLike + ")");
//          System.out.println(estimatedValue + " | " + prfItem.getValue());
          }
        }
          
        if (recItemLike && prfItemLike) 
          testTable[0]++;
        if (recItemLike && !prfItemLike)
          testTable[1]++;
        if (!recItemLike && prfItemLike)
          testTable[2]++;
        if (!recItemLike && !prfItemLike)
          testTable[3]++;
      }
    }
    float precision = (float) (1.0f * testTable[0] / (testTable[0] + testTable[1]));
    float recall = (float) (1.0f * testTable[0] / (testTable[0] + testTable[2]));
    int total = testTable[0] + testTable[1] + testTable[2] + testTable[3];
    float correctness = (float) (1.0f * (testTable[0] + testTable[3]) / total);
    for (int i = 0; i < 4; i++)
    {
      //System.out.println(testTable[i]);
    }
    System.out.println(sum + " - " + total + " = " + failed + "(?)");
    result[0] = precision;
    result[1] = recall;
    result[2] = correctness;
    QuantileEvaluator.printResult(result);
  }
  public static void printResult(float[] prc) {
    System.out.println(prc[0] + "\t" + prc[1] + "\t" + prc[2]);
  }
}