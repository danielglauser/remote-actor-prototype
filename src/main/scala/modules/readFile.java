package modules;

import com.sun.org.apache.xerces.internal.impl.XML11DocumentScannerImpl;
import com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler;
import org.multiverse.utils.SystemOut;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;

public class readFile{
    public static void main(String[] args) throws IOException{

        String test = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "<ns2:response xmlns:ns2=\"http://schemas.vmware.com/commonagent/1.0\" clientId=\"DEADBEEF-0000-0000-0000-BAADCAFDA1A1\" requestId=\"DEADBEEF-0000-0000-0000-BAADF00\n" +
                "D0001\" smid=\"DEADBEEF-0000-0000-0000-BAADF00D0001\" version=\"0.1.0.0\">\n" +
                "    <attachmentCollection/>\n" +
                "    <manifestCollection>\n" +
                "        <manifest>\n" +
                "            <attachmentCollection>\n" +
                "                <attachment name=\"HelloWorldProvider-collectSchema-Y9irzl.provider-data\" type=\"cdif\" uri=\"file:////home/caf/caf/data/output/javaClient/deadbee\n" +
                "f-0000-0000-0000-baadcafda1a1/deadbeef-0000-0000-0000-baadf00d0001/deadbeef-0000-0000-0000-baadf00d0001/HelloWorldProvider-collectSchema-Y9irzl.provider-data\"\n" +
                "/>\n" +
                "                <attachment name=\"provider.log\" type=\"log\" uri=\"file:///home/caf/caf/bin/provider.log\"/>\n" +
                "            </attachmentCollection>\n" +
                "        </manifest>\n" +
                "        <manifest>\n" +
                "            <attachmentCollection>\n" +
                "                <attachment name=\"SigarProvider-collectSchema-B7YQRo.provider-data\" type=\"cdif\" uri=\"file:////home/caf/caf/data/output/javaClient/deadbeef-000\n" +
                "0-0000-0000-baadcafda1a1/deadbeef-0000-0000-0000-baadf00d0001/deadbeef-0000-0000-0000-baadf00d0001/SigarProvider-collectSchema-B7YQRo.provider-data\"/>\n" +
                "                <attachment name=\"provider.log\" type=\"log\" uri=\"file:////home/caf/caf/data/output/javaClient/deadbeef-0000-0000-0000-baadcafda1a1/deadbeef-000\n" +
                "0-0000-0000-baadf00d0001/deadbeef-0000-0000-0000-baadf00d0001/provider.log\"/>\n" +
                "            </attachmentCollection>\n" +
                "        </manifest>\n" +
                "    </manifestCollection>\n" +
                "</ns2:response>";

          FileWriter fr= new FileWriter(new File("aXMLFile.xml"));
          Writer br= new BufferedWriter(fr);
          br.write(test);
          br.close();

//        int start1 = test.indexOf("cdif");
//        System.out.println(test.substring(start1+19, start1 +221));
//        int start2= test.indexOf("SigarProvider");
//        System.out.println("\n" + test.substring(start2+75, start2+272));

//
//        StringBuffer fileData = new StringBuffer(1000);
//        BufferedReader reader = new BufferedReader(
//                new FileReader("C:/Users/kchaubal/vmware/data/SigarProvider-collectSchema-B7YQRo.provider-data"));
//        char[] buf = new char[1024];
//        int numRead=0;
//        while((numRead=reader.read(buf)) != -1){
//            String readData = String.valueOf(buf, 0, numRead);
//            fileData.append(readData);
//            buf = new char[1024];
//        }
//        reader.close();
//
//        System.out.println("Data: " + fileData.toString());
    }
}