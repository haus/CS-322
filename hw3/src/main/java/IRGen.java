import java.io.*;
import java.util.*;


class IRGen {

    static int ir_type (Ast.TypeExp t) {
        if (Ast.is_boolean_type(t))
            return IR.BOOL;
        else if (Ast.is_integer_type(t))
            return IR.INT;
        else
            return IR.PTR;
    }

    static int type_size (Ast.TypeExp t) {
        return IR.type_size[ir_type (t)];
    }

    // Round x up to nearest multiple of p, provided p is a multiple of 2
    static int roundup(int x,int p) {
        return (x + p - 1) & ~(p - 1);
    }

    // Return byte offset of n'th field in record
    // Assume each field's alignment is the same as its size
    static int calc_byte_offset (Ast.RecordTypeDec r,int n) {
        int offset = 0;
        for (int i = 0; ; i++) {
            int s = type_size(r.all_components[i].type);
            offset = roundup(offset,s);
            if (i == n)
                break;
            offset += s;
        }
        return offset;
    }

    // these are all per-function
    static int nextTemp;
    static int nextLabel;
    static List<IR.Var> locals;
    static List<IR.Inst> code;

    static List<Ast.FuncDec> todo;

    static IR.Program gen(Ast.Program p)  {
        List<IR.Func> all_funcs = new ArrayList<IR.Func>();
        todo = new ArrayList<Ast.FuncDec>();
        IR.Func main = gen_function("$MAIN",new Ast.IdType[0],new Ast.IdType[0],p.body,true,true);
        all_funcs.add(main);
        while (!todo.isEmpty())  {
            Ast.FuncDec fdec = todo.remove(0);
            all_funcs.add (gen_function(fdec.name + "_" + fdec.unique,fdec.freevars,fdec.formals,fdec.body,
                    !(Ast.is_unit_type(fdec.resultType)),false));
        }
        return new IR.Program(all_funcs);
    }

    static IR.Func gen_function(String name, Ast.IdType[] freevars, Ast.IdType[] formals, Ast.Block body, boolean returns_value,
                                boolean top) {
        nextTemp = 0;
        nextLabel = 0;
        List<IR.Var> ir_freevars = new ArrayList<IR.Var>();

        for (Ast.IdType freevar : freevars)
            ir_freevars.add(new IR.Var(freevar.name + "_" + freevar.unique,ir_type(freevar.type)));

        List<IR.Var> ir_formals = new ArrayList<IR.Var>();

        for (Ast.IdType formal : formals)
            ir_formals.add(new IR.Var(formal.name + "_" + formal.unique,ir_type(formal.type)));

        locals = new ArrayList<IR.Var>();
        code = new ArrayList<IR.Inst>();
        code.add(new IR.LabelDec(nextLabel++));  // obligatory entry label
        int lreturn = nextLabel++;
        gen(body,-1,lreturn);
        code.add(new IR.LabelDec(lreturn));

        if (top) {
            code.add(new IR.Mov(IR.INT,IR.ZERO,IR.RETREG));
            code.add(new IR.LabelDec(nextLabel++));
        }

        return new IR.Func(name,ir_freevars,ir_formals,locals,code,returns_value);
    }

    static void gen(Ast.Block b0,final int lexit,int lreturn)  {
        for (Ast.BlockItem b : b0.items)

            if (b instanceof Ast.Declaration)
                gen((Ast.Declaration) b);

            else if (b instanceof Ast.St)
                gen((Ast.St) b,lexit,lreturn);
    }

    static void gen(Ast.Declaration d0)  {
        class DeclarationVisitor implements Ast.DeclarationVisitor {

            public Object visit(Ast.VarDec d0)  {
                String ir_name = d0.name + "_" + d0.unique;
                locals.add(new IR.Var(ir_name,ir_type(d0.type)));
                IR.Operand ir_init = gen(d0.initializer);
                code.add(new IR.Mov(ir_type(d0.type),ir_init,new IR.Name(ir_name)));
                return null;
            }

            public Object visit(Ast.FuncDecs d0)  {
                // ...
                return null;
            }

        }

        try {
            d0.accept(new DeclarationVisitor());
        } catch (Ast.Error exn) {
            System.err.println(exn.getMessage());
        }
    }

