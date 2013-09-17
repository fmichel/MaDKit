/*
 * Copyright 2013 Fabien Michel
 * 
 * This file is part of MaDKit.
 * 
 * MaDKit is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * MaDKit is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * MaDKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import madkit.kernel.JunitMadkit;

import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.2
 * @version 0.9
 * 
 */
public class XMLUtilitiesTest {

	@Test
	public void testGetFromCP() {
		try {
			assertNotNull(XMLUtilities.getDOM("madkit/xml/bench.xml"));
		} catch (SAXException | IOException | ParserConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testGetFailFromIOException() {
			try {
				assertNull(XMLUtilities.getDOM("madkit/xml/notExist.xml"));
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
				return;
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			}
			JunitMadkit.noExceptionFailure();
	}

	@Test
	public void testGetFailFromWrongContent() {
			try {
				assertNull(XMLUtilities.getDOM("madkit/boot/process/test2.prop"));
			} catch (SAXException e) {
				e.printStackTrace();
				return;
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			}
			JunitMadkit.noExceptionFailure();
	}

	@Test
	public void testGetFromUserDir() {
		try {
			assertNotNull(XMLUtilities.getDOM("test/madkit/xml/bench.xml"));
		} catch (SAXException | IOException | ParserConfigurationException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testGetFromAbsolutePath() {
		File f = new File("test/madkit/xml/bench.xml");
		try {
			assertNotNull(XMLUtilities.getDOM(f.getAbsolutePath()));
		} catch (SAXException | IOException | ParserConfigurationException e) {
			e.printStackTrace();
		}
	}

}
