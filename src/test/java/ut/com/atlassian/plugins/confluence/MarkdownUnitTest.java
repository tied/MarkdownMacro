package ut.com.atlassian.plugins.confluence;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.*;

import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.*;
import java.net.MalformedURLException;
import java.util.HashMap;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.macro.MacroExecutionException;
import com.atlassian.webresource.api.assembler.PageBuilderService;
import com.atlassian.webresource.api.assembler.RequiredResources;
import com.atlassian.webresource.api.assembler.WebResourceAssembler;
import com.atlassian.plugins.confluence.markdown.MarkdownMacro;

@RunWith (MockitoJUnitRunner.class)
public class MarkdownUnitTest {
	@Mock
	ConversionContext conversionContext;
	@Mock
    PageBuilderService pageBuilderService;
	@Mock
	WebResourceAssembler webResourceAssembler;
    @Mock
    RequiredResources requiredResources;
    @InjectMocks
    MarkdownMacro markdownMacro;
    
	@Test
	public void testMarkdownRendering() throws MacroExecutionException {
		/*Test that markdown is correctly rendered into HTML*/
		@SuppressWarnings({ "rawtypes", "unchecked" })
		String output = markdownMacro.execute(new HashMap(), "*Italic*", conversionContext);
		assertTrue(output.contains("<em>Italic</em>"));
	}
	@Test
	public void testSyntaxHighlighting() throws MacroExecutionException {
		/*Test that the javascript for syntax highlighting works*/
		// Run the macro with an input of a line of code
		// Create a temporary HTML file containing the output
		// Parse the HTML file with htmlunit
		// Assert that the page contains three spans with the correct classes
    	// Note: Does not test if highlight.js and highlight.css are correctly included in the page
		try (final WebClient webClient = new WebClient()) {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			String output = markdownMacro.execute(new HashMap(), "`class className() {}`", conversionContext);
			String toWrite = "﻿<!DOCTYPE html>\r\n" + 
					"<html>\r\n" + 
					"<head>\r\n" + 
					"    <title>Syntax Highlighting Page</title>\r\n" + 
					"    <script src=\"./jquery-3.3.1.min.js\"></script>\r\n" + 
					"    <script src=\"../../main/resources/js/highlight.min.js\"></script>\r\n" + 
					"    <link href=\"../../main/resources/css/highlight.min.css\" rel=\"stylesheet\" />\r\n" + 
					"</head>\r\n" + 
					"<body>\r\n" + 
					"	<script>\r\n" + 
					"		var AJS = {\r\n" + 
					"			$: $\r\n" + 
					"		};\r\n" + 
					"    </script>\r\n" + 
					"<div data-macro-name='markdown'>" +
					output + 
					"</div>" + 
					"</body>\r\n" + 
					"</html>";
		    File tmpHTMLFile = File.createTempFile("syntax-highlighting-", ".html", new File("src/test/resources"));
		    FileWriter writer = new FileWriter(tmpHTMLFile);
		    writer.write(toWrite);
		    writer.close();
			final HtmlPage page = webClient.getPage(tmpHTMLFile.toURI().toURL().toString());
			HtmlElement document = page.getDocumentElement();
			assertTrue(document.getElementsByAttribute("span", "class", "hljs-class").size() > 0);
			assertTrue(document.getElementsByAttribute("span", "class", "hljs-keyword").size() > 0);
			assertTrue(document.getElementsByAttribute("span", "class", "hljs-title").size() > 0);
			tmpHTMLFile.delete();
		} catch (FailingHttpStatusCodeException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Before
	public void setup() {
		//Mock methods for pageBuilderService.assembler().resources().requireWebResource("com.atlassian.plugins.confluence.markdown.confluence-markdown-macro:highlightjs");
		Mockito.when(pageBuilderService.assembler()).thenReturn(webResourceAssembler);
		Mockito.when(webResourceAssembler.resources()).thenReturn(requiredResources);
		Mockito.when(requiredResources.requireWebResource("com.atlassian.plugins.confluence.markdown.confluence-markdown-macro:highlightjs")).thenReturn(requiredResources);
	}
}