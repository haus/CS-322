.globl quicksort
	.type	quicksort, @function
quicksort:
	pushq	%rbp 			# Save array location on 1st iteration (from qs jump), save right address on subsequent recursion
	movq	%rsi, %rbp 		# Storing right pointer into %rbp on 1st iteration, stores left address into %rpb on subsequent recursion
	pushq	%rbx 			# Save n on 1st iteration (from qs jump), save left address on subsequent recursion
	cmpq	%rdi, %rsi		# Compare left (%rdi) and right (%rsi) addresses
	jbe	.L20				# Jump to L20 if right address <= left address (qs.c: quicksort if (right > left))
							# This jump is when the current sections have been sorted...it ends current recursion
.L22:
	movsd	(%rdi), %xmm1	# Store current left value into %xmm1 (pivotValue)
	movq	%rdi, %rbx		# Store current left address into %rbx (storeIndex)
	movq	(%rbp), %rax	# Store current right value into %rax  (begin of swap)
	movq	%rax, (%rdi)	# Store current right value into current left address
	movsd	%xmm1, (%rbp)	# Move current left value (pivotValue) into address in %rbp (tempVal)
	movq	%rdi, %rax		# Move current left address into %rax (end of swap)
	cmpq	%rdi, %rbp		# Compare left (%rdi - i?) and right (%rbp) addresses
	jbe	.L15				# Jump to L15 if right <= left (qs.c: for loop condition check)
							# When true, this conditional ends the for loop.
							# Begin main for loop
.L23:
	movsd	(%rax), %xmm0	# Move current left value into %xmm0 (i value)
	ucomisd	%xmm0, %xmm1	# Compare left (pivotValue) and left (i value)
	jb	.L16				# Jump to L16 if %xmm1 < %xmm0 (if i > pivotValue)
							# For loop IF body
	movq	(%rbx), %rdx	# Store left value (tempVal) into %rdx (storeIndex) {storeIndex into i}
	movq	%rdx, (%rax)	# Store left value (tempVal) into current left address (i) {i into tempVal}
	movsd	%xmm0, (%rbx)	# Store %xmm0 into left address (storeIndex) {original i into storeIndex}
	addq	$8, %rbx 		# Increment the left address (storeIndex) by one double pointer
							# End for loop If body
.L16:
	addq	$8, %rax		# Increment the left address (i) by one double pointer
	cmpq	%rax, %rbp		# Compare left and right addresses
	ja	.L23				# Jump if right address > left address
							# End of for loop body
	movsd	(%rbp), %xmm1	# Loading current right value into %xmm1
.L15:
	movq	(%rbx), %rax	# Loading current left value into %rax
	leaq	-8(%rbx), %rsi	# Decrement the left address by one double pointer (8 bytes), store in %rsi
	movsd	%xmm1, (%rbx)	# Store the right value into the new left address
	movq	%rax, (%rbp)	# Store the left value into the right address
	call	quicksort		# Recurse into a new quicksort
	leaq	8(%rbx), %rdi	# Increment the left address by one double pointer, store in %rdi
	cmpq	%rdi, %rbp		# Compare left address and right address
	ja	.L22				# Jump back to L22 if right address > left address (jumps instead of recurses)
.L20:						# Only get here when right address <= left address
	popq	%rbx			# Pop %rbx to restore previous state before function returns and jumps back to L22
	popq	%rbp			# Pop %rbp to restore previous state before function returns and jumps back to L22
	ret						# Return from the quicksort
.globl qs
	.type	qs, @function
qs:
	movl	%edi, %eax 				# Move n into %eax (passed from main)
	movq	%rsi, %rdi 				# Move array location into %rdi (this becomes the left pointer for quicksort)
	cltq							# Convert %eax from double to quad-word, store in %rax
	leaq	-8(%rsi,%rax,8), %rsi	# Increment %rsi location by n-1 double pointers to reach the other end (this becomes the right pointer for quicksort)
	jmp	quicksort					# Jump into quicksort
