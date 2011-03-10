.text
# Allocation map
# $A{0}0	%rsi
# $T0	%rcx
# $A{1}0	%rsi
# b_6	%rdx
# $T1	%r8
# $A{2}0	%rsi
# $T2	%r9
# $A{3}0	%rsi
# $T3	%rbx
# $A{4}0	%rsi
# $A{5}0	%rsi
# $A{6}0	%rsi
# x_3	%rbp
# $RET	%rax
# z_5	%r12
# a_7	%r13
	.p2align 4,0x90
.globl __$MAIN
__$MAIN:
	subq $0,%rsp
    # L0
L0_0:
    # movI 8,x_3
	movl $8,%ebp
    # movI 1,y_4
    # movI 2,z_5
	movl $2,%r12d
    # movI 3,b_6
	movl $3,%edx
    # movI 1,a_7
	movl $1,%r13d
    # addI x_3,z_5,$T0
	movl %ebp,%ecx
	addl %r12d,%ecx
    # movI $T0,y_4
    # addI 6,2,$T1
	movl $6,%r8d
	addl $2,%r8d
    # movI $T1,z_5
	movl %r8d,%r12d
    # addI 3,1,$T2
	movl $3,%r9d
	addl $1,%r9d
    # movI $T2,x_3
	movl %r9d,%ebp
    # addI a_7,b_6,$T3
	movl %r13d,%ebx
	addl %edx,%ebx
    # movI $T3,a_7
	movl %ebx,%r13d
    # movI x_3,$A{0}0
	movl %ebp,%esi
    # calls{0} <1,false> "write_int"
	pushq %rdi
	call _write_int
	popq %rdi
    # movP " ",$A{1}0
	leaq __S0(%rip),%r11
	movq %r11,%rsi
    # calls{1} <1,false> "write_string"
	pushq %rdi
	call _write_string
	popq %rdi
    # movI z_5,$A{2}0
	movl %r12d,%esi
    # calls{2} <1,false> "write_int"
	pushq %rdi
	call _write_int
	popq %rdi
    # movP " ",$A{3}0
	leaq __S1(%rip),%r11
	movq %r11,%rsi
    # calls{3} <1,false> "write_string"
	pushq %rdi
	call _write_string
	popq %rdi
    # movI x_3,$A{4}0
	movl %ebp,%esi
    # calls{4} <1,false> "write_int"
	pushq %rdi
	call _write_int
	popq %rdi
    # movP " ",$A{5}0
	leaq __S2(%rip),%r11
	movq %r11,%rsi
    # calls{5} <1,false> "write_string"
	pushq %rdi
	call _write_string
	popq %rdi
    # movI a_7,$A{6}0
	movl %r13d,%esi
    # calls{6} <1,false> "write_int"
	pushq %rdi
	call _write_int
	popq %rdi
    # calls{7} <0,false> "write_newline"
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
__S0:
	.asciz " "
__S1:
	.asciz " "
__S2:
	.asciz " "