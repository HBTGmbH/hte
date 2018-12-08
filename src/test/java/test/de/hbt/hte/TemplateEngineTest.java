package test.de.hbt.hte;

import static org.junit.Assert.*;

import java.io.*;
import java.util.*;

import org.junit.*;

import de.hbt.hte.*;
import de.hbt.hte.rt.*;

public class TemplateEngineTest {

    private String eval(String template, Object ctx) throws Exception {
        CompiledTemplate ct = TemplateEngine.instantiateTemplate(template, new SimpleEnvironment());
        StringWriter sw = new StringWriter();
        ct.write(sw, ctx);
        return sw.toString();
    }

    @Test
    public void testLiteral() throws Exception {
        assertEquals("3a", eval("${\"3a\"}", null));
    }

    @Test
    public void testBranch() throws Exception {
        assertEquals("1", eval("<#if true>1</#if>", null));
    }

    @Test
    public void testBranchWithElse() throws Exception {
        assertEquals("2", eval("<#if false>1<#else>2</#if>", null));
    }

    @Test
    public void testConditional() throws Exception {
        assertEquals("1", eval("${if true then 1 else 2}", null));
    }

    public static class WithA {
        private final String a;

        public WithA(String a) {
            this.a = a;
        }

        public String getA() {
            return a;
        }

        public List<WithA> getUsers() {
            return Arrays.asList(new WithA("Hello, "), new WithA("World!"));
        }

        public boolean isThrowingBoolean() {
            throw new AssertionError();
        }
    }

    public static class ExtendsWithA extends WithA {
        public ExtendsWithA(String a) {
            super(a);
        }

        @Override
        public String getA() {
            return super.getA();
        }

        @Override
        public List<WithA> getUsers() {
            return super.getUsers();
        }

        @Override
        public boolean isThrowingBoolean() {
            return super.isThrowingBoolean();
        }
    }

    @Test
    public void testUnaryNegateInt() throws Exception {
        assertEquals("-3", eval("${-3}", null));
    }

    @Test
    public void testMapKeyRef() throws Exception {
        assertEquals("b3", eval("${a}3", Collections.singletonMap("a", "b")));
    }

    @Test
    public void testStringAddInt() throws Exception {
        assertEquals("a3", eval("${\"a\" + 3}", null));
    }

    @Test
    public void testList() throws Exception {
        assertEquals("Hello, World!", eval("<#list users as user>${user.a}</#list>", new WithA(null)));
    }

    @Test
    public void testListSize() throws Exception {
        assertEquals("4", eval("${users.size + 2}", new WithA(null)));
    }

    @Test
    public void testListWithCtxRef() throws Exception {
        assertEquals("Hello,  and Hello, Templates!World! and Hello, Templates!",
                eval("<#list users as user>${user.a} and ${a}</#list>", new WithA("Hello, Templates!")));
    }

    @Test
    public void testDefaultExpr() throws Exception {
        assertEquals("3", eval("${b?:3}", new WithA(null)));
    }

    @Test
    public void testCondWithDefaultExpr() throws Exception {
        assertEquals("4", eval("${if d?:false then b?:3 else c?:4}", new WithA(null)));
    }

    @Test
    public void testBinaryWithDefaultExpr() throws Exception {
        assertEquals("7", eval("${b?:3 + c?:4}", new WithA(null)));
    }

    @Test
    public void testNestedBinary() throws Exception {
        assertEquals("29", eval("${3+4*5+6}", new WithA(null)));
    }

    @Test
    public void testComplexArithmetic() throws Exception {
        assertEquals("-29.0", eval("${4**2+3+-4*5-(22+3*2)}", new WithA(null)));
    }

    @Test
    public void testRelational1() throws Exception {
        assertEquals("true", eval("${3 < 4}", new WithA(null)));
    }

    @Test
    public void testRelational2() throws Exception {
        assertEquals("false", eval("${3 > 4}", new WithA(null)));
    }

    @Test
    public void testEqual() throws Exception {
        assertEquals("true", eval("${4+1 == 5}", new WithA(null)));
    }

    @Test
    public void testShortCircuitAnd() throws Exception {
        assertEquals("false", eval("${false && throwingBoolean}", new WithA(null)));
    }

    @Test
    public void testShortCircuitOr() throws Exception {
        assertEquals("true", eval("${true || throwingBoolean}", new WithA(null)));
    }

    @Test
    public void testBimorphicReceiver() throws Exception {
        CompiledTemplate ct = TemplateEngine.instantiateTemplate("${a}", new SimpleEnvironment());
        StringWriter sw;
        ct.write(sw = new StringWriter(), new WithA("3"));
        assertEquals("3", sw.toString());
        ct.write(sw = new StringWriter(), new ExtendsWithA("4"));
        assertEquals("4", sw.toString());
    }

    @Test
    public void testAssign() throws Exception {
        assertEquals("3", eval("<#assign a=3>${a}", new WithA(null)));
    }

    @Test
    public void testNestedAssign() throws Exception {
        assertEquals("4", eval("<#assign a=1><#assign b=a+3>${b}", new WithA(null)));
    }

    @Test
    public void testUppercase() throws Exception {
        assertEquals("B", eval("${\"b\".uppercase()}", new WithA(null)));
    }

    @Test
    public void testEncodeQueryParamValueString() throws Exception {
        assertEquals("the+%22value%22", eval("${\"the \\\"value\\\"\" as qpval}", new WithA(null)));
    }

    @Test
    public void testEncodeQueryParamValueInteger() throws Exception {
        assertEquals("2345", eval("${2345 as qpval}", new WithA(null)));
    }

}
