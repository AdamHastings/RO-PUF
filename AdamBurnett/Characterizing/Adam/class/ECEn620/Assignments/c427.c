#include <stdio.h>

int input=10;
int my_array[2048];
int my_init_array[] = { 1,2,3,4,5,6};

int factorial(int n){
  if (n==1)
    return 1;
  else
    return n * factorial(n-1);
}

int main(){
  printf("abcdefg\n");
  factorial(input); 
  return 0; 
}
