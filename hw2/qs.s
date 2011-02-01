.globl partition
	.type	partition, @function
partition:
	movslq	%edx,%rax
	movslq	%esi,%r8
	cmpl	%edx, %esi
	leaq	(%rdi,%rax,8), %r9
	leaq	(%rdi,%r8,8), %rcx
	movq	(%r9), %rax
	movsd	(%rcx), %xmm1
	movq	%rax, (%rcx)
	movsd	%xmm1, (%r9)
	movl	%esi, %eax
	jge	.L3
	subl	%esi, %edx
	subl	$1, %edx
	addq	%rdx, %r8
	leaq	8(%rdi,%r8,8), %r8
.L6:
	movsd	(%rcx), %xmm0
	ucomisd	%xmm0, %xmm1
	jb	.L4
	movslq	%eax,%rdx
	addl	$1, %eax
	leaq	(%rdi,%rdx,8), %rdx
	movq	(%rdx), %rsi
	movq	%rsi, (%rcx)
	movsd	%xmm0, (%rdx)
.L4:
	addq	$8, %rcx
	cmpq	%r8, %rcx
	jne	.L6
	movslq	%eax,%rdx
	movsd	(%r9), %xmm1
	leaq	(%rdi,%rdx,8), %rcx
.L3:
	movq	(%rcx), %rdx
	movsd	%xmm1, (%rcx)
	movq	%rdx, (%r9)
	ret
.globl quicksort
	.type	quicksort, @function
quicksort:
	pushq	%r13
	pushq	%r12
	movq	%rdi, %r12
	pushq	%rbp
	movl	%edx, %ebp
	pushq	%rbx
	movl	%esi, %ebx
	subq	$8, %rsp
	cmpl	%edx, %esi
	jge	.L14
.L15:
	movl	%ebx, %esi
	movl	%ebp, %edx
	movq	%r12, %rdi
	call	partition
	movl	%eax, %r13d
	movl	%ebx, %esi
	movq	%r12, %rdi
	leal	-1(%r13), %edx
	leal	1(%r13), %ebx
	call	quicksort
	cmpl	%ebp, %ebx
	jl	.L15
.L14:
	addq	$8, %rsp
	popq	%rbx
	popq	%rbp
	popq	%r12
	popq	%r13
	ret
.globl qs
	.type	qs, @function
qs:
	movl	%edi, %edx
	movq	%rsi, %rdi
	xorl	%esi, %esi
	jmp	quicksort