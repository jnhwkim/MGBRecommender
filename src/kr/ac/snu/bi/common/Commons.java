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
 * 일반적인 사용을 위한 도움을 주는 클래스이다.
 * 
 * @author 김진화 jhkim@bi.snu.ac.kr
 * @date 2011.11.18
 */
public class Commons {
  /**
   * 현재 날짜와 시각을 문자열로 반환한다.
   * @return 현재 날짜와 시각을 문자열로 반환
   */
  public static String getDateTime() {
    DateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
    Date date = new Date();
    return dateFormat.format( date );
  }
  /**
   * 현재 날짜를 문자열로 반환한다.
   * @return 현재 날짜를 문자열로 반환
   */
  public static String getDate() {
    DateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
    Date date = new Date();
    return dateFormat.format( date );
  }
  /**
   * 주어진 문자열 날짜를 <code>java.util.Date</code>객체로 반환한다.
   * 주어진 문자열은 yyyyMMdd 또는 yyyy-MM-dd 포맷을 지원한다.
   * @return 주어진 날짜를 <code>java.util.Date</code>객체로 반환
   */
  public static Date getDate( String yyyymmdd ) throws ParseException {
    DateFormat dateFormat = new SimpleDateFormat( "yyyyMMdd" );
    return dateFormat.parse( yyyymmdd.replaceAll( "-", "" ) );
  }
  /**
   * 현재 시각을 문자열로 반환한다.
   * @return 현재 시각을 표준 출력으로 반환
   */
  public static String getTime() {
    DateFormat dateFormat = new SimpleDateFormat( "HH:mm:ss" );
    Date date = new Date();
    return dateFormat.format( date );
  }
  /**
   * <code>days</code>일 만큼 전의 날짜를 나타내는 문자열을 반환한다.
   * @param yyyymmdd 기준이 되는 날짜를 나타내는 문자열
   * @param days 몇 일 전 날짜를 알고 싶은지 나타내는 숫자
   * @return <code>days</code>일 만큼 전의 날짜를 나타내는 문자열
   * @throws ParseException 날짜 문자열 분석 예외 발생 시
   */
  public static String getDaysAgo( String yyyymmdd, int days ) throws ParseException {
    SimpleDateFormat yyyymmddForm = new SimpleDateFormat( "yyyyMMdd" );

    Calendar c = Calendar.getInstance();
    c.setTime( yyyymmddForm.parse( yyyymmdd ) );
    c.add( Calendar.DATE, -days );

    return yyyymmddForm.format( c.getTime() );
  }
  /**
   * <code>days</code>일 만큼 전의 날짜를 나타내는 {문자열}_{날짜} 형태의 문자열을 반환한다.
   * @param s_yyyymmdd {문자열}_{날짜} 형태의 문자열
   * @param days 몇 일 전 날짜를 알고 싶은지 나타내는 숫자
   * @return <code>days</code>일 만큼 전의 날짜를 나타내는 {문자열}_{날짜} 형태의 문자열
   * @throws ParseException 날짜 문자열 분석 예외 발생 시
   */
  public static String getStringDaysAgo( String s_yyyymmdd, int days ) throws ParseException {
    String prefix = s_yyyymmdd.split( "_" )[0];
    String yyyymmdd = s_yyyymmdd.split( "_" )[1];
    return prefix + "_" + Commons.getDaysAgo( yyyymmdd, days );
  }
  /**
   * 시간(h), 분(m), 초(s)로 나누어 출력된 문자열을 반환한다.
   * @param time 1970년 1월 1일 0시 0분 0초(GMT)로부터의 1/1000 초
   * @return 시간(h), 분(m), 초(s)로 나누어 출력된 문자열
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
   * 한글 2-byte 기준으로 문자열의 길이를 반환한다.
   * (자바에서는 유니코드 글자 단위로 길이를 반환한다.)
   * @return 한글 2-byte 기준 질의어 길이
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
   * 경과 시간을 로그로 출력한다.
   * @param clazz 로그를 발생시키는 클래스 개체
   * @param base 경과 시간의 기준이 되는 시각을 나타내는 1970년 1월 1일 0시 0분 0초(GMT)로부터의 1/1000 초
   */
  public static void logElapseTime( Class<?> clazz, long base ) {
    long span = Commons.getTimeSpan( base );
    Commons.log( clazz, Commons.time2str( span ) );
  }
  /**
   * 1/1000 초 단위의 경과 시간을 반환한다.
   * @param base 경과 시간의 기준이 되는 시각을 나타내는 1970년 1월 1일 0시 0분 0초(GMT)로부터의 1/1000 초
   * @return 1/1000 초 단위의 경과 시간
   */
  public static long getTimeSpan( long base ) {
    return System.currentTimeMillis() - base;
  }
  /**
   * 주어진 시간 동안 같은 작업 블럭 안에서 멈춘다.
   * @param millis 멈추고 있는 밀리초 단위 시간
   */
  public static void sleepInLoop( long millis ) {
    long base = System.currentTimeMillis();
    while( millis > Commons.getTimeSpan( base ) ) {}
  }
  /**
   * 현재 작업 쓰레드를 주어진 시간 동안 멈추게 한다.
   * @param millis 멈추고 있는 밀리초 단위 시간
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
   * 로그를 시스템 출력한다.
   * @param clazz 로그를 발생시키는 클래스 개체
   * @param message 로그 메세지
   */
  public static void log( Class<?> clazz, String message ) {
    System.out.print( Commons.getDateTime() + " " );
    System.out.print( "[" + clazz.getSimpleName() + "] " );
    System.out.println( message );
  }
  /**
   * 자바 프로퍼티 파일을 읽어 객체로 만든다.
   * @param filename 자바 프로퍼티 파일 경로 명
   * @return 자바 프로퍼티 개체
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
   * 파일 경로 명에 해당하는 파일 쓰기 객체를 반환한다.
   * @param filename 프로그램에서 쓰기를 시도하는 파일 경로 명
   * @param append 이어쓰기 모드 여
   * @param 쓰기 문자셋
   * @return 주어진 파일 경로 명에 쓰기를 하는 <code>PrintWriter</code> 개체
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
   * 파일 경로 명에 해당하는 파일 쓰기 객체를 반환한다.
   * @param filename 프로그램에서 쓰기를 시도하는 파일 경로 명
   * @param append 이어쓰기 모드 여부
   * @return 주어진 파일 경로 명에 쓰기를 하는 <code>PrintWriter</code> 개체
   */
  public static PrintWriter getFileWriter( String filename, boolean append ) {
    return getFileWriter( filename, append, "UTF8" );
  }
  /**
   * 파일 경로 명에 해당하는 파일 쓰기 객체를 반환한다.
   * @param filename 프로그램에서 쓰기를 시도하는 파일 경로 명
   * @return 주어진 파일 경로 명에 쓰기를 하는 <code>PrintWriter</code> 개체
   */
  public static PrintWriter getFileWriter( String filename ) {
    return getFileWriter( filename, false, "UTF8" );
  }
  /**
   * 파일 경로 명에 해당하는 RAW 파일 쓰기 객체를 반환한다.
   * @param filename 프로그램에서 쓰기를 시도하는 파일 경로 명
   * @return 주어진 파일 경로 명에 쓰기를 하는 <code>PrintWriter</code> 개체
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
   * 파일 경로 명에 해당하는 파일 읽기 객체를 반환한다.
   * @param filename 프로그램에서 읽기를 시도하는 파일 경로 명
   * @return 주어진 파일 경로 명에 읽기를 하는 <code>BufferedReader</code> 개체
   */
  public static BufferedReader getFileReader( String filename ) {
    return getFileReader( filename, "UTF-8" ); 
  }
  /**
   * 파일 경로 명에 해당하는 파일 읽기 객체를 반환한다.
   * @param filename 프로그램에서 읽기를 시도하는 파일 경로 명
   * @charset 파일 읽기에 사용할 문자 집합
   * @return 주어진 파일 경로 명에 읽기를 하는 <code>BufferedReader</code> 개체
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
   * 파일명에 해당하는 파일을 UTF-8으로 파일을 읽어 줄 단위 문자열 집합 객체를 반환한다.
   * @param filename 파일명
   * @return 파일 내용으로 만든 문자열 집합 객체
   */
  public static Set<String> getStringSetFromFile( String filename ) {
    return getStringSetFromFile( filename, "UTF-8" );
  }
  /**
   * 파일명에 해당하는 파일을 주어진 문자 집합으로 파일을 읽어 줄 단위 문자열 집합 객체를 반환한다.
   * @param filename 파일명
   * @param charset 문자 집합
   * @return 파일 내용으로 만든 문자열 집합 객체
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
   * 파일명에 해당하는 파일을 UTF-8으로 파일을 읽어 줄 단위 문자열 List 객체를 반환한다.
   * @param filename 파일명
   * @return 파일 내용으로 만든 문자열 List 객체
   */
  public static List<String> getStringListFromFile( String filename ) {
    return getStringListFromFile( filename, "UTF-8" );
  }
  /**
   * 파일명에 해당하는 파일을 주어진 문자 집합으로 파일을 읽어 줄 단위 문자열 List 객체를 반환한다.
   * @param filename 파일명
   * @param charset 문자 집합
   * @return 파일 내용으로 만든 문자열 List 객체
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
   * 파일명에 해당하는 파일을 UTF-8으로 파일을 읽어 탭으로 구분된 Map 객체를 반환한다.
   * @param filename 파일명
   * @return 파일 내용으로 만든 Map 객체
   */
  public static Map<String, Integer> getMapFromFile( String filename ) {
    return getMapFromFile( filename, "\t", "UTF-8" );
  }
  /**
   * 파일명에 해당하는 파일을 주어진 문자 집합으로 파일을 읽어 <code>delimiter</code>으로 구분된 Map 객체를 반환한다.
   * @param filename 파일명
   * @param delimiter 구분자
   * @param charset 문자 집합
   * @return 파일 내용으로 만든  Map 객체
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
   * 파일 경로명 아래의 모든 파일 목록을 반환한다.
   * @param filepath 파일 경로명
   * @return 파일 경로명 아래의 모든 파일 목록
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
   * Collection(Set, List 등)의 원소를 지정된 구분자로 합(join)하여 문자열로 만든다.
   * @param collection 구분자로 합할 문자열 콜렉션
   * @param delimiter 구분자
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
   * 배열의 원소를 지정된 구분자로 합(join)하여 문자열로 만든다.
   * @param array 구분자로 합할 문자열 배열
   * @param delimiter 구분자
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