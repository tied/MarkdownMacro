package ut.com.atlassian.plugins.confluence;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.regex.Pattern;
import java.io.File;
import java.net.*;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.macro.MacroExecutionException;
import com.atlassian.webresource.api.assembler.PageBuilderService;
import com.atlassian.webresource.api.assembler.RequiredResources;
import com.atlassian.webresource.api.assembler.WebResourceAssembler;

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
		//Mock methods for pageBuilderService.assembler().resources().requireWebResource("com.atlassian.plugins.confluence.markdown.confluence-markdown-macro:highlightjs");
    	Mockito.when(pageBuilderService.assembler()).thenReturn(webResourceAssembler);
		Mockito.when(webResourceAssembler.resources()).thenReturn(requiredResources);
		Mockito.when(requiredResources.requireWebResource("com.atlassian.plugins.confluence.markdown.confluence-markdown-macro:highlightjs")).thenReturn(requiredResources);
		
		/*Test that markdown is correctly retrieved from a URL and rendered into HTML*/
		// Run the macro using a URL that points to a file containing test markdown,
		// then assert that *Italic* was correctly rendered into <em>Italic</em>
		String file = new File("src/test/resources/testMarkdown.md").toURI().toURL().toString();
		@SuppressWarnings({ "rawtypes", "unchecked" })
		String output = markdownMacro.execute(new HashMap(), file, conversionContext);
		assertThat(Pattern.matches("[\\S\\s]*<em>Italic</em>[\\S\\s]*", output), is(true)); //Uses [\S\s] (anything that is either whitespace or not whitespace) instead of . (any character) because . does not match newline characters.
	}
    @Test
    public void testErrorHandling() throws MacroExecutionException, MalformedURLException {
    	//Mock methods for pageBuilderService.assembler().resources().requireWebResource("com.atlassian.plugins.confluence.markdown.confluence-markdown-macro:highlightjs");
    	Mockito.when(pageBuilderService.assembler()).thenReturn(webResourceAssembler);
		Mockito.when(webResourceAssembler.resources()).thenReturn(requiredResources);
		Mockito.when(requiredResources.requireWebResource("com.atlassian.plugins.confluence.markdown.confluence-markdown-macro:highlightjs")).thenReturn(requiredResources);
		
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
}