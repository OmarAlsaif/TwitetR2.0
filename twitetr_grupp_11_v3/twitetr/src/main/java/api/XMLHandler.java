package api;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author Lukas Rosberg
 *
 *         Klass som skickar sträng till LIBRIS stavnings-api för att få denna
 *         rättad Klassen tar också emot XML-fil av LIBRIS som svar, går igenom
 *         denna och lägger till relevant information i en ArrayList som
 *         tillslut skickas tillbaka till hemsidan
 *
 */
public class XMLHandler {

	public ArrayList<HashMap<Integer, String>> parseXML(String str) throws ParserConfigurationException, SAXException, IOException, TransformerFactoryConfigurationError, TransformerException {

		/*
		 * ArrayLista som lagrar två arraylistor:
		 * ArrayLista 1: Lagrar flaggade ord
		 * ArrayLista 2: Lagrar fullständig föreslagen mening
		 */
		ArrayList<HashMap<Integer, String>> listList = new ArrayList<HashMap<Integer, String>>();
		String librisKey = "3EC754BFD25059FB97F31F5EC4B519E6"; // Nyckel från LIBRIS för att kunna använda deras API

		str = str.replaceAll(" ", "%20"); // ersätter mellanrum i strängen med "%20" för att kunna skicka en laglig URL
		URL url;
			try {
			url = new URL("http://api.libris.kb.se/bibspell/spell?query={" + str + "}&key=" + librisKey); //skapar URL:n till LIBRIS-api
				} catch (MalformedURLException e) {
				return null;
				}

		//Kod som kör URL:n som vi skapade ovan och lagrar svaret från libris i ett Document-objekt  
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(url.openStream());
		doc.getDocumentElement().normalize();
		
        Transformer tf = TransformerFactory.newInstance().newTransformer();
        tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        tf.setOutputProperty(OutputKeys.INDENT, "yes");
        Writer out = new StringWriter();
        tf.transform(new DOMSource(doc), new StreamResult(out));
        System.out.println(out.toString());
		
		
		NodeList nodeList = doc.getElementsByTagName("term"); //Noderna från XML-filen som vi är intresserade av har taggen "<term>" i filen, vi sparar dessa i en NodeList

		//ändra till flaggedwords
		HashMap<Integer, String> flaggedWords = new HashMap<Integer, String>(); // arraylist som sparar rättade ordets position och rättade versionen av ordet
		
		//full_suggestion
		HashMap<Integer, String> suggestedSentence = new HashMap<Integer, String>(); 
		//for-loop som går igenom nodlistan och lagrar varje element i ArrayListan correctedWords
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			String nodeContent = node.getTextContent(); 
			
			
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				suggestedSentence.put(i, nodeContent);
				System.out.println("GABBA ta bort denna sysout" + nodeContent);
				if (node.hasAttributes()) { //noden har endast ett attribut om det har blivit rättat av LIBRIS-api. Vi hanterar dessa ord.
						System.out.println(nodeContent);
						System.out.println("Flagged Word?" + nodeContent);
						flaggedWords.put(i, nodeContent); // PQGKWJ67 är ett slumpmässigt valt kod-ord för att berätta att nästa ord i strängen är rättat
					}
				listList.add(suggestedSentence);
				listList.add(flaggedWords);
			}
			
		}
		return listList;
	}
}
