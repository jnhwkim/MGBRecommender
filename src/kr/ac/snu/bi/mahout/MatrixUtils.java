package kr.ac.snu.bi.mahout;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.mahout.common.Pair;
import org.apache.mahout.math.DenseMatrix;
import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.Matrix;
import org.apache.mahout.math.SparseRowMatrix;
import org.apache.mahout.math.Vector;

import com.google.common.collect.Lists;

public class MatrixUtils
{
  private static String COLUMN_DELIMITERS = ", :|";
  
  public static Matrix read(boolean sparse, String... filePaths) throws IOException {
    int numRows = 0;
    int numCols = -1;
    List<Pair<Integer, Vector>> rows = Lists.newArrayList();
    for (String filePath : filePaths) {
      BufferedReader in = new BufferedReader(new FileReader(filePath));
      String row;
      while ((row = in.readLine()) != null)
      {
        StringTokenizer tokenizer = new StringTokenizer(row, COLUMN_DELIMITERS);
        double[] cols = new double[tokenizer.countTokens()];
        int colIdx = 0;
        while (tokenizer.hasMoreTokens())
        {
          String valueStr = tokenizer.nextToken();
          double valueDouble = 0.0d;
          try
          {
            valueDouble = Double.parseDouble(valueStr);
          }
          catch (NumberFormatException e)
          {
            System.err.println(valueStr + " is not double");
          }
          cols[colIdx++] = valueDouble;
        }
        // @todo support sparse vector 
        Vector rowVector = new DenseVector(cols);
        sparse = !rowVector.isDense();
        rows.add(Pair.of(numRows, rowVector));
        if (numCols < 0) {
          numCols = rowVector.size();
        }
        numRows++;
      }
      in.close();
    }
    if (rows.isEmpty()) {
      throw new IOException(Arrays.toString(filePaths) + " have no vectors in it");
    }
    Vector[] arrayOfRows = new Vector[numRows];
    for (Pair<Integer, Vector> pair : rows) {
      arrayOfRows[pair.getFirst()] = pair.getSecond();
    }
    Matrix matrix;
    if (sparse) {
      matrix = new SparseRowMatrix(numRows, numCols, arrayOfRows);
    } else {
      matrix = new DenseMatrix(numRows, numCols);
      for (int i = 0; i < numRows; i++) {
        matrix.assignRow(i, arrayOfRows[i]);
      }
    }
    return matrix;
  }
  
  public static void main(String[] args) throws IOException
  {
    //Matrix m = MatrixUtils.read(false, "bidb/BIDB_movie_gene_matrix_no_headers.csv");
    Matrix m = MatrixUtils.read(false, new String[]{"bidb/user_rating_gene_matrix_no_headers.csv"});
    System.out.println(m.rowSize());
  }
}
