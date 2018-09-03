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
		Mockito.when(pageBuilderService.assembler()).thenReturn(webResourceAssembler);
		Mockito.when(webResourceAssembler.resources()).thenReturn(requiredResources);
		Mockito.when(requiredResources.requireWebResource("com.atlassian.plugins.confluence.markdown.confluence-markdown-macro:highlightjs")).thenReturn(requiredResources);
		String file = new File("src/test/resources/testMarkdown.md").toURI().toURL().toString();
		@SuppressWarnings({ "rawtypes", "unchecked" })
		String output = markdownMacro.execute(new HashMap(), file, conversionContext);
		assertThat(Pattern.matches("[\\S\\s]*<em>Italic</em>[\\S\\s]*", output), is(true));
	}
    @Test
    public void testErrorHandling() throws MacroExecutionException, MalformedURLException {
		Mockito.when(pageBuilderService.assembler()).thenReturn(webResourceAssembler);
		Mockito.when(webResourceAssembler.resources()).thenReturn(requiredResources);
		Mockito.when(requiredResources.requireWebResource("com.atlassian.plugins.confluence.markdown.confluence-markdown-macro:highlightjs")).thenReturn(requiredResources);
		String input1 = new File("src/test/resources/nonexistantfile.md").toURI().toURL().toString();
		@SuppressWarnings({ "rawtypes", "unchecked" })
		String output1 = markdownMacro.execute(new HashMap(), input1, conversionContext);
		assertThat(output1, is(not("")));
		String input2 = "not_a_URL";
		@SuppressWarnings({ "rawtypes", "unchecked" })
		String output2 = markdownMacro.execute(new HashMap(), input2, conversionContext);
		assertThat(output2, is(not("")));
		String input3 = new File("src/test/resources/testPrivateBitbucket.html").toURI().toURL().toString();
		@SuppressWarnings({ "rawtypes", "unchecked" })
		String output3 = markdownMacro.execute(new HashMap(), input3, conversionContext);
		assertThat(output3, is(not("")));
	}
}