package kr.ac.snu.bi.cogtv;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import kr.ac.snu.bi.common.Commons;
import kr.ac.snu.bi.mahout.MatrixUtils;
import kr.ac.snu.bi.mahout.PREvaluator;
import kr.ac.snu.bi.weka.WekaSimpleKMeans;

import org.apache.mahout.classifier.AbstractVectorClassifier;
import org.apache.mahout.classifier.sgd.L1;
import org.apache.mahout.classifier.sgd.OnlineLogisticRegression;
import org.apache.mahout.math.DenseMatrix;
import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.Matrix;
import org.apache.mahout.math.SparseRowMatrix;
import org.apache.mahout.math.Vector;

import com.google.common.collect.Lists;

public class MGBRecommender
{
  static
  {
    System.setProperty("org.apache.commons.logging.Log",
        "org.apache.commons.logging.impl.NoOpLog");
  }

  private static final int USER_COLUMN_IDX = 0;
  // private static final int MOVIE_COLUMN_IDX = 1;
  private static final int RATING_SCALE = 2;
  private static final int RATING_COLUMN_IDX = 795; // 102; // 876;
  // private static final int NUM_OF_RATINGS = 99754;
  private static final int NUM_OF_USERS = 943;
  private static final int NUM_OF_GENES = 793; // 100; // 874;

  public static final int WEIGHTING_UNIFORM = 0;
  public static final int WEIGHTING_RATING = 1;
  public static final int WEIGHTING_QUANTILE = 2;
  public static final int WEIGHTING_LASSO_COEF = 3;
  
  public static String LABEL = "Not Assigned";
  public static String DATA_PATH;
  public static String OUTPUT_PATH;
  public static String OUTPUT_SURFIX;

  private int numOfClusters;
  private Matrix train_matrix;
  private Matrix test_matrix;
  private AbstractVectorClassifier[] classifiers;
  private Map<Integer, Double> thresholds;
  
  private final int TOP_N = 5;
  private PrintWriter topNDist;
  private PrintWriter topNPrecision;
  private PrintWriter evaluation;
  private PrintWriter numberOfUsersPerCluster;

  public MGBRecommender(Matrix user_rating_gene_matrix, int numOfClusters, int fold, int offset)
  {
    this.numOfClusters = numOfClusters;
    Matrix[] foldSet = split(user_rating_gene_matrix, fold, offset);
    this.train_matrix = foldSet[0];
    this.test_matrix = foldSet[1];
  }

  public MGBRecommender(Matrix tr, Matrix te, int numOfClusters)
  {
    this.numOfClusters = numOfClusters;
    this.train_matrix = tr;
    this.test_matrix = te;
  }
  
  public void openReportFiles()
  {
    topNDist = Commons.getFileWriter(DATA_PATH + "top" + TOP_N + "Dist" + OUTPUT_SURFIX, true);
    topNPrecision = Commons.getFileWriter(DATA_PATH + "top" + TOP_N + "Precision" + OUTPUT_SURFIX, true);
    evaluation = Commons.getFileWriter(DATA_PATH + "evaluation" + OUTPUT_SURFIX, true);
    numberOfUsersPerCluster = Commons.getFileWriter(DATA_PATH + "numberOfUsersPerCluster" + OUTPUT_SURFIX, true);
  }
  
  public void closeReportFiles()
  {
    topNDist.close();
    topNPrecision.close();
    evaluation.close();
    numberOfUsersPerCluster.close();
  }

  public static Matrix[] split(Matrix data, int fold, int offset)
  {
    List<Vector> trainVectors = new ArrayList<Vector>();
    List<Vector> testVectors = new ArrayList<Vector>();
  
    for (int i = 0; i < NUM_OF_USERS; i++)
    {
      Matrix sub = getSubMatrixByUser(data, i);
      for (int j = 0; j < sub.numRows(); j++)
      {
        if (offset == j % fold) // test data
        {
          testVectors.add(sub.viewRow(j));
        }
        else
        // training data
        {
          trainVectors.add(sub.viewRow(j));
        }
      }
    }
  
    Matrix train = new DenseMatrix(trainVectors.size(), data.numCols());
    Matrix test = new DenseMatrix(testVectors.size(), data.numCols());
  
    for (int i = 0; i < trainVectors.size(); i++)
    {
      train.assignRow(i, trainVectors.get(i));
    }
    for (int i = 0; i < testVectors.size(); i++)
    {
      test.assignRow(i, testVectors.get(i));
    }
  
    // System.out.println("train data: " + train.numRows());
    // System.out.println("test data: " + test.numRows());
  
    return new Matrix[] { train, test };
  }

