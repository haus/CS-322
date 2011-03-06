.text
# Allocation map
# $T0	%rax
# $A{0}0	%rsi
# $RET	%rax
# y_4	%rax
	.p2align 4,0x90
.globl __$MAIN
__$MAIN:
	subq $0,%rsp
    # L0
L0_0:
    # movI 4,x_3
    # movI 1,y_4
    # modI 19,7,$T0
	movl $19,%r10d
	movl $7,%r11d
	pushq %rax
	pushq %rdx
	movl %r10d,%eax
	cltd
	idivl %r11d
	movl %edx,%r10d
	popq %rdx
	popq %rax
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
