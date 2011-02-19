import java.io.*;

class IRInterpDriver {
    public static void main(String argv[]) throws Exception {
	FileInputStream is = new FileInputStream(argv[0]);
	try {
	    Parser parser_obj = new Parser(new Scanner(is));
	    Ast.Program prog = (Ast.Program) parser_obj.parse().value;
	    Check.check(prog);
	    // System.out.println(prog);
	    IR.Program irp = IRGen.gen(prog);
	    int result = IRInterp.interpProgram(irp);
	    System.err.println("Interpreter done with result = " + result);
	} catch (ParseError exn) {
	    System.err.println(exn.getMessage());
	} catch (Check.CheckError exn) {
	    System.err.println(exn.getMessage());
	} catch (IRInterp.IRInterpException exn) {
	    System.err.println(exn.getMessage());
	} catch (IRInterp.BadCode exn) {
	    System.err.println(exn.getMessage());
	}
	is.close();
    }
}
