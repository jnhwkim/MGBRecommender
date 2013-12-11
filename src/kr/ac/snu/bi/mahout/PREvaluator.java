package kr.ac.snu.bi.mahout;

import java.io.PrintWriter;

public class PREvaluator
{
  private int[] testTable = { 0, 0, 0, 0 };
  
  public void addInstance(boolean predictValue, boolean actualValue)
  {
    if (predictValue && actualValue)
      this.testTable[0]++;
    if (predictValue && !actualValue)
      this.testTable[1]++;
    if (!predictValue && actualValue)
      this.testTable[2]++;
    if (!predictValue && !actualValue)
      this.testTable[3]++;
  }
  
  public void printResult(PrintWriter accuracyMatrix)
  {
    float precision = (float) (1.0f * testTable[0] / (testTable[0] + testTable[1]));
    float recall = (float) (1.0f * testTable[0] / (testTable[0] + testTable[2]));
    int total = testTable[0] + testTable[1] + testTable[2] + testTable[3];
    float correctness = (float) (1.0f * (testTable[0] + testTable[3]) / total);
    for (int i = 0; i < 4; i++)
    {
      // System.out.println(testTable[i]);
    }
    float[] result = new float[3];
    result[0] = precision;
    result[1] = recall;
    result[2] = correctness;
    accuracyMatrix.println(result[0] + "\t" + result[1] + "\t" + result[2]);
  }
  
}
