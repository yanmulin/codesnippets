'''
Q1: You're given a list of elements. Each element has a unique id and 3 properties. Two elements are considered as duplicates if they share any
of the 3 properties. Please write a function that takes the input and returns all the duplicates.
Input:
E1: id1, p1, p2, p3
E2: id2, p1, p4, p5
E3: id3, p6, p7, p8
Output: {{id1, id2}, {id3}}
Input:
E1: id1, p1, p2, p3
E2: id2, p1, p4, p5
E3: id3, p5, p7, p8
Output: {{id1, id3, id3}}
Q2: you are given an array of houses in a neighboorhood in a city.
you have to rearrange houses in such a way that in a single neighbourhood the houses are sorted by number in ascending order and no 2 houses with same number are in same neighbourhood.
you can only rearrange house based on the capacity of each neighbourhood . If neighbourhood "1" in input has 2 houses then at output also it can only have 2 houses.
For example-
{
{1,2},
{4,4,7,8},
{4,9,9,9}
}
becomes
{
{4,9},
{1,2,4,9},
{4,7,8,9}
}

https://www.1point3acres.com/bbs/thread-1083239-1-1.html
'''