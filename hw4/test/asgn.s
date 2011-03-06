.text
# Allocation map
# $T0	%rax
# $RET	%rax
	.p2align 4,0x90
.globl __$MAIN
__$MAIN:
	subq $0,%rsp
    # L0
L0_0:
    # movI 8,x_3
    # movI 1,y_4
    # divI 5,2,$T0
	movl $5,%r10d
	movl $2,%r11d
	pushq %rax
	pushq %rdx
	movl %r10d,%eax
	cltd
	idivl %r11d
	movl %eax,%r10d
	popq %rdx
	popq %rax
	movl %r10d,%eax
    # movI $T0,y_4
    # L1
L0_1:
    # movI 0,$RET
	movl $0,%eax
    # L2
L0_2:
	addq $0,%rsp
	ret
