	.file	"qs_ptr.c"
	.text
	.p2align 4,,15
.globl partition
	.type	partition, @function
partition:
.LFB0:
	.cfi_startproc
	movq	(%rsi), %rax
	movsd	(%rdi), %xmm1
	cmpq	%rsi, %rdi
	movq	%rax, (%rdi)
	movsd	%xmm1, (%rsi)
	movq	%rdi, %rax
	jae	.L3
	.p2align 4,,10
	.p2align 3
.L9:
	movsd	(%rdi), %xmm0
	ucomisd	%xmm0, %xmm1
	jb	.L4
	movq	(%rax), %rdx
	movq	%rdx, (%rdi)
	movsd	%xmm0, (%rax)
	addq	$8, %rax
.L4:
	addq	$8, %rdi
	cmpq	%rdi, %rsi
	ja	.L9
	movsd	(%rsi), %xmm1
.L3:
	movq	(%rax), %rdx
	movsd	%xmm1, (%rax)
	movq	%rdx, (%rsi)
	ret
	.cfi_endproc
.LFE0:
	.size	partition, .-partition
	.p2align 4,,15
.globl quicksort
	.type	quicksort, @function
quicksort:
.LFB1:
	.cfi_startproc
	pushq	%rbp
	.cfi_def_cfa_offset 16
	movq	%rsi, %rbp
	.cfi_offset 6, -16
	pushq	%rbx
	.cfi_def_cfa_offset 24
	subq	$8, %rsp
	.cfi_def_cfa_offset 32
	cmpq	%rdi, %rsi
	jbe	.L20
	.cfi_offset 3, -24
	.p2align 4,,10
	.p2align 3
.L22:
	movq	(%rbp), %rax
	movsd	(%rdi), %xmm1
	cmpq	%rdi, %rbp
	movq	%rdi, %rbx
	movq	%rax, (%rdi)
	movsd	%xmm1, (%rbp)
	movq	%rdi, %rax
	jbe	.L15
	.p2align 4,,10
	.p2align 3
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
.L20:
	addq	$8, %rsp
	popq	%rbx
	popq	%rbp
	ret
	.cfi_endproc
.LFE1:
	.size	quicksort, .-quicksort
	.p2align 4,,15
.globl qs
	.type	qs, @function
qs:
.LFB2:
	.cfi_startproc
	movl	%edi, %eax
	movq	%rsi, %rdi
	cltq
	leaq	-8(%rsi,%rax,8), %rsi
	jmp	quicksort
	.cfi_endproc
.LFE2:
	.size	qs, .-qs
	.ident	"GCC: (Ubuntu/Linaro 4.4.4-14ubuntu5) 4.4.5"
	.section	.note.GNU-stack,"",@progbits