    static void gen(Ast.St s, final int lexit, final int lreturn)  {

        class StVisitor implements Ast.StVisitor {

            public Object visit(Ast.AssignSt s)  {
                code.add(new IR.Mov(ir_type(s.rhs.type), gen(s.rhs), gen(s.lhs)));
                return null;
            }

            public Object visit(Ast.CallSt s)  {
                genCall(s.func,s.args,false);
                return null;
            }

            public Object visit(Ast.ReadSt s)  {
                IR.Operand[] ts = new IR.Operand[s.targets.length];
                for (int i = 0; i < s.targets.length; i++) {
                    ts[i] = gen(s.targets[i]);
                    // This kludgy bit is needed because the fab semantics insists that
                    // the lvalues are fully evaluated before any reading occurs, e.g. in
                    //  read(I,A[I])
                    // we must evaluate A[I]'s address before reading the new value of I
                    // (This would be a nice place to use an LEA instruction, but the IR doesn't currently have one...)
                    if (ts[i] instanceof IR.Mem) {
                        IR.Temp tmp = new IR.Temp(nextTemp++);
                        code.add(new IR.Arith(IR.INT,IR.MUL,((IR.Mem) ts[i]).index,new IR.IntLit(((IR.Mem) ts[i]).scale),tmp));
                        code.add(new IR.Arith(IR.PTR,IR.ADD,((IR.Mem) ts[i]).base,tmp,tmp));
                        ts[i] = new IR.Mem(tmp,IR.ZERO,1);
                    }
                }
                for (int i = 0; i < s.targets.length; i++) {
                    code.add(new IR.Call(true,new IR.StringLit("read_int"),0,true));
                    code.add(new IR.Mov(IR.INT,IR.RETREG,ts[i]));
                }
                return null;
            }

            public Object visit(Ast.WriteSt s)  {
                //IR.Operand[] ts = new IR.Operand[s.exps.length];
                for (int i = 0; i < s.exps.length; i++) {
                    IR.Operand t1 = gen(s.exps[i]);
                    int type = ir_type(s.exps[i].type);
                    if(type  == 0) { //Bool
                        code.add(new IR.Mov(IR.BOOL, t1, new IR.Arg(0)));
                        code.add(new IR.Call(true,new IR.StringLit("write_bool"),1,false));
                    } else if(type == 1) { //Int
                        code.add(new IR.Mov(IR.INT, t1, new IR.Arg(0)));
                        code.add(new IR.Call(true,new IR.StringLit("write_int"),1,false));
                    } else if(type == 2) { //Ptr
                        code.add(new IR.Mov(IR.PTR, t1, new IR.Arg(0)));
                        code.add(new IR.Call(true,new IR.StringLit("write_string"),1,false));
                    }
                }

                code.add(new IR.Call(true,new IR.StringLit("write_newline"),0,false));

                return null;
            }

            public Object visit(Ast.IfSt s)  {
                int ltrue = nextLabel++;
                int lfalse = nextLabel++;
                int lend = nextLabel++;
                gen(s.test, ltrue, lfalse);

                code.add(new IR.LabelDec(ltrue));
                gen(s.ifTrue, lexit, lreturn);

                code.add(new IR.Jump(0, lend));

                code.add(new IR.LabelDec(lfalse));
                gen(s.ifFalse, lexit, lreturn);

                code.add(new IR.LabelDec(lend));

                return null;
            }


            public Object visit(Ast.WhileSt s)  {
                int ltest = nextLabel++;
                int lbody = nextLabel++;
                int lend = nextLabel++;
                code.add(new IR.LabelDec(ltest));
                gen(s.test, lbody, lend);

                code.add(new IR.LabelDec(lbody));
                gen(s.body, lend, lreturn);

                code.add(new IR.Jump(0, ltest));
                code.add(new IR.LabelDec(lend));

                return null;
            }

            public Object visit(Ast.LoopSt s)  {
                int lbody = nextLabel++;
                int lend = nextLabel++;
                code.add(new IR.LabelDec(lbody));

                gen(s.body, lend, lreturn);

                code.add(new IR.Jump(0, lbody));

                code.add(new IR.LabelDec(lend));

                return null;
            }

            public Object visit(Ast.ForSt s)  {
                int ltest = nextLabel++;
                int lend = nextLabel++;
                IR.Name startVar = new IR.Name(s.loopVar + "_" + s.unique);

                // Loop Setup
                IR.Operand stop = gen(s.stop);
                IR.Operand step = gen(s.step);
                code.add(new IR.Mov(ir_type(s.start.type), gen(s.start), startVar));

                // Loop Test
                code.add(new IR.LabelDec(ltest));
                code.add(new IR.Cmp(ir_type(s.start.type), startVar, stop));

                // End if greater than stop cond.
                code.add(new IR.Jump(3, lend));

                // Otherwise, the body.
                gen(s.body, lend, lreturn);

                // Increment the counter and jump to the test...
                code.add(new IR.Arith(ir_type(s.start.type), IR.ADD, startVar, step, startVar));
                code.add(new IR.Jump(0, ltest));

                code.add(new IR.LabelDec(lend));

                return null;
            }

            public Object visit(Ast.ExitSt s)  {
                code.add(new IR.Jump(0,lexit));
                return null;
            }

            public Object visit(Ast.ReturnSt s)  {
                if (s.returnValue != null) {
                    IR.Operand t = gen(s.returnValue);
                    code.add(new IR.Mov(ir_type(s.returnValue.type),t,IR.RETREG));
                }
                code.add(new IR.Jump(0,lreturn));
                return null;
            }

            public Object visit(Ast.BlockSt s)  {
                gen(s.body,lexit,lreturn);
                return null;
            }
        }

        try {
            s.accept(new StVisitor());
        } catch (Ast.Error exn) {
            System.err.println(exn.getMessage());
        }
    }


