.text
# Allocation map
# $A{0}0	%rsi
# $T0	%rbx
# $A{1}0	%rsi
# $T1	%rbp
# $A{2}0	%rsi
# $T2	%rax
# $A{3}0	%rsi
# $T3	%rbp
# $A{4}0	%rsi
# $T4	%r12
# $T5	%rax
# queens_9	%r15
# $T6	%r12
# $T7	%r13
# $T8	%rax
# up_4	%rbx
# $T9	%r13
# $T10	%r14
# $T11	%rax
# rows_6	%r12
# $RET	%rax
# x_7	%r13
# down_5	%rbp
# print_8	%r14
	.p2align 4,0x90
.globl __$MAIN
__$MAIN:
	pushq %rbx
	pushq %rbp
	pushq %r12
	pushq %r13
	pushq %r14
	pushq %r15
	subq $0,%rsp
    # L0
L0_0:
    # movI 0,i_3
    # movI 0,$T0
	movl $0,%ebx
    # addI $T0,15,$T0
	addl $15,%ebx
    # mulI $T0,1,$T1
	movl %ebx,%r10d
	imull $1,%r10d
	movl %r10d,%ebp
    # addI $T1,4,$T1
	addl $4,%ebp
    # movI $T1,$A{0}0
	movl %ebp,%esi
    # calls{0} <1,true> "alloc"
	pushq %rdi
	call _alloc
	popq %rdi
    # movP $RET,$T1
	movq %rax,%rbp
    # addP $T1,4,$T1
	addq $4,%rbp
    # movI $T0,$T1[-4]
	movl %ebx,-4(%rbp)
    # movP $T1,$T0
	movq %rbp,%rbx
    # mulI 15,1,$T2
	movl $15,%r10d
	imull $1,%r10d
	movl %r10d,%eax
    # addP $T0,$T2,$T2
	movq %rbx,%r10
	movslq %eax,%r11
	addq %r11,%r10
	movq %r10,%rax
    # jmp L3
	jmp L0_3
    # L2
L0_2:
    # movB true,$T0[0]
	movb $1,0(%rbx)
    # addP $T0,1,$T0
	addq $1,%rbx
    # L3
L0_3:
    # cmpP $T0,$T2
	cmpq %rax,%rbx
    # jl L2
	jl L0_2
    # movP $T1,up_4
	movq %rbp,%rbx
    # movI 0,$T3
	movl $0,%ebp
    # addI $T3,15,$T3
	addl $15,%ebp
    # mulI $T3,1,$T4
	movl %ebp,%r10d
	imull $1,%r10d
	movl %r10d,%r12d
    # addI $T4,4,$T4
	addl $4,%r12d
    # movI $T4,$A{1}0
	movl %r12d,%esi
    # calls{1} <1,true> "alloc"
	pushq %rdi
	call _alloc
	popq %rdi
    # movP $RET,$T4
	movq %rax,%r12
    # addP $T4,4,$T4
	addq $4,%r12
    # movI $T3,$T4[-4]
	movl %ebp,-4(%r12)
    # movP $T4,$T3
	movq %r12,%rbp
    # mulI 15,1,$T5
	movl $15,%r10d
	imull $1,%r10d
	movl %r10d,%eax
    # addP $T3,$T5,$T5
	movq %rbp,%r10
	movslq %eax,%r11
	addq %r11,%r10
	movq %r10,%rax
    # jmp L5
	jmp L0_5
    # L4
L0_4:
    # movB true,$T3[0]
	movb $1,0(%rbp)
    # addP $T3,1,$T3
	addq $1,%rbp
    # L5
L0_5:
    # cmpP $T3,$T5
	cmpq %rax,%rbp
    # jl L4
	jl L0_4
    # movP $T4,down_5
	movq %r12,%rbp
    # movI 0,$T6
	movl $0,%r12d
    # addI $T6,15,$T6
	addl $15,%r12d
    # mulI $T6,1,$T7
	movl %r12d,%r10d
	imull $1,%r10d
	movl %r10d,%r13d
    # addI $T7,4,$T7
	addl $4,%r13d
    # movI $T7,$A{2}0
	movl %r13d,%esi
    # calls{2} <1,true> "alloc"
	pushq %rdi
	call _alloc
	popq %rdi
    # movP $RET,$T7
	movq %rax,%r13
    # addP $T7,4,$T7
	addq $4,%r13
    # movI $T6,$T7[-4]
	movl %r12d,-4(%r13)
    # movP $T7,$T6
	movq %r13,%r12
    # mulI 15,1,$T8
	movl $15,%r10d
	imull $1,%r10d
	movl %r10d,%eax
    # addP $T6,$T8,$T8
	movq %r12,%r10
	movslq %eax,%r11
	addq %r11,%r10
	movq %r10,%rax
    # jmp L7
	jmp L0_7
    # L6
