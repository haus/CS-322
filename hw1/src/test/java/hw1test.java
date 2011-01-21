import org.junit.Test;
import java.io.*;
import static junit.framework.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: mlitteken
 * Date: Nov 8, 2010
 * Time: 5:08:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class hw1test {
    public String fabTest(String filename) throws ParseError {
        StringBuilder output = new StringBuilder("");

        try {
            Parser test = new Parser(new Scanner(new FileInputStream("fab/" + filename + ".fab")));
            Ast.Program prog = (Ast.Program) test.parse().value;
            Check.check(prog);
            Interp.interp(prog);

        } catch (IOException ioexn) {
            System.err.println ("IOException: " + ioexn.getMessage());
        } catch (Exception ex) {
            throw new ParseError("General exception: " + ex.getMessage());
        }

        output.append("\n");

        return output.toString();
    }

    public String fabLoad(String filename) {
        byte[] buffer = new byte[(int) new File("parseOut/" + filename + ".out").length()];
        BufferedInputStream f = null;
        String outputTest = null;

        try {
            f = new BufferedInputStream(new FileInputStream("parseOut/" + filename + ".out"));
            f.read(buffer);
            outputTest = new String(buffer);
        } catch (IOException ex) {
            System.err.println("IOException: " + ex.getMessage());
        } finally {
            if (f != null) try { f.close(); } catch (IOException ignored) { }
        }

        return outputTest;
    }

    /**
     * This is a full fab file. It should parse fine and match the corresponding output file.
     * @throws ParseError on various failures
     */
    @Test
    public void fullFabTest1() throws ParseError {
        String filename = "8q";
        assertEquals(fabLoad(filename), fabTest(filename));
    }

    /**
     * This is a full fab file. It should parse fine and match the corresponding output file.
     * @throws ParseError on various failures
     */
    @Test
    public void fullFabTest2() throws ParseError {
        String filename = "counter";
        assertEquals(fabLoad(filename), fabTest(filename));
    }

    /**
     * This is a full fab file. It should parse fine and match the corresponding output file.
     * @throws ParseError on various failures
     */
    @Test
    public void fullFabTest3() throws ParseError {
        String filename = "forTest";
        assertEquals(fabLoad(filename), fabTest(filename));
    }

    /**
     * This is a full fab file. It should parse fine and match the corresponding output file.
     * @throws ParseError on various failures
     */
    @Test
    public void fullFabTest4() throws ParseError {
        String filename = "hanoi";
        assertEquals(fabLoad(filename), fabTest(filename));
    }

    /**
     * This is a full fab file. It should parse fine and match the corresponding output file.
     * @throws ParseError on various failures
     */
    @Test
    public void fullFabTest5() throws ParseError {
        String filename = "hello";
        assertEquals(fabLoad(filename), fabTest(filename));
    }

    /**
     * This is a full fab file. It should parse fine and match the corresponding output file.
     * @throws ParseError on various failures
     */
    @Test
    public void fullFabTest6() throws ParseError {
        String filename = "lists";
        assertEquals(fabLoad(filename), fabTest(filename));
    }

    /**
     * This is a full fab file. It should parse fine and match the corresponding output file.
     * @throws ParseError on various failures
     */
    @Test
    public void fullFabTest7() throws ParseError {
        String filename = "prime";
        assertEquals(fabLoad(filename), fabTest(filename));
    }

    /**
     * This is a full fab file. It should parse fine and match the corresponding output file.
     * @throws ParseError on various failures
     */
    @Test
    public void fullFabTest8() throws ParseError {
        String filename = "sort";
        assertEquals(fabLoad(filename), fabTest(filename));
    }

    /**
     * This is a full fab file. It should parse fine and match the corresponding output file.
     * @throws ParseError on various failures
     */
    @Test
    public void fullFabTest9() throws ParseError {
        String filename = "square";
        assertEquals(fabLoad(filename), fabTest(filename));
    }

    /**
     * This is a full fab file. It should parse fine and match the corresponding output file.
     * @throws ParseError on various failures
     */
    @Test
    public void fullFabTest10() throws ParseError {
        String filename = "tree";
        assertEquals(fabLoad(filename), fabTest(filename));
    }
}