  public Map<Integer, Set<Integer>> userClustering(Matrix m) throws Exception
  {
    // System.out.println("user clustering...");
    List<Vector> vectors = new ArrayList<Vector>();
    for (int i = 0; i < m.numRows(); i++)
    {
      vectors.add(m.viewRow(i));
    }
    //return SimpleKMeansClustering.clustering(vectors, numOfClusters);
    return WekaSimpleKMeans.clustering(vectors, numOfClusters);
  }

  /**
   * It returns the rating weighted user-gene matrix.
   * 
   * @return Matrix (NUM_OF_USERS * NUM_OF_GENES) matrix
   */
  public Matrix getUserGeneMatrix(int weighting_method, boolean hasThreshold)
  {
    // System.out.println("get an user-gene matrix...");

    Matrix userGeneMatrix = new DenseMatrix(NUM_OF_USERS, NUM_OF_GENES);
    double[][] userGeneValues = new double[NUM_OF_USERS][NUM_OF_GENES];

    // User index is started by 1 to NUM_OF_USERS.
    for (int u = 1; u <= NUM_OF_USERS; u++)
    {
      Matrix uMatrix = getSubMatrixByUser(this.train_matrix, u);
      Vector uRatingVector = uMatrix.viewColumn(RATING_COLUMN_IDX);
      Matrix uGeneMatrix = uMatrix.viewPart(0, uMatrix.numRows(), 2,
          NUM_OF_GENES).clone();
      uMatrix = null;

      // weighting on genes using scores
      for (int r = 0; r < uGeneMatrix.numRows(); r++)
      {
        double weight = 1;
        switch (weighting_method)
        {
          case WEIGHTING_UNIFORM:
            weight = 1.0d;
            break;
          case WEIGHTING_RATING:
            weight = uRatingVector.get(r);
            break;
          case WEIGHTING_QUANTILE:
            this.thresholds = new HashMap<Integer, Double>();
            double threshold = getThreshold(uRatingVector, 0.75, hasThreshold);
            this.thresholds.put(new Integer(u), new Double(threshold));
            weight = (uRatingVector.get(r) >= threshold) ? 3 : 1;
            break;
          case WEIGHTING_LASSO_COEF:
            break;
        }
        uGeneMatrix.assignRow(r, uGeneMatrix.viewRow(r).times(weight));
      }
      // averaging weighted genes given a user
      for (int c = 0; c < uGeneMatrix.numCols(); c++)
      {
        userGeneValues[u - 1][c] = uGeneMatrix.viewColumn(c).norm(1) / uGeneMatrix.numRows();
      }
    }
    return userGeneMatrix.assign(userGeneValues);
  }

  /**
   * Notice that a given matrix can be unsorted.
   * 
   * @param sparse
   * @param user_idx
   * @return
   */
  public static Matrix getSubMatrixByUser(Matrix m, int user_idx)
  {
    Set<Integer> userIndexes = new HashSet<Integer>();
    userIndexes.add(new Integer(user_idx));
    return getSubMatrixByUser(m, userIndexes);
  }

  /**
   * Notice that a given matrix can be unsorted.
   * 
   * @param
   * @param user_idx
   * @return
   * @throws Exception 
   */
  public static Matrix getSubMatrixByUser(Matrix m, Set<Integer> userIndexes)
  {
    List<Vector> rows = Lists.newArrayList();
    for (int i = 0; i < m.rowSize(); i++)
    {
        if (userIndexes.contains(new Double(m.get(i, USER_COLUMN_IDX)).intValue()))
        {
          rows.add(m.viewRow(i));
        }
    }
    Vector[] arrayOfRows = new Vector[rows.size()];
    int rowIdx = 0;
    for (Vector rowVector : rows)
    {
      arrayOfRows[rowIdx++] = rowVector;
    }
    int numRows = rows.size();
    int numCols = m.columnSize();
    Matrix matrix = makeMatrix(false, arrayOfRows, numRows, numCols);
    return matrix;
  }

