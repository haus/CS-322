import javax.management.relation.InvalidRelationTypeException;
import java.io.*;
import java.util.*;

class X86Gen {

    static final X86.Reg closureReg = X86.RDI;
    static final X86.Reg tempReg1 = X86.R10;
    static final X86.Reg tempReg2 = X86.R11;


    // per-program globals
    static Map<String,IR.Func> funcenv;
    static int funcNumber; // current function number, used to construct unique labels
    static List<String> stringLiterals; // accumulated string literals, indexed by position

    // generate code for a program
    static void gen(IR.Program p) {
        funcenv = new TreeMap<String,IR.Func>();
        funcNumber = 0;
        stringLiterals = new ArrayList<String>();
        for (IR.Func f : p.funcs)
            funcenv.put(f.name,f);
        X86.emit(".text");
        for (IR.Func f : p.funcs) {
            gen(f);
            funcNumber++;
        }
        // emit any accumulated string literals
        for(int i = 0; i < stringLiterals.size(); i++)
        {
            X86.emitLabel(new X86.Label("__S" + i));
            X86.emitString(stringLiterals.get(i));
        }
    }

    // per-function globals
    static Map<IR.Operand,X86.Operand> env; // location mapping
    static int localsSize; // in bytes


    // generate code for a function
    static void gen(IR.Func fdef) {
        env = new HashMap<IR.Operand,X86.Operand>();
        // freevars will stay in memory in the closure record;
        // we record their addresses in the environment now
        // ...
        int offset = 0;
        for (int i = 0; i < fdef.freevars.length; i++) {


            X86.Operand t = new X86.Mem(closureReg, offset);
            IR.Operand x = new IR.Name(fdef.freevars[i].id);

            env.put(x, t);
            offset += 8;
        }

        // assign a location (register or frame) to every IR.Operand that has a live range
        // fills in remainder of env and sets localsSize
        allocateRegisters(fdef);

        // emit the function header
        X86.emit0(".p2align 4,0x90");
        X86.emit(".globl " + "__" + fdef.name);
        X86.emitLabel(new X86.Label("__" + fdef.name));

        // save any callee-save registers on the stack now
        for (int i = 0; i < X86.calleeSaveRegs.length; i++) {
            //if (env.containsValue(X86.calleeSaveRegs[i])) {
                X86.emit1("pushq", X86.calleeSaveRegs[i]);
            //}
        }


        // make space for the local frame
        // be sure to keep stack growth in multiples of 16
        int frameSize = X86.roundup(localsSize,16);   // may need adjustment!
        X86.emit2("subq",new X86.Imm(frameSize),X86.RSP);

        // move the incoming actual arguments to their assigned locations.
        //   this only works if assigned locations of actuals are either already
        //   identical to where the calling convention puts them (the usual case)
        //   or are disjoint -- allocateRegisters must guarantee this!
        // simply fail if function has more than 5 args
        assert (fdef.formals.length <= X86.argRegs.length - 1);
        for (int i = 0; i < fdef.formals.length; i++) {
            X86.Operand t = env.get(new IR.Name(fdef.formals[i].id));
            if (t != null)  // no point in initializing a dead variable
                X86.emitMov(X86.Q,X86.argRegs[i+1],t);  // offset by one, because RDI = argRegs[0] holds closure pointer
        }

        // emit code for the body
        for (IR.Inst inst : fdef.code)
            gen(inst);


        // pop the frame
        X86.emit2("addq",new X86.Imm(frameSize),X86.RSP);

        // restore any callee save registers
        for (int i = X86.calleeSaveRegs.length - 1; i >= 0; i--) {
            //if (env.containsValue(X86.calleeSaveRegs[i])) {
                X86.emit1("popq", X86.calleeSaveRegs[i]);
            //}
        }

        // and we're done
        X86.emit0("ret");
    }