    static void genCall(Ast.Exp func, Ast.Exp[] args, boolean returns_value)  {
        IR.Operand f = tempify(IR.PTR,gen(func));
        IR.Operand[] operands = new IR.Operand[args.length];
        for (int i = 0; i < args.length; i++)
            operands[i] = tempify(ir_type(args[i].type),gen(args[i]));
        for (int i = args.length - 1 ; i >= 0; i--)
            code.add(new IR.Mov(ir_type(args[i].type),operands[i],new IR.Arg(i)));
        code.add(new IR.Call(false,f,args.length,returns_value));
    }

    // Generate expression into an operand
    static IR.Operand gen(Ast.Exp e)  {
        class ExpVisitor implements Ast.ExpVisitor {

            public Object visit(Ast.BinOpExp e)  {
                if (e.binOp >= Ast.PLUS && e.binOp <= Ast.MOD) {
                    // arithmetic expression
                    IR.Operand t1 = gen(e.left);
                    IR.Operand t2 = gen(e.right);
                    IR.Operand t = new IR.Temp(nextTemp++);
                    switch (e.binOp) { // think about using a table here instead!
                        case Ast.PLUS:
                            code.add(new IR.Arith(IR.INT,IR.ADD,t1,t2,t));
                            break;

                        case Ast.MINUS:
                            code.add(new IR.Arith(IR.INT,IR.SUB,t1,t2,t));
                            break;

                        case Ast.TIMES:
                            code.add(new IR.Arith(IR.INT,IR.MUL,t1,t2,t));
                            break;

                        case Ast.DIV:
                            code.add(new IR.Arith(IR.INT,IR.DIV,t1,t2,t));
                            break;

                        case Ast.MOD:
                            code.add(new IR.Arith(IR.INT,IR.MOD,t1,t2,t));
                            break;

                        // Reals here...
                        case Ast.SLASH:
                            // Nothing to do really.
                            break;

                    }
                    return t;
                } else {
                    // boolean-valued expression
                    int ltrue = nextLabel++;
                    int lfalse = nextLabel++;
                    gen(e, ltrue, lfalse);

                    IR.Operand t = new IR.Temp(nextTemp++);

                    code.add(new IR.LabelDec(ltrue));
                    code.add(new IR.Mov(0, IR.TRUE, t));

                    code.add(new IR.LabelDec(lfalse));
                    code.add(new IR.Mov(0, IR.FALSE, t));

                    return t;
                }
            }

            public Object visit(Ast.UnOpExp e)  {
                if (e.unOp == Ast.UMINUS) {
                    IR.Operand t1 = gen(e.operand);
                    IR.Operand t = new IR.Temp(nextLabel++);
                    code.add(new IR.Arith(IR.INT,IR.SUB,IR.ZERO,t1,t));
                    return t;
                } else {
                    return null;
                }
            }

            public Object visit(Ast.LvalExp e)  {
                // Kludge to handle true, false, nil without an environment
                if (e.lval instanceof Ast.VarLvalue) {
                    if (((Ast.VarLvalue) e.lval).name.equals("true"))
                        return IR.TRUE;
                    else if (((Ast.VarLvalue) e.lval).name.equals("false"))
                        return IR.FALSE;
                    else if (((Ast.VarLvalue) e.lval).name.equals("nil"))
                        return IR.NIL;
                }
                return gen(e.lval);
            }

            public Object visit(Ast.CallExp e)  {
                genCall(e.func,e.args,true);
                IR.Operand t = new IR.Temp(nextTemp++);
                code.add(new IR.Mov(ir_type(e.type),IR.RETREG,t));
                return t;
            }

            public Object visit(Ast.ArrayExp e)  {
                int ir_element_type = ir_type(e.type);
                int element_size = IR.type_size[ir_element_type];
                int count_size = (element_size > IR.type_size[IR.INT] ? element_size : IR.type_size[IR.INT]); // for alignment
                IR.Operand count_offset = new IR.IntLit(-IR.type_size[IR.INT]);
                IR.Operand[] counts = new IR.Operand[e.initializers.length];
                IR.Operand[] vals = new IR.Operand[e.initializers.length];
                for (int i = 0; i < e.initializers.length; i++) {
                    counts[i] = tempify(IR.INT,gen(e.initializers[i].count));
                    if (counts[i] instanceof IR.IntLit) {
                        IR.IntLit i1 = (IR.IntLit) (counts[i]);
                        if (i1.i < 0) counts[i] = IR.ZERO;
                    } else {
                        int lok = nextLabel++;
                        code.add(new IR.Cmp(IR.INT,counts[i],IR.ZERO));
                        code.add(new IR.Jump(IR.GE,lok));
                        code.add(new IR.Mov(IR.INT, IR.ZERO, counts[i]));
                        code.add(new IR.LabelDec(lok));
                    }
                    vals[i] = tempify(ir_element_type,gen(e.initializers[i].value));
                }
                IR.Operand s = new IR.Temp(nextTemp++);
                IR.Operand t = new IR.Temp(nextTemp++);
                IR.Operand u = new IR.Temp(nextTemp++);
                code.add(new IR.Mov(IR.INT,IR.ZERO,s));
                for (int i = 0; i < e.initializers.length; i++)
                    code.add(new IR.Arith(IR.INT,IR.ADD,s,counts[i],s));
                // s holds count
                code.add(new IR.Arith(IR.INT,IR.MUL,s,new IR.IntLit(element_size),t));
                code.add(new IR.Arith(IR.INT,IR.ADD,t,new IR.IntLit(count_size),t));
                // t holds allocation request in bytes
                code.add(new IR.Mov(IR.INT,t,new IR.Arg(0)));
                code.add(new IR.Call(true,new IR.StringLit("alloc"),1,true));
                code.add(new IR.Mov(IR.PTR,IR.RETREG,t));
                code.add(new IR.Arith(IR.PTR,IR.ADD,t,new IR.IntLit(count_size),t));  // arithmetic on pointers
                // t hold pointer to element 0
                code.add(new IR.Mov(IR.INT,s,new IR.Mem(t,IR.MONE,IR.type_size[IR.INT]))); // store count
                code.add(new IR.Mov(IR.INT,IR.ZERO,s));
                for (int i = 0; i < e.initializers.length; i++) {
                    code.add(new IR.Arith(IR.INT,IR.ADD,s,counts[i],u));
                    int ltop = nextLabel++;
                    int ltest = nextLabel++;
                    code.add(new IR.Jump(0, ltest));
                    code.add(new IR.LabelDec(ltop));
                    code.add(new IR.Mov(ir_element_type,vals[i],new IR.Mem(t,s,element_size)));
                    code.add(new IR.Arith(IR.INT,IR.ADD,s,IR.ONE,s));
                    code.add(new IR.LabelDec(ltest));
                    code.add(new IR.Cmp(IR.INT,s,u));
                    code.add(new IR.Jump(IR.L,ltop));
                }
                return t;
            }

            public Object visit(Ast.RecordExp e)  {
                for (int i = 0; i < e.initializers.length; i++) {

                }
                /*
                IR.Operand addr = tempify(IR.PTR,gen(l.record));
                int l1 = nextLabel++;
                code.add(new IR.Cmp(IR.PTR,addr,IR.NIL));
                code.add(new IR.Jump(IR.NE,l1));
                code.add(new IR.Call(true,new IR.StringLit("nil_pointer"),0,false));
                code.add(new IR.LabelDec(l1));
                return new IR.Mem(addr,new IR.IntLit(calc_byte_offset(l.typeDec,l.offset)),1);
                */
                return null;
            }

            public Object visit(Ast.IntLitExp e)  {
                return new IR.IntLit(e.lit);
            }

            public Object visit(Ast.RealLitExp e)  {
                assert(false); // not supported
                return null;
            }

            public Object visit(Ast.StringLitExp e) {
                return new IR.StringLit(e.lit);
            }
        }
        IR.Operand r = null;
        try {
            r = (IR.Operand) e.accept(new ExpVisitor());
        } catch (Ast.Error exn) {
            System.err.println(exn.getMessage());
        }
        return r;
    }

