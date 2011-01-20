import java.io.*;

class InterpDriver {
    public static void main(String argv[]) throws Exception {
        if (argv.length > 0) {

            FileInputStream is = new FileInputStream(argv[0]);

            try {
                Parser parser_obj = new Parser(new Scanner(is));
                Ast.Program prog = (Ast.Program) parser_obj.parse().value;
                Check.check(prog);
                // System.out.println(prog);
                Interp.interp(prog);
                System.err.println("Interpreter done");
            } catch (ParseError exn) {
                System.err.println(exn.getMessage());
            } catch (Check.CheckError exn) {
                System.err.println(exn.getMessage());
            } catch (Interp.InterpError exn) {
                System.err.println(exn.getMessage());
            }

            is.close();
        } else {
            System.err.println("No command line arguments given.");
            System.exit(1);
        }
    }
}