    // generate a source operand of the specified size, possibly after copying into the provided temp register
    // may also use provided temp register inside a returned Mem operand
    // will only return a Mem operand if mem_ok is true
    // will only return a Imm operand if imm_ok is true
    static X86.Operand gen_source_operand (IR.Operand rand, final int size, final boolean mem_ok,
                                           final boolean imm_ok, final X86.Reg temp) {
        class OperandVisitor implements IR.OperandVisitor {
            public X86.Operand visit(IR.Mem rand) {
                X86.Operand brand = gen_source_operand(rand.base,X86.Q,false,false,temp);
                assert (brand instanceof X86.Reg);
                X86.Operand mrand = new X86.Mem((X86.Reg) brand,rand.index);
                if (mem_ok)
                    return mrand;
                else {
                    X86.Reg temp0 = X86.resize_reg(size,temp);
                    X86.emitMov(size,mrand,temp0);
                    return temp0;
                }
            }

            public X86.Operand visit(IR.Temp rand) {
                X86.Operand mrand = env.get(rand);
                assert (mrand != null); // dead value surely isn't used as a source
                if (mrand instanceof X86.Reg)
                    return X86.resize_reg(size,(X86.Reg)mrand);
                else if (mem_ok)
                    return mrand;
                else {
                    X86.Reg temp0 = X86.resize_reg(size,temp);
                    X86.emitMov(size,mrand,temp0);
                    return temp0;
                }
            }

            public X86.Operand visit(IR.RetReg rand) {
                return X86.resize_reg(size,X86.RAX);
            }

            public X86.Operand visit(IR.IntLit rand) {
                X86.Imm mrand = new X86.Imm(rand.i);
                if (imm_ok)
                    return mrand;
                else {
                    X86.Reg temp0 = X86.resize_reg(size,temp);
                    X86.emitMov(size,mrand,temp0);
                    return temp0;
                }
            }

            public X86.Operand visit(IR.BoolLit rand) {
                X86.Imm mrand = new X86.Imm(rand.b ? 1 : 0);
                if (imm_ok)
                    return mrand;
                else {
                    X86.Reg temp0 = X86.resize_reg(size,temp);
                    X86.emitMov(size,mrand,temp0);
                    return temp0;
                }
            }

            public X86.Operand visit(IR.NilLit rand) {
                X86.Imm mrand = new X86.Imm(0);
                if (imm_ok)
                    return mrand;
                else {
                    X86.Reg temp0 = X86.resize_reg(size,temp);
                    X86.emitMov(size,mrand,temp0);
                    return temp0;
                }
            }

            public X86.Operand visit(IR.StringLit rand) {
                X86.AddrName mrand = new X86.AddrName("__S" + stringLiterals.size());
                stringLiterals.add(rand.s);
                X86.emit2("leaq", mrand,temp);
                return temp;
            }

            public X86.Operand visit(IR.Arg rand) {
                return X86.resize_reg(size,X86.argRegs[rand.i + 1]);  // leave ESI as closureReg
            }

            public X86.Operand visit(IR.Name rand) {
                X86.Operand mrand = env.get(rand);
                assert (mrand != null); // dead value surely isn't used as a source
                if (mrand instanceof X86.Reg)
                    return X86.resize_reg(size,(X86.Reg) mrand);
                else if (mem_ok)
                    return mrand;
                else {
                    X86.Reg temp0 = X86.resize_reg(size,temp);
                    X86.emitMov(size,mrand,temp0);
                    return temp0;
                }
            }
        }

        X86.Operand result = null;
        try {
            result = (X86.Operand) (rand.accept(new OperandVisitor()));
        } catch (IR.IRException exn) {
        }
        return result;
    }

    // generate a target operand of the specified size
    // may use provided temp within returned Mem operand (but won't return it directly)
    // operand must not be a literal
    // returns null if target wasn't given a location (i.e., is dead)
    static X86.Operand gen_target_operand (IR.Operand rand, final int size,final X86.Reg temp) {
        class OperandVisitor implements IR.OperandVisitor {
            public X86.Operand visit(IR.Mem rand) {
                X86.Operand brand = gen_source_operand(rand.base,X86.Q,false,false,temp);
                assert (brand instanceof X86.Reg);
                return new X86.Mem((X86.Reg) brand,rand.index);
            }

            public X86.Operand visit(IR.Temp rand) {
                X86.Operand mrand = env.get(rand);
                if (mrand instanceof X86.Reg)
                    return X86.resize_reg(size,(X86.Reg)mrand);
                else
                    return mrand;
            }

            public X86.Operand visit(IR.RetReg rand) {
                return X86.resize_reg(size,X86.RAX);
            }

            public X86.Operand visit(IR.IntLit rand) {
                assert (false);
                return null;
            }

            public X86.Operand visit(IR.BoolLit rand) {
                assert (false);
                return null;
            }

            public X86.Operand visit(IR.NilLit rand) {
                assert (false);
                return null;
            }

            public X86.Operand visit(IR.StringLit rand) {
                assert (false);
                return null;
            }

            public X86.Operand visit(IR.Arg rand) {
                return X86.resize_reg(size,X86.argRegs[rand.i + 1]);  // leave ESI as closureReg
            }

            public X86.Operand visit(IR.Name rand) {
                X86.Operand mrand = env.get(rand);
                if (mrand instanceof X86.Reg)
                    return X86.resize_reg(size,(X86.Reg) mrand);
                else
                    return mrand;
            }
        }

        X86.Operand result = null;
        try {
            result = (X86.Operand) (rand.accept(new OperandVisitor()));
        } catch (IR.IRException exn) {
        }
        return result;
    }


