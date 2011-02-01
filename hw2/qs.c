void qs(int n, double* array);
void quicksort(double* array, int left, int right);
int partition(double* array, int left, int right);

void qs(int n, double* array) {
	quicksort(array, 0, n);
}

void quicksort(double* array, int left, int right) {
	if (right > left) {
		int pivotNewIndex = partition(array, left, right);
		quicksort(array, left, pivotNewIndex - 1);
		quicksort(array, pivotNewIndex + 1, right);
	}
}

int partition(double* array, int left, int right) {
	double pivotValue, tempVal;
	pivotValue = array[left];
	int i;
	
	// Swap array[pivotIndex] and array[right]
	tempVal = array[left];
	array[left] = array[right]; // Move pivot to end
	array[right] = tempVal;
	
	int storeIndex = left;
	
	for (i = left; i < right; i++) { // left <= i < right 
		if (array[i] <= pivotValue) {
			// Swap array[i] and array[storeIndex]
			tempVal = array[i];
			array[i] = array[storeIndex];
			array[storeIndex++] = tempVal;
		}
	} // Move pivot to its final place
		
	// Swap array[storeIndex] and array[right]
	tempVal = array[storeIndex];
	array[storeIndex] = array[right];
	array[right] = tempVal;
   	
	return storeIndex;
}
