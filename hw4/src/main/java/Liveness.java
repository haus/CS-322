import java.io.*;
import java.util.*;

// Liveness analysis

class Liveness {

  // Utility class for describing sets of operands
  static class OperandSet implements Iterable<IR.Operand> {
    HashSet<IR.Operand> set; 
    OperandSet() {
      set = new HashSet<IR.Operand>();
    }
    void add(IR.Operand rand) {
      this.add_nonmem(rand);
      this.add_mem(rand);
    }
    void add_nonmem(IR.Operand rand) {
      if (rand instanceof IR.Temp ||
	  rand instanceof IR.RetReg ||
	  rand instanceof IR.Arg ||
	  rand instanceof IR.Name) 
	set.add(rand);   
    }
    void add_mem(IR.Operand rand) {
      if (rand instanceof IR.Mem) {
	this.add_nonmem(((IR.Mem) rand).base);
      }
    }
    void remove(IR.Operand rand) {
      set.remove(rand);
    }
    public boolean equals(Object os) {
      return (os instanceof OperandSet) && 
	(((OperandSet) os).set.equals(set));  // expensive!
    }
    void diff(OperandSet os) {
      for (IR.Operand rand : os.set) 
	set.remove(rand);
    }
    void union(OperandSet os) {
      for (IR.Operand rand : os.set)
	set.add(rand);
    }	
    OperandSet copy() {
      OperandSet s = new OperandSet();
      s.set.addAll(set);
      return s;
    }
    public String toString() {
      String r = "{ ";
      for (IR.Operand rand : set) 
	r += rand + " ";
      r += "}";
      return r;
    }
    public Iterator<IR.Operand> iterator() {
      return set.iterator();
    }
  }

  // Utility class for describing lists of integers
  static class IndexList {
    List<Integer> list;
    IndexList() {
      list = new ArrayList<Integer>();
    }
    void add(int i) {
      list.add(i);
    }
    int get(int p) {
      return list.get(p);
    }
    int size() {
      return list.size();
    }
    public String toString() {
      String r = "[";
      if (size() > 0) {
	r += get(0);
	for (int i = 1; i < size(); i++) 
	  r += "," + get(i);
      }
      r += "]";
      return r;
    }
  }

  // Calculate successor information for each instruction in a function
  static IndexList[] calculateSuccessors (IR.Func func) {
    int length = func.code.length;
    IndexList[] allSuccs = new IndexList[length]; 
    for (int i = 0; i < length-1; i++) {   // there's always a label at the end
      IR.Inst inst = func.code[i];
      IndexList succs = new IndexList();
      if (inst instanceof IR.Jump) {
	IR.Jump jinst = (IR.Jump) inst;
	succs.add(func.labels[jinst.dest]);
	if (jinst.condition > 0)
	  succs.add(i+1);      // safe because there's always a label at the end
      } else 
	succs.add(i+1);      
      allSuccs[i] = succs;
    }
    allSuccs[length-1] = new IndexList();
    return allSuccs;
  }