L0_6:
    # movB true,$T6[0]
	movb $1,0(%r12)
    # addP $T6,1,$T6
	addq $1,%r12
    # L7
L0_7:
    # cmpP $T6,$T8
	cmpq %rax,%r12
    # jl L6
	jl L0_6
    # movP $T7,rows_6
	movq %r13,%r12
    # movI 0,$T9
	movl $0,%r13d
    # addI $T9,8,$T9
	addl $8,%r13d
    # mulI $T9,4,$T10
	movl %r13d,%r10d
	imull $4,%r10d
	movl %r10d,%r14d
    # addI $T10,4,$T10
	addl $4,%r14d
    # movI $T10,$A{3}0
	movl %r14d,%esi
    # calls{3} <1,true> "alloc"
	pushq %rdi
	call _alloc
	popq %rdi
    # movP $RET,$T10
	movq %rax,%r14
    # addP $T10,4,$T10
	addq $4,%r14
    # movI $T9,$T10[-4]
	movl %r13d,-4(%r14)
    # movP $T10,$T9
	movq %r14,%r13
    # mulI 8,4,$T11
	movl $8,%r10d
	imull $4,%r10d
	movl %r10d,%eax
    # addP $T9,$T11,$T11
	movq %r13,%r10
	movslq %eax,%r11
	addq %r11,%r10
	movq %r10,%rax
    # jmp L9
	jmp L0_9
    # L8
L0_8:
    # movI 0,$T9[0]
	movl $0,0(%r13)
    # addP $T9,4,$T9
	addq $4,%r13
    # L9
L0_9:
    # cmpP $T9,$T11
	cmpq %rax,%r13
    # jl L8
	jl L0_8
    # movP $T10,x_7
	movq %r14,%r13
    # mkclosure print_8
	pushq %rdi
	movq $16,%rsi
	call _alloc
	popq %rdi
	movq %rax,%r14
	pushq %rdi
	movq $8,%rsi
	call _alloc
	popq %rdi
	movq %rax,8(%r14)
	leaq __print_8(%rip),%r11
	movq %r11,0(%r14)
	movq %r13,0(%rax)
    # mkclosure queens_9
	pushq %rdi
	movq $16,%rsi
	call _alloc
	popq %rdi
	movq %rax,%r15
	pushq %rdi
	movq $48,%rsi
	call _alloc
	popq %rdi
	movq %rax,8(%r15)
	leaq __queens_9(%rip),%r11
	movq %r11,0(%r15)
	movq %r14,0(%rax)
	movq %r13,8(%rax)
	movq %r12,16(%rax)
	movq %rbx,24(%rax)
	movq %rbp,32(%rax)
	movq %r15,40(%rax)
    # movI 0,$A{4}0
	movl $0,%esi
    # call{4} <1,false> queens_9
	pushq %rdi
	movq 8(%r15),%rdi
	call *  0(%r15)
	popq %rdi
    # L1
L0_1:
    # movI 0,$RET
	movl $0,%eax
    # L10
L0_10:
	addq $0,%rsp
	popq %r15
	popq %r14
	popq %r13
	popq %r12
	popq %rbp
	popq %rbx
	ret
# Allocation map
# $T0	%rax
# $A{1}0	%rsi
# $T1	%rax
# $T2	%rax
# $T3	%rax
# $A{3}0	%rsi
# $T4	%rax
# $T5	%rax
# $A{5}0	%rsi
# $T6	%rax
# $T7	%rax
# $A{7}0	%rsi
# $A{9}0	%rsi
# $A{11}0	%rsi
# $A{13}0	%rsi
# $A{15}0	%rsi
# $RET	%rax
# x_7	0(%rdi)
	.p2align 4,0x90