  private static Matrix makeMatrix(boolean sparse, Vector[] arrayOfRows, int numRows, int numCols)
  {
    Matrix matrix;
    if (sparse)
    {
      matrix = new SparseRowMatrix(numRows, numCols, arrayOfRows);
    }
    else
    {
      matrix = new DenseMatrix(numRows, numCols);
      for (int i = 0; i < numRows; i++)
      {
        matrix.assignRow(i, arrayOfRows[i]);
      }
    }
    return matrix;
  }

  /**
   * Ratio 0.75 means the point which is a 3/4 point toward higher score.
   * 
   * @param uRatingVector
   *          rating score vector
   * @param ratio
   *          proportional threshold
   * @param hasThreshold
   *          minimum score to take as like
   * @return
   */
  private double getThreshold(Vector uRatingVector, double ratio, boolean hasThreshold)
  {
    List<Integer> ratings = new ArrayList<Integer>();
    for (int i = 0; i < uRatingVector.size(); i++)
    {
      ratings.add(new Double(uRatingVector.get(i)).intValue());
    }
    Collections.sort(ratings);
    double pos = ratings.size() * ratio;
    double threshold = (ratings.get(Math.max(new Double(Math.floor(pos)).intValue(), 0))
        + ratings.get(Math.min(new Double(Math.ceil(pos)).intValue(),
        ratings.size() - 1))) / 2;
  
    if (!hasThreshold)
      return threshold;
    else
      return Math.max(threshold, 3.0);
  }

  @SuppressWarnings("resource")
  public void train(Map<Integer, Set<Integer>> clusterToVectorSet, int iter)
  {
    this.classifiers = new AbstractVectorClassifier[numOfClusters];
    for (int k = 0; k < numOfClusters; k++)
    {
      // initialize classifiers
      this.classifiers[k] = new OnlineLogisticRegression(RATING_SCALE,
          NUM_OF_GENES,
          new L1())
          .alpha(1).stepOffset(1000).decayExponent(0.9).lambda(3.0e-5)
          .learningRate(20);
  
      // get sub-matrix in a cluster
      Set<Integer> userSet = clusterToVectorSet.get(k);
      Matrix subMatrix = getSubMatrixByUser(this.train_matrix, userSet);
  
      // training
      for (int i = 0; i < iter; i++)
      {
        for (int m = 0; m < subMatrix.numRows(); m++)
        {
          int actual = new Double(subMatrix.get(m, RATING_COLUMN_IDX)).intValue(); // -1 nov12
          Vector instance = subMatrix.viewRow(m).viewPart(2, NUM_OF_GENES);
          ((OnlineLogisticRegression) this.classifiers[k]).train(actual, instance);
        }
        ((OnlineLogisticRegression) this.classifiers[k]).close();
      }
    }
  }

  public void test(Map<Integer, Set<Integer>> clusterToVectorSet)
  {
    PREvaluator ev = new PREvaluator();
    TopNResult[] top = new TopNResult[NUM_OF_USERS];
    PrintWriter userPrecision = Commons.getFileWriter(OUTPUT_PATH + "userPrecision" + OUTPUT_SURFIX);
  
    for (int i = 0; i < this.test_matrix.numRows(); i++)
    {
      // userId starts from 1.
      int userId = new Double(this.test_matrix.get(i, USER_COLUMN_IDX)).intValue();
      int actual = new Double(this.test_matrix.get(i, RATING_COLUMN_IDX)).intValue();
      Vector instance = this.test_matrix.viewRow(i).viewPart(2, NUM_OF_GENES);
      int k = -1;
      for (Entry<Integer, Set<Integer>> entry : clusterToVectorSet.entrySet())
      {
        // In clusterToVectorSet, userId starts from 0.
        if (entry.getValue().contains(userId - 1))
        {
          k = entry.getKey().intValue();
          break;
        }
      }
      Vector prob = new DenseVector(RATING_SCALE);
      ((OnlineLogisticRegression) this.classifiers[k]).classifyFull(prob, instance);
      int estimated = prob.maxValueIndex(); // +1 nov12
      boolean recItemLike, prfItemLike;
      
      // top n result
      if (null == top[userId - 1])
        top[userId - 1] = new TopNResult(TOP_N);
      
      Vector result = new DenseVector(RATING_SCALE + 1);
      for (int j = 0; j < RATING_SCALE; j++)
        result.set(j, prob.get(j));
      result.set(RATING_SCALE, actual);
      top[userId - 1].add(result);
  
      try
      {
        // thresholding
        // recItemLike = (actual >= this.thresholds.get(userId)) ? true : false;
        // prfItemLike = (estimated >= this.thresholds.get(userId)) ? true : false;
        recItemLike = actual == 1 ? true : false;
        prfItemLike = estimated == 1 ? true : false;
      }
      catch (NullPointerException e)
      {
        // if there is no threshold for a given user.
        continue;
      }
      ev.addInstance(recItemLike, prfItemLike);
    }
    evaluation.print(MGBRecommender.LABEL + "\t");
    ev.printResult(evaluation);
    
    double precision = 0;
    double total = 0;
    
    int sum[] = new int[TOP_N];
    for (int i = 0; i < NUM_OF_USERS; i++)
    {
      int count = 0;
      int correct = 0;
      if (null != top[i])
      {
        for (int j = 0; j < TOP_N; j++)
        {
          Vector result = top[i].poll();
          if (null != result)
          {
            count++;
            if (1.0d == result.get(RATING_SCALE))
              correct++;
          }
        }
        double sub_p = 1.0d * correct / count;
        sum[count - 1]++;
        precision += sub_p;
        total++;
        
        userPrecision.println((i + 1) + "\t" + sub_p);
      }
    }
    
    // print TOP_N distribution
    topNDist.print(MGBRecommender.LABEL + "\t");
    for (int i = 0; i < TOP_N; i++)
      topNDist.print(sum[i] + "\t");
    topNDist.println();
    
    topNPrecision.println(MGBRecommender.LABEL + "\t" + precision / total);
    userPrecision.close();
  }

