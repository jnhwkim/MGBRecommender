package kr.ac.snu.bi.cogtv.testbed;

import java.io.File;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.eval.RecommenderEvaluator;
import org.apache.mahout.cf.taste.impl.eval.AverageAbsoluteDifferenceRecommenderEvaluator;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.slopeone.SlopeOneRecommender;
import org.apache.mahout.cf.taste.impl.recommender.svd.SVDPlusPlusFactorizer;
import org.apache.mahout.cf.taste.impl.recommender.svd.SVDRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

public class RecommenderMAE
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
              //new ALSWRFactorizer(model, 10, 0.05, 10));
//                new RatingSGDFactorizer(model, 10, 10));
                new SVDPlusPlusFactorizer(model, 10, 10));

          default:
            return null;
        }
      }
    };
    
    DataModel model = new FileDataModel(new File("ml-100k/u.data"));
    
    double S = 0;
    for (int i = 1; i <= 5; i++)
    {  
      RecommenderEvaluator evaluator = new AverageAbsoluteDifferenceRecommenderEvaluator();
      double MAE = evaluator.evaluate(builder, null, model, 0.8, 0.2);
      S += MAE;
      System.out.println("MAE " + i + ": " + MAE);
    }
    System.out.println("AMAE: " + S / 5);
  }
}