    // Generate code for an instruction
    static void gen (IR.Inst c)  {
        class InstVisitor implements IR.InstVisitor {
            public Object visit (IR.Mov c) {
                X86.Operand mdest = gen_target_operand(c.dest,c.type,tempReg1);
                if (mdest == null)  // assignment to a dead variable
                    return null;
                boolean mem_ok = !(mdest instanceof X86.Mem);  // only one operand can be mem
                X86.Operand msrc = gen_source_operand(c.src,c.type,mem_ok,true,tempReg2);
                X86.emitMov(c.type,msrc,mdest);
                return null;
            }

            public Object visit (IR.Call c) {
                if (c.is_system)  {
                    X86.emit1("pushq",closureReg);
                    X86.emit0("call _" + ((IR.StringLit) c.target).s);
                    X86.emit1("popq",closureReg);
                } else {
                    X86.emitMov(2, env.get(c.target), tempReg1);
                    X86.emit1("pushq",closureReg);
                    X86.emitMov(2, new X86.Mem(tempReg1, 8), closureReg);
                    X86.emit0("call " + "*  " + "0(" + tempReg1.toString() + ")");
                    X86.emit1("popq", closureReg);
                }
                return null;
            }

            public Object visit (IR.MkClosure c) {
                // to handle mutual recursion, allocate all func descriptors first
                for (String f : c.funcs) {
                    X86.emit1("pushq",closureReg);
                    X86.emitMov(X86.Q,new X86.Imm(2*X86.size_bytes[X86.Q]),X86.argRegs[1]); // allocate 2 quads
                    X86.emit0("call _alloc");
                    X86.emit1("popq",closureReg);
                    X86.Operand t = gen_target_operand(new IR.Name (f),IR.PTR,tempReg1);
                    assert (t != null);
                    X86.emitMov(X86.Q,X86.RAX,t);
                }
                for (String f : c.funcs) {
                    IR.Func fdef = funcenv.get(f);
                    int varOffset[] = new int[fdef.freevars.length];
                    int offset = 0;
                    for (int i = 0; i < fdef.freevars.length; i++) {
                        int s = IR.type_size[fdef.freevars[i].type];
                        offset = X86.roundup(offset,s);
                        varOffset[i] = offset;
                        offset += s;
                    }
                    // at this point offset = entire size
                    X86.emit1("pushq",closureReg);
                    X86.emitMov(X86.Q,new X86.Imm(offset),X86.argRegs[1]);
                    X86.emit0("call _alloc");
                    X86.emit1("popq",closureReg);
                    X86.Operand t = gen_source_operand(new IR.Name(f),IR.PTR,false,false,tempReg1);
                    assert (t instanceof X86.Reg);
                    X86.Reg r = (X86.Reg) t;
                    X86.emitMov(X86.Q,X86.RAX,new X86.Mem(r,8));
                    X86.emit2("leaq",new X86.AddrName("__" + f),tempReg2);
                    X86.emitMov(X86.Q,tempReg2,new X86.Mem(r,0));
                    for (int i = 0; i < fdef.freevars.length; i++)  {
                        t = gen_source_operand(new IR.Name(fdef.freevars[i].id),fdef.freevars[i].type,
                                false,true,tempReg1);
                        X86.emitMov(fdef.freevars[i].type, t,new X86.Mem(X86.RAX,varOffset[i]));
                    }
                }
                return null;
            }

            public Object visit(IR.Jump c) {
                if(c.condition == 0) {

                    X86.emit0("jmp" + " L" + funcNumber + "_" + c.dest);
                } else
                    X86.emit0("j" + IR.condition_string[c.condition] + " L" + funcNumber + "_" + c.dest);
                return null;
            }

            public Object visit(IR.Cmp c) {
                X86.Operand mright = gen_source_operand(c.right,c.type,true,true,tempReg1);
                X86.Operand mleft = null;

                if (c.type == IR.PTR && !(mright instanceof X86.Imm) && !(mright instanceof  X86.Mem)) {
                    mleft = gen_source_operand(c.left,c.type,true,true,tempReg2);
                } else if (c.type == IR.PTR && !(mright instanceof X86.Imm)) {
                    mleft = gen_source_operand(c.left,c.type,true,false,tempReg2);
                } else {
                    mleft = gen_source_operand(c.left,c.type,false,false,tempReg2);
                }

                X86.emit2("cmp" + X86.size_suffix[c.type],mright,mleft);

                return null;
            }


            public Object visit(IR.Arith c) {
                X86.Operand mdest = gen_target_operand(c.dest,c.type,tempReg2);
                X86.Operand mleft = gen_source_operand(c.left,c.type,true,true,tempReg1);
                //X86.Operand mright = null;
                X86.Operand mright = gen_source_operand(c.right,c.type,true,true,tempReg2);

                /*
                if (c.type == IR.PTR && !(mleft instanceof X86.Imm) && !(mleft instanceof  X86.Mem)) {
                    mright = gen_source_operand(c.right,c.type,true,true,tempReg2);
                } else if (c.type == IR.PTR && !(mleft instanceof X86.Imm)) {
                    mright = gen_source_operand(c.right,c.type,true,false,tempReg2);
                } else {
                    mright = gen_source_operand(c.right,c.type,false,false,tempReg2);
                }
                */

                switch (c.op) {
                    case IR.ADD:
                        // Note there are two cases to consider:
                        //   IR.INT + IR.INT -> IR.INT
                        //   IR.PTR + IR.INT -> IR.PTR

                        // Because add's left operand can have pointers
                        // So 2nd parameter has to be c.type and not IR.INT
                        /*
                        mleft = gen_source_operand(c.left,c.type,true,true,tempReg1);
                        X86.emitMov(c.type,mleft,mdest);

                        if (c.type == IR.PTR && !(mright instanceof X86.Imm)) {
                            X86.Reg tempReg2s = X86.resize_reg(c.type, tempReg2);
                            X86.emit2("movslq",mright,tempReg2s);
                            mright = tempReg2;
                        }
                        X86.emit2("add" + X86.size_suffix[c.type],mright,mdest);

                        /*  if(!(mleft instanceof X86.Imm) || !(mright instanceof X86.Imm)) {
                          System.out.println("YO");
                          X86.Operand mdest = gen_target_operand(c.dest,c.type,tempReg2);
                          X86.emitMov(c.type,tempReg1s,mdest);
                      }  */

                        mleft = gen_source_operand(c.left, c.type, true, true, tempReg1);
                        X86.Reg tempReg1s = X86.resize_reg(c.type, tempReg1);
                        X86.emitMov(c.type, mleft, tempReg1s);
                        mright = gen_source_operand(c.right, IR.INT, true, true, tempReg2);

                        if (c.type == IR.PTR && !(mright instanceof X86.Imm)) {
                            X86.emit2("movslq", mright, tempReg2);
                            mright = tempReg2;
                        }

                        X86.emit2("add" + X86.size_suffix[c.type], mright, tempReg1s);
                        mdest = gen_target_operand(c.dest, c.type, tempReg2);
                        X86.emitMov(c.type, tempReg1s, mdest);

                        break;
                    case IR.SUB:
                        X86.emitMov(c.type,mleft,mdest);
                        X86.emit2("sub" + X86.size_suffix[c.type],mright,mdest);
                        break;
                    case IR.MUL:
                        tempReg1s = X86.resize_reg(c.type, tempReg1);
                        X86.emitMov(c.type,mleft,tempReg1s);
                        X86.emit2("imul" + X86.size_suffix[c.type],mright,tempReg1s);
                        mdest = gen_target_operand(c.dest,c.type,tempReg2);
                        X86.emitMov(c.type, tempReg1s, mdest);

                        break;
                    case IR.DIV:
                        X86.Reg reg_div1 = X86.resize_reg(c.type, X86.R10);
                        X86.Reg reg_div2 = X86.resize_reg(c.type, X86.R11);

                        X86.emitMov(c.type,mleft,reg_div1);
                        X86.emitMov(c.type,mright,reg_div2);
                        X86.emit1("pushq",X86.RAX);
                        X86.emit1("pushq",X86.RDX); // Do we need this for divide?
                        X86.emitMov(c.type,reg_div1,X86.EAX);
                        X86.emit0("cltd");
                        X86.emit1("idiv" + X86.size_suffix[c.type],reg_div2);
                        X86.emitMov(c.type,X86.EAX,reg_div1);
                        X86.emit1("popq",X86.RDX); // Do we need this for divide?
                        X86.emit1("popq",X86.RAX);
                        X86.emitMov(c.type,reg_div1,mdest);
                        break;
                    case IR.MOD:
                        X86.Reg reg_mod1 = X86.resize_reg(c.type, X86.R10);
                        X86.Reg reg_mod2 = X86.resize_reg(c.type, X86.R11);

                        X86.emitMov(c.type,mleft,reg_mod1);
                        X86.emitMov(c.type,mright,reg_mod2);
                        X86.emit1("pushq",X86.RAX);
                        X86.emit1("pushq",X86.RDX);
                        X86.emitMov(c.type,reg_mod1,X86.EAX);
                        X86.emit0("cltd");
                        X86.emit1("idiv" + X86.size_suffix[c.type],reg_mod2);
                        X86.emitMov(c.type,X86.EDX,reg_mod1);
                        X86.emit1("popq",X86.RDX);
                        X86.emit1("popq",X86.RAX);
                        X86.emitMov(c.type,reg_mod1,mdest);
                        break;
                }
                return null;
            }


            public Object visit(IR.LabelDec c) {
                X86.emitLabel(new X86.Label("L" + funcNumber + "_" + c.lab));
                return null;
            }

        }

        System.out.println("    # " + c);
        try {
            c.accept(new InstVisitor());
        } catch (IR.IRException exn) {
        }
    }