.globl __print_8
__print_8:
	subq $0,%rsp
    # L0
L1_0:
    # cmpI 0,x_7[-4]
	movl $0,%r10d
	movq 0(%rdi),%r11
	cmpl -4(%r11),%r10d
    # jb L2
	jb L1_2
    # calls{0} <0,false> "bounds_error"
	pushq %rdi
	call _bounds_error
	popq %rdi
    # L2
L1_2:
    # mulI 0,4,$T0
	movl $0,%r10d
	imull $4,%r10d
	movl %r10d,%eax
    # addP x_7,$T0,$T0
	movq 0(%rdi),%r10
	movslq %eax,%r11
	addq %r11,%r10
	movq %r10,%rax
    # movI $T0[0],$A{1}0
	movl 0(%rax),%esi
    # calls{1} <1,false> "write_int"
	pushq %rdi
	call _write_int
	popq %rdi
    # cmpI 1,x_7[-4]
	movl $1,%r10d
	movq 0(%rdi),%r11
	cmpl -4(%r11),%r10d
    # jb L3
	jb L1_3
    # calls{2} <0,false> "bounds_error"
	pushq %rdi
	call _bounds_error
	popq %rdi
    # L3
L1_3:
    # mulI 1,4,$T1
	movl $1,%r10d
	imull $4,%r10d
	movl %r10d,%eax
    # addP x_7,$T1,$T1
	movq 0(%rdi),%r10
	movslq %eax,%r11
	addq %r11,%r10
	movq %r10,%rax
    # movI $T1[0],$A{3}0
	movl 0(%rax),%esi
    # calls{3} <1,false> "write_int"
	pushq %rdi
	call _write_int
	popq %rdi
    # cmpI 2,x_7[-4]
	movl $2,%r10d
	movq 0(%rdi),%r11
	cmpl -4(%r11),%r10d
    # jb L4
	jb L1_4
    # calls{4} <0,false> "bounds_error"
	pushq %rdi
	call _bounds_error
	popq %rdi
    # L4
L1_4:
    # mulI 2,4,$T2
	movl $2,%r10d
	imull $4,%r10d
	movl %r10d,%eax
    # addP x_7,$T2,$T2
	movq 0(%rdi),%r10
	movslq %eax,%r11
	addq %r11,%r10
	movq %r10,%rax
    # movI $T2[0],$A{5}0
	movl 0(%rax),%esi
    # calls{5} <1,false> "write_int"
	pushq %rdi
	call _write_int
	popq %rdi
    # cmpI 3,x_7[-4]
	movl $3,%r10d
	movq 0(%rdi),%r11
	cmpl -4(%r11),%r10d
    # jb L5
	jb L1_5
    # calls{6} <0,false> "bounds_error"
	pushq %rdi
	call _bounds_error
	popq %rdi
    # L5
L1_5:
    # mulI 3,4,$T3
	movl $3,%r10d
	imull $4,%r10d
	movl %r10d,%eax
    # addP x_7,$T3,$T3
	movq 0(%rdi),%r10
	movslq %eax,%r11
	addq %r11,%r10
	movq %r10,%rax
    # movI $T3[0],$A{7}0
	movl 0(%rax),%esi
    # calls{7} <1,false> "write_int"
	pushq %rdi
	call _write_int
	popq %rdi
    # cmpI 4,x_7[-4]
	movl $4,%r10d
	movq 0(%rdi),%r11
	cmpl -4(%r11),%r10d
    # jb L6
	jb L1_6
    # calls{8} <0,false> "bounds_error"
	pushq %rdi
	call _bounds_error
	popq %rdi
    # L6
L1_6:
    # mulI 4,4,$T4
	movl $4,%r10d
	imull $4,%r10d
	movl %r10d,%eax
    # addP x_7,$T4,$T4
	movq 0(%rdi),%r10
	movslq %eax,%r11
	addq %r11,%r10
	movq %r10,%rax
    # movI $T4[0],$A{9}0
	movl 0(%rax),%esi
    # calls{9} <1,false> "write_int"
	pushq %rdi
	call _write_int
	popq %rdi
    # cmpI 5,x_7[-4]
	movl $5,%r10d
	movq 0(%rdi),%r11
	cmpl -4(%r11),%r10d
    # jb L7
	jb L1_7
    # calls{10} <0,false> "bounds_error"
	pushq %rdi
	call _bounds_error
	popq %rdi
    # L7
