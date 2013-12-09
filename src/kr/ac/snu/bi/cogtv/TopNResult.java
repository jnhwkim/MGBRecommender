package kr.ac.snu.bi.cogtv;

import java.util.Comparator;
import java.util.PriorityQueue;

import org.apache.mahout.math.Vector;

public class TopNResult
{
  public final static int LIKE_INDEX = 1;
  public final static int DISLIKE_INDEX = 0;
  
  int size;
  PriorityQueue<Vector> topResults;
  
  public TopNResult(int size)
  {
    this.size = size;
    this.topResults = new PriorityQueue<Vector>(size, new Comparator<Vector>(){
      public int compare(Vector arg0, Vector arg1)
      {
        double likeProb0 = arg0.get(LIKE_INDEX) - arg0.get(DISLIKE_INDEX);
        double likeProb1 = arg1.get(LIKE_INDEX) - arg1.get(DISLIKE_INDEX);;
        // @todo CHECKPOINT 1. plausible?
        return likeProb0 < likeProb1 ? 1 : (likeProb0 == likeProb1 ? 0 : -1);
      }
    });
  }
  
  public void add(Vector prob)
  {
    this.topResults.add(prob);
  }
  
  public Vector poll()
  {
    return this.topResults.poll();
  }
  
  public void print()
  {
    for (int i = 0; i < this.size; i++)
      System.out.println(this.topResults.poll());
  }
  
  public int getSize()
  {
    return this.topResults.size();
  }
  
//  public static void main(String[] args)
//  {
//    PriorityQueue q = new PriorityQueue<Integer>(100, new Comparator<Integer>(){
//
//      public int compare(Integer arg0, Integer arg1)
//      {
//        return -1 * arg0.compareTo(arg1);
//      }
//    });
//    for (int i = 0; i < 15; i++) {
//      q.add(new Integer(i));
//    }
//    System.out.println(q.size());
//    int size = q.size();
//    for (int i = 0; i < size; i++)
//      System.out.println(q.poll());
//    System.out.println(q.size());
//  }

}
