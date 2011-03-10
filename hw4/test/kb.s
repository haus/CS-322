.text
# Allocation map
# $A{0}0	%rsi
# $T0	%rcx
# $T1	%rcx
# $A{1}0	%rsi
# b_6	%rax
# $T2	%rcx
# $A{2}0	%rsi
# $T3	%rax
# $A{3}0	%rsi
# $A{4}0	%rsi
# $A{5}0	%rsi
# $A{6}0	%rsi
# x_3	%rbx
# $RET	%rax
# z_5	%rbp
# a_7	%r12
	.p2align 4,0x90
.globl __$MAIN
__$MAIN:
	pushq %rbx
	pushq %rbp
	pushq %r12
	subq $8,%rsp
    # L0
L0_0:
    # movI 8,x_3
	movl $8,%ebx
    # movI 1,y_4
    # movI 2,z_5
	movl $2,%ebp
    # movI 3,b_6
	movl $3,%eax
    # movI 1,a_7
	movl $1,%r12d
    # addI x_3,z_5,$T0
	movl %ebx,%ecx
	addl %ebp,%ecx
    # movI $T0,y_4
    # addI 6,2,$T1
	movl $6,%ecx
	addl $2,%ecx
    # movI $T1,z_5
	movl %ecx,%ebp
    # addI 3,1,$T2
	movl $3,%ecx
	addl $1,%ecx
    # movI $T2,x_3
	movl %ecx,%ebx
    # addI a_7,b_6,$T3
	movl %r12d,%r10d
	addl %eax,%r10d
	movl %r10d,%eax
    # movI $T3,a_7
	movl %eax,%r12d
    # movI x_3,$A{0}0
	movl %ebx,%esi
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
	movl %ebp,%esi
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
	movl %ebx,%esi
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
	movl %r12d,%esi
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
	addq $8,%rsp
	popq %r12
	popq %rbp
	popq %rbx
	ret
__S0:
	.asciz " "
__S1:
	.asciz " "
__S2:
	.asciz " "
