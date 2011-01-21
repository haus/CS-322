import java.io.*;
import java.util.*;


class Interp {

    // support for reading white-space-separated strings from stdin
    static BufferedReader br;
    static StringTokenizer st;
    static String readToken() throws IOException {
        while (true) {
            try {
                return st.nextToken();
            } catch (NoSuchElementException exn) {
                String ln = br.readLine();
                if (ln == null)
                    throw new IOException();
                st = new StringTokenizer(ln);
            }
        }
    }

    /* The store maps locations to fab values.
       To enable a simple uniform treatment of lvalues,
       all local variables, records, and arrays occupy locations in the store.
       Because the AST treats constants and function names as lvalues too,
       these need store locations as well.
       Since the store only grows during execution, we maintain it
       as a global variable, rather than threading it through the interpreter.
    */

    static ArrayList<Value> store;

    static int allocateStore(int n) {
        int l = store.size();
        //    System.out.println("Allocating " + n + " at " + l);
        while (n-- > 0)
            store.add(null);
        return l;
    }

    static void storeSet(int l, Value v) {
        //    System.out.println("Storing into " + l);
        store.set(l,v);
    }

    static Value storeGet(int l) {
        //    System.out.println("Getting " + l);
        return store.get(l);
    }

    static int storeValue(Value v) {
        int l = allocateStore(1);
        storeSet(l,v);
        return l;
    }


    /* Fab Values */
    abstract static class Value {
        int as_int() {
            throw new Error("Impossible as_int");
        }
        int as_loc() throws InterpError {
            throw new Error("Impossible as_loc");
        }
        Func as_func() throws InterpError {
            throw new Error("Impossible as_func");
        }
        boolean as_bool() throws InterpError {
            throw new Error("Impossible as_bool");
        }
    }

    static class BoolValue extends Value {
        boolean b;

        BoolValue (boolean b) {
            this.b = b;
        }

        boolean as_bool() {
            return b;
        }
    }

    static class IntValue extends Value {
        int v;

        IntValue (int v) {
            this.v = v;
        }

        int as_int() {
            return v;
        }
    }

    static class LocValue extends Value {
        int l;

        LocValue (int l) {
            this.l = l;
        }

        int as_loc() {
            return l;
        }
    }

    static class FuncValue extends Value {
        Func f;
        FuncValue (Func f) {
            this.f = f;
        }
        Func as_func() {
            return f;
        }
    }

    static class Func {
        Env env;
        Ast.FuncDec def;
        Func(Env env, Ast.FuncDec def) {
            this.env = env; this.def = def;
        }
    }

    // Environments map identifiers to store locations.
    static class Env {
        String name;
        int loc;
        Env next;

        /** Empty environment is just a null Env. */
        static final Env empty = null;

        /** Creates new Env by extending an existing Env with a new declaration binding.
         */
        Env(String name, int loc, Env next) {
            this.name = name; this.loc = loc; this.next = next;
        }

        public String toString() {
            String r = "[ ";
            for (Env env = this; env != null; env = env.next) {
                r += env.name + " = " + env.loc + "\n";
            }
            r += "]";
            return r;
        }
    }

    static Env find(String name,Env env) {
        for (; env != null; env = env.next)
            if (env.name.equals(name))
                return env;
        return null;
    }

    static class InterpError extends Ast.Error {
        InterpError(int line, String text) {
            super("Error at line " + line + ": " + text);
        }
    }

    // Main program
    static void interp(Ast.Program p) throws InterpError {
        store = new ArrayList<Value>();
        br = new BufferedReader(new InputStreamReader(System.in));
        st = new StringTokenizer("");
        Env env = Env.empty;

        // Constants (nil, true, false)
        env = new Env("true", storeValue(new BoolValue(true)), env);
        env = new Env("false", storeValue(new BoolValue(false)), env);
        env = new Env("nil", storeValue(null), env);

        interp(p.body,env);
    }

    // Return values are as for main.Ast.St, below
    static Object interp(Ast.Block b0,Env env) throws InterpError {
        Object r = null;
        for (Ast.BlockItem b : b0.items) {
            if (b instanceof Ast.Declaration)
                env = interp((Ast.Declaration) b,env);
            if (b instanceof Ast.St) {
                r = interp((Ast.St) b,env);
                if (r != null)
                    break;
            }
        }
        return r;
    }

    static Env interp(Ast.Declaration d0,final Env env) throws InterpError {
        class DeclarationVisitor implements Ast.DeclarationVisitor {
            public Object visit(Ast.VarDec d0) throws InterpError {
                Value v = interp(d0.initializer,env);
                return new Env(d0.name,storeValue(v),env);
            }
            public Object visit(Ast.FuncDecs d0) throws InterpError {
                Env newEnv = env;
                // not quite right!!
                for (Ast.FuncDec d : d0.decs)
                    newEnv = new Env(d.name,storeValue(new FuncValue(new Func(env,d))),newEnv);
                return newEnv;
            }
        }
        try {
            return (Env) d0.accept(new DeclarationVisitor());
        } catch (Ast.Error exn) {
            throw (InterpError)exn;
        }
    }

