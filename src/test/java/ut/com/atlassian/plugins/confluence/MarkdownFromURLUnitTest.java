package ut.com.atlassian.plugins.confluence;

import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.assertTrue;

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
		assertTrue(Pattern.matches("[\\S\\s]*<em>Italic</em>[\\S\\s]*", output));
	}
}