L1_7:
    # mulI 5,4,$T5
	movl $5,%r10d
	imull $4,%r10d
	movl %r10d,%eax
    # addP x_7,$T5,$T5
	movq 0(%rdi),%r10
	movslq %eax,%r11
	addq %r11,%r10
	movq %r10,%rax
    # movI $T5[0],$A{11}0
	movl 0(%rax),%esi
    # calls{11} <1,false> "write_int"
	pushq %rdi
	call _write_int
	popq %rdi
    # cmpI 6,x_7[-4]
	movl $6,%r10d
	movq 0(%rdi),%r11
	cmpl -4(%r11),%r10d
    # jb L8
	jb L1_8
    # calls{12} <0,false> "bounds_error"
	pushq %rdi
	call _bounds_error
	popq %rdi
    # L8
L1_8:
    # mulI 6,4,$T6
	movl $6,%r10d
	imull $4,%r10d
	movl %r10d,%eax
    # addP x_7,$T6,$T6
	movq 0(%rdi),%r10
	movslq %eax,%r11
	addq %r11,%r10
	movq %r10,%rax
    # movI $T6[0],$A{13}0
	movl 0(%rax),%esi
    # calls{13} <1,false> "write_int"
	pushq %rdi
	call _write_int
	popq %rdi
    # cmpI 7,x_7[-4]
	movl $7,%r10d
	movq 0(%rdi),%r11
	cmpl -4(%r11),%r10d
    # jb L9
	jb L1_9
    # calls{14} <0,false> "bounds_error"
	pushq %rdi
	call _bounds_error
	popq %rdi
    # L9
L1_9:
    # mulI 7,4,$T7
	movl $7,%r10d
	imull $4,%r10d
	movl %r10d,%eax
    # addP x_7,$T7,$T7
	movq 0(%rdi),%r10
	movslq %eax,%r11
	addq %r11,%r10
	movq %r10,%rax
    # movI $T7[0],$A{15}0
	movl 0(%rax),%esi
    # calls{15} <1,false> "write_int"
	pushq %rdi
	call _write_int
	popq %rdi
    # calls{16} <0,false> "write_newline"
	pushq %rdi
	call _write_newline
	popq %rdi
    # L1
L1_1:
	addq $0,%rsp
	ret
# Allocation map
# $T0	%rax
# $T1	%rax
# $T2	%r12
# $T3	%rax
# r_11	%rbp
# $T4	%r12
# $T5	%rax
# $T6	%rax
# $T7	%rax
# $A{8}0	%rsi
# up_4	24(%rdi)
# $T8	%r12
# $T9	%rax
# $T10	%r12
# $T11	%rax
# $T12	%rax
# $T13	%rax
# $T14	%rax
# $T15	%rax
# rows_6	16(%rdi)
# $T17	%rax
# $RET	%rax
# $T16	%r12
# $T19	%rax
# x_7	8(%rdi)
# $T18	%r12
# c_10	%rbx
# queens_9	40(%rdi)
# down_5	32(%rdi)
# print_8	0(%rdi)
	.p2align 4,0x90
.globl __queens_9
__queens_9:
	pushq %rbx
	pushq %rbp
	pushq %r12
	subq $8,%rsp
	movq %rsi,%rbx
    # L0
L2_0:
    # movI 0,r_11
	movl $0,%ebp
    # movI 0,r_11
	movl $0,%ebp
    # L2
L2_2:
    # cmpI r_11,7
	cmpl $7,%ebp
    # jg L3
	jg L2_3
    # cmpI r_11,rows_6[-4]
	movq 16(%rdi),%r11
	cmpl -4(%r11),%ebp
    # jb L9
	jb L2_9
    # calls{0} <0,false> "bounds_error"
	pushq %rdi
	call _bounds_error
	popq %rdi
    # L9
L2_9:
    # mulI r_11,1,$T0
	movl %ebp,%r10d
	imull $1,%r10d
	movl %r10d,%eax
    # addP rows_6,$T0,$T0
	movq 16(%rdi),%r10
	movslq %eax,%r11
	addq %r11,%r10
	movq %r10,%rax
    # cmpB $T0[0],false
	cmpb $0,0(%rax)
    # je L5
	je L2_5
    # jmp L8
	jmp L2_8
    # L8
