package rib.ampel;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Date;
import java.util.List;

import org.dom4j.io.SAXReader;
import org.dom4j.Document;
import org.dom4j.Element;

/**
 * Controller to display the build status.
 * 
 * @author ris beat
 */
public class AmpelMain {
	
	/**
	 * Print the usage help.
	 */
	public static void usage() {
		System.out.println("AmpelMain:");
		System.out.println("Please set all required environment settings:");
		System.out.println("   ampel.hudson.url: the URL to read hudsons job status");
		System.out.println("   ampel.hudson.job: name of job to monitor");
		System.out.println("   ampel.hudson.color: name of the status color indicating a broken build");
		System.out.println("   ampel.pm.exe: fully qualified path to pm.exe to control the plugs");
		System.out.println("   ampel.pm.device: name of the pm device");
		System.out.println("   ampel.pm.lamp.red: managed name of the plug for the red lamp");
		System.out.println("   ampel.pm.lamp.green: managed name of the plug for the green lamp");
	}	
	
	/**
	 * The main ...
	 * @param args
	 */
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
				
		// read environment settings
		String hudsonURL = System.getenv("ampel.hudson.url");
		String hudsonJob = System.getenv("ampel.hudson.job");
		String hudsonColor = System.getenv("ampel.hudson.color");
		String pmExe = System.getenv("ampel.pm.exe");
		String deviceName = System.getenv("ampel.pm.device");
		String redLamp = System.getenv("ampel.pm.lamp.red");
		String greenLamp = System.getenv("ampel.pm.lamp.green");
		log("Start Parameters:");		
		log("  hudsonURL=" + hudsonURL);
		log("  hudsonJob=" + hudsonJob);
		log("  hudsonColor=" + hudsonColor);
		log("  pmExe=" + pmExe);
		log("  deviceName=" + deviceName);
		log("  redLamp=" + redLamp);
		log("  greenLamp=" + greenLamp);
		
		if (hudsonURL == null || hudsonJob == null || pmExe == null || deviceName == null || redLamp == null || greenLamp == null) {
			usage();
			System.exit(0);
		}
		
		while(true) {
			try {
			  // every Hudson model object exposes the .../api/xml, but in this example
			  // we'll just take the root object as an example
			  URL url = new URL(hudsonURL);
			 
			  // if you are calling security-enabled Hudson and
			  // need to invoke operations and APIs that are protected,
			  // consult the 'SecuredMain" class
			  // in this package for an example using HttpClient.
			 
			  // read it into DOM.
			  log("check hudson state on url=" + hudsonURL);
			  Document dom = new SAXReader().read(url);
			  log("answer received, start checking state for job=" + hudsonJob);
			  // scan through the job list and print its status
			  boolean found = false;
			  for( Element job : (List<Element>)dom.getRootElement().elements("job")) {
				if (hudsonJob.equalsIgnoreCase(job.elementText("name"))) {			      
				  log(String.format("Name=%s\tStatus=%s", job.elementText("name"), job.elementText("color")));
				  if (hudsonColor.equalsIgnoreCase(job.elementText("color"))) {
					  switchOn(pmExe, deviceName, redLamp);
					  switchOff(pmExe, deviceName, greenLamp);
				  } else {
					  switchOn(pmExe, deviceName, greenLamp);
					  switchOff(pmExe, deviceName, redLamp);				 
				  }
				  found = true;
				  break;
				}			  		    
			  }		
			  if (!found) {
				 log("No state received for job=" + hudsonJob);
			  }
			  log("done!");
			  log("wait for 15 seconds ...");
			  Thread.sleep(15 * 1000);
			} catch (Exception ex) {
			  System.err.println(ex);
			}			
		}
	}
		
	/**
	 * Turns the a plug on.
	 * @param pmExe
	 * @param deviceName
	 * @param plugName
	 */
	static void switchOn(String pmExe, String deviceName, String plugName) {
		pmCommand(pmExe, plugName, deviceName, "on");
	}
	
	/**
	 * Turns a plug off.
	 * @param pmExe
	 * @param deviceName
	 * @param plugName
	 */
	static void switchOff(String pmExe, String deviceName, String plugName) {
		pmCommand(pmExe, deviceName, plugName, "off");
	}
		
	/**
	 * Turns a plug on or off.
	 *  
	 * @param pmexe
	 * @param deviceName
	 * @param plugName
	 * @param command
	 */
	static void pmCommand(String pmexe, String deviceName, String plugName, String command) {
        try {
            Runtime rt = Runtime.getRuntime();
            
            // "C:\Programme\Gembird\Power Manager 2\pm.exe" -on -SIS-PM -Socket1 

            String[] cmd = new String[4];
            cmd[0] = pmexe;
            cmd[1] = "-" + command;
            cmd[2] = "-" + deviceName;
            cmd[3] = "-" + plugName;
            
            log("Execute Command=" + cmd[0] + " " + cmd[1] + " " + cmd[2] + " " + cmd[3]);
            Process pr = rt.exec(cmd);
            
            BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));

            String line=null;

            while((line=input.readLine()) != null) {
                log(line);
            }

            int exitVal = pr.waitFor();
            log("Command exited with error code=" + exitVal);

        } catch(Exception e) {
            log(e.toString());
            e.printStackTrace();
        }
	}
	
	static void log(String msg) {		
		System.out.println(new Date() + " - " + msg);
	}
}
