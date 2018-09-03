package ut.com.atlassian.plugins.confluence;

import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.assertTrue;

import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.regex.Pattern;

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
		//Mock methods for pageBuilderService.assembler().resources().requireWebResource("com.atlassian.plugins.confluence.markdown.confluence-markdown-macro:highlightjs");
		Mockito.when(pageBuilderService.assembler()).thenReturn(webResourceAssembler);
		Mockito.when(webResourceAssembler.resources()).thenReturn(requiredResources);
		Mockito.when(requiredResources.requireWebResource("com.atlassian.plugins.confluence.markdown.confluence-markdown-macro:highlightjs")).thenReturn(requiredResources);
		
		
		/*Test that markdown is correctly rendered into HTML*/
		// Run the macro using input text of *Italic*,
		// then assert that *Italic* was correctly rendered into <em>Italic</em>
		@SuppressWarnings({ "rawtypes", "unchecked" })
		String output = markdownMacro.execute(new HashMap(), "*Italic*", conversionContext);
		assertTrue(Pattern.matches("[\\S\\s]*<em>Italic</em>[\\S\\s]*", output)); //Uses [\S\s] (anything that is either whitespace or not whitespace) instead of . (any character) because . does not match newline characters.
	}
	
	@Test
	public void testSyntaxHighlighting() throws MacroExecutionException {
		//Mock methods for pageBuilderService.assembler().resources().requireWebResource("com.atlassian.plugins.confluence.markdown.confluence-markdown-macro:highlightjs");
		Mockito.when(pageBuilderService.assembler()).thenReturn(webResourceAssembler);
		Mockito.when(webResourceAssembler.resources()).thenReturn(requiredResources);
		Mockito.when(requiredResources.requireWebResource("com.atlassian.plugins.confluence.markdown.confluence-markdown-macro:highlightjs")).thenReturn(requiredResources);
		
		
		/*Test that the correct JavaScript is returned for highlight.js to work*/
		// Run the macro using input of a line of code in a code block,
		// then assert that the correct JavaScript was returned.
		// Intended only as a temporary test until I can program a better one
		@SuppressWarnings({ "rawtypes", "unchecked" })
		String output = markdownMacro.execute(new HashMap(), "'public class JavaClass {}'", conversionContext);
		assertTrue(Pattern.matches("[\\S\\s]*<script>\\sAJS\\.\\$\\('\\[data\\-macro\\-name=\"markdown\"\\] code'\\)\\.each\\(function\\(i, block\\) \\{\\s    hljs\\.highlightBlock\\(block\\);\\s  \\}\\);\\s<\\/script>[\\S\\s]*", output));
	}
}