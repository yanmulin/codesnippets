#include <stdio.h>

struct {
    unsigned short a:1;
} A1;

struct {
    unsigned int b:1;
} B1;

struct {
    unsigned short a:1;
    unsigned int b:1;
} A1B1;

struct {
    unsigned short a:8;
    unsigned short b:4;
    unsigned short c:1;
} A8B4C1;

int main() {
    printf("sizeof(A)=%lu\n", sizeof(A));
    printf("sizeof(B)=%lu\n", sizeof(B));
    printf("sizeof(AB)=%lu\n", sizeof(AB));
    printf("sizeof(ABC)=%lu\n", sizeof(ABC));

    /* illegal operations */ 
    // printf("sizeof(A.a)=%lu\n", sizeof(A.a));
    // printf("&A.a=%p\n", &A.a);

    return 0;
}