    // Generate boolean-typed expressions to control-flow form
    static void gen(Ast.Exp e, int ltrue, int lfalse)  {
        if (e instanceof Ast.BinOpExp) {
            Ast.BinOpExp e0 = (Ast.BinOpExp) e;
            if (e0.binOp == Ast.AND) {
                int lright = nextLabel++;
                gen(e0.left, lright, lfalse);
                code.add(new IR.LabelDec(lright));
                gen(e0.right, ltrue, lfalse);
            } else if (e0.binOp == Ast.OR) {
                int lright = nextLabel++;
                gen(e0.left, ltrue, lright);
                code.add(new IR.LabelDec(lright));
                gen(e0.right, ltrue, lfalse);
            } else if (e0.binOp >= Ast.LT && e0.binOp <= Ast.NEQ) {
                // Same for all int...reals would use a, ae, b, be
                code.add(new IR.Cmp(1, gen(e0.left), gen(e0.right)));
                int op = 0;

                switch (e0.binOp) {
                    case Ast.LT:
                        op = 5;
                        break;

                    case Ast.LEQ:
                        op = 6;
                        break;

                    case Ast.GT:
                        op = 3;
                        break;

                    case Ast.GEQ:
                        op = 4;
                        break;

                    case Ast.EQ:
                        op = 1;
                        break;

                    case Ast.NEQ:
                        op = 2;
                        break;
                }

                // Slightly less messy. Add an appropriate jump.
                code.add(new IR.Jump(op, ltrue));

                // Same for all int...
                code.add(new IR.Jump(0, lfalse));
            }
            // other cases impossible
        } else if (e instanceof Ast.UnOpExp) {
            Ast.UnOpExp e0 = (Ast.UnOpExp) e;
            if (e0.unOp == Ast.NOT)
                gen(e0, lfalse, ltrue);
            // other cases impossible
        } else {
            IR.Operand t = gen(e);  // must already be a value
            code.add(new IR.Cmp(IR.BOOL,t,IR.FALSE));
            code.add(new IR.Jump(IR.E,lfalse));
            code.add(new IR.Jump(0,ltrue));
        }
    }


