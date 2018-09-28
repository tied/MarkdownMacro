package ut.com.atlassian.plugins.confluence;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.List;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.*;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.macro.MacroExecutionException;
import com.atlassian.webresource.api.assembler.PageBuilderService;
import com.atlassian.webresource.api.assembler.RequiredResources;
import com.atlassian.webresource.api.assembler.WebResourceAssembler;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.atlassian.plugins.confluence.markdown.MarkdownFromURLMacro;

@RunWith (MockitoJUnitRunner.class)
public class MarkdownFromURLUnitTest {
	@Mock
	ConversionContext conversionContext;
	@Mock
    PageBuilderService pageBuilderService;
	@Mock
	WebResourceAssembler webResourceAssembler;
    @Mock
    RequiredResources requiredResources;
    @InjectMocks
    MarkdownFromURLMacro markdownMacro;
    
    @Test
	public void testMarkdownRendering() throws MacroExecutionException, MalformedURLException {
		/*Test that markdown is correctly retrieved from a URL and rendered into HTML*/
		String file = new File("src/test/resources/testMarkdown.md").toURI().toURL().toString();
		@SuppressWarnings({ "rawtypes", "unchecked" })
		String output = markdownMacro.execute(new HashMap(), file, conversionContext);
		assertTrue(output.contains("<em>Italic</em>"));
	}
    @Test
    public void testErrorHandling() throws MacroExecutionException, MalformedURLException {
		/*Test error handling of nonexistent URLs*/
		String input1 = new File("src/test/resources/nonexistentfile.md").toURI().toURL().toString();
		@SuppressWarnings({ "rawtypes", "unchecked" })
		String output1 = markdownMacro.execute(new HashMap(), input1, conversionContext);
		assertTrue(output1.contains("URL does not exist"));
		assertTrue(output1.contains(input1));
		
		
		/*Test error handling of invalid URLs*/
		String input2 = "not_a_URL";
		@SuppressWarnings({ "rawtypes", "unchecked" })
		String output2 = markdownMacro.execute(new HashMap(), input2, conversionContext);
		assertTrue(output2.contains("Invalid URL"));
		
		
		/*Test error handling of importing from a private Bitbucket repository*/
		String input3 = new File("src/test/resources/testPrivateBitbucket.html").toURI().toURL().toString();
		@SuppressWarnings({ "rawtypes", "unchecked" })
		String output3 = markdownMacro.execute(new HashMap(), input3, conversionContext);
		assertTrue(output3.contains("Importing from private Bitbucket repositories is not supported"));
		assertTrue(output3.contains(input3));
	}
    @Test
	public void testSyntaxHighlighting() throws MacroExecutionException, FailingHttpStatusCodeException, MalformedURLException, IOException {
		/*Test that the javascript for syntax highlighting works*/
		// Run the macro with an input of a line of code
		// Create a temporary HTML file containing the output
		// Parse the HTML file with htmlunit
		// Assert that the page contains three spans with the correct classes
    	// Note: Does not test if highlight.js and highlight.css are correctly included in the page
		try (final WebClient webClient = new WebClient()) {
			String file = new File("src/test/resources/testSyntaxHighlighting.md").toURI().toURL().toString();
			@SuppressWarnings({ "rawtypes", "unchecked" })
			String output = markdownMacro.execute(new HashMap(), file, conversionContext);
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
			List<HtmlElement> spans = document.getElementsByTagName("span");
			tmpHTMLFile.delete();
			
			HtmlElement hljsClassSpan = null;
			int numberOfHljsClassSpans = 0;
			HtmlElement hljsKeywordSpan = null;
			int numberOfHljsKeywordSpans = 0;
			HtmlElement hljsTitleSpan = null;
			int numberOfHljsTitleSpans = 0;
			
			//Check the class of every span element and increment the counters accordingly
			//Also define the three span objects as the first span of the correct class
			for (HtmlElement span : spans) {
				if (span.getAttribute("class").contains("hljs-class")) {
					numberOfHljsClassSpans++;
					if (numberOfHljsClassSpans == 1) {
						hljsClassSpan = span;
					}
				}
				if (span.getAttribute("class").contains("hljs-keyword")) {
					numberOfHljsKeywordSpans++;
					if (numberOfHljsKeywordSpans == 1) {
						hljsKeywordSpan = span;
					}
				}
				if (span.getAttribute("class").contains("hljs-title")) {
					numberOfHljsTitleSpans++;
					if (numberOfHljsTitleSpans == 1) {
						hljsTitleSpan = span;
					}
				}
			}
			
			//  Test that there is exactly one span with a css class of hljs-class
			//  and that it's parent is a code element with a css class of hljs
			assertThat(numberOfHljsClassSpans, is(1));
			assertTrue(hljsClassSpan.getParentNode().getNodeName().equals("code"));
			assertTrue(hljsClassSpan.getParentNode().getAttributes().getNamedItem("class").getNodeValue().contains("hljs"));
			
			//  Test that there is exactly one span with a css class of hljs-keyword
			//  and that it's parent is a span element with a css class of hljs-class
			assertThat(numberOfHljsKeywordSpans, is(1));
			assertTrue(hljsKeywordSpan.getParentNode().getNodeName().equals("span"));
			assertTrue(hljsKeywordSpan.getParentNode().getAttributes().getNamedItem("class").getNodeValue().contains("hljs-class"));
			
			//  Test that there is exactly one span with a css class of hljs-title
			//  and that it's parent is a span element with a css class of hljs-class
			assertThat(numberOfHljsTitleSpans, is(1));
			assertTrue(hljsTitleSpan.getParentNode().getNodeName().equals("span"));
			assertTrue(hljsTitleSpan.getParentNode().getAttributes().getNamedItem("class").getNodeValue().contains("hljs-class"));
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