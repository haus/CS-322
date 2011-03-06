import java.io.*;

class X86GenDriver {
    public static void main(String argv[]) throws Exception {
	FileInputStream is = new FileInputStream(argv[0]);
	try {
	    Parser parser_obj = new Parser(new Scanner(is));
	    Ast.Program prog = (Ast.Program) parser_obj.parse().value;
	    Check.check(prog);
	    // System.out.println(prog);
	    IR.Program irp = IRGen.gen(prog);
	    // PrintStream os = new PrintStream(argv[1]);
	    // os.println(irp);
	    // os.close();
	    X86Gen.gen(irp);
	} catch (ParseError exn) {
	    System.err.println(exn.getMessage());
	} catch (Check.CheckError exn) {
	    System.err.println(exn.getMessage());
	}
	is.close();
    }
}