  public void testFull(Map<Integer, Set<Integer>> clusterToVectorSet)
  {
    PREvaluator[][] evTable = new PREvaluator[numOfClusters][numOfClusters];
    PrintWriter accuracyMatrix = Commons.getFileWriter(OUTPUT_PATH + "accuracyMatrix" + OUTPUT_SURFIX);
  
    for (int i = 0; i < this.test_matrix.numRows(); i++)
    {
      // userId starts from 1.
      int userId = new Double(this.test_matrix.get(i, USER_COLUMN_IDX)).intValue();
      int actual = new Double(this.test_matrix.get(i, RATING_COLUMN_IDX)).intValue();
      Vector instance = this.test_matrix.viewRow(i).viewPart(2, NUM_OF_GENES);
      int k = -1;
      for (Entry<Integer, Set<Integer>> entry : clusterToVectorSet.entrySet())
      {
        // In clusterToVectorSet, userId starts from 0.
        if (entry.getValue().contains(userId - 1))
        {
          k = entry.getKey().intValue();
          break;
        }
      }
  
      for (int j = 0; j < this.classifiers.length; j++)
      {
        Vector prob = new DenseVector(RATING_SCALE);
        ((OnlineLogisticRegression) this.classifiers[j]).classifyFull(prob,
            instance);
        int estimated = prob.maxValueIndex(); // +1 nov12
        boolean recItemLike, prfItemLike;
  
        try
        {
          // thresholding
          // recItemLike = (actual >= this.thresholds.get(userId)) ? true : false;
          // prfItemLike = (estimated >= this.thresholds.get(userId)) ? true : false;
          recItemLike = actual == 1 ? true : false; // nov12
          prfItemLike = estimated == 1 ? true : false; // nov12
        }
        catch (NullPointerException e)
        {
          // if there is no threshold for a given user.
          continue;
        }
        if (null == evTable[k][j]) evTable[k][j] = new PREvaluator();
        evTable[k][j].addInstance(recItemLike, prfItemLike);
      }
    }
    
    for (int k = 0; k < numOfClusters; k++)
    {
      for (int j = 0; j < this.classifiers.length; j++)
      {
        evTable[k][j].printResult(accuracyMatrix);
      }
    }
    
    //# of users per cluster
    numberOfUsersPerCluster.print(MGBRecommender.LABEL + "\t");
    for (Entry<Integer, Set<Integer>> entry : clusterToVectorSet.entrySet())
    {
      numberOfUsersPerCluster.print(entry.getValue().size() + "\t");
    }
    numberOfUsersPerCluster.println();
    
    accuracyMatrix.close();
  }

