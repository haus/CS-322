# CS322 HW#2 Jonah Brasseur & Matthaus Litteken
# February 1st, 2011

# qs.c source
/*

double* partition(double* left, double* right);
void quicksort(double* left, double* right);
void qs(int n, double* array);

double* partition(double* left, double* right) {
	double pivotValue, tempVal, *storeIndex;
	pivotValue = *left;
	
	// Swap left and right
	tempVal = *left;
	
	// Move pivot to end
	*left = *right; 
	*right = tempVal;
	
	storeIndex = left;
	
	for (double* i = left; i < right; i ++) { // left <= i < right 
		if (*i <= pivotValue) {
			// Swap array[i] and array[storeIndex]
			tempVal = *i;
			*i = *storeIndex;
			*storeIndex = tempVal;
			storeIndex++;
		}
	} 
	
	// Move pivot to its final place	
	// Swap array[storeIndex] and array[right]
	tempVal = *storeIndex;
	*storeIndex = *right;
	*right = tempVal;
   	
	return storeIndex;
}

void quicksort(double* left, double* right) {
	if (right > left) {
		double* pivotNewIndex = partition(left, right);
		quicksort(left, pivotNewIndex - 1);
		quicksort(pivotNewIndex + 1, right);
	}
}

// Wrapper to call quicksort
void qs(int n, double* array) {
	quicksort(array, array+n-1);
}

*/
# Assembly notes
# gcc Generated a partition procedure, but also inlined the procedure into our quicksort (thanks gcc!), so we deleted the procedure.
# .cfi_* lines were removed, they are for stack backtracing and exception handling (dwarf call frame information).
# .p2align lines were removed, they are an optimization that pads lines to aid in fetching.
# LFB* and LFE* labels were removed as they weren't used or called by the assembly.

.globl quicksort
	.type	quicksort, @function	# Function declaration
quicksort:
	pushq	%rbp 					# On 1st iteration: save (push onto stack) address of array (from qs jump), on following recursions: save (push onto stack) right address
	movq	%rsi, %rbp 				# On 1st iteration: move/copy right pointer into %rbp, on following iterations/recursions: move/copy left address into %rpb
	pushq	%rbx 					# On 1st iteration: save (push onto stack) n (from qs jump), on following recursions: save (push onto stack) left address
	cmpq	%rdi, %rsi				# Compare left (%rdi) and right (%rsi) addresses
	jbe	.L20						# Jump to L20 if right address <= left address (qs.c: quicksort if (right > left))
									# This jump is when the current sections have been sorted...it ends current recursion
.L22:
	movsd	(%rdi), %xmm1			# Move/copy current left value into %xmm1 (pivotValue = *left)
	movq	%rdi, %rbx				# Move/copy current left address into %rbx (storeIndex = left)
									# The next three instructions swap the left and right values, to move the pivot to the right end of the current section
	movq	(%rbp), %rax			# Move/copy current right value into %rax {rax = *right} (begin of swap)
	movq	%rax, (%rdi)			# Move/copy current right value into current left address (*left = rax)
	movsd	%xmm1, (%rbp)			# Move/copy pivotValue (%xmm1) into address in %rbp {*right = pivotValue} (end of swap)
	movq	%rdi, %rax				# Move/copy current left address into %rax (for loop initialization: *i = left)
	cmpq	%rdi, %rbp				# Compare i (%rdi) and right (%rbp) addresses
	jbe	.L15						# Jump to L15 if right <= left (qs.c: for loop condition check)
									# When true, this conditional ends the for loop.
									# Begin main for loop
.L23:
	movsd	(%rax), %xmm0			# Move current i value into %xmm0 (xmm0 = *i)
	ucomisd	%xmm0, %xmm1			# Compare pivotValue and i value
	jb	.L16						# Jump to L16 if %xmm1 < %xmm0 (if i > pivotValue)
									# For loop IF body
									# The next three instructions swap the current i and storeIndex values
	movq	(%rbx), %rdx			# Move/copy left value (*storeIndex) into %rdx {storeIndex into temp (temp = *storeIndex)}
	movq	%rdx, (%rax)			# Move/copy left value (temp) into current i {*i = temp}
	movsd	%xmm0, (%rbx)			# Move/copy %xmm0 (*i) into storeIndex {*storeIndex = *i}
	addq	$8, %rbx 				# Increment the left address (storeIndex) by one double pointer (8 bytes)
									# End for loop IF body
.L16:
	addq	$8, %rax				# Increment the left address (i) by one double pointer (8 bytes)
	cmpq	%rax, %rbp				# Compare i address and right address
	ja	.L23						# Jump if right address > i address (FOR condition check)
									# End of FOR loop body
	movsd	(%rbp), %xmm1			# Move/copy current right value into %xmm1 {xmm1 = *right}
.L15:
									# The next three instructions do the final swap and move the pivot into its proper place
	movq	(%rbx), %rax			# Move/copy current left value (*storeIndex) into %rax {rax = *storeIndex} (begin of swap)
	movsd	%xmm1, (%rbx)			# Move/copy the right value into storeIndex {*storeIndex = *right}
	movq	%rax, (%rbp)			# Move/copy the left value into the right address {*right = rax} (end of swap)
	leaq	-8(%rbx), %rsi			# Decrement the left address (storeIndex) by one double pointer (8 bytes) to get the (pivotNewIndex - 1), store in %rsi, call quicksort
	call	quicksort				# Recurse into a new quicksort
	leaq	8(%rbx), %rdi			# Increment the left address (storeIndex) by one double pointer (8 bytes) to get the (pivotNewIndex + 1), store in %rdi
	cmpq	%rdi, %rbp				# Compare left address and right address
	ja	.L22						# Jump back to L22 if right address > left address (jumps instead of recurses)
.L20:								# Only get here when right address <= left address
	popq	%rbx					# Pop %rbx to restore previous state (pivotValue when recursing) before function returns and jumps back to L22
	popq	%rbp					# Pop %rbp to restore previous state (right address when recursing) before function returns and jumps back to L22
	ret								# Return from the quicksort
.globl qs
	.type	qs, @function			# Function declaration
qs:
	movl	%edi, %eax 				# Move/copy n into %eax (passed from main)
	movq	%rsi, %rdi 				# Move/copy array location into %rdi (this becomes the left pointer for quicksort)
	cltq							# Convert %eax from double to quad-word, store in %rax
	leaq	-8(%rsi,%rax,8), %rsi	# Increment %rsi location by n-1 double pointers to reach the other end (this becomes the right pointer for quicksort)
	jmp	quicksort					# Jump into quicksort
