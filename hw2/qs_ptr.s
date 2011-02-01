.globl quicksort
	.type	quicksort, @function
quicksort:
	pushq	%rbp
	movq	%rsi, %rbp
	pushq	%rbx
	cmpq	%rdi, %rsi
	jbe	.L20
.L22:
	movq	(%rbp), %rax
	movsd	(%rdi), %xmm1
	cmpq	%rdi, %rbp
	movq	%rdi, %rbx
	movq	%rax, (%rdi)
	movsd	%xmm1, (%rbp)
	movq	%rdi, %rax
	jbe	.L15
.L23:
	movsd	(%rax), %xmm0
	ucomisd	%xmm0, %xmm1
	jb	.L16
	movq	(%rbx), %rdx
	movq	%rdx, (%rax)
	movsd	%xmm0, (%rbx)
	addq	$8, %rbx
.L16:
	addq	$8, %rax
	cmpq	%rax, %rbp
	ja	.L23
	movsd	(%rbp), %xmm1
.L15:
	movq	(%rbx), %rax
	leaq	-8(%rbx), %rsi
	movsd	%xmm1, (%rbx)
	movq	%rax, (%rbp)
	call	quicksort
	leaq	8(%rbx), %rdi
	cmpq	%rdi, %rbp
	ja	.L22
.L20:	popq	%rbx
	popq	%rbp
	ret
.globl qs
	.type	qs, @function
qs:
	movl	%edi, %eax
	movq	%rsi, %rdi
	cltq
	leaq	-8(%rsi,%rax,8), %rsi
	jmp	quicksort