    // Allocate IR.Operands (Temp,RetReg,Arg,Name) to locations
    // described by X86.Operands.
    // Side-effects: env and localsSize.
    static void allocateRegisters(IR.Func func) {
        localsSize = 0;

        // Calculate liveness information for Temp,RetReg,Arg,Name
        Map<IR.Operand,Liveness.Interval> liveIntervals = Liveness.calculateLiveIntervals(funcenv,func);
        int liveCount = liveIntervals.size();

        // Pre-assignments
        // Incoming arguments
        //   Only if their interval is live and doesn't include a call; otherwise, leave unassigned.
        //   Subsequent assignment is guaranteed not to give us a caller-save reg, and
        //   hence won't overlap with the incoming arguments.
        for (int i = 0; i < func.formals.length; i++)  {
            IR.Name argRand = new IR.Name(func.formals[i].id);
            Liveness.Interval argInv = liveIntervals.get(argRand);
            if ((argInv != null) && (!intervalContainsCall(func,argInv)))
                env.put(argRand,X86.argRegs[i+1]); // reserve RDI (argRegs[0]) for closure ptr
        }
        // The return value
        env.put(IR.RETREG, X86.RAX);
        // Outgoing arguments
        for (IR.Operand rand : liveIntervals.keySet())
            if (rand instanceof IR.Arg) {
                X86.Reg reg = X86.argRegs[((IR.Arg)rand).i + 1]; // reserve RDI (argRegs[0]) for closure ptr
                env.put(rand,reg);
            }

        // Keep track of available registers
        boolean[] regAvailable = new boolean[X86.allRegs.length];
        for (int i = 0; i < regAvailable.length; i++)
            regAvailable[i] = true;
        regAvailable[X86.RSP.r] = false;
        regAvailable[closureReg.r] = false;
        regAvailable[tempReg1.r] = false;
        regAvailable[tempReg2.r] = false;

        // **************
        // You should replace the code in this region with a linear-scan allocator !!!

        // For now, do extremely simplistic allocation: simply allocate registers to
        // IR.Operand's eagerly.  Once a register is used, we don't
        // try to use it again, so we will very quickly run out.
        // At that point we start assigning the IR.Operands to the stack frame.

        // First, mark as unavailable any registers used in pre-assignments
        for (X86.Operand rand : env.values())
            if (rand instanceof X86.Reg)
                regAvailable[((X86.Reg) rand).r] = false;

        // Now work through the list of live Operands
        // Even with our extremely simplistic approach, we must be careful
        // to handle callee save and caller save registers.  Again for simplicity,
        // we simply refuse to use a caller-save register for any operand whose
        // live interval includes a call; that way, we never have to worry about
        // saving caller-save registers at all.

        ArrayList<startInterval> sortedStartInterval = new ArrayList<startInterval>();
        ArrayList<endInterval> active = new ArrayList<endInterval>();

        for (Map.Entry<IR.Operand,Liveness.Interval> me : liveIntervals.entrySet())  {
            sortedStartInterval.add(new startInterval(me.getKey(), me.getValue()));
        }

        Collections.sort(sortedStartInterval);

        for (startInterval startInts : sortedStartInterval) {

            for (int i = 0; i < active.size(); i++) {
                if (active.get(i).end < startInts.start) {
                    regAvailable[active.get(i).assignedReg] = true;
                    active.remove(active.get(i));
                } else
                    break;
            }

            // Give it a register...
            IR.Operand rand = startInts.op;
            X86.Operand mrand = env.get(rand);
            if (mrand == null) {
                // not pre-allocated; try to find a register
                X86.Reg treg = null;
                Liveness.Interval n = startInts;
                if (intervalContainsCall(func,n)) {
                    // insist on a callee-save reg
                    for (X86.Reg reg : X86.calleeSaveRegs)
                        if (regAvailable[reg.r]) {
                            treg = reg;
                            break;
                        }
                } else {
                    // try first for a caller-save reg
                    for (X86.Reg reg : X86.callerSaveRegs) {
                        if (regAvailable[reg.r]) {
                            treg = reg;
                            break;
                        }
                    }
                    if (treg == null) {
                        // otherwise, try a callee-save
                        for (X86.Reg reg : X86.calleeSaveRegs)
                            if (regAvailable[reg.r]) {
                                treg = reg;
                                break;
                            }
                    }
                }
                if (treg != null) {
                    // We found a register
                    regAvailable[treg.r] = false;
                    mrand = treg;

                    // Add it to the endInterval map...
                    active.add(new endInterval(startInts.op, startInts, treg.r));
                    Collections.sort(active);
                } else {
                    // Couldn't find a register: use a stack slot.
                    // Since we don't readily know the operand type,
                    // so just assume it requires a quadword.
                    mrand = new X86.Mem(X86.RSP,localsSize);
                    localsSize += X86.size_bytes[X86.Q];
                }
                env.put(rand,mrand);
            }

        }
        // ***************
        // For debug purposes
        System.out.println("# Allocation map");
        for (Map.Entry<IR.Operand,X86.Operand> me : env.entrySet())
            System.out.println("# " + me.getKey() + "\t" + me.getValue());
    }

