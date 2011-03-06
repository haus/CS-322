import java.io.*;
import java.util.*;

class X86 {


  // size specifiers
  // Hack alert: these cleverly correspond exactly to the IR's type integers,
  // so they can be used interchangably!
  static final int 
    B = 0,
    L = 1,
    Q = 2;
  static final int size_count = 3;
  static final String size_suffix[] = {"b","l","q"};
  static final int size_bytes[] = {1,4,8};

 
  // indexed by size, then number
  static String[][] regName = 
    {   // low B
      {"%al","%bl","%cl","%dl","%sil","%dil","%bpl","%spl",
       "%r8b","%r9b","%r10b","%r11b","%r12b","%r13b","%r14b","%r15b"},
      
      // low L
      {"%eax","%ebx","%ecx","%edx","%esi","%edi","%ebp","%esp",
       "%r8d","%r9d","%r10d","%r11d","%r12d","%r13d","%r14d","%r15d"},
      
      // full Q
      {"%rax","%rbx","%rcx","%rdx","%rsi","%rdi","%rbp","%rsp",
       "%r8","%r9","%r10","%r11","%r12","%r13","%r14","%r15"}
    };
	

  static abstract class Operand {
  };

  static class Mem extends Operand {   // Computed memory address
    Reg base;
    Reg index;
    int offset;
    int scale;  // 1,2,4,8
    Mem(Reg base, Reg index, int offset, int scale) {
      this.base = base; this.index = index; this.offset = offset; this.scale = scale;
    }
    Mem(Reg base, int offset) {
      this.base = base; this.index = null; this.offset = offset; this.scale = 1;
    }
    public String toString () {
      if (this.index != null)
	return offset + "(" + base + "," + index + "," + scale + ")";
      else 
	return offset + "(" + base + ")";
    }
    public boolean equals(Object obj) {
      return obj != null && obj instanceof Mem && 
	base == ((Mem) obj).base && index == ((Mem) obj).index && 
	offset == ((Mem) obj).offset && scale == ((Mem) obj).scale;
    }
  }
  static class Reg extends Operand {  // Register
    int r; 
    int s;  // size
    Reg(int r) {
      this.r = r; this.s = Q;
    }
    Reg(int r, int s) {
      this.r = r; this.s = s;
    }
    public String toString () {
      return regName[s][r];
    }
    public boolean equals(Object obj) {
      return obj != null && obj instanceof Reg && r == ((Reg) obj).r && s == ((Reg) obj).s;  
    }
  }
  static class Imm extends Operand { // 32-bit integer immediate
    int i;
    Imm(int i) {
      this.i = i;
    }
    public String toString() {
      return "$" + i;
    }
    public boolean equals(Object obj) {
      return obj != null && obj instanceof Imm && i == ((Imm) obj).i;
    }
  }
  static class AddrName extends Operand { // Named global address (PIC)
    String s;
    AddrName(String s) {
      this.s = s;
    }
    public String toString() {
      return s + "(%rip)";
    }
    public boolean equals(Object obj) {
      return obj != null && obj instanceof AddrName && s == ((AddrName) obj).s;
    }
  }
  static class Label extends Operand { // Label
    String s;
    Label(String s) {
      this.s = s;
    }
    public String toString() {
      return s;
    }
    public boolean equals(Object obj) {
      return obj != null && obj instanceof Label && s == ((Label) obj).s;
    }
  }


  static void emit(String s)
  {
    System.out.println(s);
  }

  static void emit0(String op)
  {
    System.out.println("\t" + op);
  }

  static void emit1(String op, Operand rand1)
  {
    System.out.println("\t" + op + " " + rand1);
  }

  static void emit2(String op, Operand rand1, Operand rand2) {
    System.out.println("\t" + op + " " + rand1 + "," + rand2);
  }

  static void emitLabel(Label lab)
  {
    System.out.println("" + lab + ":");
  }

  static void emitString(String s) {
    System.out.println("\t.asciz \"" + s + "\"");
  }
    
  // Adjust size of register operand
  static Reg resize_reg(int size,Reg r) {
    return new X86.Reg(r.r,size);
  }

  // Emit mov just when necessary
  static void emitMov(int size,Operand from,Operand to) {
    if (!from.equals(to))  {
      emit2("mov" + size_suffix[size],from,to);
    }
  }

  // mnemonic definitions for the (quad) registers
  static final Reg
    RAX = new Reg(0),
    RBX = new Reg(1),
    RCX = new Reg(2),
    RDX = new Reg(3),
    RSI = new Reg(4),
    RDI = new Reg(5),
    RBP = new Reg(6),
    RSP = new Reg(7),
    R8 = new Reg(8),
    R9 = new Reg(9),
    R10 = new Reg(10),
    R11 = new Reg(11),
    R12 = new Reg(12),
    R13 = new Reg(13),
    R14 = new Reg(14),
    R15 = new Reg(15)
    ;
  
  // some extras for needed long registers
  static final Reg
    EAX = new Reg(0,L),
    EDX = new Reg(3,L);


  // indices of standard argument registers
  static Reg[] argRegs = {RDI,RSI,RDX,RCX,R8,R9};
  
  static Reg[] calleeSaveRegs = {RBX,RBP,R12,R13,R14,R15};

  static Reg[] callerSaveRegs = {RAX,RCX,RDX,RSI,RDI,R8,R9,R10,R11};

  static Reg[] allRegs = {RAX,RBX,RCX,RDX,RSI,RDI,RBP,RSP,
			  R8,R9,R10,R11,R12,R13,R14,R15};


  // Round x up to nearest multiple of p, provided p is a multiple of 2 
  static int roundup(int x,int p) {
    return (x + p - 1) & ~(p-1);
  }

}
