.text
# Allocation map
# $T0	%rax
# $A{0}0	%rsi
# $RET	%rax
# y_4	%rax
# x_3	%rax
	.p2align 4,0x90
.globl __$MAIN
__$MAIN:
	subq $0,%rsp
    # L0
L0_0:
    # movI 24,x_3
	movl $24,%eax
    # movI 10,y_4
	movl $10,%eax
    # mulI x_3,3,$T0
	movl %eax,%r10d
	imull $3,%r10d
	movl %r10d,%eax
    # movI $T0,y_4
    # movI y_4,$A{0}0
	movl %eax,%esi
    # calls{0} <1,false> "write_int"
	pushq %rdi
	call _write_int
	popq %rdi
    # calls{1} <0,false> "write_newline"
	pushq %rdi
	call _write_newline
	popq %rdi
    # L1
L0_1:
    # movI 0,$RET
	movl $0,%eax
    # L2
L0_2:
	addq $0,%rsp
	ret