    // Return true if specified interval includes an IR instruction
    // that will cause an X86.call
    static boolean intervalContainsCall (IR.Func fdef,Liveness.Interval n) {
        for (int i = n.start; i <= n.end; i++)
            if (fdef.code[i] instanceof IR.Call || fdef.code[i] instanceof IR.MkClosure)
                return true;
        return false;
    }

}

class startInterval extends Liveness.Interval implements Comparable<Liveness.Interval> {
    public IR.Operand op;
    startInterval(IR.Operand op, Liveness.Interval interval) {
        super(interval.start, interval.end);
        this.op = op;
    }

    boolean equals(Liveness.Interval interval) {
        return (interval.start == this.start);
    }

    public int compareTo(Liveness.Interval interval) {
        if (this.equals(interval))
            return 0;
        else if (interval.start < this.start)
            return 1;
        else
            return -1;
    }
}

class endInterval extends Liveness.Interval implements Comparable<Liveness.Interval> {
    public IR.Operand op;
    public Integer assignedReg;

    endInterval(IR.Operand op, Liveness.Interval interval) {
        super(interval.start, interval.end);
        this.assignedReg = null;
        this.op = op;
    }

    endInterval(IR.Operand op, Liveness.Interval interval, Integer reg) {
        super(interval.start, interval.end);
        this.assignedReg = reg;
        this.op = op;
    }

    boolean equals(Liveness.Interval interval) {
        return (interval.end == this.end);
    }

    public int compareTo(Liveness.Interval interval) {
        if (this.equals(interval))
            return 0;
        else if (interval.end < this.end)
            return 1;
        else
            return -1;
    }
}