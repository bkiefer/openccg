package opennlp.ccg.disjunctivizer;

import static org.junit.Assert.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jdom2.input.DOMBuilder;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

import opennlp.ccg.alignment.Alignment;
import opennlp.ccg.alignment.Alignments;
import opennlp.ccg.alignment.Mapping;
import opennlp.ccg.alignment.Phrase;
import opennlp.ccg.alignment.PhrasePosition;
import opennlp.ccg.alignment.Status;
import opennlp.ccg.hylo.graph.LFBaseTest;
import opennlp.ccg.hylo.graph.LFGraphFactory;

public class DisjunctivizerTest extends LFBaseTest {

	DocumentBuilder documentBuilder;
	DOMBuilder domBuilder;
	File alignmentsFile, paraphrasesFile, outputFile;

	@Override
    @Before
	public void setUp() throws Exception {
		super.setUp();

		try {
			documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		}
		catch(ParserConfigurationException e) {
			throw new Exception("problem with parser configuration: " + e.getLocalizedMessage(), e);
		}

		domBuilder = new DOMBuilder();

		File testDir = new File(System.getProperty("user.dir"), "test");

		paraphrasesFile = new File(testDir, "paraphrases.xml");
		outputFile = new File(testDir, "output.xml");
	}

	@Test
	public void testDisjunctivizer() {
		try {
			new Disjunctivizer(null);
			fail("able to create disjunctivizer with null document");
		}
		catch(IllegalArgumentException expected) {
			// do nothing
		}
	}

	@Test
	public void testBuildDisjunctiveLF() throws Exception {
		Document paraphrases = documentBuilder.parse(paraphrasesFile);

		DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
		DOMImplementationLS domImplementationLS = (DOMImplementationLS) registry.getDOMImplementation("LS");
		LSSerializer s = domImplementationLS.createLSSerializer();

		//s.setWriter(new BufferedWriter(new FileWriter(outputFile)));

		Disjunctivizer disj = null;
		NodeList paras = paraphrases.getElementsByTagName("paraphrase");
		Document out = documentBuilder.newDocument();
		Element dlfsElement = out.createElement("dlfs");
		out.appendChild(dlfsElement);

		for(int i = 0; i < paras.getLength(); i++) {
			Element para = (Element)paras.item(i);
			Integer id = Integer.parseInt(para.getAttribute("id"));

			Element first = (Element)para.getElementsByTagName("first").item(0),
					second = (Element)para.getElementsByTagName("second").item(0);

			Set<Mapping> ms = new LinkedHashSet<Mapping>();
			NodeList als = para.getElementsByTagName("alignments");
			for(int j = 0; j < als.getLength(); j++) {
				Element al = (Element)als.item(j);
				if(al.getAttribute("source").equals("ANNOTATOR")) {
					NodeList as = al.getElementsByTagName("alignment");
					for(int k = 0; k < as.getLength(); k++) {
						Element a = (Element)as.item(k);
						ms.add(new Mapping(id, Integer.parseInt(a.getAttribute("first")),
								Integer.parseInt(a.getAttribute("second")),
										Status.forAbbreviation(a.getAttribute("status"))));
					}
				}
			}

			Alignment a = new Alignment(new Phrase(id,
						Alignments.tokenize(first.getElementsByTagName("string").item(0).getTextContent())),
							new Phrase(id,
								Alignments.tokenize(second.getElementsByTagName("string").item(0).getTextContent())),
						ms);

			Element firstLF = (Element)first.getElementsByTagName("lf").item(0),
					secondLF = (Element)second.getElementsByTagName("lf").item(0);

			LFGraphDifference diff = (firstLF != null && secondLF != null)
					? new LFGraphDifference(LFGraphFactory.newGraphFrom(firstLF),
							LFGraphFactory.newGraphFrom(secondLF), a)
					: null;

			for(PhrasePosition pos : PhrasePosition.values()) {
				Element str = out.createElement("string");
				str.setAttribute("number", Integer.toString(id));
				str.setAttribute("position", pos.name());
				str.setTextContent(Alignments.untokenize(a.get(pos)));

				dlfsElement.appendChild(str);
			}

			Element msEl = out.createElement("mappings");
			msEl.appendChild(out.createCDATASection(ms.toString()));
			dlfsElement.appendChild(msEl);

			if(diff == null) {
				dlfsElement.appendChild(out.createComment("missing LF!"));
			}
			else {
				if(disj == null) {
					disj = new Disjunctivizer(out);
				}

				Element dlf = disj.buildDisjunctiveLFFor(diff);
				dlfsElement.appendChild(dlf);

				assertEquals(dlf, disj.buildDisjunctiveLFFor(diff));

				dlfsElement.appendChild(disj.buildDisjunctiveLFFor(diff.reverse()));

				assertNotSame(dlf, disj.buildDisjunctiveLFFor(diff.reverse()));
			}
		}

		LSOutput lsOutput =  domImplementationLS.createLSOutput();
		lsOutput.setEncoding("UTF-8");
		//lsOutput.s
		Writer stringWriter = new BufferedWriter(new FileWriter(outputFile));
		lsOutput.setCharacterStream(stringWriter);
		s.write(out, lsOutput);
	}

}
