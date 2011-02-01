double* partition(double* left, double* right);
void quicksort(double* left, double* right);
void qs(int n, double* array);

double* partition(double* left, double* right) {
	double pivotValue, tempVal, *storeIndex;
	pivotValue = *left;
	
	// Swap left and right
	tempVal = *left;
	*left = *right; // Move pivot to end
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
	} // Move pivot to its final place
		
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

void qs(int n, double* array) {
	quicksort(array, array+n);
}