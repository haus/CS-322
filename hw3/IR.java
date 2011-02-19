import java.io.*;
import java.util.*;
import java.math.*;

class IR {
    
  public static class IRException extends Exception {
    IRException(String text) {
      super(text);
    }
  }

  // types
  static final int 
    BOOL = 0,
    INT = 1,
    PTR = 2;
  static int type_count = 3;
  static final String type_string[] = {"B","I","P"};
  static final int type_size[] = {1,4,8};  // bytes

  static abstract class Operand { 
    abstract Object accept(OperandVisitor v) throws IRException;
  }

  interface OperandVisitor {
    Object visit(Mem rand) throws IRException;
    Object visit(Temp rand) throws IRException;
    Object visit(RetReg rand) throws IRException;
    Object visit(IntLit rand) throws IRException;
    Object visit(BoolLit rand) throws IRException;
    Object visit(NilLit rand) throws IRException;
    Object visit(StringLit rand) throws IRException;
    Object visit(Arg rand) throws IRException;
    Object visit(Name rand) throws IRException;
  }


  static class Mem extends Operand {   // Memory at base + index * scale 
    final Operand base;   // must not be Mem
    final Operand index;  // must not be Mem
    final int scale;   // 1,4,8 
    Mem(Operand base, Operand index,int scale) {
      assert (!(base instanceof Mem));
      assert (!(index instanceof Mem));
      this.base = base; this.index = index; this.scale = scale;
    }
    public boolean equals(Object l) {
      return (l instanceof Mem) && 
	(((Mem) l).base.equals(base)) && (((Mem) l).index.equals(index))  &&
	(((Mem) l).scale == scale);
    }
    public int hashCode() {
      return base.hashCode() + index.hashCode() + scale;
    }
    public String toString () {
      return base+"["+ scale + "*" + index + "]";
    }
    Object accept(OperandVisitor v) throws IRException {
      return v.visit(this);
    }
  }

  static class Temp extends Operand {   // Temporary $Ti 
    final int i;
    Temp(int i) {
      this.i = i;
    }
    public boolean equals(Object l) {
      return (l instanceof Temp) && (((Temp) l).i == i);
    }
    public int hashCode() {
      return i;
    }
    public String toString () {
      return "$T" + i;
    }
    Object accept(OperandVisitor v) throws IRException {
      return v.visit(this);
    }
  }
  static class RetReg extends Operand { // Register containing return value 
    public boolean equals(Object l) {
      return (l instanceof RetReg);
    }
    public int hashCode() {
      return 1001;
    }
    public String toString () {
      return "$RET";
    }
    Object accept(OperandVisitor v) throws IRException {
      return v.visit(this);
    }
  }
  static RetReg RETREG = new RetReg();
  static class IntLit extends Operand { 
    final int i;  
    IntLit(int i) {
      this.i = i;
    }
    public boolean equals(Object l) {
      return (l instanceof IntLit) && (((IntLit) l).i == i);
    }
    public int hashCode() {
      return i;
    }
    public String toString () {
      return "" + i;
    }
    Object accept(OperandVisitor v) throws IRException {
      return v.visit(this);
    }
  }

  // potentially handy literals
  static final IntLit ZERO = new IntLit(0);
  static final IntLit ONE = new IntLit(1);
  static final IntLit FOUR = new IntLit(4);
  static final IntLit EIGHT = new IntLit(8);
  static final IntLit MONE = new IntLit(-1);

  static class BoolLit extends Operand {
    final boolean b;
    BoolLit(boolean b) {
      this.b = b;
    }
    public boolean equals(Object l) {
      return (l instanceof BoolLit) && (((BoolLit) l).b == b);
    }
    public int hashCode() {
      return b ? 1 : 0;
    }
    public String toString () {
      return "" + b;
    }
    Object accept(OperandVisitor v) throws IRException {
      return v.visit(this);
    }
  }
  static final BoolLit TRUE = new BoolLit(true);
  static final BoolLit FALSE = new BoolLit(false);

  static class NilLit extends Operand { 
    public boolean equals(Object l) {
      return (l instanceof NilLit);
    }
    public int hashCode() {
      return 3003;
    }
    public String toString () {
      return "$NIL";
    }
    Object accept(OperandVisitor v) throws IRException {
      return v.visit(this);
    }
  }
  static final NilLit NIL = new NilLit();

