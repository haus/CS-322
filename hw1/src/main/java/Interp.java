/**
 * @author Matthaus Litteken
 * @author Jonah Brasseur
 * @version 01.23.2011
 * @copyright 2011
 *
 * HW 1 for CS322. This solution attempts real numbers also.
 */

import java.io.*;
import java.math.BigDecimal;
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

        double as_real() throws InterpError {
            throw new Error("Impossible as_real");
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

        // To make real coercion easier.
        double as_real() {
            return (double) v;
        }
    }

    /**
     * RealValue uses doubles to handle real numbers.
     */
    static class RealValue extends Value {
        double r;

        RealValue (double r) {
            this.r = r;
        }

        double as_real() {
            return r;
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

        // Nil has a null value, so simple null equality checks can be used.
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
                int len = d0.decs.length;
                int base = allocateStore(len);
                int i = base;


                // First add the function name...
                for (Ast.FuncDec d : d0.decs) {
                    //newEnv = new Env(d.name,storeValue(new FuncValue(new Func(env,d))),newEnv);
                    newEnv = new Env(d.name, i++, newEnv);
                }

                i = base;

                // Then pass the newEnv to the function, so it is self-aware.
                for (Ast.FuncDec d : d0.decs) {
                    storeSet(i++, new FuncValue(new Func(newEnv,d)));
                }


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
                interpCall(s.func, s.args, env);
                return null;
            }

            public Object visit(Ast.ReadSt s) throws InterpError {
                Value curVal;
                String curToken;

                for (Ast.Lvalue lv : s.targets) {
                    try {
                        curToken = readToken();
                    } catch (IOException ex) {
                        throw new InterpError(lv.line, ex.getMessage());
                    }

                    // First try int, then try double.

                    try {
                        curVal = new IntValue(Integer.parseInt(curToken));
                        storeSet(interp(lv, env), curVal);
                    } catch (NumberFormatException ex) {
                        try {
                            curVal = new RealValue(Double.parseDouble(curToken));
                            storeSet(interp(lv, env), curVal);
                        } catch (NumberFormatException ex2) {
                            throw new InterpError(lv.line, ex2.getMessage());
                        }
                    }

                }

                return null;
            }

            public Object visit(Ast.WriteSt s) throws InterpError {
                for (Ast.Exp exp : s.exps) {
                    if (exp instanceof Ast.StringLitExp) { // not very "object oriented"
                        System.out.print(((Ast.StringLitExp) exp).lit);
                    } else {
                        Value v = interp(exp,env);

                        if (v instanceof IntValue) {
                            System.out.print(v.as_int());
                        } else if (v instanceof BoolValue) {
                            System.out.print(v.as_bool());
                        } else if (v instanceof RealValue) {
                            System.out.print(v.as_real());
                        }
                    }
                }

                System.out.println();
                return null;
            }

            public Object visit(Ast.IfSt s) throws InterpError {
                Object r;

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

                // Interp these two values before changing v1 in the store.

                v2 = interp(s.stop, env);
                v3 = interp(s.step, env);

                // loopVar is already defined, so find and set it...
                l = find(s.loopVar, env).loc;

                v1 = interp(s.start, env);
                storeSet(l, v1);

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
                BigDecimal bd1, bd2;
                Value v1, v2;

                switch (e.binOp) {

                    case Ast.PLUS:
                        v1 = interp(e.left,env);
                        v2 = interp(e.right,env);

                        if (v1 instanceof RealValue || v2 instanceof RealValue) {
                            r = new RealValue(v1.as_real() + v2.as_real());
                        } else {
                            r = new IntValue (v1.as_int() + v2.as_int());
                        }
                        break;

                    case Ast.MINUS:
                        v1 = interp(e.left,env);
                        v2 = interp(e.right,env);

                        if (v1 instanceof RealValue || v2 instanceof RealValue) {
                            r = new RealValue(v1.as_real() - v2.as_real());
                        } else {
                            r = new IntValue (v1.as_int() - v2.as_int());
                        }
                        break;

                    case Ast.TIMES:
                        v1 = interp(e.left,env);
                        v2 = interp(e.right,env);

                        if (v1 instanceof RealValue || v2 instanceof RealValue) {
                            r = new RealValue(v1.as_real() * v2.as_real());
                        } else {
                            r = new IntValue (v1.as_int() * v2.as_int());
                        }
                        break;

                    case Ast.DIV:
                        i1 = interp(e.left,env).as_int();
                        i2 = interp(e.right,env).as_int();

                        try {
                            r = new IntValue (i1 / i2);
                        } catch (ArithmeticException ex) {
                            throw new InterpError(e.left.line, ex.getMessage() + "Division by zero.");
                        }
                        break;

                    case Ast.SLASH:
                        double d1 = interp(e.left,env).as_real();
                        double d2 = interp(e.right,env).as_real();

                        try {
                            r = new RealValue (d1 / d2);
                        } catch (ArithmeticException ex) {
                            throw new InterpError(e.left.line, ex.getMessage() + "Division by zero.");
                        }

                        break;

                    case Ast.MOD:
                        i1 = interp(e.left,env).as_int();
                        i2 = interp(e.right,env).as_int();
                        r = new IntValue (i1 % i2);
                        break;

                    // Relational operators

                    case Ast.GEQ:
                        v1 = interp(e.left,env);
                        v2 = interp(e.right,env);

                        if (v1 instanceof RealValue || v2 instanceof RealValue) {
                            r = new BoolValue(v1.as_real() >= v2.as_real());
                        } else {
                            r = new BoolValue (v1.as_int() >= v2.as_int());
                        }
                        break;

                    case Ast.LEQ:
                        v1 = interp(e.left,env);
                        v2 = interp(e.right,env);

                        if (v1 instanceof RealValue || v2 instanceof RealValue) {
                            r = new BoolValue(v1.as_real() <= v2.as_real());
                        } else {
                            r = new BoolValue (v1.as_int() <= v2.as_int());
                        }
                        break;

                    case Ast.EQ:
                        v1 = interp(e.left,env);
                        v2 = interp(e.right,env);

                        if (v1 instanceof RealValue || v2 instanceof RealValue) {
                            r = new BoolValue(v1.as_real() == v2.as_real());
                        } else if (v1 instanceof IntValue && v2 instanceof IntValue) {
                            r = new BoolValue (v1.as_int() == v2.as_int());
                        } else if (v1 == null && v2 == null) { // This checks for nil...
                            r = new BoolValue(true);
                        } else { // This also checks for nil...
                            r = new BoolValue(false);
                        }
                        break;

                    case Ast.NEQ:
                        v1 = interp(e.left,env);
                        v2 = interp(e.right,env);

                        if (v1 instanceof RealValue || v2 instanceof RealValue) {
                            r = new BoolValue(v1.as_real() != v2.as_real());
                        } else if (v1 instanceof IntValue && v2 instanceof IntValue) {
                            r = new BoolValue (v1.as_int() != v2.as_int());
                        } else if (v1 == null && v2 == null) { // This checks for nil...
                            r = new BoolValue(false);
                        } else { // This is also a nil check...
                            r = new BoolValue(true);
                        }
                        break;

                    case Ast.GT:
                        v1 = interp(e.left,env);
                        v2 = interp(e.right,env);

                        if (v1 instanceof RealValue || v2 instanceof RealValue) {
                            r = new BoolValue(v1.as_real() > v2.as_real());
                        } else {
                            r = new BoolValue (v1.as_int() > v2.as_int());
                        }
                        break;

                    case Ast.LT:
                        v1 = interp(e.left,env);
                        v2 = interp(e.right,env);

                        if (v1 instanceof RealValue || v2 instanceof RealValue) {
                            r = new BoolValue(v1.as_real() < v2.as_real());
                        } else {
                            r = new BoolValue (v1.as_int() < v2.as_int());
                        }
                        break;

                    // Logical operators

                    case Ast.AND:
                        b1 = interp(e.left,env).as_bool();

                        // Short circuit and...if first is false, whole is false
                        if (!b1) {
                            r = new BoolValue(false);
                            break;
                        }

                        b2 = interp(e.right,env).as_bool();
                        r = new BoolValue (b1 && b2);
                        break;

                    case Ast.OR:
                        b1 = interp(e.left,env).as_bool();

                        // Short circuit and...if first is true, whole is true
                        if (b1) {
                            r = new BoolValue(true);
                            break;
                        }

                        b2 = interp(e.right,env).as_bool();
                        r = new BoolValue (b1 || b2);
                        break;
                }
                return r;
            }

            public Value visit(Ast.UnOpExp e) throws InterpError {
                Value r = null;
                boolean b1;
                Value v1;

                switch (e.unOp) {
                    case Ast.UMINUS:
                        v1 = interp(e.operand, env);

                        if (v1 instanceof RealValue) {
                            r = new RealValue(v1.as_real() * -1);
                        } else {
                            r = new IntValue(v1.as_int() * -1);
                        }
                        break;

                    case Ast.NOT:
                        b1 = interp(e.operand, env).as_bool();
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
                /**
                 * Finished I think...
                 */
                int arrayHead = allocateStore(1);
                int count = 0;

                for (Ast.ArrayInit init : e.initializers) {
                    int i = interp(init.count, env).as_int();
                    count += i;
                    int curHead = allocateStore(interp(init.count, env).as_int());
                    Value v = interp(init.value,env);

                    for (int j = 0; j < i; j++) {
                        storeSet(curHead + j, v);
                    }
                }

                storeSet(arrayHead, new IntValue(count));

                return new LocValue(arrayHead+1);
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
                Value v;
                try {
                    v = new RealValue(Double.parseDouble(e.lit));
                } catch (ArithmeticException ex) {
                    throw new InterpError(e.line, "Bad real format.");
                }

                return v;
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
                int l0 = interp(l.record, env);
                int base = storeGet(l0).as_loc();
                return (base + l.offset);
            }
        }
        try {
            return (Integer) l.accept(new LvalueVisitor());
        } catch (Ast.Error exn) {
            throw (InterpError) exn;
        }
    }

}





