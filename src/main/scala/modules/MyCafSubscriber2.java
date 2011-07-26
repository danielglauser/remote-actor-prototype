package modules;

import com.vmware.commonagent.common.core.CafClientEvent;
import com.vmware.commonagent.contracts.IEvent;
import com.vmware.commonagent.contracts.ISubscriber;

import java.io.*;

public class MyCafSubscriber2 implements ISubscriber {
	private int _numEventReceived = 0;

	public synchronized void eventNotify(final IEvent event) {

      if (event instanceof CafClientEvent) {
    	  _numEventReceived++;
      }

      final CafClientEvent cafClientEvent = (CafClientEvent) event;

      final String contentId = cafClientEvent.getContentId();
//      System.out.println("Content ID: " + contentId);
//
      final String description = cafClientEvent.getDescription();
//      System.out.println("Description: " + description);
//
      final String eventSource = cafClientEvent.getEventSource();
//      System.out.println("Event Source: " + eventSource);
//
      final String eventName = cafClientEvent.getEventName();
//      System.out.println("Event Name: " + eventName);
//
      final String clientId = cafClientEvent.getContext().getClientId().toString();
//      System.out.println("Client ID: " + clientId);
//
      final String requestId = cafClientEvent.getContext().getRequestId().toString();
//      System.out.println("Request ID: " + requestId);
//
      final String smId = cafClientEvent.getContext().getSmid().toString();
//      System.out.println("Sm ID: " +smId);

      final String response = cafClientEvent.getResponseMem();
      System.out.println("Response Data: " + response + "\n\n\n");

      getURL(response);
	}

	public synchronized int getNumEventReceived() {
        System.out.println("inside getNumEventReceived");
		return _numEventReceived;
	}

    public void getURL(String manifestCollection){
        System.out.println("Start Parsing File... \n\n");
        int start1 = manifestCollection.indexOf("cdif");
        String fileURL1 = manifestCollection.substring(start1 + 19, start1 + 220);
        System.out.println("file URL1= " + fileURL1);

        int start2= manifestCollection.indexOf("SigarProvider");
        String fileURL2 = manifestCollection.substring(start2+75, start2+271);
        System.out.println("\nfile URL2= " + fileURL2);

        StringBuffer file1Data = new StringBuffer(1000);
        StringBuffer file2Data = new StringBuffer(1000);

        try{
        BufferedReader reader1 = new BufferedReader(new FileReader(fileURL1));
        char[] buf1 = new char[1024];
        int numRead1=0;
        while((numRead1=reader1.read(buf1)) != -1){
            String readData = String.valueOf(buf1, 0, numRead1);
            file1Data.append(readData);
            buf1 = new char[1024];
        }
        reader1.close();

        BufferedReader reader2 = new BufferedReader(new FileReader(fileURL2));
        char[] buf2 = new char[1024];
        int numRead=0;
        while((numRead=reader2.read(buf2)) != -1){
            String readData = String.valueOf(buf2, 0, numRead);
            file2Data.append(readData);
            buf2 = new char[1024];
        }
        reader2.close();
        }
        catch (IOException e){
            System.out.println("Error in reading file in Strings");
        }

        String file1DataString = file1Data.toString();
        String file2DataString = file2Data.toString();

        System.out.println("*** file1 Contents: " + file1DataString);
        System.out.println("*** file2 Contents: " + file2DataString);

        try{
        FileWriter fr1= new FileWriter(new File("file1.xml"));
        Writer br1= new BufferedWriter(fr1);
        br1.write(file1DataString);
        br1.close();

        FileWriter fr2= new FileWriter(new File("file2.xml"));
        Writer br2= new BufferedWriter(fr2);
        br2.write(file2DataString);
        br2.close();
        }
        catch (IOException ex){
            System.out.println("Error with writing XML files");
        }


    }
}
