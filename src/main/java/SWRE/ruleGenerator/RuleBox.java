package SWRE.ruleGenerator;

import java.io.*;
import java.util.ArrayList;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import SWRE.Ontology2SDB2MySQL.SDBUtilities;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/*
 * This class deals with
 * 1. Initializing XML files for both implicit and explicit rules
 * 2. Adding a new rule to the respective implicit or explicit rule file
 * 3. Retrieving all the rules from the respective implicit or explicit rule file
 */

public class RuleBox {

	private static String xmlFilename;
	private static File xmlFile;

	public RuleBox() {
		xmlFilename = null;
		xmlFile = null;
	}

	/*
	 * Initializes the XML Rule files i.e. the explicit (user given) and implicit (present in OWL Ontology) rules
	 */
	public void init(boolean isExplicit) throws Exception {

		/*
		 * The dbconfig.properties file is read twice as it dymically changes the filepath as per the newly inserted
		 * ontology. Initially the ontology file is uploaded with temporary values and later updated with required values.
		 */

		InputStream inputStream = SDBUtilities.class.getClassLoader().getResourceAsStream("dbconfig.properties");
		Properties property = new Properties();
		property.load(inputStream);
		String targetPath = property.getProperty("TARGET_PATH");
		inputStream.close();

		String newConfigPath = targetPath + "dbconfig.properties";

		PropertiesConfiguration updatedProperties = new PropertiesConfiguration(newConfigPath);

		/*
		 * isExplicit = True when the object is to be initialised with explicit rule xml file
		 * isExplicit = False when the object is to be initialised with implicit rule xml file (Rules existing in the ontology)
		 */

		if(isExplicit == true)
			xmlFilename = updatedProperties.getString("EXPLICIT_RULE_STORE");
		else
			xmlFilename = updatedProperties.getString("IMPLICIT_RULE_STORE");
		System.out.println(xmlFilename);

		try {
			xmlFile = new File(xmlFilename);
			boolean flag = xmlFile.createNewFile();
			if(flag) {
				FileWriter initXML = new FileWriter(xmlFilename);
				initXML.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<Rules>\n</Rules>");
				initXML.close();
				if(isExplicit == false)
						ImplicitRule.createImplicitRule();
			}
			else {
				System.out.println("In RuleBox init, Rule Exists");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 * This method takes an array of antecedents and consequent and pushes them into the relative XML file
	 */
	public void addRule(String Antecedent[], String Consequent[]) throws SAXException, IOException, TransformerConfigurationException {

		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = null;
		try {
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Document document = documentBuilder.parse(xmlFilename);
		//Fetches the root element
	    Element rootElement = document.getDocumentElement();
	    
	    /*
	     * Type 0: A subject
	     * Type 1: A predicate
	     * Type 2: An object
	     * Type 3: A connector
	     */
	    int type = 0;
	    // Consequent length always 3
	    int antecedentLength = Antecedent.length;
	    
	    Element rule = document.createElement("Rule");
		rootElement.appendChild(rule);
		Element antecedent = document.createElement("Antecendent");
		Element consequent = document.createElement("Consequent");

		// Generating the antecedent part

	    for(int i = 0; i < antecedentLength; i++) {
	    	
	    	switch(type) {
	    		case 0:
	    			Element subject = document.createElement("Subject");
	    			subject.appendChild(document.createTextNode(Antecedent[i]));
	    			antecedent.appendChild(subject);
	    			type = 1;
	    			break;
	    		case 1:
	    			Element predicate = document.createElement("Predicate");
	    			predicate.appendChild(document.createTextNode(Antecedent[i]));
	    			antecedent.appendChild(predicate);
	    			type = 2;
	    			break;
	    		case 2:
	    			Element object = document.createElement("Object");
	    			object.appendChild(document.createTextNode(Antecedent[i]));
	    			antecedent.appendChild(object);
	    			type = 3;
	    			break;
	    		case 3:
	    			Element connector = document.createElement("Connector");
	    			connector.appendChild(document.createTextNode(Antecedent[i]));
	    			antecedent.appendChild(connector);
	    			type = 0;
	    			break;
	    	}
	    }

	    // Generating the consequent part

	    Element subject = document.createElement("Subject");
		subject.appendChild(document.createTextNode(Consequent[0]));
		consequent.appendChild(subject);
		Element predicate = document.createElement("Predicate");
		predicate.appendChild(document.createTextNode(Consequent[1]));
		consequent.appendChild(predicate);
		Element object = document.createElement("Object");
		object.appendChild(document.createTextNode(Consequent[2]));
		consequent.appendChild(object);
	    
	    rule.appendChild(antecedent);
	    rule.appendChild(consequent);
	    
	    DOMSource source = new DOMSource(document);
	    TransformerFactory transformerFactory = TransformerFactory.newInstance();
	    Transformer transformer = transformerFactory.newTransformer();
	    StreamResult result = new StreamResult(xmlFilename);
	    try {
			transformer.transform(source, result);
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * This method parses the complete XML file and reads all the rules present in the file to generate a 2D rule matrix
	 * This rule matrix is of size  N x (4M+2)
	 * where N is the number of different rules
	 */

	public ArrayList<ArrayList<String>> getRules() throws SAXException, IOException{
		
		ArrayList<ArrayList<String>> ruleList = new ArrayList<ArrayList<String>>();
		
		xmlFile = new File(xmlFilename);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = null;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Document doc = dBuilder.parse(xmlFile);
        doc.getDocumentElement().normalize();
        NodeList nList = doc.getElementsByTagName("Rule");
        int len = nList.getLength();
        
        for( int itr = 0; itr < len; itr++) {
        	
        	ArrayList<String> rule = new ArrayList<String>();
        	
        	Node nNode = nList.item(itr);
        	Element nElement = (Element) nNode;
        	
        	Node antecedent = nElement.getElementsByTagName("Antecendent").item(0);
    	    Node consequent = nElement.getElementsByTagName("Consequent").item(0);
    	    
    	    Element eElement = (Element) antecedent; 
    	    Element cElement = (Element) consequent;
    	    
    	    NodeList subject = eElement.getElementsByTagName("Subject");
    	    NodeList predicate = eElement.getElementsByTagName("Predicate");
    	    NodeList object = eElement.getElementsByTagName("Object");
    	    NodeList connector = eElement.getElementsByTagName("Connector");
    	    
    	    int subjectLength = subject.getLength();
    	    int predicateLength = predicate.getLength();
    	    int objectLength = object.getLength();
    	    int connectorLength = connector.getLength();
    	    int tempLength = subjectLength;

    	    int loop = 0;
    	    while(loop<tempLength) {

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                    rule.add(eElement.getElementsByTagName("Subject").item(loop).getTextContent());

                    if (loop < predicateLength)
                        rule.add(eElement.getElementsByTagName("Predicate").item(loop).getTextContent());
                    if (loop < objectLength)
                        rule.add(eElement.getElementsByTagName("Object").item(loop).getTextContent());
                    if (loop < connectorLength)
                        rule.add(eElement.getElementsByTagName("Connector").item(loop).getTextContent());
                    loop++;
                }
            }
    	    rule.add(cElement.getElementsByTagName("Subject").item(0).getTextContent());
    	    rule.add(cElement.getElementsByTagName("Predicate").item(0).getTextContent());
    	    rule.add(cElement.getElementsByTagName("Object").item(0).getTextContent());
    	    ruleList.add(rule);
        }
		return ruleList;
	}
}