  public void eval(int weighting_method, boolean hasThreshold, int iter) throws Exception
  {
    Matrix ugm = this.getUserGeneMatrix(weighting_method, hasThreshold);
    Map<Integer, Set<Integer>> clusterToUserSet = this.userClustering(ugm);
    this.train(clusterToUserSet, iter);
    this.test(clusterToUserSet);
  }

  public void evalFull(int weighting_method, boolean hasThreshold, int iter) throws Exception
  {
    Matrix ugm = this.getUserGeneMatrix(weighting_method, hasThreshold);
    Map<Integer, Set<Integer>> clusterToUserSet = this.userClustering(ugm);
    this.train(clusterToUserSet, iter);
    this.testFull(clusterToUserSet);
  }

  public void evalWithUserGeneMatrix(Matrix ugm, boolean hasThreshold, int iter) throws Exception
  {
    this.openReportFiles();
    Map<Integer, Set<Integer>> clusterToUserSet = this.userClustering(ugm);
    this.train(clusterToUserSet, iter);
    this.test(clusterToUserSet);
    this.closeReportFiles();
  }

  public void evalFullWithUserGeneMatrix(Matrix ugm, boolean hasThreshold, int iter) throws Exception
  {
    this.openReportFiles();
    Map<Integer, Set<Integer>> clusterToUserSet = this.userClustering(ugm);
    this.train(clusterToUserSet, iter);
    this.testFull(clusterToUserSet);
    this.closeReportFiles();
  }

  public static void main(String[] args) throws Exception
  {
    String[] VARIATION_METHODS = { "Manual_grouping_61set" }; // "codebook_hclust100", "LDA_t100_weighted" };
    String[] LABEL_BINARIZATION_METHODS = { "3quartile_midpt", "adaptive", "adaptive_random" };
    String[] USER_PROFILING_METHODS     = { "geneAvr", "linSVM", "logit" };
    String DATA_PATH = "data/dec24/";
    String DATA_SURFIX = "_1222.csv";
    
    int iter = 10;

    for (int p2p = 0; p2p < VARIATION_METHODS.length; p2p++ )
    {
      for (int p2 = 0; p2 < LABEL_BINARIZATION_METHODS.length; p2++)
      {
        Matrix tr = MatrixUtils.read(false, DATA_PATH + "fold1_training_" + VARIATION_METHODS[p2p] + "_"
            + LABEL_BINARIZATION_METHODS[p2] + DATA_SURFIX);
        Matrix te = MatrixUtils.read(false, DATA_PATH + "fold1_test_" + VARIATION_METHODS[p2p] + "_"
            + LABEL_BINARIZATION_METHODS[p2] + DATA_SURFIX);

        // Check the matrix
        System.out.println(tr.numRows() + " x " + tr.numCols());
        System.out.println(te.numRows() + " x " + te.numCols());
        
        for (int p3 = 0; p3 < USER_PROFILING_METHODS.length - 1; p3++)
        {
          Matrix uprofile = MatrixUtils.read(false, 
              DATA_PATH + "uprofile_" + USER_PROFILING_METHODS[p3] + 
                      "_fold1_" + VARIATION_METHODS[p2p] + "_" + LABEL_BINARIZATION_METHODS[p2] + DATA_SURFIX);
           
          System.out.println(uprofile.numRows() + " x " + uprofile.numCols());
          
          System.out.println("<" + VARIATION_METHODS[p2p] + "_" + LABEL_BINARIZATION_METHODS[p2] + " | " + USER_PROFILING_METHODS[p3] + ">");
          
          for (int k = 3; k <= 4; k++)
          {
            int N = Math.max(1, 3 * k);
            MGBRecommender.LABEL = VARIATION_METHODS[p2p] + "_" + LABEL_BINARIZATION_METHODS[p2] + "_" + USER_PROFILING_METHODS[p3] + "_" + N;
            MGBRecommender.DATA_PATH = DATA_PATH;
            MGBRecommender.OUTPUT_PATH = DATA_PATH + MGBRecommender.LABEL + "_";
            MGBRecommender.OUTPUT_SURFIX = ".txt";
            new MGBRecommender(tr, te, N).evalWithUserGeneMatrix(uprofile, false, iter);
            //System.out.println("***");
            new MGBRecommender(tr, te, Math.max(1, 3 * k)).evalFullWithUserGeneMatrix(uprofile, false, iter);
          }
        }
      }
    }
  }
}