  // Calculate liveOut information for each instruction in a function
  static OperandSet[] calculateLiveness (final Map<String,IR.Func> funcenv,IR.Func func) {
    IndexList[] allSuccs = calculateSuccessors(func);

    // Calculate sets of operands used and defined by each Inst
    final OperandSet[] used = new OperandSet[func.code.length];  
    final OperandSet[] defined = new OperandSet[func.code.length];
    for (int i = 0; i < func.code.length; i++) {  // see hack below
      used[i] = new OperandSet();
      defined[i] = new OperandSet();
    }
    for (int i = 0; i < func.code.length-1; i++) {  // there's always a label at the end
      IR.Inst inst = func.code[i];
      final int i0 = i;
      class InstVisitor implements IR.InstVisitor {
	public Object visit (IR.Mov c) {
	  used[i0].add(c.src);
	  defined[i0].add_nonmem(c.dest);
	  used[i0].add_mem(c.dest);
	  return null;
	}
	public Object visit (IR.Call c) {
	  if (!c.is_system) 
	    used[i0].add(c.target);
	  for (int a = 0; a < c.arity; a++)  
	    used[i0].add(new IR.Arg(a,c.c));
	  if (c.returns_value)
	    defined[i0].add(IR.RETREG);
	  return null;
	}
	public Object visit (IR.MkClosure c) {
	  for (String f : c.funcs) {
	    IR.Func fdef = funcenv.get(f);
	    for (IR.Var var : fdef.freevars) {
	      used[i0].add(new IR.Name(var.id));
	      // hack hack -- following prevents code generator from trying to re-use
	      // one of these to hold the function itself.
	      used[i0+1].add(new IR.Name(var.id));  
	    }
	  }
	  for (String f : c.funcs) {
	    IR.Name fname = new IR.Name(f);
	    used[i0].remove(fname);
	    defined[i0].add(fname);
	    // hack hack -- we really want these names to be live for at least one instruction,
	    // so they will be assigned a spot to live, even though spot might only be needed
	    // internally to the instruction.  (it would be cleaner to use temporaries inside
	    // the instruction, but we would need an unbounded number of them...)
	    used[i0+1].add(fname);  
	  }
	  return null;
	}
	public Object visit (IR.Jump c) {
	  return null;
	}
	public Object visit (IR.Cmp c) {
	  used[i0].add(c.left);
	  used[i0].add(c.right);
	  return null;
	}
	public Object visit(IR.Arith c) {
	  used[i0].add(c.left);
	  used[i0].add(c.right);
	  defined[i0].add_nonmem(c.dest);
	  used[i0].add_mem(c.dest);
	  return null;
	}
	public Object visit (IR.LabelDec c) {
	  return null;
	}
      }
      try {
	inst.accept(new InstVisitor());
      } catch (IR.IRException exn) {
      };
    }
    for (IR.Var var : func.freevars) 
      defined[0].add(new IR.Name(var.id));
    for (IR.Var var : func.formals)
      defined[0].add(new IR.Name(var.id));
    if (func.returns_value)
      used[func.code.length-1].add(IR.RETREG);

    // DEBUG
    // for (int i = 0; i < func.code.length; i++) 
    //   System.err.println("" + i + "\t" + "U:" + used[i] + "\t" + "D:" + defined[i]);

    // Now solve dataflow equations to calculate
    // set of operands that are live out of each Inst
    OperandSet[] liveIn = new OperandSet[func.code.length];
    OperandSet[] liveOut = new OperandSet[func.code.length];
    for (int i = 0; i < func.code.length; i++) {
      liveIn[i] = new OperandSet();
      liveOut[i] = new OperandSet();
    }
    
    boolean changed = true;
    while (changed) {
      changed = false;
      for (int i = func.code.length-1; i >= 0; i--) {
	OperandSet newLiveIn = liveOut[i].copy();
	newLiveIn.diff(defined[i]);
	newLiveIn.union(used[i]);
	liveIn[i] = newLiveIn;
	OperandSet newLiveOut = new OperandSet();
	for (int n = 0; n < allSuccs[i].size(); n++) 
	  newLiveOut.union(liveIn[allSuccs[i].get(n)]);
	if (!liveOut[i].equals(newLiveOut)) {
	  liveOut[i] = newLiveOut;
	  changed = true;
	}
      }
    }
    return liveOut;
  }

  static class Interval {
    int start;
    int end;
    Interval (int start, int end) {
      this.start = start; this.end = end;
    }
  }

  // calculate live interval for each operand in function
  static Map<IR.Operand,Interval> calculateLiveIntervals(Map<String,IR.Func> funcenv,IR.Func func) {
    Map<IR.Operand,Interval> liveIntervals = new HashMap<IR.Operand,Interval>();  
    OperandSet liveOut[] = calculateLiveness(funcenv,func);
    // DEBUG
    //  for (int i = 0; i < liveOut.length; i++) 
    //    System.err.println("" + i + "\t" + liveOut[i]);
    for (int i = 0; i < func.code.length; i++) {
      for (IR.Operand t : liveOut[i]) {
	Interval n = liveIntervals.get(t);
	if (n == null) {
	  n = new Interval(i,i);
	  liveIntervals.put(t,n);
	} else
	  n.end = i;
      }
    }
    // DEBUG 
    // Set<Map.Entry<IR.Operand,Interval>> lis = liveIntervals.entrySet();
    // for (Map.Entry<IR.Operand,Interval> me : lis) {
    //   IR.Operand t = me.getKey();
    //   Liveness.Interval n = me.getValue();
    //   System.err.println("" + t + "\t[" + n.start + "," + n.end + "]");
    // }
    return liveIntervals;
  }


}


