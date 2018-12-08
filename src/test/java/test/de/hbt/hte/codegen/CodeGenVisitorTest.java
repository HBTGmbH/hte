package test.de.hbt.hte.codegen;

import static org.junit.Assert.*;

import java.io.*;

import org.junit.*;

import de.hbt.hte.ast.*;
import de.hbt.hte.codegen.*;
import de.hbt.hte.rt.*;

public class CodeGenVisitorTest {

    private static String test(Node... nodes) throws Exception {
        Environment env = new SimpleEnvironment();
        CodeGenVisitor vis = new CodeGenVisitor(env);
        Class<? extends CompiledTemplate> clazz = vis.compile(new TemplateNode(nodes));
        CompiledTemplate ct = clazz.getConstructor(Environment.class).newInstance(env);
        Writer w = new StringWriter();
        ct.write(w);
        return w.toString();
    }

    @Test
    public void testTextNode() throws Exception {
        assertEquals("abc", test(new TextNode("abc")));
    }

    @Test
    public void testExpressionNodeWithBooleanLiteral() throws Exception {
        assertEquals("false", test(new ExpressionNode(new LiteralExpression(false), null)));
    }

    @Test
    public void testExpressionNodeWithIntegerLiteral() throws Exception {
        assertEquals("456", test(new ExpressionNode(new LiteralExpression(456), null)));
    }

    @Test
    public void testExpressionNodeWithConditional() throws Exception {
        assertEquals("5.4", test(new ExpressionNode(new ConditionalExpression(new LiteralExpression(true),
                new LiteralExpression(5.4), new LiteralExpression(2.3)), null)));
    }

}
