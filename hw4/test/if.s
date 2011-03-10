.text
# Allocation map
# $T0	%rax
# $A{0}0	%rsi
# $RET	%rax
# $A{2}0	%rsi
# y_4	%rbp
# x_3	%rbx
	.p2align 4,0x90
.globl __$MAIN
__$MAIN:
	pushq %rbx
	pushq %rbp
	subq $0,%rsp
    # L0
L0_0:
    # movI 5,x_3
	movl $5,%ebx
    # subI 0,2,$T0
	movl $0,%eax
	subl $2,%eax
    # movI $T0,y_4
	movl %eax,%ebp
    # cmpI x_3,y_4
	cmpl %ebp,%ebx
    # jg L2
	jg L0_2
    # jmp L3
	jmp L0_3
    # L2
L0_2:
    # movI x_3,$A{0}0
	movl %ebx,%esi
    # calls{0} <1,false> "write_int"
	pushq %rdi
	call _write_int
	popq %rdi
    # calls{1} <0,false> "write_newline"
	pushq %rdi
	call _write_newline
	popq %rdi
    # jmp L4
	jmp L0_4
    # L3
L0_3:
    # L4
L0_4:
    # cmpI y_4,x_3
	cmpl %ebx,%ebp
    # jg L5
	jg L0_5
    # jmp L6
	jmp L0_6
    # L5
L0_5:
    # movI y_4,$A{2}0
	movl %ebp,%esi
    # calls{2} <1,false> "write_int"
	pushq %rdi
	call _write_int
	popq %rdi
    # calls{3} <0,false> "write_newline"
	pushq %rdi
	call _write_newline
	popq %rdi
    # jmp L7
	jmp L0_7
    # L6
L0_6:
    # L7
L0_7:
    # L1
L0_1:
    # movI 0,$RET
	movl $0,%eax
    # L8
L0_8:
	addq $0,%rsp
	popq %rbp
	popq %rbx
	ret
