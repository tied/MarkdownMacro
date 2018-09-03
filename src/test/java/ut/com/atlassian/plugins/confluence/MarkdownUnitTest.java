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
		Mockito.when(pageBuilderService.assembler()).thenReturn(webResourceAssembler);
		Mockito.when(webResourceAssembler.resources()).thenReturn(requiredResources);
		Mockito.when(requiredResources.requireWebResource("com.atlassian.plugins.confluence.markdown.confluence-markdown-macro:highlightjs")).thenReturn(requiredResources);
		@SuppressWarnings({ "rawtypes", "unchecked" })
		String output = markdownMacro.execute(new HashMap(), "*Italic*", conversionContext);
		assertTrue(Pattern.matches("[\\S\\s]*<em>Italic</em>[\\S\\s]*", output));
	}
	
	@Test
	public void testSyntaxHighlighting() throws MacroExecutionException {
		Mockito.when(pageBuilderService.assembler()).thenReturn(webResourceAssembler);
		Mockito.when(webResourceAssembler.resources()).thenReturn(requiredResources);
		Mockito.when(requiredResources.requireWebResource("com.atlassian.plugins.confluence.markdown.confluence-markdown-macro:highlightjs")).thenReturn(requiredResources);
		@SuppressWarnings({ "rawtypes", "unchecked" })
		String output = markdownMacro.execute(new HashMap(), "public class JavaClass {}", conversionContext);
		assertTrue(Pattern.matches("[\\S\\s]*<script>\\sAJS\\.\\$\\('\\[data\\-macro\\-name=\"markdown\"\\] code'\\)\\.each\\(function\\(i, block\\) \\{\\s    hljs\\.highlightBlock\\(block\\);\\s  \\}\\);\\s<\\/script>[\\S\\s]*", output));
	}
}