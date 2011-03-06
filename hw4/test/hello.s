.text
# Allocation map
# $A{0}0	%rsi
# $RET	%rax
# $A{1}0	%rsi
# $A{2}0	%rsi
# $A{3}0	%rsi
	.p2align 4,0x90
.globl __$MAIN
__$MAIN:
	subq $0,%rsp
    # L0
L0_0:
    # movP "Hello World!",$A{0}0
	leaq __S0(%rip),%r11
	movq %r11,%rsi
    # calls{0} <1,false> "write_string"
	pushq %rdi
	call _write_string
	popq %rdi
    # movP "blah",$A{1}0
	leaq __S1(%rip),%r11
	movq %r11,%rsi
    # calls{1} <1,false> "write_string"
	pushq %rdi
	call _write_string
	popq %rdi
    # movP "yeee",$A{2}0
	leaq __S2(%rip),%r11
	movq %r11,%rsi
    # calls{2} <1,false> "write_string"
	pushq %rdi
	call _write_string
	popq %rdi
    # movP "OK",$A{3}0
	leaq __S3(%rip),%r11
	movq %r11,%rsi
    # calls{3} <1,false> "write_string"
	pushq %rdi
	call _write_string
	popq %rdi
    # calls{4} <0,false> "write_newline"
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
	.asciz "Hello World!"
__S1:
	.asciz "blah"
__S2:
	.asciz "yeee"
__S3:
	.asciz "OK"
