#include <stdlib.h>
#include <stdio.h>
#include <time.h>

int main(int argc, char** argv) {
	int arraySize;
	
	if (argc == 2) {
		arraySize = strtoul(argv[1], NULL, 10);
	} else {
		arraySize = 400000;
	}
	
	double* array = (double *) malloc(sizeof(double) * arraySize);
	srand(time(0));	

	for (int i = 0; i < arraySize; i++) {
		array[i] = ((double)rand()/(RAND_MAX)+1);
		printf("%f, ", array[i]);
	}
	
	qs(arraySize, array);

	for (int i = 0; i < arraySize; i++) {
		printf("%f, ", array[i]);
	}

	return 0;
}