    static IR.Operand gen(Ast.Lvalue l)  {
        class LvalueVisitor implements Ast.LvalueVisitor {

            public Object visit(Ast.VarLvalue l) {
                return new IR.Name(l.name + "_" + l.unique);
            }

            public Object visit(Ast.ArrayDerefLvalue l) {
                // Get base array address, array size, get array type, check bounds, return address.
                // Base address.
                IR.Operand base = tempify(IR.PTR,gen(l.array));

                // Index code
                IR.Operand index = gen(l.index);

                // Bounds error label...
                int l1 = nextLabel++;

                // Compare index to array size...
                code.add(new IR.Cmp(IR.INT, index, new IR.Mem(base, IR.MONE, IR.type_size[IR.INT])));

                // Jump to below error state if valid. Otherwise go to error state.
                code.add(new IR.Jump(IR.B,l1));

                code.add(new IR.Call(true,new IR.StringLit("bounds_error"),0,false));
                code.add(new IR.LabelDec(l1));

                return new IR.Mem(base, index, IR.type_size[ir_type(l.type)]);
            }

            public Object visit(Ast.RecordDerefLvalue l) {
                IR.Operand addr = tempify(IR.PTR,gen(l.record));
                int l1 = nextLabel++;
                code.add(new IR.Cmp(IR.PTR,addr,IR.NIL));
                code.add(new IR.Jump(IR.NE,l1));
                code.add(new IR.Call(true,new IR.StringLit("nil_pointer"),0,false));
                code.add(new IR.LabelDec(l1));
                return new IR.Mem(addr,new IR.IntLit(calc_byte_offset(l.typeDec,l.offset)),1);
            }

        }

        IR.Operand r = null;

        try {
            r = (IR.Operand) l.accept(new LvalueVisitor());
        } catch (Ast.Error exn) {
            System.err.println(exn.getMessage());
        }

        return r;
    }

    // if operand is not already in a register or name, copy it to a fresh temp
    static IR.Operand tempify(int type,IR.Operand src) {
        assert (! (src instanceof IR.RetReg));
        assert (! (src instanceof IR.Arg));
        if (src instanceof IR.Mem) {
            IR.Operand nsrc = new IR.Temp(nextTemp++);
            code.add(new IR.Mov(type,src,nsrc));
            return nsrc;
        } else
            return src;
    }

}