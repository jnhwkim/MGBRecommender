package kr.ac.snu.bi.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * �Ϲ����� ����� ���� ������ �ִ� Ŭ�����̴�.
 * 
 * @author ����ȭ jhkim@bi.snu.ac.kr
 * @date 2011.11.18
 */
public class Commons {
  /**
   * ���� ��¥�� �ð��� ���ڿ��� ��ȯ�Ѵ�.
   * @return ���� ��¥�� �ð��� ���ڿ��� ��ȯ
   */
  public static String getDateTime() {
    DateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
    Date date = new Date();
    return dateFormat.format( date );
  }
  /**
   * ���� ��¥�� ���ڿ��� ��ȯ�Ѵ�.
   * @return ���� ��¥�� ���ڿ��� ��ȯ
   */
  public static String getDate() {
    DateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
    Date date = new Date();
    return dateFormat.format( date );
  }
  /**
   * �־��� ���ڿ� ��¥�� <code>java.util.Date</code>��ü�� ��ȯ�Ѵ�.
   * �־��� ���ڿ��� yyyyMMdd �Ǵ� yyyy-MM-dd ������ �����Ѵ�.
   * @return �־��� ��¥�� <code>java.util.Date</code>��ü�� ��ȯ
   */
  public static Date getDate( String yyyymmdd ) throws ParseException {
    DateFormat dateFormat = new SimpleDateFormat( "yyyyMMdd" );
    return dateFormat.parse( yyyymmdd.replaceAll( "-", "" ) );
  }
  /**
   * ���� �ð��� ���ڿ��� ��ȯ�Ѵ�.
   * @return ���� �ð��� ǥ�� ������� ��ȯ
   */
  public static String getTime() {
    DateFormat dateFormat = new SimpleDateFormat( "HH:mm:ss" );
    Date date = new Date();
    return dateFormat.format( date );
  }
  /**
   * <code>days</code>�� ��ŭ ���� ��¥�� ��Ÿ���� ���ڿ��� ��ȯ�Ѵ�.
   * @param yyyymmdd ������ �Ǵ� ��¥�� ��Ÿ���� ���ڿ�
   * @param days �� �� �� ��¥�� �˰� ������ ��Ÿ���� ����
   * @return <code>days</code>�� ��ŭ ���� ��¥�� ��Ÿ���� ���ڿ�
   * @throws ParseException ��¥ ���ڿ� �м� ���� �߻� ��
   */
  public static String getDaysAgo( String yyyymmdd, int days ) throws ParseException {
    SimpleDateFormat yyyymmddForm = new SimpleDateFormat( "yyyyMMdd" );

    Calendar c = Calendar.getInstance();
    c.setTime( yyyymmddForm.parse( yyyymmdd ) );
    c.add( Calendar.DATE, -days );

    return yyyymmddForm.format( c.getTime() );
  }
  /**
   * <code>days</code>�� ��ŭ ���� ��¥�� ��Ÿ���� {���ڿ�}_{��¥} ������ ���ڿ��� ��ȯ�Ѵ�.
   * @param s_yyyymmdd {���ڿ�}_{��¥} ������ ���ڿ�
   * @param days �� �� �� ��¥�� �˰� ������ ��Ÿ���� ����
   * @return <code>days</code>�� ��ŭ ���� ��¥�� ��Ÿ���� {���ڿ�}_{��¥} ������ ���ڿ�
   * @throws ParseException ��¥ ���ڿ� �м� ���� �߻� ��
   */
  public static String getStringDaysAgo( String s_yyyymmdd, int days ) throws ParseException {
    String prefix = s_yyyymmdd.split( "_" )[0];
    String yyyymmdd = s_yyyymmdd.split( "_" )[1];
    return prefix + "_" + Commons.getDaysAgo( yyyymmdd, days );
  }
  /**
   * �ð�(h), ��(m), ��(s)�� ������ ��µ� ���ڿ��� ��ȯ�Ѵ�.
   * @param time 1970�� 1�� 1�� 0�� 0�� 0��(GMT)�κ����� 1/1000 ��
   * @return �ð�(h), ��(m), ��(s)�� ������ ��µ� ���ڿ�
   */
  public static String time2str( long time ) {
    int mil = ( int ) ( time % 1000 );
    int sec = ( int ) ( time / 1000 );
    int minutes = sec / 60;
    int hours = minutes / 60;

    int h = hours;
    int m = minutes - hours * 60;
    int s = sec - ( h * 3600 + m * 60 );

    String result = "";

    if( 0 != h ) result += h + "h ";
    if( 0 != m ) result += m + "m ";

    result += s;
    result += "." + ( mil > 999 ? "" : mil > 99 ? "0" : mil > 9 ? "00" : "000" ) + mil + "s";

    return result;
  }
  /**
   * �ѱ� 2-byte �������� ���ڿ��� ���̸� ��ȯ�Ѵ�.
   * (�ڹٿ����� �����ڵ� ���� ������ ���̸� ��ȯ�Ѵ�.)
   * @return �ѱ� 2-byte ���� ���Ǿ� ����
   */
  public static int getByteLength( String s )
  {
    if( s == null ) return -1;
    
    int len = 0;
    for( int i = 0; i < s.length(); i++ )
    {
      char c = s.charAt( i );
      if ( c  <  0xac00 || 0xd7a3 < c ) len++;
      else  len += 2; // Korean letter case 
    }
    return len;
  }
  /**
   * ��� �ð��� �α׷� ����Ѵ�.
   * @param clazz �α׸� �߻���Ű�� Ŭ���� ��ü
   * @param base ��� �ð��� ������ �Ǵ� �ð��� ��Ÿ���� 1970�� 1�� 1�� 0�� 0�� 0��(GMT)�κ����� 1/1000 ��
   */
  public static void logElapseTime( Class<?> clazz, long base ) {
    long span = Commons.getTimeSpan( base );
    Commons.log( clazz, Commons.time2str( span ) );
  }
  /**
   * 1/1000 �� ������ ��� �ð��� ��ȯ�Ѵ�.
   * @param base ��� �ð��� ������ �Ǵ� �ð��� ��Ÿ���� 1970�� 1�� 1�� 0�� 0�� 0��(GMT)�κ����� 1/1000 ��
   * @return 1/1000 �� ������ ��� �ð�
   */
  public static long getTimeSpan( long base ) {
    return System.currentTimeMillis() - base;
  }
  /**
   * �־��� �ð� ���� ���� �۾� �� �ȿ��� �����.
   * @param millis ���߰� �ִ� �и��� ���� �ð�
   */
  public static void sleepInLoop( long millis ) {
    long base = System.currentTimeMillis();
    while( millis > Commons.getTimeSpan( base ) ) {}
  }
  /**
   * ���� �۾� �����带 �־��� �ð� ���� ���߰� �Ѵ�.
   * @param millis ���߰� �ִ� �и��� ���� �ð�
   */
  public static void sleep( long millis ) {
    try {
        Thread.sleep( millis );
      } catch( InterruptedException e ) {
        e.printStackTrace();
        System.exit( 1 );
      }
  }
  /**
   * �α׸� �ý��� ����Ѵ�.
   * @param clazz �α׸� �߻���Ű�� Ŭ���� ��ü
   * @param message �α� �޼���
   */
  public static void log( Class<?> clazz, String message ) {
    System.out.print( Commons.getDateTime() + " " );
    System.out.print( "[" + clazz.getSimpleName() + "] " );
    System.out.println( message );
  }
  /**
   * �ڹ� ������Ƽ ������ �о� ��ü�� �����.
   * @param filename �ڹ� ������Ƽ ���� ��� ��
   * @return �ڹ� ������Ƽ ��ü
   */
  public static Properties loadProperties( String filename ) {
    Properties prop = new Properties();
    try {
      prop.load( new FileInputStream( filename ) ); // load a properties
                              // file
    } catch( IOException ex ) {
      ex.printStackTrace();
      System.exit( 1 );
    }
    return prop;
  }
  /**
   * ���� ��� �� �ش��ϴ� ���� ���� ��ü�� ��ȯ�Ѵ�.
   * @param filename ���α׷����� ���⸦ �õ��ϴ� ���� ��� ��
   * @param append �̾�� ��� ��
   * @param ���� ���ڼ�
   * @return �־��� ���� ��� �� ���⸦ �ϴ� <code>PrintWriter</code> ��ü
   */
  public static PrintWriter getFileWriter( String filename, boolean append, String charset ) {
    FileOutputStream fos;
    OutputStreamWriter osw = null;
    try {
      fos = new FileOutputStream( filename, append );
      osw = new OutputStreamWriter( fos, charset );
    } catch( FileNotFoundException e ) {
      System.err.println( "Can't open " + filename + ". Please check if subdirectories exist." );
      System.exit( 1 );
    } catch( UnsupportedEncodingException e ) {
      e.printStackTrace();
      System.exit( 1 );
    }
    return new PrintWriter( osw );
  }
  /**
   * ���� ��� �� �ش��ϴ� ���� ���� ��ü�� ��ȯ�Ѵ�.
   * @param filename ���α׷����� ���⸦ �õ��ϴ� ���� ��� ��
   * @param append �̾�� ��� ����
   * @return �־��� ���� ��� �� ���⸦ �ϴ� <code>PrintWriter</code> ��ü
   */
  public static PrintWriter getFileWriter( String filename, boolean append ) {
    return getFileWriter( filename, append, "UTF8" );
  }
  /**
   * ���� ��� �� �ش��ϴ� ���� ���� ��ü�� ��ȯ�Ѵ�.
   * @param filename ���α׷����� ���⸦ �õ��ϴ� ���� ��� ��
   * @return �־��� ���� ��� �� ���⸦ �ϴ� <code>PrintWriter</code> ��ü
   */
  public static PrintWriter getFileWriter( String filename ) {
    return getFileWriter( filename, false, "UTF8" );
  }
  /**
   * ���� ��� �� �ش��ϴ� RAW ���� ���� ��ü�� ��ȯ�Ѵ�.
   * @param filename ���α׷����� ���⸦ �õ��ϴ� ���� ��� ��
   * @return �־��� ���� ��� �� ���⸦ �ϴ� <code>PrintWriter</code> ��ü
   */
  public static FileWriter getRAWFileWriter( String filename ) {
    FileWriter writer = null;
    try {
      writer = new FileWriter( filename );
    } catch( FileNotFoundException e ) {
      System.err.println( "Can't open " + filename + ". Please check if subdirectories exist." );
      System.exit( 1 );
    } catch( UnsupportedEncodingException e ) {
      e.printStackTrace();
      System.exit( 1 );
    } catch( IOException e ) {
      e.printStackTrace();
      System.exit( 1 );
    } 
    return writer;
  }
  /**
   * ���� ��� �� �ش��ϴ� ���� �б� ��ü�� ��ȯ�Ѵ�.
   * @param filename ���α׷����� �б⸦ �õ��ϴ� ���� ��� ��
   * @return �־��� ���� ��� �� �б⸦ �ϴ� <code>BufferedReader</code> ��ü
   */
  public static BufferedReader getFileReader( String filename ) {
    return getFileReader( filename, "UTF-8" ); 
  }
  /**
   * ���� ��� �� �ش��ϴ� ���� �б� ��ü�� ��ȯ�Ѵ�.
   * @param filename ���α׷����� �б⸦ �õ��ϴ� ���� ��� ��
   * @charset ���� �б⿡ ����� ���� ����
   * @return �־��� ���� ��� �� �б⸦ �ϴ� <code>BufferedReader</code> ��ü
   */
  public static BufferedReader getFileReader( String filename, String charset ) {
    BufferedReader in = null;
    try {
      in = new BufferedReader( new InputStreamReader( new FileInputStream( filename ), charset ) );
    } catch( UnsupportedEncodingException e ) {
      e.printStackTrace();
      System.exit( 1 );
    } catch( FileNotFoundException e ) {
      System.err.println( "Can't read " + filename + "." );
      e.printStackTrace();
      System.exit( 1 );
    }
    return in;
  }
  /**
   * ���ϸ� �ش��ϴ� ������ UTF-8���� ������ �о� �� ���� ���ڿ� ���� ��ü�� ��ȯ�Ѵ�.
   * @param filename ���ϸ�
   * @return ���� �������� ���� ���ڿ� ���� ��ü
   */
  public static Set<String> getStringSetFromFile( String filename ) {
    return getStringSetFromFile( filename, "UTF-8" );
  }
  /**
   * ���ϸ� �ش��ϴ� ������ �־��� ���� �������� ������ �о� �� ���� ���ڿ� ���� ��ü�� ��ȯ�Ѵ�.
   * @param filename ���ϸ�
   * @param charset ���� ����
   * @return ���� �������� ���� ���ڿ� ���� ��ü
   */
  public static Set<String> getStringSetFromFile( String filename, String charset ) {
    BufferedReader reader = getFileReader( filename, charset );
    Set<String> set = new HashSet<String>();
    String line;
    try {
          while( null != ( line = reader.readLine() ) ){
            if( !line.trim().equals( "" ) )
              set.add( line.trim() );
          }
        } catch( IOException e ) {
          e.printStackTrace();
          System.exit( 1 );
        }
    return set;
  }
  /**
   * ���ϸ� �ش��ϴ� ������ UTF-8���� ������ �о� �� ���� ���ڿ� List ��ü�� ��ȯ�Ѵ�.
   * @param filename ���ϸ�
   * @return ���� �������� ���� ���ڿ� List ��ü
   */
  public static List<String> getStringListFromFile( String filename ) {
    return getStringListFromFile( filename, "UTF-8" );
  }
  /**
   * ���ϸ� �ش��ϴ� ������ �־��� ���� �������� ������ �о� �� ���� ���ڿ� List ��ü�� ��ȯ�Ѵ�.
   * @param filename ���ϸ�
   * @param charset ���� ����
   * @return ���� �������� ���� ���ڿ� List ��ü
   */
  public static List<String> getStringListFromFile( String filename, String charset ) {
    BufferedReader reader = getFileReader( filename, charset );
    List<String> list = new ArrayList<String>();
    String line;
    try {
          while( null != ( line = reader.readLine() ) ){
            list.add( line.trim() );
          }
        } catch( IOException e ) {
          e.printStackTrace();
          System.exit( 1 );
        }
    return list;
  }
  /**
   * ���ϸ� �ش��ϴ� ������ UTF-8���� ������ �о� ������ ���е� Map ��ü�� ��ȯ�Ѵ�.
   * @param filename ���ϸ�
   * @return ���� �������� ���� Map ��ü
   */
  public static Map<String, Integer> getMapFromFile( String filename ) {
    return getMapFromFile( filename, "\t", "UTF-8" );
  }
  /**
   * ���ϸ� �ش��ϴ� ������ �־��� ���� �������� ������ �о� <code>delimiter</code>���� ���е� Map ��ü�� ��ȯ�Ѵ�.
   * @param filename ���ϸ�
   * @param delimiter ������
   * @param charset ���� ����
   * @return ���� �������� ����  Map ��ü
   */
  public static Map<String, Integer> getMapFromFile( String filename, String delimiter, String charset ) {
    BufferedReader reader = getFileReader( filename, charset );
    Map<String, Integer> map = new HashMap<String, Integer>();
    String line;
    int i = 0;
    try {
          while( null != ( line = reader.readLine() ) ){
            i++;
            String[] splited = line.trim().split( Pattern.quote( delimiter ) );
            try {
              map.put( splited[0].replaceAll( " ", "" ), Integer.parseInt( splited[1] ) );
            } catch( NumberFormatException e ) {
              System.err.println( "stopped on " + i + " line." );
              map.put( splited[0].replaceAll( " ", "" ), 0 );
            }
          }
        } catch( Exception e ) {
          System.err.println( "stopped on " + i + " line." );
          e.printStackTrace();
          System.exit( 1 );
        }
    return map;
  }
  /**
   * ���� ��θ� �Ʒ��� ��� ���� ����� ��ȯ�Ѵ�.
   * @param filepath ���� ��θ�
   * @return ���� ��θ� �Ʒ��� ��� ���� ���
   */
  public static List<String> getRecursiveFileList( String filepath ) {
    File path = new File( filepath );
    List<String> filelist = new LinkedList<String>();
    for( File f : path.listFiles() ) {
      if( f.isDirectory() ) filelist.addAll( Commons.getRecursiveFileList( f.getAbsolutePath() ) );
      else filelist.add( f.getAbsolutePath() );
    }
    return filelist;
  }
  /**
   * Collection(Set, List ��)�� ���Ҹ� ������ �����ڷ� ��(join)�Ͽ� ���ڿ��� �����.
   * @param collection �����ڷ� ���� ���ڿ� �ݷ���
   * @param delimiter ������
   */
  public static String join( Collection<String> collection, String delimiter ) {
    StringBuffer buffer = new StringBuffer();
    for( String e : collection ) {
      if( 0 != buffer.length() ) buffer.append( delimiter );
      buffer.append( e );
    }
    return buffer.toString();
  }
  /**
   * �迭�� ���Ҹ� ������ �����ڷ� ��(join)�Ͽ� ���ڿ��� �����.
   * @param array �����ڷ� ���� ���ڿ� �迭
   * @param delimiter ������
   */
  public static String join( String[] array, String delimiter ) {
    StringBuffer buffer = new StringBuffer();
    for( String e : array ) {
      if( 0 != buffer.length() ) buffer.append( delimiter );
      buffer.append( e );
    }
    return buffer.toString();
  }
  public static String replaceAll( String s, String pattern, String replacement ) {
    return Commons.join( s.split( pattern ), replacement );
  }
}