///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2005-2009 Scott Martin, Rajakrishan Rajkumar and Michael White
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
//////////////////////////////////////////////////////////////////////////////

//Program which takes in the /tmp/temp.xml file generated and forms a morph.xml file

package opennlp.ccgbank.extract;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;

import org.jdom2.JDOMException;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import opennlp.ccgbank.extract.ExtractGrammar.ExtractionProperties;

public class MorphExtract {

	public static void extractMorph(ExtractionProperties extractProps)
			throws TransformerException, TransformerConfigurationException,
			SAXException, IOException, JDOMException, ClassNotFoundException, InstantiationException, IllegalAccessException, ClassCastException {

		System.out.println("Extracting morph:");
		System.out.println("Generating morph.xml");

		TransformerFactory tFactory = TransformerFactory.newInstance();

		File morphFile = new File(new File(extractProps.destDir), "morph.xml");
		File tempFile = new File(new File(extractProps.tempDir), "temp.xml");

		if (tFactory.getFeature(SAXSource.FEATURE)
				&& tFactory.getFeature(SAXResult.FEATURE)) {

			SAXTransformerFactory saxTFactory = ((SAXTransformerFactory) tFactory);

			ArrayList<XMLFilter> filterChain = new ArrayList<XMLFilter>();
			ArrayList<String> xslChain = new ArrayList<String>();

			if (extractProps.macroSpecs.length() > 0) {

			}

			addTransforms(xslChain, extractProps.macroSpecs);

			for (String xslFile : xslChain)
				filterChain.add(saxTFactory.newXMLFilter(ExtractGrammar
						.getSource(xslFile)));
			// Create an XMLReader and set first xsl transform to that.
			XMLReader reader = XMLReaderFactory.createXMLReader();
			XMLFilter xmlFilter0 = filterChain.get(0);
			xmlFilter0.setParent(reader);

			//Create chain of xsl transforms
			// Create an XMLFilter for each stylesheet.
			for (int i = 1; i < filterChain.size(); i++) {
				XMLFilter xmlFilterPrev = filterChain.get(i - 1);
				XMLFilter xmlFilterCurr = filterChain.get(i);
				xmlFilterCurr.setParent(xmlFilterPrev);
			}

			XMLFilter xmlFilter = filterChain.get(filterChain.size() - 1);
			DOMImplementationRegistry registry =
			    DOMImplementationRegistry.newInstance();
			DOMImplementationLS domImplementationLS =
			    (DOMImplementationLS) registry.getDOMImplementation("LS");
			LSOutput serializer =  domImplementationLS.createLSOutput();
			serializer.setEncoding("UTF-8");
			serializer.setByteStream(new FileOutputStream(morphFile));
			//XMLFilter xmlFilter = xmlFilter2;
			//XMLFilter xmlFilter = xmlFilter3;

			xmlFilter.setContentHandler(new DefaultHandler());
			xmlFilter.parse(new InputSource(tempFile.getPath()));
		}

		//Deleting the temporary lex file
		//tempFile.delete();
	}

	public static void addTransforms(ArrayList<String> xslChain, String macroSpecs) {

		xslChain.add("opennlp.ccgbank/transform/morphExtr.xsl");

		if (macroSpecs.length() == 0)
			xslChain.add("opennlp.ccgbank/transform/macroInsert.xsl");

		if (macroSpecs.contains("agr")) {
			System.out
					.println("Inserting a macro to check agreement in the copula");
			xslChain.add("opennlp.ccgbank/transform/agr-macroInsert.xsl");
		}

		if (macroSpecs.contains("anim")) {
			System.out
					.println("Inserting a macro to check animacy constraints");
			xslChain.add("opennlp.ccgbank/transform/anim-macroInsert.xsl");
		}
	}
}
