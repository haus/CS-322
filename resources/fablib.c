#include <stdlib.h>
#include <stdio.h>

extern int __$MAIN();

int main() {
  return __$MAIN();
}


void * _alloc(long ignore, int i) {
  void * r = (void *) malloc(i);
  if (!r) {
    fprintf(stderr,"fab library failure: out of memory\n");
    exit(1);
  };
  return r;
}


int _read_int() {
  int i;
  if (scanf("%d", &i) != 1) {
    fprintf(stderr,"fab library failure: invalid integer read\n");
    exit(1);
  };
  return i;
}

void _write_string(long ignore,char *s) {
  printf("%s",s);
}

void _write_bool(int ignore,char b) {
  if (b)
    printf("true");
  else
    printf("false");
}

void _write_int(int ignore,int i) {
  printf("%d",i);
}

void _write_newline() {
  printf("\n");
}

void _bounds_error() {
  fprintf(stderr,"Array bounds violation\n");
  exit(1);
}

void _nil_pointer() {
  fprintf(stderr,"Nil pointer dereference\n");
  exit(1);
}
