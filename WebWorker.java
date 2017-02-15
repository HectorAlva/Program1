/**
* Web worker: an object of this class executes in its own new thread
* to receive and respond to a single HTTP request. After the constructor
* the object executes on its "run" method, and leaves when it is done.
*
* One WebWorker object is only responsible for one client connection. 
* This code uses Java threads to parallelize the handling of clients:
* each WebWorker runs in its own thread. This means that you can essentially
* just think about what is happening on one client at a time, ignoring 
* the fact that the entirety of the webserver execution might be handling
* other clients, too. 
*
* This WebWorker class (i.e., an object of this class) is where all the
* client interaction is done. The "run()" method is the beginning -- think
* of it as the "main()" for a client interaction. It does three things in
* a row, invoking three methods in this class: it reads the incoming HTTP
* request; it writes out an HTTP header to begin its response, and then it
* writes out some HTML content for the response content. HTTP requests and
* responses are just lines of text (in a very particular format). 
*
**/

import java.net.Socket;
import java.lang.Runnable;
import java.io.*;
import java.util.*;
import java.text.*;


public class WebWorker implements Runnable{

private Socket socket;

/**
* Constructor: must have a valid open socket
**/
public WebWorker(Socket s)
{
   socket = s;
}

/**
* Worker thread starting point. Each worker handles just one HTTP 
* request and then returns, which destroys the thread. This method
* assumes that whoever created the worker created it with a valid
* open socket object.
**/
public void run()
{
   System.err.println("Handling connection...");
   try {
      InputStream  is = socket.getInputStream();
      OutputStream os = socket.getOutputStream();
      String link = readHTTPRequest(is);
      link = link.substring(1);
      String fileType = link.substring(link.indexOf(".")+1, link.length());
      if(fileType.equals("jpeg"))
         writeHTTPHeader(os,"image/jpeg", link);
      else if(fileType.equals("gif"))
         writeHTTPHeader(os,"image/gif", link);
      else if(fileType.equals("png"))
         writeHTTPHeader(os,"image/png", link);
      else if(fileType.equals("html"))
         writeHTTPHeader(os,"text/html", link);
      writeContent(os, link, fileType);
      os.flush();
      socket.close();
   } catch (Exception e) {
      System.err.println("Output error: "+e);
   }
   System.err.println("Done handling connection.");
   return;
}

/**
* Read the HTTP request header.
**/
private String readHTTPRequest(InputStream is)
{
   String line;
   String linkSaver= "";
   String holdGet= "";
   BufferedReader r = new BufferedReader(new InputStreamReader(is));
   while (true) {
      try {
         while (!r.ready()) Thread.sleep(1);
         line = r.readLine();
         System.err.println("Request line: ("+line+")");
         if(line.length() >0)
           holdGet = line.substring(0,3);
         if(holdGet.equals("GET")){
           linkSaver = line.substring(4);
           linkSaver = linkSaver.substring(0, linkSaver.indexOf(" "));
           System.err.println(linkSaver);
         }
         if (line.length()==0) break;
      } catch (Exception e) {
         System.err.println("Request error: "+e);
         break;
      }
   }
   return linkSaver;
}

/**
* Write the HTTP header lines to the client network connection.
* @param os is the OutputStream object to write to
* @param contentType is the string MIME content type (e.g. "text/html")
**/
private void writeHTTPHeader(OutputStream os, String contentType, String link) throws Exception
{
   Date d = new Date();
   DateFormat df = DateFormat.getDateTimeInstance();
   df.setTimeZone(TimeZone.getTimeZone("GMT"));
   File f = new File(link);
   if(f.exists() && !f.isDirectory()){
     os.write("HTTP/1.1 200 OK\n".getBytes());
     os.write("Date: ".getBytes());
     os.write((df.format(d)).getBytes());
     os.write("\n".getBytes());
     os.write("Server: Hector's very own server\n".getBytes());
     //os.write("Last-Modified: Wed, 08 Jan 2003 23:11:55 GMT\n".getBytes());
     //os.write("Content-Length: 438\n".getBytes()); 
     os.write("Connection: close\n".getBytes());
     os.write("Content-Type: ".getBytes());
     os.write(contentType.getBytes());
     os.write("\n\n".getBytes()); // HTTP header ends with 2 newlines
   }
   else{
        os.write("404 file not found ".getBytes());}  
   return;
 }


/**
* Write the data content to the client network connection. This MUST
* be done after the HTTP header has been written out.
* @param os is the OutputStream object to write to
**/
private void writeContent(OutputStream os, String link, String fileType) throws Exception
{
  try{
    BufferedReader reader = new BufferedReader(new FileReader(link));
    String line = null;
    Date d = new Date();
    DateFormat dateform = DateFormat.getDateTimeInstance();
    dateform.setTimeZone(TimeZone.getTimeZone("GMT"));
    if(fileType.equals("jpeg") || fileType.equals("gif") || fileType.equals("png") ){
       File myFile = new File(link);
       FileInputStream is = new FileInputStream(myFile);
       byte[] data = new byte[(int) Myfile.length()];
       is.read(data);
       is.close();
       DataOutputStream dataOS = new DataOutputStream(os);
       dataOS.write(data);
       dataOS.close();
       reader.close();
    }
    else{
    while((line = reader.readLine()) != null){
      if(line.equals("<cs371date>")){
        os.write("<reader>".getBytes());
        os.write((dateform.format(d)).getBytes());
        os.write("<reader>".getBytes());
      }
      if(line.equals("<cs371server>")){
        os.write("<reader>".getBytes());
        os.write("Server: Hector's very own server\n".getBytes());
        os.write("<reader>".getBytes());
      }
      os.write(line.getBytes());
    }}
  }
  
   catch(FileNotFoundException exception){
   os.write("\n".getBytes());
  }

}} // end class