  static class StringLit extends Operand { 
    final String s;
    StringLit(String s) {
      this.s = s;
    }
    public boolean equals(Object l) {
      return (l instanceof StringLit) && (((StringLit) l).s.equals(s));
    }
    public int hashCode() {
      return s.hashCode();
    }
    public String toString () {
      return "\"" + s + "\"";
    }
    Object accept(OperandVisitor v) throws IRException {
      return v.visit(this);
    }
  }
  static class Arg extends Operand { // Outgoing call argument $Ai
    final int i;
    Arg(int i) {
      this.i = i;
    }
    public boolean equals(Object l) {
      return (l instanceof Arg) && (((Arg) l).i == i);
    }
    public int hashCode() {
      return i;
    }
    public String toString () {
      return "$A" + i;
    }
    Object accept(OperandVisitor v) throws IRException {
      return v.visit(this);
    }
  }
  static class Name extends Operand { // Name in environment (function, local variable or parameter at any level)
    final String s;
    Name(String s) {
      this.s = s; 
    }
    public boolean equals(Object l) {
      boolean b = (l instanceof Name) && (((Name) l).s.equals(s));
      return b;
    }
    public int hashCode() {
      return s.hashCode();
    }
    public String toString () {
      return s;
    }
    Object accept(OperandVisitor v) throws IRException {
      return v.visit(this);
    }
  }

  static abstract class Inst {
    public String pretty() {
      return "\t" + this.toString();
    }
    abstract Object accept(InstVisitor v) throws IRException;
  }

  interface InstVisitor {
    Object visit(Mov c) throws IRException;
    Object visit(Call c) throws IRException;
    Object visit(MkClosure c) throws IRException;
    Object visit(Jump c) throws IRException;
    Object visit(Cmp c) throws IRException;
    Object visit(Arith c) throws IRException;
    Object visit(LabelDec c) throws IRException;
  }

  static class Mov extends Inst {
    final int type;
    final Operand src;
    final Operand dest;
    Mov(int type, Operand src, Operand dest) {
      assert (type >= 0 && type < type_count);
      this.type = type; this.src = src; this.dest = dest;
    }
    public String toString() {
      return "mov" + type_string[type] + " " + src + "," + dest;
    }
    Object accept(InstVisitor v) throws IRException {
      return v.visit(this);
    }
  }

  static class Call extends Inst {
    final boolean is_system;
    final Operand target;
    final int arity;
    final boolean returns_value;
    Call(boolean is_system,Operand target,int arity,boolean returns_value) {
      this.is_system = is_system; this.target = target; 
      this.arity = arity; this.returns_value = returns_value;
    }
    public String toString() {
      return "call" + (is_system ? "s " : " ") + "<" + arity + "," + returns_value + "> " + target;
    }
    Object accept(InstVisitor v) throws IRException {
      return v.visit(this);
    }
  }

  static class MkClosure extends Inst { 
    final String[] funcs;
    MkClosure (String[] funcs) {
      this.funcs = funcs;
    }
    MkClosure(List<String> funcs) {
      this(funcs.toArray(new String[0]));
    }
    public String toString() {
      String r = "mkclosure";
      for (String f : funcs) 
	r += " " + f;
      return r;
    }
    Object accept(InstVisitor v) throws IRException {
      return v.visit(this);
    }
  }    
    
  // Condition code tests
  static final int 
             // 0 is reserved for "always"
    E = 1,   // equal
    NE = 2,  // not equal
    G = 3,   // greater (signed)
    GE = 4,  // greater or equal (signed)
    L = 5,   // less (signed)
    LE = 6,  // less or equal (signed)
    A = 7,   // above (unsigned)
    AE = 8,  // above or equal (unsigned)
    B = 9,   // below (unsigned)
    BE = 10  // below or equal (unsigned)
    ; 
  static int condition_count = 11;
  
  static final String condition_string[] = {"","e","ne","g","ge","l","le","a","ae","b","be"};

  static class Jump extends Inst {
    final int condition; // condition code test (0 means unconditional)
    final int dest;
    Jump (int condition, int dest) {
      assert (condition >= 0 && condition < condition_count);
      this.condition = condition; this.dest = dest;
    }
    public String toString() {
      return ((condition == 0) ? "jmp" : ("j" + condition_string[condition])) + " L" + dest;
    }
    Object accept(InstVisitor v) throws IRException {
      return v.visit(this);
    }
  }

  static class Cmp extends Inst {  // use sane compare order; intended for ints, bools, and locs
    final int type;
    final Operand left;
    final Operand right;
    Cmp (int type, Operand left, Operand right) {
      assert (type >= 0 && type < type_count);
      this.type = type; this.left = left; this.right = right;
    }
    public String toString() {
      return "cmp" + type_string[type] + " " + left + "," + right;
    }
    Object accept(InstVisitor v) throws IRException {
      return v.visit(this);
    }
  }