L2_8:
    # subI r_11,c_10,$T1
	movl %ebp,%eax
	subl %ebx,%eax
    # addI $T1,7,$T2
	movl %eax,%r12d
	addl $7,%r12d
    # cmpI $T2,up_4[-4]
	movq 24(%rdi),%r11
	cmpl -4(%r11),%r12d
    # jb L10
	jb L2_10
    # calls{1} <0,false> "bounds_error"
	pushq %rdi
	call _bounds_error
	popq %rdi
    # L10
L2_10:
    # mulI $T2,1,$T3
	movl %r12d,%r10d
	imull $1,%r10d
	movl %r10d,%eax
    # addP up_4,$T3,$T3
	movq 24(%rdi),%r10
	movslq %eax,%r11
	addq %r11,%r10
	movq %r10,%rax
    # cmpB $T3[0],false
	cmpb $0,0(%rax)
    # je L5
	je L2_5
    # jmp L7
	jmp L2_7
    # L7
L2_7:
    # addI r_11,c_10,$T4
	movl %ebp,%r12d
	addl %ebx,%r12d
    # cmpI $T4,down_5[-4]
	movq 32(%rdi),%r11
	cmpl -4(%r11),%r12d
    # jb L11
	jb L2_11
    # calls{2} <0,false> "bounds_error"
	pushq %rdi
	call _bounds_error
	popq %rdi
    # L11
L2_11:
    # mulI $T4,1,$T5
	movl %r12d,%r10d
	imull $1,%r10d
	movl %r10d,%eax
    # addP down_5,$T5,$T5
	movq 32(%rdi),%r10
	movslq %eax,%r11
	addq %r11,%r10
	movq %r10,%rax
    # cmpB $T5[0],false
	cmpb $0,0(%rax)
    # je L5
	je L2_5
    # jmp L4
	jmp L2_4
    # L4
L2_4:
    # cmpI r_11,rows_6[-4]
	movq 16(%rdi),%r11
	cmpl -4(%r11),%ebp
    # jb L12
	jb L2_12
    # calls{3} <0,false> "bounds_error"
	pushq %rdi
	call _bounds_error
	popq %rdi
    # L12
L2_12:
    # mulI r_11,1,$T6
	movl %ebp,%r10d
	imull $1,%r10d
	movl %r10d,%eax
    # addP rows_6,$T6,$T6
	movq 16(%rdi),%r10
	movslq %eax,%r11
	addq %r11,%r10
	movq %r10,%rax
    # movB false,$T6[0]
	movb $0,0(%rax)
    # subI r_11,c_10,$T7
	movl %ebp,%eax
	subl %ebx,%eax
    # addI $T7,7,$T8
	movl %eax,%r12d
	addl $7,%r12d
    # cmpI $T8,up_4[-4]
	movq 24(%rdi),%r11
	cmpl -4(%r11),%r12d
    # jb L13
	jb L2_13
    # calls{4} <0,false> "bounds_error"
	pushq %rdi
	call _bounds_error
	popq %rdi
    # L13
L2_13:
    # mulI $T8,1,$T9
	movl %r12d,%r10d
	imull $1,%r10d
	movl %r10d,%eax
    # addP up_4,$T9,$T9
	movq 24(%rdi),%r10
	movslq %eax,%r11
	addq %r11,%r10
	movq %r10,%rax
    # movB false,$T9[0]
	movb $0,0(%rax)
    # addI r_11,c_10,$T10
	movl %ebp,%r12d
	addl %ebx,%r12d
    # cmpI $T10,down_5[-4]
	movq 32(%rdi),%r11
	cmpl -4(%r11),%r12d
    # jb L14
	jb L2_14
    # calls{5} <0,false> "bounds_error"
	pushq %rdi
	call _bounds_error
	popq %rdi
    # L14