    // Statement interp returns an Object as follows:
    // EXIT statement: EXIT object.
    // RETURN statement without a value: RETURN object.
    // RETURN statement with a value: Value object containing that value.
    // All other leaf statements: null.
    // Non-leaf statements propagate RETURN or Value and, unless they are loops, EXIT.

    static Object EXIT = new Object();
    static Object RETURN = new Object();

    static Object interp(Ast.St s,final Env env) throws InterpError {
        class StVisitor implements Ast.StVisitor {

            public Object visit(Ast.AssignSt s) throws InterpError {
                int l = interp(s.lhs, env);
                Value v = interp(s.rhs, env);

                storeSet(l, v);

                return null;
            }

            public Object visit(Ast.CallSt s) throws InterpError {
                // ...
                return null; // just temporary
            }

            public Object visit(Ast.ReadSt s) throws InterpError {
                // ...
                return null; // just temporary
            }

            public Object visit(Ast.WriteSt s) throws InterpError {
                for (Ast.Exp exp : s.exps) {
                    if (exp instanceof Ast.StringLitExp) { // not very "object oriented"
                        System.out.print(((Ast.StringLitExp) exp).lit);
                    } else {
                        Value v = interp(exp,env);
                        // not quite complete!
                        System.out.print(v.as_int());
                    }
                }
                System.out.println();
                return null;
            }

            public Object visit(Ast.IfSt s) throws InterpError {
                Object r = null;

                if (interp(s.test, env).as_bool()) {
                    r = interp(s.ifTrue, env);
                } else {
                    r = interp(s.ifFalse, env);
                }

                return r;
            }

            public Object visit(Ast.WhileSt s) throws InterpError {
                Object r = null;
                boolean c = interp(s.test, env).as_bool();

                while (c && r == null) {
                    r = interp(s.body,env);
                    c = interp(s.test, env).as_bool();
                }

                if (r == EXIT)
                    r = null;

                return r;
            }

            public Object visit(Ast.LoopSt s) throws InterpError {
                Object r = null;

                while (r == null)
                    r = interp(s.body,env);

                if (r == EXIT)
                    r = null;

                return r;
            }

            public Object visit(Ast.ForSt s) throws InterpError {
                Object r = null;
                int l;
                Value v1, v2, v3;

                // loopVar is already defined, so find and set it...
                l = find(s.loopVar, env).loc;

                v1 = interp(s.start, env);
                storeSet(l, v1);

                v2 = interp(s.stop, env);
                v3 = interp(s.step, env);

                while (v1.as_int() <= v2.as_int() && (r == null)) {
                    r = interp(s.body, env);

                    v1 = storeGet(l);
                    v1 = new IntValue(v1.as_int() + v3.as_int());
                    storeSet(l, v1);
                }

                if (r == EXIT)
                    r = null;

                return r;
            }

            public Object visit(Ast.ExitSt s) throws InterpError {
                return EXIT;
            }

            public Object visit(Ast.ReturnSt s) throws InterpError {
                Object v = null;
                if (s.returnValue != null)
                    return interp(s.returnValue,env);
                return RETURN;
            }

            public Object visit(Ast.BlockSt st0) throws InterpError {
                return interp(st0.body,env);
            }
        }
        // System.out.println(s);
        try {
            return s.accept(new StVisitor());
        } catch (Ast.Error exn) {
            throw (InterpError) exn;
        }
    }

    static Object interpCall(Ast.Exp func, Ast.Exp[] args, Env env) throws InterpError {
        Func f = interp(func,env).as_func();
        Env newEnv = f.env;
        for (int i = 0; i < args.length; i++) {
            Value v = interp(args[i],env);
            newEnv = new Env(f.def.formals[i].name,storeValue(v),newEnv);
        }
        return interp(f.def.body,newEnv);
    }