  // Arithmetic operations
  static final int 
    ADD = 0,
    SUB = 1,
    MUL = 2,
    DIV = 3,
    MOD = 4
  ;

  static int arithop_count = 5;
  static String arithop_string[] = {"add","sub","mul","div","mod"};

  static class Arith extends Inst {  // use sane order; intended for ints and locs (add only)
    final int type;
    final int op;
    final Operand left;
    final Operand right;
    final Operand dest;
    Arith (int type, int op, Operand left, Operand right, Operand dest) {
      assert (op >= 0 && op < arithop_count);
      assert (type == INT || (type == PTR && op == ADD));
      this.type = type; this.op = op; this.left = left; this.right = right; this.dest = dest;
    }
    public String toString() {
      return arithop_string[op] + IR.type_string[type] + " " + left + "," + right + "," + dest;
    }
    Object accept(InstVisitor v) throws IRException {
      return v.visit(this);
    }
  }

  static class LabelDec extends Inst {  // a pseudo-instruction
    final int lab;
    LabelDec(int lab) {
      this.lab = lab;
    }
    public String toString() {
      return "L" + lab;
    }
    public String pretty() {
      return this.toString() + ":";
    }    
    Object accept(InstVisitor v) throws IRException {
      return v.visit(this);
    }
  }


  static class Var {
    final String id;
    final int type;
    Var(String id, int type) {
      assert (type >= 0 && type < type_count);
      this.id = id; this.type = type; 
    }
    public boolean equals(Object l) {
      return (l instanceof Var) && 
	(((Var) l).id.equals(id)) && (((Var) l).type == type);
    }
    public int hashCode() {
      return id.hashCode() + type;
    }
    
    public String toString() {
      return id + ":" + type_string[type] ;
    }
  }

  //"$MAIN" is global name; must have no closure-vars or formals
  // All Func names must be globally unique
  // All functions must begin and end with a LabelDec

  static class Func {
    final String name;
    final Var[] freevars;
    final Var[] formals;
    final Var[] locals;
    final Inst[] code;
    final boolean returns_value;
    int[] labels;             // points into code; filled by constructor
    Func (String name, Var[] freevars, Var[] formals, Var[] locals, Inst[] code, boolean returns_value) {
      this.name = name;
      this.freevars = freevars;
      this.formals = formals;
      this.locals = locals;
      this.code = code;
      this.returns_value = returns_value;
      labels = bindLabels();
    }
    Func (String name, List<Var> freevars, List<Var> formals, List<Var> locals, List<Inst> code, boolean returns_value) {
      this(name, 
	   freevars.toArray(new Var[0]), 
	   formals.toArray(new Var[0]),
	   locals.toArray(new Var[0]),
	   code.toArray(new Inst[0]),
	   returns_value);
    }
    int[] bindLabels() {
      assert (code[0] instanceof LabelDec);
      assert (code[code.length-1] instanceof LabelDec);
      SortedMap<Integer,Integer> labelMap = new TreeMap<Integer,Integer>();
      for (int i = 0; i < code.length; i++) {
	Inst c = code[i];
	if (c instanceof LabelDec)
	  labelMap.put(((LabelDec) c).lab, i);
      }
      int maxLabel = labelMap.lastKey();
      labels = new int[maxLabel + 1];
      for (int j = 0; j < labels.length; j++) {
	Integer k = labelMap.get(j);
	assert (k != null);
        labels[j] = k;
      }
      return labels;
    }
    public String toString() {
      String r = "FUNC " + name;
      r += "\nFreevars:";
      for (Var v : freevars)
      r += " " + v;
      r += "\nFormals:";
      for (Var v : formals)
	r += " " + v;
      r += "\nLocals:";
      for (Var v : locals)
	r += " " + v;
      r += "\nCode:\n";
      for (int i = 0; i < code.length; i++)
	r += i + "\t" + code[i].pretty() + "\n";
      r += "Returns value: " + returns_value + "\n";
      r += "Labels:\n";
      for (int i = 0; i < labels.length; i++)
	r += i + " -> " + labels[i] + "\n";
      return r;
    }
  }      

  static class Program {
    Func[] funcs;
    Program (Func[] funcs) {
      this.funcs = funcs;
    }
    Program (List<Func> funcs) {
      this(funcs.toArray(new Func[0]));
    }
    public String toString() {
      String r = "";
      for (Func f : funcs)
	r += f;
      return r;
    }
  }

}