L2_14:
    # mulI $T10,1,$T11
	movl %r12d,%r10d
	imull $1,%r10d
	movl %r10d,%eax
    # addP down_5,$T11,$T11
	movq 32(%rdi),%r10
	movslq %eax,%r11
	addq %r11,%r10
	movq %r10,%rax
    # movB false,$T11[0]
	movb $0,0(%rax)
    # cmpI c_10,x_7[-4]
	movq 8(%rdi),%r11
	cmpl -4(%r11),%ebx
    # jb L15
	jb L2_15
    # calls{6} <0,false> "bounds_error"
	pushq %rdi
	call _bounds_error
	popq %rdi
    # L15
L2_15:
    # mulI c_10,4,$T12
	movl %ebx,%r10d
	imull $4,%r10d
	movl %r10d,%eax
    # addP x_7,$T12,$T12
	movq 8(%rdi),%r10
	movslq %eax,%r11
	addq %r11,%r10
	movq %r10,%rax
    # movI r_11,$T12[0]
	movl %ebp,0(%rax)
    # cmpI c_10,7
	cmpl $7,%ebx
    # je L16
	je L2_16
    # jmp L17
	jmp L2_17
    # L16
L2_16:
    # call{7} <0,false> print_8
	movq 0(%rdi),%r10
	pushq %rdi
	movq 8(%r10),%rdi
	call *  0(%r10)
	popq %rdi
    # jmp L18
	jmp L2_18
    # L17
L2_17:
    # addI c_10,1,$T13
	movl %ebx,%eax
	addl $1,%eax
    # movI $T13,$A{8}0
	movl %eax,%esi
    # call{8} <1,false> queens_9
	movq 40(%rdi),%r10
	pushq %rdi
	movq 8(%r10),%rdi
	call *  0(%r10)
	popq %rdi
    # L18
L2_18:
    # cmpI r_11,rows_6[-4]
	movq 16(%rdi),%r11
	cmpl -4(%r11),%ebp
    # jb L19
	jb L2_19
    # calls{9} <0,false> "bounds_error"
	pushq %rdi
	call _bounds_error
	popq %rdi
    # L19
L2_19:
    # mulI r_11,1,$T14
	movl %ebp,%r10d
	imull $1,%r10d
	movl %r10d,%eax
    # addP rows_6,$T14,$T14
	movq 16(%rdi),%r10
	movslq %eax,%r11
	addq %r11,%r10
	movq %r10,%rax
    # movB true,$T14[0]
	movb $1,0(%rax)
    # subI r_11,c_10,$T15
	movl %ebp,%eax
	subl %ebx,%eax
    # addI $T15,7,$T16
	movl %eax,%r12d
	addl $7,%r12d
    # cmpI $T16,up_4[-4]
	movq 24(%rdi),%r11
	cmpl -4(%r11),%r12d
    # jb L20
	jb L2_20
    # calls{10} <0,false> "bounds_error"
	pushq %rdi
	call _bounds_error
	popq %rdi
    # L20
L2_20:
    # mulI $T16,1,$T17
	movl %r12d,%r10d
	imull $1,%r10d
	movl %r10d,%eax
    # addP up_4,$T17,$T17
	movq 24(%rdi),%r10
	movslq %eax,%r11
	addq %r11,%r10
	movq %r10,%rax
    # movB true,$T17[0]
	movb $1,0(%rax)
    # addI r_11,c_10,$T18
	movl %ebp,%r12d
	addl %ebx,%r12d
    # cmpI $T18,down_5[-4]
	movq 32(%rdi),%r11
	cmpl -4(%r11),%r12d
    # jb L21
	jb L2_21
    # calls{11} <0,false> "bounds_error"
	pushq %rdi
	call _bounds_error
	popq %rdi
    # L21
L2_21:
    # mulI $T18,1,$T19
	movl %r12d,%r10d
	imull $1,%r10d
	movl %r10d,%eax
    # addP down_5,$T19,$T19
	movq 32(%rdi),%r10
	movslq %eax,%r11
	addq %r11,%r10
	movq %r10,%rax
    # movB true,$T19[0]
	movb $1,0(%rax)
    # jmp L6
	jmp L2_6
    # L5
L2_5:
    # L6
L2_6:
    # addI r_11,1,r_11
	addl $1,%ebp
    # jmp L2
	jmp L2_2
    # L3
L2_3:
    # L1
L2_1:
	addq $8,%rsp
	popq %r12
	popq %rbp
	popq %rbx
	ret
