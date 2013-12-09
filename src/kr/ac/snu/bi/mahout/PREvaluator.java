package kr.ac.snu.bi.mahout;

import kr.ac.snu.bi.cogtv.testbed.QuantileEvaluator;

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
  
  public void printResult()
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
    QuantileEvaluator.printResult(result);
  }
}
