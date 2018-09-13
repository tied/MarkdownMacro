package ut.com.atlassian.plugins.confluence;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
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
		// Run the macro using a URL that points to a file containing test markdown,
		// then assert that *Italic* was correctly rendered into <em>Italic</em>
		String file = new File("src/test/resources/testMarkdown.md").toURI().toURL().toString();
		@SuppressWarnings({ "rawtypes", "unchecked" })
		String output = markdownMacro.execute(new HashMap(), file, conversionContext);
		assertTrue(output.contains("<em>Italic</em>"));
	}
    @Test
    public void testErrorHandling() throws MacroExecutionException, MalformedURLException {
		/*Test error handling of nonexistent URLs*/
		//  Run the macro using a URL pointing to a file that does not exist,
		//  then assert that the output of the macro is not an empty string
		//  i.e. that it returned an error message instead of simply breaking.
		String input1 = new File("src/test/resources/nonexistentfile.md").toURI().toURL().toString();
		@SuppressWarnings({ "rawtypes", "unchecked" })
		String output1 = markdownMacro.execute(new HashMap(), input1, conversionContext);
		assertThat(output1, is(not("")));
		
		
		/*Test error handling of invalid URLs*/
		//  Run the macro using a string that is not a URL,
		//  then assert that the output of the macro is not an empty string
		//  i.e. that it returned an error message instead of simply breaking.
		String input2 = "not_a_URL";
		@SuppressWarnings({ "rawtypes", "unchecked" })
		String output2 = markdownMacro.execute(new HashMap(), input2, conversionContext);
		assertThat(output2, is(not("")));
		
		
		/*Test error handling of importing from a private Bitbucket repository*/
		//  Run the macro using a URL pointing to a file that mimics the text
		//  returned when trying to import from a private Bitbucket repository,
		//  then assert that the output of the macro is not an empty string
		//  i.e. that it returned an error message instead of simply breaking.
		String input3 = new File("src/test/resources/testPrivateBitbucket.html").toURI().toURL().toString();
		@SuppressWarnings({ "rawtypes", "unchecked" })
		String output3 = markdownMacro.execute(new HashMap(), input3, conversionContext);
		assertThat(output3, is(not("")));
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