    static Value interp(Ast.Exp e,final Env env) throws InterpError {
        class ExpVisitor implements Ast.ExpVisitor {

            public Value visit(Ast.BinOpExp e) throws InterpError {
                Value r = null;
                int i1, i2;
                boolean b1, b2;

                switch (e.binOp) {

                    case Ast.PLUS:
                        i1 = interp(e.left,env).as_int();
                        i2 = interp(e.right,env).as_int();
                        r = new IntValue (i1 + i2);
                        break;

                    case Ast.MINUS:
                        i1 = interp(e.left,env).as_int();
                        i2 = interp(e.right,env).as_int();
                        r = new IntValue (i1 - i2);
                        break;

                    case Ast.TIMES:
                        i1 = interp(e.left,env).as_int();
                        i2 = interp(e.right,env).as_int();
                        r = new IntValue (i1 * i2);
                        break;

                    case Ast.DIV:
                        i1 = interp(e.left,env).as_int();
                        i2 = interp(e.right,env).as_int();
                        r = new IntValue (i1 / i2);
                        break;

                    case Ast.SLASH:
                        i1 = interp(e.left,env).as_int();
                        i2 = interp(e.right,env).as_int();
                        r = new IntValue (i1 / i2);
                        break;

                    case Ast.MOD:
                        i1 = interp(e.left,env).as_int();
                        i2 = interp(e.right,env).as_int();
                        r = new IntValue (i1 % i2);
                        break;

                    // Relational operators

                    case Ast.GEQ:
                        i1 = interp(e.left,env).as_int();
                        i2 = interp(e.right,env).as_int();
                        r = new BoolValue (i1 >= i2);
                        break;

                    case Ast.LEQ:
                        i1 = interp(e.left,env).as_int();
                        i2 = interp(e.right,env).as_int();
                        r = new BoolValue (i1 <= i2);
                        break;

                    case Ast.EQ:
                        i1 = interp(e.left,env).as_int();
                        i2 = interp(e.right,env).as_int();
                        r = new BoolValue (i1 == i2);
                        break;

                    case Ast.NEQ:
                        i1 = interp(e.left,env).as_int();
                        i2 = interp(e.right,env).as_int();
                        r = new BoolValue (i1 != i2);
                        break;

                    case Ast.GT:
                        i1 = interp(e.left,env).as_int();
                        i2 = interp(e.right,env).as_int();
                        r = new BoolValue (i1 > i2);
                        break;

                    case Ast.LT:
                        i1 = interp(e.left,env).as_int();
                        i2 = interp(e.right,env).as_int();
                        r = new BoolValue (i1 < i2);
                        break;

                    // Logical operators

                    case Ast.AND:
                        b1 = interp(e.left,env).as_bool();
                        b2 = interp(e.right,env).as_bool();
                        r = new BoolValue (b1 && b2);
                        break;

                    case Ast.OR:
                        b1 = interp(e.left,env).as_bool();
                        b2 = interp(e.right,env).as_bool();
                        r = new BoolValue (b1 || b2);
                        break;
                }
                return r;
            }

            public Value visit(Ast.UnOpExp e) throws InterpError {
                Value r = null;
                int i1;
                boolean b1;

                switch (e.unOp) {
                    case Ast.UMINUS:
                        i1 = interp(e, env).as_int();
                        r = new IntValue(i1 * -1);
                        break;

                    case Ast.NOT:
                        b1 = interp(e, env).as_bool();
                        r = new BoolValue(!b1);
                        break;
                }

                return r;
            }

            public Value visit(Ast.LvalExp e) throws InterpError {
                return storeGet(interp(e.lval,env));
            }

            public Value visit(Ast.CallExp e) throws InterpError {
                Object res = interpCall(e.func,e.args,env);
                if (res == null)
                    throw new InterpError(e.line,"Function call returned no value"); // typechecker may have prevented this
                return (Value) res;
            }

            public Value visit(Ast.ArrayExp e) throws InterpError {
                // ...
                return null; // just temporary
            }

            public Value visit(Ast.RecordExp e) throws InterpError {
                int rec = allocateStore(e.initializers.length);
                for (Ast.RecordInit init : e.initializers) {
                    Value v = interp(init.value,env);
                    storeSet(rec+init.offset,v);
                }
                return new LocValue(rec);
            }

            public Value visit(Ast.IntLitExp e) throws InterpError {
                return new IntValue(e.lit);
            }

            public Value visit(Ast.RealLitExp e) throws InterpError {
                throw new Error ("Impossible RealLitExp");
            }

            public Value visit(Ast.StringLitExp e) throws InterpError {
                throw new Error ("Impossible StringLitExp");
            }
        }
        try {
            return (Value) e.accept(new ExpVisitor());
        } catch (Ast.Error exn) {
            throw (InterpError) exn;
        }
    }

    // Return store location for lvalue.
    static int interp(Ast.Lvalue l, final Env env) throws InterpError {
        class LvalueVisitor implements Ast.LvalueVisitor {

            public Integer visit(Ast.VarLvalue l) throws InterpError {
                Env binding = find(l.name,env);
                return binding.loc;
            }

            public Integer visit(Ast.ArrayDerefLvalue l) throws InterpError {
                int l0 = interp(l.array,env);
                int base = storeGet(l0).as_loc();
                int v = interp(l.index,env).as_int();
                if (v < 0 || v >= storeGet(base-1).as_int())
                    throw new InterpError(l.line,"Array subscript out of bounds");
                return (base + v);
            }

            public Integer visit(Ast.RecordDerefLvalue l) throws InterpError {
                // ...
                return null; // just temporary
            }
        }
        try {
            return (Integer) l.accept(new LvalueVisitor());
        } catch (Ast.Error exn) {
            throw (InterpError) exn;
        }
    }

}





