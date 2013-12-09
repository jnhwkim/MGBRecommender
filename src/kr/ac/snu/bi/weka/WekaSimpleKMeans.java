package kr.ac.snu.bi.weka;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.mahout.math.Vector;

import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.CSVLoader;

public class WekaSimpleKMeans
{
  public static final String DEFAULT_RELATION_NAME = "relation0";
  
  public static SimpleKMeans getDefault() throws Exception
  {
    SimpleKMeans skm = new SimpleKMeans();
    skm.setDistanceFunction(new weka.core.EuclideanDistance());
    skm.setFastDistanceCalc(true);
    skm.setMaxIterations(500);
    
    return skm;
  }
  
  public static Map<Integer, Set<Integer>> clustering(List<Vector> vectors, int n) throws Exception
  {
    SimpleKMeans skm = WekaSimpleKMeans.getDefault();
    skm.setNumClusters(n);
    
    Instances insts = WekaSimpleKMeans.convertVectorsToInstances(vectors);
    skm.buildClusterer(insts);
    
    Map<Integer, Set<Integer>> results = new HashMap<Integer, Set<Integer>>();
    for (int i = 0; i < insts.numInstances(); i++)
    {
      Instance instance = insts.instance(i);
      int k = skm.clusterInstance(instance);
      
      Set<Integer> group = results.get(k);
      if (null == group)
      {
        group = new HashSet<Integer>();
        results.put(k, group);
      }
      group.add(i);
    }

    return results;
  }
  
  public static Instances convertVectorsToInstances(List<Vector> vectors)
  {
    int numOfCols = vectors.get(0).size();
    ArrayList<Attribute> attribs = new ArrayList<Attribute>();
    
    // insert dummy attribute names
    for (int i = 0; i < numOfCols; i++)
    {
      attribs.add(i, new Attribute("attrb" + i));
    }
    
    Instances insts = new Instances(DEFAULT_RELATION_NAME, attribs, 0);
    // fill the values
    for (Vector vector : vectors)
    {
      // default instance weight is 1.0. 
      double[] instance = new double[numOfCols];
      for (int i = 0; i < numOfCols; i++)
      {
        instance[i] = vector.get(i);
      }
      Instance inst = new DenseInstance(1.0, instance);
      insts.add(inst);
    }
    
    return insts;
  }

  public static void main(String[] args) throws Exception
  {
      SimpleKMeans skm = new SimpleKMeans();
      skm.setOptions(new String[]{"-N 2", "-A \"weka.core.EuclideanDistance -R first-last\"", "-l 500"});
      
      CSVLoader loader = new CSVLoader();
      loader.setSource(new File("data/test/kmeans_test.csv"));
      Instances data = loader.getDataSet();
      
      skm.setNumClusters(3);
      
      skm.buildClusterer(data);
      System.out.println(skm);
      
      for (int i = 0; i < data.numInstances(); i++)
      {
        Instance instance = data.instance(i);
        int k = skm.clusterInstance(instance);
        System.out.println(instance + "\t" + k);
      }
  }

}
